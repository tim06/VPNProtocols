package com.tim.basevpn;

import com.tim.basevpn.state.ConnectionState;

interface IConnectionStateListener {
  oneway void stateChanged(in ConnectionState status);
  oneway void trafficUpdate(long txRate, long rxRate, long txTotal, long rxTotal);
}
