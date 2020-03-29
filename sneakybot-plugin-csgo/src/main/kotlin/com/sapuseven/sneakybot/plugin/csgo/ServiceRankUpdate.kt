package com.sapuseven.sneakybot.plugin.csgo

import com.github.theholywaffle.teamspeak3.api.event.BaseEvent
import com.sapuseven.sneakybot.plugins.PluggableService
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.ConsoleCommand

class ServiceRankUpdate : PluggableService {
    override fun preInit(pluginManager: PluginManager) {
        // unused
    }

    @kotlinx.serialization.ImplicitReflectionSerializer
    @kotlinx.serialization.UnstableDefault
    override fun postInit(pluginManager: PluginManager) {
        val timer = TimerRankUpdate()
        pluginManager.addTimer(timer, 1)
    }

    override fun stop(pluginManager: PluginManager) {
        // unused
    }

    override fun onEventReceived(e: BaseEvent) {
        // unused
    }

    override fun onCommandExecuted(cmd: ConsoleCommand, invokerId: Int) {
        // unused
    }

    override fun setPluginManager(pluginManager: PluginManager) {
        // unused
    }
}
