package com.sapuseven.sneakybot.plugin.command.panic

import com.sapuseven.sneakybot.plugins.PluggableCommand
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand

class CommandPanic : PluggableCommand {
    private lateinit var manager: PluginManager

    override val command: Command
        get() {
            val cmd = Command()
            cmd.commandName = "panic"
            cmd.addParameter("<everywhere>")
            cmd.help =
                "Bans every client in the current channel for one second.\n" + "If EVERYWHERE is 'everywhere', this affects all clients on the whole server."
            return cmd
        }

    override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
        manager.api?.let { api ->
            for (client in api.clients)
                if (client.channelId == api.getClientInfo(invokerId).channelId || cmd.paramCount() == 2 && cmd.getParam(
                        1
                    ).equals("everywhere", ignoreCase = true)
                )
                    api.banClient(client.id, 1, "PANIC!")
            return true
        } ?: run {
            return false
        }
    }

    override fun setPluginManager(pluginManager: PluginManager) {
        this.manager = pluginManager
    }
}