package com.tim.vpnprotocols.xrayNeko.fmt

import com.esotericsoftware.kryo.io.ByteBufferInput
import com.esotericsoftware.kryo.io.ByteBufferOutput
import com.tim.vpnprotocols.xrayNeko.fmt.http.HttpBean
import com.tim.vpnprotocols.xrayNeko.fmt.hysteria.HysteriaBean
import com.tim.vpnprotocols.xrayNeko.fmt.hysteria.canUseSingBox
import com.tim.vpnprotocols.xrayNeko.fmt.internal.ChainBean
import com.tim.vpnprotocols.xrayNeko.fmt.mieru.MieruBean
import com.tim.vpnprotocols.xrayNeko.fmt.naive.NaiveBean
import com.tim.vpnprotocols.xrayNeko.fmt.shadowsocks.ShadowsocksBean
import com.tim.vpnprotocols.xrayNeko.fmt.shadowtls.ShadowTLSBean
import com.tim.vpnprotocols.xrayNeko.fmt.socks.SOCKSBean
import com.tim.vpnprotocols.xrayNeko.fmt.ssh.SSHBean
import com.tim.vpnprotocols.xrayNeko.fmt.trojan.TrojanBean
import com.tim.vpnprotocols.xrayNeko.fmt.trojan_go.TrojanGoBean
import com.tim.vpnprotocols.xrayNeko.fmt.tuic.TuicBean
import com.tim.vpnprotocols.xrayNeko.fmt.v2ray.VMessBean
import com.tim.vpnprotocols.xrayNeko.fmt.v2ray.isTLS
import com.tim.vpnprotocols.xrayNeko.fmt.wireguard.WireGuardBean
import com.tim.vpnprotocols.xrayNeko.parser.applyDefaultValues

