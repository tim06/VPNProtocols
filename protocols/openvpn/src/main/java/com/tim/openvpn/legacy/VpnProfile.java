/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.tim.openvpn.legacy;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Locale;
import java.util.UUID;
import java.util.Vector;

public class VpnProfile implements Serializable {
    public static final String INLINE_TAG = "[[INLINE]]";
    public static final String DISPLAYNAME_TAG = "[[NAME]]";
    public static final int MAXLOGLEVEL = 4;
    public static final int TYPE_CERTIFICATES = 0;
    public static final int TYPE_PKCS12 = 1;
    public static final int TYPE_KEYSTORE = 2;
    public static final int TYPE_USERPASS = 3;
    public static final int TYPE_STATICKEYS = 4;
    public static final int TYPE_USERPASS_CERTIFICATES = 5;
    public static final int TYPE_USERPASS_PKCS12 = 6;
    public static final int TYPE_USERPASS_KEYSTORE = 7;
    public static final int X509_VERIFY_TLSREMOTE = 0;
    public static final int X509_VERIFY_TLSREMOTE_COMPAT_NOREMAPPING = 1;
    public static final int X509_VERIFY_TLSREMOTE_DN = 2;
    public static final int X509_VERIFY_TLSREMOTE_RDN = 3;
    public static final int X509_VERIFY_TLSREMOTE_RDN_PREFIX = 4;
    public static final int AUTH_RETRY_NONE_FORGET = 0;
    public static final int AUTH_RETRY_NOINTERACT = 2;
    public static final boolean mIsOpenVPN22 = false;
    private static final long serialVersionUID = 7085688938959334563L;
    public static String DEFAULT_DNS1 = "9.9.9.9";
    public static String DEFAULT_DNS2 = "2620:fe::fe";

    public int mAuthenticationType = TYPE_KEYSTORE;
    public String mName;
    public String mClientCertFilename;
    public String mTLSAuthDirection = "";
    public String mTLSAuthFilename;
    public String mClientKeyFilename;
    public String mCaFilename;
    public boolean mUseLzo = false;
    public String mPKCS12Filename;
    public boolean mUseTLSAuth = false;
    public String mDNS1 = DEFAULT_DNS1;
    public String mDNS2 = DEFAULT_DNS2;
    public String mIPv4Address;
    public String mIPv6Address;
    public boolean mOverrideDNS = false;
    public String mSearchDomain = "blinkt.de";
    public boolean mUseDefaultRoute = true;
    public boolean mUsePull = true;
    public String mCustomRoutes;
    public boolean mCheckRemoteCN = true;
    public boolean mExpectTLSCert = false;
    public String mRemoteCN = "";
    public String mPassword = "";
    public String mUsername = "";
    public boolean mRoutenopull = false;
    public boolean mUseRandomHostname = false;
    public boolean mUseFloat = false;
    public boolean mUseCustomConfig = false;
    public String mCustomConfigOptions = "";
    public String mVerb = "1";  //ignored
    public String mCipher = "";
    public boolean mNobind = true;
    public boolean mUseDefaultRoutev6 = true;
    public String mCustomRoutesv6 = "";
    public boolean mPersistTun = false;
    public String mConnectRetryMax = "-1";
    public String mConnectRetry = "2";
    public String mConnectRetryMaxTime = "300";
    public String mAuth = "";
    public int mX509AuthType = X509_VERIFY_TLSREMOTE_RDN;
    public String mx509UsernameField = null;
    public boolean mAllowLocalLAN;
    public String mExcludedRoutes;
    public int mMssFix = 0; // -1 is default,
    public boolean mRemoteRandom = false;
    public String mCrlFilename;
    public int mAuthRetry = AUTH_RETRY_NONE_FORGET;
    public int mTunMtu;
    public boolean mPushPeerInfo = false;
    public long mLastUsed;
    public String mServerName = "openvpn.example.com";
    public String mServerPort = "1194";
    public boolean mUseUdp = true;
    public boolean mTemporaryProfile = false;
    public String mDataCiphers = "";
    public boolean mBlockUnusedAddressFamilies = true;
    public boolean mCheckPeerFingerprint = false;
    public String mPeerFingerPrints = "";
    public int mCompatMode = 0;
    public boolean mUseLegacyProvider = false;
    public String mTlSCertProfile = "";

