  package io.nekohasekai.sagernet.ktx

import android.content.Context

  fun Context.launchCustomTab(link: String) {
    /*CustomTabsIntent.Builder().apply {
        setColorScheme(CustomTabsIntent.COLOR_SCHEME_SYSTEM)
        setColorSchemeParams(
            CustomTabsIntent.COLOR_SCHEME_LIGHT,
            CustomTabColorSchemeParams.Builder().apply {
                setToolbarColor(getColorAttr(R.attr.colorPrimary))
            }.build()
        )
        setColorSchemeParams(
            CustomTabsIntent.COLOR_SCHEME_DARK,
            CustomTabColorSchemeParams.Builder().apply {
                setToolbarColor(getColorAttr(R.attr.colorPrimary))
            }.build()
        )
    }.build().apply {
        if (intent.resolveActivity(packageManager) != null) {
            launchUrl(this@launchCustomTab, Uri.parse(link))
        }
    }*/
}