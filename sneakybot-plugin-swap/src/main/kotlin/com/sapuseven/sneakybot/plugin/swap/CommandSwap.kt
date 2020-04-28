package com.sapuseven.sneakybot.plugin.swap

import com.github.theholywaffle.teamspeak3.api.exception.TS3CommandFailedException
import com.sapuseven.sneakybot.plugins.PluggableCommand
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand

class CommandSwap : PluggableCommand {
	private lateinit var manager: PluginManager

	override val command: Command
		get() {
			val cmd = Command()
			cmd.commandName = "swap"
			cmd.addParameter("[channel_id_1]")
			cmd.addParameter("[channel_id_2]")
			cmd.help = "Move clients from channel 1 to channel 2 and vice versa."
			return cmd
		}

	override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
		return when (cmd.paramCount()) {
			3 -> {
				val channelId1 = cmd.getParam(1).toIntOrNull() ?: return sendHelpMessage(invokerId)
				val channelId2 = cmd.getParam(2).toIntOrNull() ?: return sendHelpMessage(invokerId)
				manager.api?.let { api ->
					api.clients.forEach {
						try {
							if (it.channelId == channelId1) {
								api.moveClient(it.id, channelId2)
							} else if (it.channelId == channelId2) {
								api.moveClient(it.id, channelId1)
							}
						} catch (e: TS3CommandFailedException) {
							manager.sendMessage(
								"Client ${it.id} could not be moved: ${e.error.message} (${e.error.id})",
								invokerId
							)
							return false
						}
					}
					true
				}
				false
			}
			else -> sendHelpMessage(invokerId)
		}
	}

	private fun sendHelpMessage(invokerId: Int): Boolean {
		manager.sendMessage(
			"This isn't the right way of using this command!\nTry '!help ${command.commandName}'",
			invokerId
		)
		return false
	}

	override fun setPluginManager(pluginManager: PluginManager) {
		this.manager = pluginManager
	}
}
