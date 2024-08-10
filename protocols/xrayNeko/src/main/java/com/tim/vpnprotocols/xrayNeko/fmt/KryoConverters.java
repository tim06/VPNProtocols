package com.tim.vpnprotocols.xrayNeko.fmt;

import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.tim.vpnprotocols.xrayNeko.util.JavaUtil;
import com.tim.vpnprotocols.xrayNeko.util.KryosKt;
import com.tim.vpnprotocols.xrayNeko.util.Logs;
import com.tim.vpnprotocols.xrayNeko.fmt.http.HttpBean;
import com.tim.vpnprotocols.xrayNeko.fmt.hysteria.HysteriaBean;
import com.tim.vpnprotocols.xrayNeko.fmt.internal.ChainBean;
import com.tim.vpnprotocols.xrayNeko.fmt.mieru.MieruBean;
import com.tim.vpnprotocols.xrayNeko.fmt.naive.NaiveBean;
import com.tim.vpnprotocols.xrayNeko.fmt.shadowsocks.ShadowsocksBean;
import com.tim.vpnprotocols.xrayNeko.fmt.shadowtls.ShadowTLSBean;
import com.tim.vpnprotocols.xrayNeko.fmt.socks.SOCKSBean;
import com.tim.vpnprotocols.xrayNeko.fmt.ssh.SSHBean;
import com.tim.vpnprotocols.xrayNeko.fmt.trojan.TrojanBean;
import com.tim.vpnprotocols.xrayNeko.fmt.trojan_go.TrojanGoBean;
import com.tim.vpnprotocols.xrayNeko.fmt.tuic.TuicBean;
import com.tim.vpnprotocols.xrayNeko.fmt.v2ray.VMessBean;
import com.tim.vpnprotocols.xrayNeko.fmt.wireguard.WireGuardBean;

public class KryoConverters {

    private static final byte[] NULL = new byte[0];

    
    public static byte[] serialize(Serializable bean) {
        if (bean == null) return NULL;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteBufferOutput buffer = KryosKt.byteBuffer(out);
        bean.serializeToBuffer(buffer);
        buffer.flush();
        buffer.close();
        return out.toByteArray();
    }

    public static <T extends Serializable> T deserialize(T bean, byte[] bytes) {
        if (bytes == null) return bean;
        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        ByteBufferInput buffer = KryosKt.byteBuffer(input);
        try {
            bean.deserializeFromBuffer(buffer);
        } catch (KryoException e) {
            Logs.INSTANCE.w(e);
        }
        bean.initializeDefaultValues();
        return bean;
    }

    
    public static SOCKSBean socksDeserialize(byte[] bytes) {
        if (JavaUtil.isEmpty(bytes)) return null;
        return deserialize(new SOCKSBean(), bytes);
    }

    
    public static HttpBean httpDeserialize(byte[] bytes) {
        if (JavaUtil.isEmpty(bytes)) return null;
        return deserialize(new HttpBean(), bytes);
    }

    
    public static ShadowsocksBean shadowsocksDeserialize(byte[] bytes) {
        if (JavaUtil.isEmpty(bytes)) return null;
        return deserialize(new ShadowsocksBean(), bytes);
    }

    
    public static ConfigBean configDeserialize(byte[] bytes) {
        if (JavaUtil.isEmpty(bytes)) return null;
        return deserialize(new ConfigBean(), bytes);
    }

    
    public static VMessBean vmessDeserialize(byte[] bytes) {
        if (JavaUtil.isEmpty(bytes)) return null;
        return deserialize(new VMessBean(), bytes);
    }

    
    public static TrojanBean trojanDeserialize(byte[] bytes) {
        if (JavaUtil.isEmpty(bytes)) return null;
        return deserialize(new TrojanBean(), bytes);
    }

    
    public static TrojanGoBean trojanGoDeserialize(byte[] bytes) {
        if (JavaUtil.isEmpty(bytes)) return null;
        return deserialize(new TrojanGoBean(), bytes);
    }

    
    public static MieruBean mieruDeserialize(byte[] bytes) {
        if (JavaUtil.isEmpty(bytes)) return null;
        return deserialize(new MieruBean(), bytes);
    }

    
    public static NaiveBean naiveDeserialize(byte[] bytes) {
        if (JavaUtil.isEmpty(bytes)) return null;
        return deserialize(new NaiveBean(), bytes);
    }

    
    public static HysteriaBean hysteriaDeserialize(byte[] bytes) {
        if (JavaUtil.isEmpty(bytes)) return null;
        return deserialize(new HysteriaBean(), bytes);
    }

    
    public static SSHBean sshDeserialize(byte[] bytes) {
        if (JavaUtil.isEmpty(bytes)) return null;
        return deserialize(new SSHBean(), bytes);
    }

    
    public static WireGuardBean wireguardDeserialize(byte[] bytes) {
        if (JavaUtil.isEmpty(bytes)) return null;
        return deserialize(new WireGuardBean(), bytes);
    }

    
    public static TuicBean tuicDeserialize(byte[] bytes) {
        if (JavaUtil.isEmpty(bytes)) return null;
        return deserialize(new TuicBean(), bytes);
    }

    
    public static ShadowTLSBean shadowTLSDeserialize(byte[] bytes) {
        if (JavaUtil.isEmpty(bytes)) return null;
        return deserialize(new ShadowTLSBean(), bytes);
    }

    
    public static ChainBean chainDeserialize(byte[] bytes) {
        if (JavaUtil.isEmpty(bytes)) return null;
        return deserialize(new ChainBean(), bytes);
    }

    
    /*public static NekoBean nekoDeserialize(byte[] bytes) {
        if (JavaUtil.isEmpty(bytes)) return null;
        return deserialize(new NekoBean(), bytes);
    }

    
    public static SubscriptionBean subscriptionDeserialize(byte[] bytes) {
        if (JavaUtil.isEmpty(bytes)) return null;
        return deserialize(new SubscriptionBean(), bytes);
    }*/

}
