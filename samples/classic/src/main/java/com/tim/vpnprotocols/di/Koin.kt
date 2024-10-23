package com.tim.vpnprotocols.di

import android.app.Application
import android.content.ContentResolver
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.tim.vpnprotocols.storage.dataStore
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * @Author: Тимур Ходжатов
 */
fun Application.initKoin() {
    startKoin {
        androidLogger()
        androidContext(this@initKoin)
        modules(
            appModule,
            openVPNModule,
            shadowsocksrModule
        )
    }
}

val appModule = module {
    single<DataStore<Preferences>> {
        androidApplication().dataStore
    }
    single<ContentResolver> {
        androidApplication().contentResolver
    }
}
