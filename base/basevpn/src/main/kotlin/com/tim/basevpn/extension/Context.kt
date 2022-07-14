package com.tim.basevpn.extension

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner

/**
 * @Author: Timur Hojatov
 */
fun Context.getActivity(): ComponentActivity {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is ComponentActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    throw IllegalStateException("Can't find Activity for context")
}

fun LifecycleOwner.getContext() = when (this) {
    is Fragment -> {
        requireContext()
    }
    is Activity -> {
        getActivity()
    }
    else -> throw IllegalStateException("Unknown lifecycleOwner: $this")
}
