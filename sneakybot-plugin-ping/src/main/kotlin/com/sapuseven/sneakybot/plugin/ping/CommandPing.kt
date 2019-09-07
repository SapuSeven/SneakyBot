package com.sapuseven.sneakybot.plugin.ping

import com.sapuseven.sneakybot.plugins.PluggableCommand
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand
import org.slf4j.LoggerFactory

class CommandPing : PluggableCommand {
    private lateinit var manager: PluginManager
    private val log = LoggerFactory.getLogger(CommandPing::class.java)

    override val command: Command = Command()

    init {
        command.commandName = "ping"
        command.help = "pong."
    }

    override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
        return if (::manager.isInitialized) {
            manager.sendMessage("pong", invokerId)
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
