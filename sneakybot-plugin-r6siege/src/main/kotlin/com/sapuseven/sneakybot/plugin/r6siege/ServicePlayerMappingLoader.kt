package com.sapuseven.sneakybot.plugin.r6siege

import com.github.theholywaffle.teamspeak3.api.event.BaseEvent
import com.sapuseven.sneakybot.plugins.PluggableService
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.ConsoleCommand

class ServicePlayerMappingLoader : PluggableService {
	override fun preInit(pluginManager: PluginManager) {
		// TODO: Load PlayerMapping from config
	}

	override fun postInit(pluginManager: PluginManager) {
		// unused
	}

	override fun stop(pluginManager: PluginManager) {
		// TODO: Save PlayerMapping to config
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