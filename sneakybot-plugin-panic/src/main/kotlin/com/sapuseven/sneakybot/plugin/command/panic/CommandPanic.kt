package com.sapuseven.sneakybot.plugin.command.panic

import com.github.theholywaffle.teamspeak3.api.exception.TS3CommandFailedException
import com.sapuseven.sneakybot.plugins.PluggableCommand
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand
import org.slf4j.LoggerFactory

class CommandPanic : PluggableCommand {
    private lateinit var manager: PluginManager
    private val log = LoggerFactory.getLogger(CommandPanic::class.java)

    override val command: Command
        get() {
            val cmd = Command()
            cmd.commandName = "panic"
            cmd.addParameter("<\"everywhere\">")
            cmd.help =
                "Bans every client in the current channel for one second (except yourself).\n" + "If 'everywhere' is specified, this affects all clients on the whole server."
            return cmd
        }

    override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
        manager.api?.let { api ->
            for (client in api.clients)
                if (client.id != invokerId
                    && (client.channelId == api.getClientInfo(invokerId).channelId
                            || (cmd.paramCount() == 2 && cmd.getParam(1).equals("everywhere", ignoreCase = true)))
                )
                    try {
                        api.banClient(client.id, 1, "PANIC!")
                    } catch (e: TS3CommandFailedException) {
                        manager.sendMessage("Failed to ban user #${client.id} (${client.nickname})", invokerId)

                        log.warn("Failed to ban user #${client.id} (${client.nickname}): ${e.error.message}")
                    }
            return true
        } ?: run {
            return false
        }
    }

    override fun setPluginManager(pluginManager: PluginManager) {
        this.manager = pluginManager
    }
}