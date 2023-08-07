package com.tim.openvpn;

import android.os.Handler;
import android.os.HandlerThread;

import com.tim.basevpn.state.ConnectionState;
import com.tim.openvpn.log.OpenVPNLogger;
import com.tim.openvpn.model.CIDRIP;
import com.tim.openvpn.service.IOpenVPNService;
import com.tim.openvpn.utils.NetworkUtils;

import net.openvpn.ovpn3.ClientAPI_Config;
import net.openvpn.ovpn3.ClientAPI_EvalConfig;
import net.openvpn.ovpn3.ClientAPI_Event;
import net.openvpn.ovpn3.ClientAPI_ExternalPKICertRequest;
import net.openvpn.ovpn3.ClientAPI_ExternalPKISignRequest;
import net.openvpn.ovpn3.ClientAPI_LogInfo;
import net.openvpn.ovpn3.ClientAPI_OpenVPNClient;
import net.openvpn.ovpn3.ClientAPI_OpenVPNClientHelper;
import net.openvpn.ovpn3.ClientAPI_Status;
import net.openvpn.ovpn3.ClientAPI_TransportStats;

import java.util.Locale;

public class OpenVPNThreadv3 extends ClientAPI_OpenVPNClient implements Runnable, OpenVPNManagement {
    final static long EmulateExcludeRoutes = (1 << 16);
    public static final String VPNSERVICE_TUN = "vpnservice-tun";

    static {
        System.loadLibrary("ovpn3");
    }

    private final String configuration;
    private final IOpenVPNService mService;
    /* The methods in OpenVPN3 can take a long time, so we do async messages to handle them
     * to avoid ANR on the service main thread */
    private final Handler mHandler;

