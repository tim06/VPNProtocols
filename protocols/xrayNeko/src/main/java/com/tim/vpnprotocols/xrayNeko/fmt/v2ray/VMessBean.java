package com.tim.vpnprotocols.xrayNeko.fmt.v2ray;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import com.tim.vpnprotocols.xrayNeko.util.JavaUtil;
import com.tim.vpnprotocols.xrayNeko.fmt.KryoConverters;

public class VMessBean extends StandardV2RayBean {

    public Integer alterId; // alterID == -1 --> VLESS

    @Override
    public void initializeDefaultValues() {
        super.initializeDefaultValues();

        alterId = alterId != null ? alterId : 0;
        encryption = JavaUtil.isNotBlank(encryption) ? encryption : "auto";
    }

    @NotNull
    @Override
    public VMessBean clone() {
        return KryoConverters.deserialize(new VMessBean(), KryoConverters.serialize(this));
    }

    public static final Creator<VMessBean> CREATOR = new CREATOR<VMessBean>() {
        @NonNull
        @Override
        public VMessBean newInstance() {
            return new VMessBean();
        }

        @Override
        public VMessBean[] newArray(int size) {
            return new VMessBean[size];
        }
    };
}
