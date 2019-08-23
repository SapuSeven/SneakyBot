package com.sapuseven.sneakybot.plugin.service.stats

import com.sapuseven.sneakybot.plugins.PluggableCommand
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand

class CommandStats : PluggableCommand {
    private lateinit var manager: PluginManager

    override val command: Command
        get() {
            val cmd = Command()
            cmd.commandName = "stats"
            cmd.addParameter("<client_id>")
            cmd.help =
                "Show general user statistics.\n" + "If CLIENT_ID is used, advanced stats for this user will be shown."
            return cmd
        }

    override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
        manager.sendMessage("This command is not implemented yet.", invokerId)
        return false
    }

    override fun setPluginManager(pluginManager: PluginManager) {
        this.manager = pluginManager
    }
}
