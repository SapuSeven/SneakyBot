package com.sapuseven.sneakybot.plugin.poke

import com.sapuseven.sneakybot.plugins.PluggableCommand
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand
import kotlin.concurrent.thread

class CommandPoke : PluggableCommand {
    private lateinit var manager: PluginManager

    companion object {
        private const val MAX_COUNT = 50
    }

    override val command: Command
        get() {
            val cmd = Command()
            cmd.commandName = "poke"
            cmd.addParameter("[client_id]")
            cmd.addParameter("<count>")
            cmd.addParameter("<message>")
            cmd.help = "Pokes a client COUNT times with MESSAGE.\nCOUNT can be a maximum of $MAX_COUNT."
            return cmd
        }

    override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
        return when (cmd.paramCount()) {
            2 -> {
                val targetUserId = cmd.getParam(1).toIntOrNull() ?: return sendHelpMessage(invokerId)
                manager.api?.pokeClient(targetUserId, "")
                true
            }
            3, 4 -> {
                val targetUserId = cmd.getParam(1).toIntOrNull() ?: return sendHelpMessage(invokerId)
                val count = cmd.getParam(2).toIntOrNull() ?: return sendHelpMessage(invokerId)

                multiPoke(targetUserId, invokerId, if (cmd.paramCount() == 4) cmd.getParam(3) else "", count)
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

    private fun multiPoke(clientId: Int, invokerId: Int, message: String, count: Int = 1): Boolean {
        if (count > MAX_COUNT) return sendHelpMessage(invokerId)

        thread {
            for (i in 0 until count) {
                manager.api?.pokeClient(clientId, message)
                Thread.sleep(500)
            }
        }

        return true
    }

    override fun setPluginManager(pluginManager: PluginManager) {
        this.manager = pluginManager
    }
}
