package com.tim.openvpn.thread

import android.net.LocalSocket
import com.tim.basevpn.state.ConnectionState
import com.tim.openvpn.VpnStatus
import com.tim.openvpn.commandprocessors.*
import com.tim.openvpn.commandprocessors.needok.*
import com.tim.openvpn.model.TunOptions
import com.tim.openvpn.service.FileDescriptorProtector
import com.tim.openvpn.service.TunOpener
import java.io.FileDescriptor

/**
 * @Author: Тимур Ходжатов
 */
class OpenVPNMessageProcessor(
    private val tunOpener: TunOpener,
    private val fileDescriptorProtector: FileDescriptorProtector,
    private val stateListener: (ConnectionState) -> Unit,
    private val onVpnStop: () -> Unit
) {

    private var socket: LocalSocket? = null

    private val commandProcessors = mutableMapOf<String, CommandProcessor>()

    fun process(message: String) {
        message.split("\n")
            .filter { it.isNotBlank() }
            .forEach { messageRow ->
                VpnStatus.log("OpenVPN Received Message: $messageRow")
                val parts = messageRow.split(":", limit = 2)
                val cmd = parts.firstOrNull()?.substring(1).orEmpty()
                val argument = parts.getOrNull(1).orEmpty()
                commandProcessors.getOrElse(cmd) {
                    VpnStatus.log("OpenVPN not find processor for command: $messageRow")
                    null
                }?.process(argument)
            }
    }

    fun setSocket(socket: LocalSocket) {
        commandProcessors.clear()
        commandProcessors.putAll(buildProcessorList(socket))
        this.socket = socket
    }

    fun setFileDescriptors(fileDescriptors: Array<FileDescriptor>) {
        getNeedOkCommandProcessors()
            .filterIsInstance<FileDescriptorsReceiver>()
            .forEach { tunOptionsReceiver ->
                tunOptionsReceiver.setFileDescriptors(fileDescriptors)
            }
    }

    fun setTunOptions(tunOptions: TunOptions? = null) = getNeedOkCommandProcessors()
        .filterIsInstance<TunOptionsReceiver>()
        .forEach { tunOptionsReceiver ->
            tunOptionsReceiver.onNewTunOptions(tunOptions)
        }

    private fun getNeedOkCommandProcessors() = commandProcessors.values
        .filterIsInstance<NeedokProcessorsHolder>()
        .flatMap { it.commandProcessors.values }

    private fun buildProcessorList(
        socket: LocalSocket
    ): Map<String, CommandProcessor> {
        return listOf(
            InfoMessageProcessor(),
            LogMessageProcessor(),
            HoldMessageProcessor(socket),
            ProxyMessageProcessor(socket),
            StateMessageProcessor(stateListener),
            FatalAllMessageProcessor(onVpnStop),
            NeedOkMessageProcessor(
                commandProcessors = listOf(
                    DnsServerNeedokCommandProcessor(socket),
                    DnsDomainNeedokCommandProcessor(socket),
                    RouteNeedokCommandProcessor(socket),
                    PersistTunNeedokCommandProcessor(socket),
                    ProtectFdNeedokCommandProcessor(
                        socket = socket,
                        fileDescriptorProtector = fileDescriptorProtector
                    ),
                    IfConfigNeedokCommandProcessor(
                        socket = socket
                    ) { tunOptions ->
                        setTunOptions(tunOptions)
                    },
                    OpenTunNeedokCommandProcessor(
                        socket = socket,
                        tunOpener = tunOpener
                    )
                ).associateBy { it.command }
            ),
        ).associateBy { it.command }
    }
}