package com.sapuseven.sneakybot.plugin.csgo

import com.sapuseven.sneakybot.plugins.PluggableCommand
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand
import org.slf4j.LoggerFactory

class CommandCs : PluggableCommand {
    private lateinit var manager: PluginManager
    private val log = LoggerFactory.getLogger(CommandCs::class.java)

    override val command: Command = Command()

    init {
        command.commandName = "cs"
        command.help = "go"
    }

    override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
        return if (::manager.isInitialized) {
            manager.sendMessage("go", invokerId)
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
