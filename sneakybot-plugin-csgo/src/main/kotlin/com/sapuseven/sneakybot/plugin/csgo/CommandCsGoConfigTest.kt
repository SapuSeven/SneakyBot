package com.sapuseven.sneakybot.plugin.csgo

import com.sapuseven.sneakybot.plugins.PluggableCommand
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand
import org.slf4j.LoggerFactory


class CommandCsGoConfigTest : PluggableCommand {
    private lateinit var manager: PluginManager
    private val log = LoggerFactory.getLogger(CommandCsGoConfigTest::class.java)

    override val command: Command = Command()

    init {
        command.commandName = "csgoconfigtest"
        command.help = "Queries the plugin config data and prints the result."
    }

    override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
        return if (::manager.isInitialized) {
            manager.getConfiguration("PluginCsGo-TsSteamMapping").apply {
                manager.sendMessage("Ts <-> Steam Mapping:\n" + this.keys().joinToString("\n") {
                    "$it: ${this.get(it, "")}"
                }, invokerId)
            }
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
