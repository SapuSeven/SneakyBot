package com.sapuseven.sneakybot.plugin.help

import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode
import com.github.theholywaffle.teamspeak3.api.event.BaseEvent
import com.github.theholywaffle.teamspeak3.api.event.TextMessageEvent
import com.sapuseven.sneakybot.plugins.PluggableService
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.ConsoleCommand

class ServiceHelp : PluggableService {
	lateinit var manager: PluginManager

	val helpMessageTitle = "Hi! What do you need help with? Reply with the corresponding number:\n"
	val helpTopics = listOf(
		1 to "CS:GO Ranks",
		0 to "Other"
	)

	override fun preInit(pluginManager: PluginManager) {
		// unused
	}

	override fun postInit(pluginManager: PluginManager) {
		// unused
	}

	override fun stop(pluginManager: PluginManager) {
		// unused
	}

	override fun onEventReceived(e: BaseEvent) {
		if (e is TextMessageEvent
			&& e.targetMode == TextMessageTargetMode.SERVER
			&& e.message == "!help"
		) {
			sendHelpMessage(e.invokerId)
		}
	}

	override fun onCommandExecuted(cmd: ConsoleCommand, invokerId: Int) {
		// unused
	}

	override fun setPluginManager(pluginManager: PluginManager) {
		manager = pluginManager
	}

	private fun sendHelpMessage(clientId: Int) {
		manager.sendMessage(
			helpMessageTitle + helpTopics.joinToString("\n", transform = { "${it.first}. ${it.second}" }),
			clientId, true
		)
	}
}
