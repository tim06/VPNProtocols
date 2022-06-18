package com.tim.basevpn;

import com.tim.basevpn.IConnectionStateListener;

interface IVPNService {
    oneway void startVPN();
    void stopVPN();

    oneway void registerCallback(IConnectionStateListener cb);
    oneway void unregisterCallback(IConnectionStateListener cb);
}
