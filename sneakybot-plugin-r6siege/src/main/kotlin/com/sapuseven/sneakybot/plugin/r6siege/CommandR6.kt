package com.sapuseven.sneakybot.plugin.r6siege

import com.sapuseven.sneakybot.plugins.PluggableCommand
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand
import org.slf4j.LoggerFactory

class CommandR6 : PluggableCommand {
	private lateinit var manager: PluginManager
	private val log = LoggerFactory.getLogger(CommandR6::class.java)

	override val command: Command = Command()

	init {
		command.commandName = "r6"
		command.help = "siege"
	}

	override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
		return if (::manager.isInitialized) {
			manager.sendMessage("siege", invokerId)
			true
		} else {
			log.warn("PluginManager not initialized")
			false
		}
	}

	override fun setPluginManager(pluginManager: PluginManager) {
		this.manager = pluginManager
	}
}
