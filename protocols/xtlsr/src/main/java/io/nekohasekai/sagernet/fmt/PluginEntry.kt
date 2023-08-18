package io.nekohasekai.sagernet.fmt

import com.tim.xtlsr.R
import io.nekohasekai.sagernet.SagerNet

enum class PluginEntry(
    val pluginId: String,
    val displayName: String,
    val packageName: String, // for play and f-droid page
    val downloadSource: DownloadSource = DownloadSource()
) {
    TrojanGo(
        "trojan-go-plugin",
        SagerNet.application.getString(R.string.action_trojan_go),
        "io.nekohasekai.sagernet.plugin.trojan_go"
    ),
    NaiveProxy(
        "naive-plugin",
        SagerNet.application.getString(R.string.action_naive),
        "io.nekohasekai.sagernet.plugin.naive"
    ),
    Hysteria(
        "hysteria-plugin",
        SagerNet.application.getString(R.string.action_hysteria),
        "moe.matsuri.exe.hysteria",
        DownloadSource(
            playStore = false,
            fdroid = false,
            downloadLink = "https://github.com/MatsuriDayo/plugins/releases?q=Hysteria"
        )
    ),
    TUIC(
        "tuic-plugin",
        "TUIC(v4)",
        "moe.matsuri.exe.tuic",
        DownloadSource(
            playStore = false,
            fdroid = false,
            downloadLink = "https://github.com/MatsuriDayo/plugins/releases?q=tuic"
        )
    ),
    TUIC5(
        "tuic-v5-plugin",
        "TUIC(v5)",
        "moe.matsuri.exe.tuic5",
        DownloadSource(
            playStore = false,
            fdroid = false,
            downloadLink = "https://github.com/MatsuriDayo/plugins/releases?q=tuic"
        )
    ),
    ;

    data class DownloadSource(
        val playStore: Boolean = true,
        val fdroid: Boolean = true,
        val downloadLink: String = "https://matsuridayo.github.io/"
    )

    companion object {

        fun find(name: String): PluginEntry? {
            for (pluginEntry in enumValues<PluginEntry>()) {
                if (name == pluginEntry.pluginId) {
                    return pluginEntry
                }
            }
            return null
        }

    }

}