    public OpenVPNThreadv3(IOpenVPNService openVpnService, String config) {
        OpenVPNLogger.d("OpenVPNThreadv3", "Configuration: \n" + config);
        configuration = config;
        mService = openVpnService;
        HandlerThread mHandlerThread = new HandlerThread("OpenVPN3Thread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    @Override
    public void run() {
        if (!setConfig(configuration))
            return;

        OpenVPNLogger.d("OpenVPNThreadv3", ClientAPI_OpenVPNClientHelper.platform());
        OpenVPNLogger.d("OpenVPNThreadv3", ClientAPI_OpenVPNClientHelper.copyright());

        mHandler.postDelayed(this::pollStatus, 2 * 1000);

        ClientAPI_Status status = connect();
        if (status.getError()) {
            OpenVPNLogger.e("OpenVPNThreadv3", String.format("connect() error: %s: %s", status.getStatus(), status.getMessage()));
        }
        OpenVPNLogger.d("OpenVPNThreadv3", "NOPROCESS" + "OpenVPN3 thread finished");
        mHandler.removeCallbacks(this::pollStatus);
    }

    @Override
    public boolean tun_builder_set_remote_address(String address, boolean ipv6) {
        OpenVPNLogger.d("OpenVPNThreadv3", "tun_builder_set_remote_address()");
        mService.setMtu(1500);
        return true;
    }

    @Override
    public boolean tun_builder_set_mtu(int mtu) {
        OpenVPNLogger.d("OpenVPNThreadv3", "tun_builder_set_mtu()");
        mService.setMtu(mtu);
        return true;
    }

    @Override
    public boolean tun_builder_add_dns_server(String address, boolean ipv6) {
        OpenVPNLogger.d("OpenVPNThreadv3", "tun_builder_add_dns_server()");
        mService.addDNS(address);
        return true;
    }

    @Override
    public boolean tun_builder_add_route(String address, int prefix_length, int metric, boolean ipv6) {
        OpenVPNLogger.d("OpenVPNThreadv3", "tun_builder_add_route()");
        if (address.equals("remote_host"))
            return false;

        if (ipv6)
            mService.addRoutev6(address + "/" + prefix_length, "tun");
        else
            mService.addRoute(new CIDRIP(address, prefix_length), true);
        return true;
    }

    @Override
    public boolean tun_builder_exclude_route(String address, int prefix_length, int metric, boolean ipv6) {
        OpenVPNLogger.d("OpenVPNThreadv3", "tun_builder_exclude_route()");
        if (ipv6)
            mService.addRoutev6(address + "/" + prefix_length, "wifi0");
        else {
            CIDRIP route = new CIDRIP(address, prefix_length);
            mService.addRoute(route, false);
        }
        return true;
    }

    @Override
    public boolean tun_builder_add_search_domain(String domain) {
        OpenVPNLogger.d("OpenVPNThreadv3", "tun_builder_add_search_domain()");
        mService.setDomain(domain);
        return true;
    }

    @Override
    public boolean tun_builder_set_proxy_http(String host, int port) {
        OpenVPNLogger.d("OpenVPNThreadv3", "tun_builder_set_proxy_http()");
        return false;//mService.addHttpProxy(host, port);
    }

    @Override
    public boolean tun_builder_set_proxy_https(String host, int port) {
        OpenVPNLogger.d("OpenVPNThreadv3", "tun_builder_set_proxy_https()");
        return false;
    }

    @Override
    public int tun_builder_establish() {
        OpenVPNLogger.d("OpenVPNThreadv3", "tun_builder_establish()");
        return mService.openTun().detachFd();
    }

    @Override
    public boolean tun_builder_set_session_name(String name) {
        OpenVPNLogger.d("OpenVPNThreadv3", "We should call this session: " + name);
        return true;
    }

    @Override
    public boolean tun_builder_add_address(String address, int prefix_length, String gateway, boolean ipv6, boolean net30) {
        OpenVPNLogger.d("OpenVPNThreadv3", "tun_builder_add_address()");
        if (!ipv6)
            mService.setLocalIP(new CIDRIP(address, prefix_length));
        else
            mService.setLocalIPv6(address + "/" + prefix_length);
        return true;
    }

    @Override
    public boolean tun_builder_new() {
        OpenVPNLogger.d("OpenVPNThreadv3", "tun_builder_new()");
        return true;
    }

    @Override
    public boolean tun_builder_set_layer(int layer) {
        OpenVPNLogger.d("OpenVPNThreadv3", "tun_builder_set_layer()");
        return layer == 3;
    }

    @Override
    public boolean tun_builder_reroute_gw(boolean ipv4, boolean ipv6, long flags) {
        OpenVPNLogger.d("OpenVPNThreadv3", "tun_builder_reroute_gw()");
        if ((flags & EmulateExcludeRoutes) != 0)
            return true;
        if (ipv4)
            mService.addRoute("0.0.0.0", "0.0.0.0", "127.0.0.1", VPNSERVICE_TUN);

        if (ipv6)
            mService.addRoutev6("::/0", VPNSERVICE_TUN);

        return true;
    }

    private boolean setConfig(String vpnconfig) {

        ClientAPI_Config config = new ClientAPI_Config();
        /*if (mVp.getPasswordPrivateKey() != null)
            config.setPrivateKeyPassword(mVp.getPasswordPrivateKey());*/

        config.setContent(vpnconfig);
        config.setTunPersist(true);
        // config.setGuiVersion(VpnProfile.getVersionEnvString(mService));
        //config.setSsoMethods("openurl,webauth,crtext");
        //config.setPlatformVersion(mVp.getPlatformVersionEnvString());
        config.setExternalPkiAlias("extpki");
        config.setCompressionMode("asym");


        config.setHwAddrOverride(NetworkUtils.getFakeMacAddrFromSAAID(mService.getCtResolver()));
        config.setInfo(true);
        config.setAllowLocalLanAccess(false);
        config.setRetryOnAuthFailed(false);
        config.setEnableLegacyAlgorithms(false);
        /* We want the same app internal route emulation for OpenVPN 2 and OpenVPN 3 */
        config.setEnableRouteEmulation(false);
        /*if (mVp.mCompatMode > 0 && mVp.mCompatMode < 20500)
            config.setEnableNonPreferredDCAlgorithms(true);
        if (!TextUtils.isEmpty(mVp.mTlSCertProfile))
            config.setTlsCertProfileOverride(mVp.mTlSCertProfile);*/

        ClientAPI_EvalConfig ec = eval_config(config);
        if (ec.getExternalPki()) {
            OpenVPNLogger.d("OpenVPNThreadv3", "OpenVPN3 core assumes an external PKI config");
        }
        if (ec.getError()) {
            OpenVPNLogger.d("OpenVPNThreadv3", "OpenVPN config file parse error: " + ec.getMessage());
            return false;
        } else {
            config.setContent(vpnconfig);
            return true;
        }
    }

    @Override
    public void external_pki_cert_request(ClientAPI_ExternalPKICertRequest certreq) {
        OpenVPNLogger.d("OpenVPNThreadv3", "Got external PKI certificate request from OpenVPN core");
    }

    @Override
    public void external_pki_sign_request(ClientAPI_ExternalPKISignRequest signreq) {
        OpenVPNLogger.d("OpenVPNThreadv3", "Got external PKI signing request from OpenVPN core for algorithm " + signreq.getAlgorithm());
    }

    @Override
    public boolean socket_protect(int socket, String remote, boolean ipv6) {
        OpenVPNLogger.d("OpenVPNThreadv3", "socket_protect()");
        return mService.protectFd(socket);

    }

    @Override
    public void stopVPN() {
        mHandler.post(this::stop);
    }

    @Override
    public void networkChange() {
        mHandler.post(() -> {
            reconnect(1);
        });
    }

    @Override
    public void sendCRResponse(String response) {
        mHandler.post(() -> {
            post_cc_msg("CR_RESPONSE," + response + "\n");
        });
    }

    @Override
    public void log(ClientAPI_LogInfo arg0) {
        String logmsg = arg0.getText();
        while (logmsg.endsWith("\n"))
            logmsg = logmsg.substring(0, logmsg.length() - 1);

        OpenVPNLogger.d("OpenVPNThreadv3", logmsg);
    }

    @Override
    public void event(ClientAPI_Event event) {
        String name = event.getName();
        String info = event.getInfo();
        if (name.equals("INFO")) {
            if (info.startsWith("OPEN_URL:") || info.startsWith("CR_TEXT:")
                    || info.startsWith("WEB_AUTH:")) {
                mService.trigger_sso(info);
            } else {
                OpenVPNLogger.d("OpenVPNThreadv3", info);
            }
        } else if (name.equals("COMPRESSION_ENABLED") || name.equals(("WARN"))) {
            OpenVPNLogger.d("OpenVPNThreadv3", String.format(Locale.US, "%s: %s", name, info));
        } else {
            ConnectionState state = ConnectionState.IDLE;
            switch (name) {
                case "RESOLVE":
                case "WAIT":
                case "RECONNECTING":
                case "CONNECTING":
                case "GET_CONFIG":
                case "ASSIGN_IP":
                    state = ConnectionState.CONNECTING;
                    break;
                case "DISCONNECTED":
                    state = ConnectionState.DISCONNECTED;
                    break;
                case "CONNECTED":
                    state = ConnectionState.CONNECTED;
                    break;
            }
            mService.updateState(state);
            OpenVPNLogger.d("OpenVPNThreadv3qwe", name + info);
        }
        if (event.getError()) {
            OpenVPNLogger.e("OpenVPNThreadv3", String.format("EVENT(Error): %s: %s", name, info));
        }
    }

    @Override
    public net.openvpn.ovpn3.ClientAPI_StringVec tun_builder_get_local_networks(boolean ipv6) {
        net.openvpn.ovpn3.ClientAPI_StringVec nets = new net.openvpn.ovpn3.ClientAPI_StringVec();
        for (String net : NetworkUtils.getLocalNetworks(mService.getConnectivityManager(), ipv6)) {
            nets.add(net);
        }
        return nets;
    }

    @Override
    public boolean pause_on_connection_timeout() {
        OpenVPNLogger.d("OpenVPNThreadv3", "pause on connection timeout?! ");
        return true;
    }


    // When a connection is close to timeout, the core will call this
    // method.  If it returns false, the core will disconnect with a
    // CONNECTION_TIMEOUT event.  If true, the core will enter a PAUSE
    // state.

    @Override
    public void stop() {
        super.stop();
        mService.openvpnStopped();
    }

    @Override
    public void reconnect() {
        mHandler.post(() -> {
            reconnect(1);
        });
    }

    private void pollStatus() {
        ClientAPI_TransportStats t = transport_stats();
        long in = t.getBytesIn();
        long out = t.getBytesOut();
        OpenVPNLogger.d("OpenVPNThreadv3", "in: " + in + " / " + "out: " + out);
    }
}