    // Public attributes, since I got mad with getter/setter
    // set members to default values
    private UUID mUuid;

    public VpnProfile(String name) {
        mUuid = UUID.randomUUID();
        mName = name;

        mLastUsed = System.currentTimeMillis();
    }

    public static String openVpnEscape(String unescaped) {
        if (unescaped == null)
            return null;
        String escapedString = unescaped.replace("\\", "\\\\");
        escapedString = escapedString.replace("\"", "\\\"");
        escapedString = escapedString.replace("\n", "\\n");

        if (escapedString.equals(unescaped) && !escapedString.contains(" ") &&
                !escapedString.contains("#") && !escapedString.contains(";")
                && !escapedString.equals("")  && !escapedString.contains("'"))
            return unescaped;
        else
            return '"' + escapedString + '"';
    }

    //! Put inline data inline and other data as normal escaped filename
    public static String insertFileData(String cfgentry, String filedata) {
        if (filedata == null) {
            return String.format("%s %s\n", cfgentry, "file missing in config profile");
        } else if (isEmbedded(filedata)) {
            String dataWithOutHeader = getEmbeddedContent(filedata);
            return String.format(Locale.ENGLISH, "<%s>\n%s\n</%s>\n", cfgentry, dataWithOutHeader, cfgentry);
        } else {
            return String.format(Locale.ENGLISH, "%s %s\n", cfgentry, openVpnEscape(filedata));
        }
    }

    public static String getEmbeddedContent(String data) {
        if (!data.contains(INLINE_TAG))
            return data;

        int start = data.indexOf(INLINE_TAG) + INLINE_TAG.length();
        return data.substring(start);
    }

    public static boolean isEmbedded(String data) {
        if (data == null)
            return false;
        if (data.startsWith(INLINE_TAG) || data.startsWith(DISPLAYNAME_TAG))
            return true;
        else
            return false;
    }

    public void clearDefaults() {
        mServerName = "unknown";
        mUsePull = false;
        mUseLzo = false;
        mUseDefaultRoute = false;
        mUseDefaultRoutev6 = false;
        mExpectTLSCert = false;
        mCheckRemoteCN = false;
        mPersistTun = false;
        mAllowLocalLAN = true;
        mPushPeerInfo = false;
        mMssFix = 0;
        mNobind = false;
    }

    public String getName() {
        if (TextUtils.isEmpty(mName))
            return "No profile name";
        return mName;
    }

