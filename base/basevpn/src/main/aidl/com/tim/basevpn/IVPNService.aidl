package com.tim.basevpn;

import com.tim.basevpn.IConnectionStateListener;
import com.tim.basevpn.state.ConnectionState;
import com.tim.basevpn.configuration.VpnConfiguration;

interface IVPNService {
    oneway void startVPN();
    oneway void stopVPN();

    ConnectionState getState();

    oneway void registerCallback(IConnectionStateListener cb);
    oneway void unregisterCallback(IConnectionStateListener cb);
}