data class ProxyEntity(
    var id: Long = 1L,
    var groupId: Long = 0L,
    var type: Int = 0,
    var userOrder: Long = 0L,
    var tx: Long = 0L,
    var rx: Long = 0L,
    var status: Int = 0,
    var ping: Int = 0,
    var uuid: String = "",
    var error: String? = null,
    var socksBean: SOCKSBean? = null,
    var httpBean: HttpBean? = null,
    var ssBean: ShadowsocksBean? = null,
    var vmessBean: VMessBean? = null,
    var trojanBean: TrojanBean? = null,
    var trojanGoBean: TrojanGoBean? = null,
    var mieruBean: MieruBean? = null,
    var naiveBean: NaiveBean? = null,
    var hysteriaBean: HysteriaBean? = null,
    var tuicBean: TuicBean? = null,
    var sshBean: SSHBean? = null,
    var wgBean: WireGuardBean? = null,
    var shadowTLSBean: ShadowTLSBean? = null,
    var chainBean: ChainBean? = null,
    //var nekoBean: NekoBean? = null,
    var configBean: ConfigBean? = null,
) : com.tim.vpnprotocols.xrayNeko.fmt.Serializable() {

    companion object {
        const val TYPE_SOCKS = 0
        const val TYPE_HTTP = 1
        const val TYPE_SS = 2
        const val TYPE_VMESS = 4
        const val TYPE_TROJAN = 6

        const val TYPE_TROJAN_GO = 7
        const val TYPE_MIERU = 21
        const val TYPE_NAIVE = 9
        const val TYPE_HYSTERIA = 15
        const val TYPE_TUIC = 20

        const val TYPE_SSH = 17
        const val TYPE_WG = 18

        const val TYPE_SHADOWTLS = 19

        const val TYPE_CONFIG = 998
        const val TYPE_NEKO = 999

        const val TYPE_CHAIN = 8

        //val chainName by lazy { app.getString(R.string.proxy_chain) }

        private val placeHolderBean = SOCKSBean().applyDefaultValues()

        @JvmField
        val CREATOR = object : Serializable.CREATOR<ProxyEntity>() {

            override fun newInstance(): ProxyEntity {
                return ProxyEntity()
            }

            override fun newArray(size: Int): Array<ProxyEntity?> {
                return arrayOfNulls(size)
            }
        }
    }

    @Transient
    var dirty: Boolean = false

    override fun initializeDefaultValues() {
    }

    override fun serializeToBuffer(output: ByteBufferOutput) {
        output.writeInt(0)

        //output.writeLong(id)
        output.writeLong(groupId)
        output.writeInt(type)
        output.writeLong(userOrder)
        output.writeLong(tx)
        output.writeLong(rx)
        output.writeInt(status)
        output.writeInt(ping)
        output.writeString(uuid)
        output.writeString(error)

        val data = KryoConverters.serialize(requireBean())
        output.writeVarInt(data.size, true)
        output.writeBytes(data)

        output.writeBoolean(dirty)
    }

    override fun deserializeFromBuffer(input: ByteBufferInput) {
        val version = input.readInt()

        groupId = input.readLong()
        type = input.readInt()
        userOrder = input.readLong()
        tx = input.readLong()
        rx = input.readLong()
        status = input.readInt()
        ping = input.readInt()
        uuid = input.readString()
        error = input.readString()
        putByteArray(input.readBytes(input.readVarInt(true)))

        dirty = input.readBoolean()
    }


    fun putByteArray(byteArray: ByteArray) {
        when (type) {
            TYPE_SOCKS -> socksBean = KryoConverters.socksDeserialize(byteArray)
            TYPE_HTTP -> httpBean = KryoConverters.httpDeserialize(byteArray)
            TYPE_SS -> ssBean = KryoConverters.shadowsocksDeserialize(byteArray)
            TYPE_VMESS -> vmessBean = KryoConverters.vmessDeserialize(byteArray)
            TYPE_TROJAN -> trojanBean = KryoConverters.trojanDeserialize(byteArray)
            TYPE_TROJAN_GO -> trojanGoBean = KryoConverters.trojanGoDeserialize(byteArray)
            TYPE_MIERU -> mieruBean = KryoConverters.mieruDeserialize(byteArray)
            TYPE_NAIVE -> naiveBean = KryoConverters.naiveDeserialize(byteArray)
            TYPE_HYSTERIA -> hysteriaBean = KryoConverters.hysteriaDeserialize(byteArray)
            TYPE_SSH -> sshBean = KryoConverters.sshDeserialize(byteArray)
            TYPE_WG -> wgBean = KryoConverters.wireguardDeserialize(byteArray)
            TYPE_TUIC -> tuicBean = KryoConverters.tuicDeserialize(byteArray)
            TYPE_SHADOWTLS -> shadowTLSBean = KryoConverters.shadowTLSDeserialize(byteArray)
            TYPE_CHAIN -> chainBean = KryoConverters.chainDeserialize(byteArray)
            //TYPE_NEKO -> nekoBean = KryoConverters.nekoDeserialize(byteArray)
            TYPE_CONFIG -> configBean = KryoConverters.configDeserialize(byteArray)
        }
    }

    fun displayType(): String = when (type) {
        TYPE_SOCKS -> socksBean!!.protocolName()
        TYPE_HTTP -> if (httpBean!!.isTLS()) "HTTPS" else "HTTP"
        TYPE_SS -> "Shadowsocks"
        TYPE_VMESS -> if (vmessBean!!.isVLESS) "VLESS" else "VMess"
        TYPE_TROJAN -> "Trojan"
        TYPE_TROJAN_GO -> "Trojan-Go"
        TYPE_MIERU -> "Mieru"
        TYPE_NAIVE -> "Naïve"
        TYPE_HYSTERIA -> "Hysteria" + hysteriaBean!!.protocolVersion
        TYPE_SSH -> "SSH"
        TYPE_WG -> "WireGuard"
        TYPE_TUIC -> "TUIC"
        TYPE_SHADOWTLS -> "ShadowTLS"
        TYPE_CHAIN -> "chainName"
        //TYPE_NEKO -> nekoBean!!.displayType()
        TYPE_CONFIG -> configBean!!.displayType()
        else -> "Undefined type $type"
    }

    fun displayName() = requireBean().displayName()
    fun displayAddress() = requireBean().displayAddress()

    fun requireBean(): AbstractBean {
        return when (type) {
            TYPE_SOCKS -> socksBean
            TYPE_HTTP -> httpBean
            TYPE_SS -> ssBean
            TYPE_VMESS -> vmessBean
            TYPE_TROJAN -> trojanBean
            TYPE_TROJAN_GO -> trojanGoBean
            TYPE_MIERU -> mieruBean
            TYPE_NAIVE -> naiveBean
            TYPE_HYSTERIA -> hysteriaBean
            TYPE_SSH -> sshBean
            TYPE_WG -> wgBean
            TYPE_TUIC -> tuicBean
            TYPE_SHADOWTLS -> shadowTLSBean
            TYPE_CHAIN -> chainBean
            //TYPE_NEKO -> nekoBean
            TYPE_CONFIG -> configBean
            else -> error("Undefined type $type")
        } ?: error("Null ${displayType()} profile")
    }

    fun haveLink(): Boolean {
        return when (type) {
            TYPE_CHAIN -> false
            else -> true
        }
    }

    fun haveStandardLink(): Boolean {
        return when (requireBean()) {
            is SSHBean -> false
            is WireGuardBean -> false
            is ShadowTLSBean -> false
            //is NekoBean -> nekoBean!!.haveStandardLink()
            is ConfigBean -> false
            else -> true
        }
    }

    /*fun toStdLink(compact: Boolean = false): String = with(requireBean()) {
        when (this) {
            is SOCKSBean -> toUri()
            is HttpBean -> toUri()
            is ShadowsocksBean -> toUri()
            is VMessBean -> toUriVMessVLESSTrojan(false)
            is TrojanBean -> toUriVMessVLESSTrojan(true)
            is TrojanGoBean -> toUri()
            is NaiveBean -> toUri()
            is HysteriaBean -> toUri()
            is TuicBean -> toUri()
            is NekoBean -> shareLink()
            else -> toUniversalLink()
        }
    }*/

    /*fun exportConfig(): Pair<String, String> {
        var name = "${requireBean().displayName()}.json"

        return with(requireBean()) {
            StringBuilder().apply {
                val config = buildConfig(this@ProxyEntity, forExport = true)
                append(config.config)

                if (!config.externalIndex.all { it.chain.isEmpty() }) {
                    name = "profiles.txt"
                }

                for ((chain) in config.externalIndex) {
                    chain.entries.forEachIndexed { index, (port, profile) ->
                        when (val bean = profile.requireBean()) {
                            is TrojanGoBean -> {
                                append("\n\n")
                                append(bean.buildTrojanGoConfig(port))
                            }

                            is MieruBean -> {
                                append("\n\n")
                                append(bean.buildMieruConfig(port))
                            }

                            is NaiveBean -> {
                                append("\n\n")
                                append(bean.buildNaiveConfig(port))
                            }

                            is HysteriaBean -> {
                                append("\n\n")
                                append(bean.buildHysteria1Config(port, null))
                            }
                        }
                    }
                }
            }.toString()
        } to name
    }*/

    fun needExternal(): Boolean {
        return when (type) {
            TYPE_TROJAN_GO -> true
            TYPE_MIERU -> true
            TYPE_NAIVE -> true
            TYPE_HYSTERIA -> !hysteriaBean!!.canUseSingBox()
            TYPE_NEKO -> true
            else -> false
        }
    }

    /*fun needCoreMux(): Boolean {
        return when (type) {
            TYPE_VMESS -> if (vmessBean!!.isVLESS) {
                Protocols.isProfileNeedMux(vmessBean!!) && Protocols.shouldEnableMux("vless")
            } else {
                Protocols.isProfileNeedMux(vmessBean!!) && Protocols.shouldEnableMux("vmess")
            }

            TYPE_TROJAN -> Protocols.isProfileNeedMux(trojanBean!!)
                    && Protocols.shouldEnableMux("trojan")

            TYPE_SS -> !ssBean!!.sUoT && Protocols.shouldEnableMux("shadowsocks")
            else -> false
        }
    }*/

    fun putBean(bean: AbstractBean): ProxyEntity {
        socksBean = null
        httpBean = null
        ssBean = null
        vmessBean = null
        trojanBean = null
        trojanGoBean = null
        mieruBean = null
        naiveBean = null
        hysteriaBean = null
        sshBean = null
        wgBean = null
        tuicBean = null
        shadowTLSBean = null
        chainBean = null
        configBean = null
        //nekoBean = null

        when (bean) {
            is SOCKSBean -> {
                type = TYPE_SOCKS
                socksBean = bean
            }

            is HttpBean -> {
                type = TYPE_HTTP
                httpBean = bean
            }

            is ShadowsocksBean -> {
                type = TYPE_SS
                ssBean = bean
            }

            is VMessBean -> {
                type = TYPE_VMESS
                vmessBean = bean
            }

            is TrojanBean -> {
                type = TYPE_TROJAN
                trojanBean = bean
            }

            is TrojanGoBean -> {
                type = TYPE_TROJAN_GO
                trojanGoBean = bean
            }

            is MieruBean -> {
                type = TYPE_MIERU
                mieruBean = bean
            }

            is NaiveBean -> {
                type = TYPE_NAIVE
                naiveBean = bean
            }

            is HysteriaBean -> {
                type = TYPE_HYSTERIA
                hysteriaBean = bean
            }

            is SSHBean -> {
                type = TYPE_SSH
                sshBean = bean
            }

            is WireGuardBean -> {
                type = TYPE_WG
                wgBean = bean
            }

            is TuicBean -> {
                type = TYPE_TUIC
                tuicBean = bean
            }

            is ShadowTLSBean -> {
                type = TYPE_SHADOWTLS
                shadowTLSBean = bean
            }

            is ChainBean -> {
                type = TYPE_CHAIN
                chainBean = bean
            }

            /*is NekoBean -> {
                type = TYPE_NEKO
                nekoBean = bean
            }*/

            is ConfigBean -> {
                type = TYPE_CONFIG
                configBean = bean
            }

            else -> error("Undefined type $type")
        }
        return this
    }

    /*fun settingIntent(ctx: Context, isSubscription: Boolean): Intent {
        return Intent(
            ctx, when (type) {
                TYPE_SOCKS -> SocksSettingsActivity::class.java
                TYPE_HTTP -> HttpSettingsActivity::class.java
                TYPE_SS -> ShadowsocksSettingsActivity::class.java
                TYPE_VMESS -> VMessSettingsActivity::class.java
                TYPE_TROJAN -> TrojanSettingsActivity::class.java
                TYPE_TROJAN_GO -> TrojanGoSettingsActivity::class.java
                TYPE_MIERU -> MieruSettingsActivity::class.java
                TYPE_NAIVE -> NaiveSettingsActivity::class.java
                TYPE_HYSTERIA -> HysteriaSettingsActivity::class.java
                TYPE_SSH -> SSHSettingsActivity::class.java
                TYPE_WG -> WireGuardSettingsActivity::class.java
                TYPE_TUIC -> TuicSettingsActivity::class.java
                TYPE_SHADOWTLS -> ShadowTLSSettingsActivity::class.java
                TYPE_CHAIN -> ChainSettingsActivity::class.java
                TYPE_NEKO -> NekoSettingActivity::class.java
                TYPE_CONFIG -> ConfigSettingActivity::class.java
                else -> throw IllegalArgumentException()
            }
        ).apply {
            putExtra(ProfileSettingsActivity.EXTRA_PROFILE_ID, id)
            putExtra(ProfileSettingsActivity.EXTRA_IS_SUBSCRIPTION, isSubscription)
        }
    }*/

    /*@androidx.room.Dao
    interface Dao {

        @Query("select * from proxy_entities")
        fun getAll(): List<ProxyEntity>

        @Query("SELECT id FROM proxy_entities WHERE groupId = :groupId ORDER BY userOrder")
        fun getIdsByGroup(groupId: Long): List<Long>

        @Query("SELECT * FROM proxy_entities WHERE groupId = :groupId ORDER BY userOrder")
        fun getByGroup(groupId: Long): List<ProxyEntity>

        @Query("SELECT * FROM proxy_entities WHERE id in (:proxyIds)")
        fun getEntities(proxyIds: List<Long>): List<ProxyEntity>

        @Query("SELECT COUNT(*) FROM proxy_entities WHERE groupId = :groupId")
        fun countByGroup(groupId: Long): Long

        @Query("SELECT  MAX(userOrder) + 1 FROM proxy_entities WHERE groupId = :groupId")
        fun nextOrder(groupId: Long): Long?

        @Query("SELECT * FROM proxy_entities WHERE id = :proxyId")
        fun getById(proxyId: Long): ProxyEntity?

        @Query("DELETE FROM proxy_entities WHERE id IN (:proxyId)")
        fun deleteById(proxyId: Long): Int

        @Query("DELETE FROM proxy_entities WHERE groupId = :groupId")
        fun deleteByGroup(groupId: Long)

        @Query("DELETE FROM proxy_entities WHERE groupId in (:groupId)")
        fun deleteByGroup(groupId: LongArray)

        @Delete
        fun deleteProxy(proxy: ProxyEntity): Int

        @Delete
        fun deleteProxy(proxies: List<ProxyEntity>): Int

        @Update
        fun updateProxy(proxy: ProxyEntity): Int

        @Update
        fun updateProxy(proxies: List<ProxyEntity>): Int

        @Insert
        fun addProxy(proxy: ProxyEntity): Long

        @Insert
        fun insert(proxies: List<ProxyEntity>)

        @Query("DELETE FROM proxy_entities WHERE groupId = :groupId")
        fun deleteAll(groupId: Long): Int

        @Query("DELETE FROM proxy_entities")
        fun reset()

    }*/

    override fun describeContents(): Int {
        return 0
    }
}