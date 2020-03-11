package com.sapuseven.sneakybot.plugin.csgo

import com.sapuseven.sneakybot.plugins.PluggableCommand
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand
import org.slf4j.LoggerFactory


class CommandCsGoMappingId : PluggableCommand {
    private lateinit var manager: PluginManager
    private val log = LoggerFactory.getLogger(CommandCsGoMappingId::class.java)

    override val command: Command = Command()

    init {
        command.commandName = "csgomappingid"
        command.addParameter("[client_id]")
        command.help = "Prints the unique id of the specified client.\n" +
                "Use this for PluginCsGo-SteamMapping in the config file."
    }

    override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
        return if (::manager.isInitialized) {
            if (cmd.paramCount() != 2) {
                manager.sendMessage(
                    "This isn't the right way of using this command!\nTry '!help ${command.commandName}'",
                    invokerId
                )
                return false
            }

            val uid = cmd.getParam(1).toIntOrNull()?.let { manager.api?.getClientInfo(it)?.uniqueIdentifier }
            manager.sendMessage("Client unique id: $uid", invokerId)
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