    public String getConfigFile(boolean configForOvpn3) {
        StringBuilder cfg = new StringBuilder();
        cfg.append("# Config for OpenVPN 3 C++\n");

        boolean useTLSClient = (mAuthenticationType != TYPE_STATICKEYS);

        if (useTLSClient && mUsePull)
            cfg.append("client\n");
        else if (mUsePull)
            cfg.append("pull\n");
        else if (useTLSClient)
            cfg.append("tls-client\n");

        cfg.append("verb " + MAXLOGLEVEL + "\n");

        if (mConnectRetryMax == null) {
            mConnectRetryMax = "-1";
        }

        if (!mConnectRetryMax.equals("-1"))
            cfg.append("connect-retry-max ").append(mConnectRetryMax).append("\n");

        if (TextUtils.isEmpty(mConnectRetry))
            mConnectRetry = "2";

        if (TextUtils.isEmpty(mConnectRetryMaxTime))
            mConnectRetryMaxTime = "300";


        if (!mIsOpenVPN22)
            cfg.append("connect-retry ").append(mConnectRetry).append(" ").append(mConnectRetryMaxTime).append("\n");
        else if (mIsOpenVPN22 && !mUseUdp)
            cfg.append("connect-retry ").append(mConnectRetry).append("\n");

        cfg.append("resolv-retry 60\n");

        cfg.append("dev tun\n");

        switch (mAuthenticationType) {
            case VpnProfile.TYPE_USERPASS_CERTIFICATES:
                cfg.append("auth-user-pass\n");
            case VpnProfile.TYPE_CERTIFICATES:
                // Ca
                if (!TextUtils.isEmpty(mCaFilename)) {
                    cfg.append(insertFileData("ca", mCaFilename));
                }

                // Client Cert + Key
                cfg.append(insertFileData("key", mClientKeyFilename));
                cfg.append(insertFileData("cert", mClientCertFilename));

                break;
            case VpnProfile.TYPE_USERPASS_PKCS12:
                cfg.append("auth-user-pass\n");
            case VpnProfile.TYPE_PKCS12:
                cfg.append(insertFileData("pkcs12", mPKCS12Filename));

                if (!TextUtils.isEmpty(mCaFilename)) {
                    cfg.append(insertFileData("ca", mCaFilename));
                }
                break;
            case VpnProfile.TYPE_USERPASS:
                cfg.append("auth-user-pass\n");
                if (!TextUtils.isEmpty(mCaFilename))
                    cfg.append(insertFileData("ca", mCaFilename));
                if (configForOvpn3) {
                    // OpenVPN 3 needs to be told that a client certificate is not required
                    cfg.append("client-cert-not-required\n");
                }
        }

        if (mCheckPeerFingerprint) {
            cfg.append("<peer-fingerprint>\n").append(mPeerFingerPrints).append("\n</peer-fingerprint>\n");
        }

        if (!TextUtils.isEmpty(mCrlFilename))
            cfg.append(insertFileData("crl-verify", mCrlFilename));

        if (mUseLzo) {
            cfg.append("comp-lzo\n");
        }

        if (mUseTLSAuth) {
            boolean useTlsCrypt = mTLSAuthDirection.equals("tls-crypt");
            boolean useTlsCrypt2 = mTLSAuthDirection.equals("tls-crypt-v2");

            if (mAuthenticationType == TYPE_STATICKEYS)
                cfg.append(insertFileData("secret", mTLSAuthFilename));
            else if (useTlsCrypt)
                cfg.append(insertFileData("tls-crypt", mTLSAuthFilename));
            else if (useTlsCrypt2)
                cfg.append(insertFileData("tls-crypt-v2", mTLSAuthFilename));
            else
                cfg.append(insertFileData("tls-auth", mTLSAuthFilename));

            if (!TextUtils.isEmpty(mTLSAuthDirection) && !useTlsCrypt && !useTlsCrypt2) {
                cfg.append("key-direction ");
                cfg.append(mTLSAuthDirection);
                cfg.append("\n");
            }

        }

        if (!mUsePull) {
            if (!TextUtils.isEmpty(mIPv4Address))
                cfg.append("ifconfig ").append(cidrToIPAndNetmask(mIPv4Address)).append("\n");

            if (!TextUtils.isEmpty(mIPv6Address)) {
                // Use our own ip as gateway since we ignore it anyway
                String fakegw = mIPv6Address.split("/", 2)[0];
                cfg.append("ifconfig-ipv6 ").append(mIPv6Address).append(" ").append(fakegw).append("\n");
            }

        }

        if (mUsePull && mRoutenopull)
            cfg.append("route-nopull\n");

        String routes = "";

        if (mUseDefaultRoute)
            routes += "route 0.0.0.0 0.0.0.0 vpn_gateway\n";
        else {
            for (String route : getCustomRoutes(mCustomRoutes)) {
                routes += "route " + route + " vpn_gateway\n";
            }

            for (String route : getCustomRoutes(mExcludedRoutes)) {
                routes += "route " + route + " net_gateway\n";
            }
        }


        if (mUseDefaultRoutev6)
            cfg.append("route-ipv6 ::/0\n");
        else
            for (String route : getCustomRoutesv6(mCustomRoutesv6)) {
                routes += "route-ipv6 " + route + "\n";
            }

        cfg.append(routes);

        if (mOverrideDNS || !mUsePull) {
            if (!TextUtils.isEmpty(mDNS1)) {
                cfg.append("dhcp-option DNS ").append(mDNS1).append("\n");
            }
            if (!TextUtils.isEmpty(mDNS2)) {
                cfg.append("dhcp-option DNS ").append(mDNS2).append("\n");
            }
            if (!TextUtils.isEmpty(mSearchDomain))
                cfg.append("dhcp-option DOMAIN ").append(mSearchDomain).append("\n");

        }

        if (mMssFix != 0) {
            if (mMssFix != 1450) {
                if (configForOvpn3)
                    cfg.append(String.format(Locale.US, "mssfix %d mtu\n", mMssFix));
                else
                    cfg.append(String.format(Locale.US, "mssfix %d\n", mMssFix));
            } else
                cfg.append("mssfix\n");
        }

        if (mTunMtu >= 48 && mTunMtu != 1500) {
            cfg.append(String.format(Locale.US, "tun-mtu %d\n", mTunMtu));
        }

        if (mNobind)
            cfg.append("nobind\n");

        if (!TextUtils.isEmpty(mDataCiphers)) {
            cfg.append("data-ciphers ").append(mDataCiphers).append("\n");
        }

        if (mCompatMode > 0) {
            int major = mCompatMode / 10000;
            int minor = mCompatMode % 10000 / 100;
            int patch = mCompatMode % 100;
            cfg.append(String.format(Locale.US, "compat-mode %d.%d.%d\n", major, minor, patch));

        }

        if (!TextUtils.isEmpty(mCipher)) {
            cfg.append("cipher ").append(mCipher).append("\n");
        }

        if (!TextUtils.isEmpty(mAuth)) {
            cfg.append("auth ").append(mAuth).append("\n");
        }

        // Obscure Settings dialog
        if (mUseRandomHostname)
            cfg.append("#my favorite options :)\nremote-random-hostname\n");

        if (mUseFloat)
            cfg.append("float\n");

        if (mPersistTun) {
            cfg.append("persist-tun\n");
            cfg.append("# persist-tun also enables pre resolving to avoid DNS resolve problem\n");
            if (!mIsOpenVPN22)
                cfg.append("preresolve\n");
        }

        if (mPushPeerInfo)
            cfg.append("push-peer-info\n");


        if (mUseCustomConfig) {
            cfg.append("# Custom configuration options\n");
            cfg.append("# You are on your on own here :)\n");
            cfg.append(mCustomConfigOptions);
            cfg.append("\n");

        }

        return cfg.toString();
    }

