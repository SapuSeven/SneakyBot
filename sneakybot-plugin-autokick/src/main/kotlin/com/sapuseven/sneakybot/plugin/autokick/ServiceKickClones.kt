package com.sapuseven.sneakybot.plugin.autokick

import com.github.theholywaffle.teamspeak3.api.event.BaseEvent
import com.github.theholywaffle.teamspeak3.api.event.ClientJoinEvent
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerQueryInfo
import com.sapuseven.sneakybot.plugins.PluggableService
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.ConsoleCommand

class ServiceKickClones : PluggableService {
	private lateinit var manager: PluginManager
	private lateinit var whoAmI: ServerQueryInfo
	private var enabled = false

	override fun preInit(pluginManager: PluginManager) {
		// unused
	}

	override fun postInit(pluginManager: PluginManager) {
		if (enabled) {
			whoAmI = pluginManager.api!!.whoAmI()
			Utils.kickClones(pluginManager)
		}
	}

	override fun stop(pluginManager: PluginManager) {
		// unused
	}

	override fun onEventReceived(e: BaseEvent) {
		if (enabled && e is ClientJoinEvent) {
			manager.api?.let { api ->
				if (api.clients.filter { it.id != e.clientId }.any { it.uniqueIdentifier == e.uniqueClientIdentifier })
					api.kickClientFromServer(e.clientId)
			}
		}
	}

	override fun onCommandExecuted(cmd: ConsoleCommand, invokerId: Int) {
	}

	override fun setPluginManager(pluginManager: PluginManager) {
		this.manager = pluginManager
		enabled = pluginManager.getConfiguration("PluginAutoKick").getBoolean("serviceEnabled", enabled)
	}
}
