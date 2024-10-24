package com.tim.singBox.command

import com.tim.libbox.OutboundGroup
import com.tim.libbox.StatusMessage
import com.tim.singBox.command.CommandClient.Handler
import kotlinx.coroutines.CoroutineScope

class CommandClientObserver(scope: CoroutineScope): Handler {
    private val commandClient = CommandClient(scope, CommandClient.ConnectionType.Log, this)

    fun init() {
        commandClient.connect()
    }

    fun destroy() {
        commandClient.disconnect()
    }

    override fun onConnected() {
        super.onConnected()
        println("CommandClient: onConnected()")
    }

    override fun onDisconnected() {
        super.onDisconnected()
        println("CommandClient: onDisconnected()")
    }

    override fun updateStatus(status: StatusMessage) {
        super.updateStatus(status)
        println("CommandClient: updateStatus -> $status")
    }

    override fun updateGroups(newGroups: MutableList<OutboundGroup>) {
        super.updateGroups(newGroups)
        println("CommandClient: updateGroups -> ${newGroups.map { it }}")
    }

    override fun clearLogs() {
        super.clearLogs()
    }

    override fun appendLogs(message: List<String>) {
        super.appendLogs(message)
        println("CommandClient: appendLogs -> ${message.map { it }}")
    }

    override fun initializeClashMode(modeList: List<String>, currentMode: String) {
        super.initializeClashMode(modeList, currentMode)
    }

    override fun updateClashMode(newMode: String) {
        super.updateClashMode(newMode)
    }
}