    private Collection<String> getCustomRoutes(String routes) {
        Vector<String> cidrRoutes = new Vector<>();
        if (routes == null) {
            // No routes set, return empty vector
            return cidrRoutes;
        }
        for (String route : routes.split("[\n \t]")) {
            if (!route.equals("")) {
                String cidrroute = cidrToIPAndNetmask(route);
                if (cidrroute == null)
                    return cidrRoutes;

                cidrRoutes.add(cidrroute);
            }
        }

        return cidrRoutes;
    }

    private Collection<String> getCustomRoutesv6(String routes) {
        Vector<String> cidrRoutes = new Vector<>();
        if (routes == null) {
            // No routes set, return empty vector
            return cidrRoutes;
        }
        for (String route : routes.split("[\n \t]")) {
            if (!route.equals("")) {
                cidrRoutes.add(route);
            }
        }

        return cidrRoutes;
    }

    private String cidrToIPAndNetmask(String route) {
        String[] parts = route.split("/");

        // No /xx, assume /32 as netmask
        if (parts.length == 1)
            parts = (route + "/32").split("/");

        if (parts.length != 2)
            return null;
        int len;
        try {
            len = Integer.parseInt(parts[1]);
        } catch (NumberFormatException ne) {
            return null;
        }
        if (len < 0 || len > 32)
            return null;


        long nm = 0xffffffffL;
        nm = (nm << (32 - len)) & 0xffffffffL;

        String netmask = String.format(Locale.ENGLISH, "%d.%d.%d.%d", (nm & 0xff000000) >> 24, (nm & 0xff0000) >> 16, (nm & 0xff00) >> 8, nm & 0xff);
        return parts[0] + "  " + netmask;
    }
}




