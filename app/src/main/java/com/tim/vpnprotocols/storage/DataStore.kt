package com.tim.vpnprotocols.storage

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

/**
 * @Author: Тимур Ходжатов
 */
val Context.dataStore by preferencesDataStore("settings")
