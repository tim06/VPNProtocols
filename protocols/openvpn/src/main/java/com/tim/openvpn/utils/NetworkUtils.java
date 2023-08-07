package com.tim.openvpn.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.provider.Settings;

import com.tim.openvpn.model.CIDRIP;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.Vector;

public class NetworkUtils {

    public static Vector<String> getLocalNetworks(ConnectivityManager connectivityManager, boolean ipv6) {

        Vector<String> nets = new Vector<>();
        Network[] networks = connectivityManager.getAllNetworks();
        for (Network network : networks) {
            NetworkInfo ni = connectivityManager.getNetworkInfo(network);
            LinkProperties li = connectivityManager.getLinkProperties(network);

            NetworkCapabilities nc = connectivityManager.getNetworkCapabilities(network);

            // Ignore network if it has no capabilities
            if (nc == null)
                continue;

            // Skip VPN networks like ourselves
            if (nc.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
                continue;

            // Also skip mobile networks
            if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                continue;


            for (LinkAddress la : li.getLinkAddresses()) {
                if ((la.getAddress() instanceof Inet4Address && !ipv6) ||
                        (la.getAddress() instanceof Inet6Address && ipv6)) {
                    NetworkSpace.IpAddress ipaddress;
                    if (la.getAddress() instanceof Inet6Address)
                        ipaddress = new NetworkSpace.IpAddress((Inet6Address) la.getAddress(), la.getPrefixLength(), true);
                    else
                        ipaddress = new NetworkSpace.IpAddress(new CIDRIP(la.getAddress().getHostAddress(), la.getPrefixLength()), true);

                    nets.add(ipaddress.toString());
                }
            }
        }

        return nets;
    }

    @SuppressLint("HardwareIds")
    public static String getFakeMacAddrFromSAAID(ContentResolver contentResolver) {
        char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

        String saaid = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);

        if (saaid == null)
            return null;

        StringBuilder ret = new StringBuilder();
        if (saaid.length() >= 6) {
            byte[] sb = saaid.getBytes();
            for (int b = 0; b <= 6; b++) {
                if (b != 0)
                    ret.append(":");
                int v = sb[b] & 0xFF;
                ret.append(HEX_ARRAY[v >>> 4]);
                ret.append(HEX_ARRAY[v & 0x0F]);
            }
        }
        return ret.toString();
    }
}
