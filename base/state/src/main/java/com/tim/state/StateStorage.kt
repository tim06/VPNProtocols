package com.tim.state

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.MultiProcessDataStoreFactory
import androidx.datastore.core.createMultiProcessCoordinator
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import kotlinx.coroutines.Dispatchers
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.File

fun getStateStorage(context: Context): DataStore<Preferences> {
    return MultiProcessDataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = FileSystem.Companion.SYSTEM,
            serializer = PreferencesSerializer,
            coordinatorProducer = { path, _ ->
                createMultiProcessCoordinator(
                    context = Dispatchers.IO,
                    file = path.toFile()
                )
            },
            producePath = {
                File(
                    context.applicationContext.filesDir,
                    "state_storage"
                ).toOkioPath()
            }
        )
    )
}

