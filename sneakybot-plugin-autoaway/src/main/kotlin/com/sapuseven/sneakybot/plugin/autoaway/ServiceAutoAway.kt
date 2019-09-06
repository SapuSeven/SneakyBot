package com.sapuseven.sneakybot.plugin.autoaway

import com.github.theholywaffle.teamspeak3.api.event.BaseEvent
import com.sapuseven.sneakybot.plugins.PluggableService
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.ConsoleCommand

class ServiceAutoAway : PluggableService {
    companion object {
        private const val AFK_CHANNEL_NAME = "AFK"
    }

    @Throws(Exception::class)
    override fun preInit(pluginManager: PluginManager) {
        // unused
   }

    override fun postInit(pluginManager: PluginManager) {
        pluginManager.api?.let { api ->
            val channels = api.channels
            val defaultChannel = channels.find { it.isDefault }
            val afkChannel = channels.find { it.name == AFK_CHANNEL_NAME }
            val whoAmI = api.whoAmI()
            val timer = TimerIdleCheck(whoAmI, defaultChannel, afkChannel)
            pluginManager.addTimer(timer, 10)
        }
    }

    override fun stop(pluginManager: PluginManager) {
        // unused
    }

    override fun onEventReceived(e: BaseEvent) {
        // unused
    }

    override fun onCommandExecuted(c: ConsoleCommand, invokerId: Int) {
        // unused
    }

    override fun setPluginManager(pluginManager: PluginManager) {
        // unused
    }
}
