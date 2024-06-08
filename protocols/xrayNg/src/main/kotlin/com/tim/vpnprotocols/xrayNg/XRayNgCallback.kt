package com.tim.vpnprotocols.xrayNg

import libv2ray.V2RayVPNServiceSupportsSet

internal class XRayNgCallback(
    private val onEmitStatus: (p0: Long, p1: String?) -> Long = { _, _ -> 0L },
    private val prepare: () -> Long = { 0L },
    private val protect: (p0: Long) -> Boolean = { false },
    private val setup: (p0: String?) -> Long = { 0L },
    private val shutdown: () -> Long = { 0L },
) : V2RayVPNServiceSupportsSet {
    override fun onEmitStatus(p0: Long, p1: String?): Long = onEmitStatus.invoke(p0, p1)

    override fun prepare(): Long = prepare.invoke()

    override fun protect(p0: Long): Boolean = protect.invoke(p0)

    override fun setup(p0: String?): Long = setup.invoke(p0)

    override fun shutdown(): Long = shutdown.invoke()
}