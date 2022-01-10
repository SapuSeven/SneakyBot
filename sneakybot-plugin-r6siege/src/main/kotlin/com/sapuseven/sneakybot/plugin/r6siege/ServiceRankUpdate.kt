package com.sapuseven.sneakybot.plugin.r6siege

import com.github.theholywaffle.teamspeak3.api.event.BaseEvent
import com.github.theholywaffle.teamspeak3.api.event.ClientJoinEvent
import com.sapuseven.sneakybot.plugins.PluggableService
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.ConsoleCommand

class ServiceRankUpdate : PluggableService {
    override fun preInit(pluginManager: PluginManager) {
        // unused
    }

    override fun postInit(pluginManager: PluginManager) {
        val timer = TimerRankUpdate()
        pluginManager.addTimer(timer, 60, 120)
    }

    override fun stop(pluginManager: PluginManager) {
        // unused
    }

    override fun onEventReceived(e: BaseEvent) {
        if (e is ClientJoinEvent) {
            // TODO: On-demand rank update
        }
    }

    override fun onCommandExecuted(cmd: ConsoleCommand, invokerId: Int) {
        // unused
    }

    override fun setPluginManager(pluginManager: PluginManager) {
        // unused
    }
}
