package com.tim.vpnprotocols.xrayNeko.fmt.internal;


import com.tim.vpnprotocols.xrayNeko.fmt.AbstractBean;

public abstract class InternalBean extends AbstractBean {

    @Override
    public String displayAddress() {
        return "";
    }

    @Override
    public boolean canICMPing() {
        return false;
    }

    @Override
    public boolean canTCPing() {
        return false;
    }

    @Override
    public boolean canMapping() {
        return false;
    }
}
