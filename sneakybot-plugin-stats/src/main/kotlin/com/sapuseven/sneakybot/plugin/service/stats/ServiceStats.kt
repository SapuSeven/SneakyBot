package com.sapuseven.sneakybot.plugin.service.stats

import com.github.theholywaffle.teamspeak3.api.event.BaseEvent
import com.sapuseven.sneakybot.plugins.PluggableService
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.ConsoleCommand

class ServiceStats : PluggableService {
    private var manager: PluginManager? = null

    @Throws(Exception::class)
    override fun preInit(pluginManager: PluginManager) {
    }

    override fun postInit(pluginManager: PluginManager) {
    }

    override fun stop(pluginManager: PluginManager) {
    }

    override fun onEventReceived(e: BaseEvent) {
        logEvent(e)
    }

    override fun onCommandExecuted(c: ConsoleCommand, invokerId: Int) {
        logCommand(c, invokerId)
    }

    override fun setPluginManager(manager: PluginManager) {
        this.manager = manager
    }

    private fun logEvent(e: BaseEvent) {
        // TODO: implement
    }

    private fun logCommand(c: ConsoleCommand, invokerId: Int) {
        // TODO: implement
    }
}