package com.sapuseven.sneakybot.plugin.poke

import com.sapuseven.sneakybot.plugins.PluggableCommand
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand

class CommandPoke : PluggableCommand {
    private lateinit var manager: PluginManager

    override val command: Command
        get() {
            val cmd = Command()
            cmd.commandName = "poke"
            cmd.addParameter("[client_id]")
            cmd.addParameter("<count>")
            cmd.addParameter("<message>")
            cmd.help = "Pokes a client COUNT times with MESSAGE.\n" + "COUNT can be a maximum of 50."
            return cmd
        }

    override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
        if (cmd.paramCount() == 2) {
            return try {
                manager.api?.pokeClient(Integer.parseInt(cmd.getParam(1)), "") != null
            } catch (e: NumberFormatException) {
                manager.sendMessage(
                    "You specified an invalid number!\nTry '!help poke' for more information about the correct syntax.",
                    invokerId
                )
                false
            }
        } else if (cmd.paramCount() == 3 || cmd.paramCount() == 4) {
            try {
                val count = Integer.parseInt(cmd.getParam(2))
                if (count > 50) {
                    manager.sendMessage("You can't poke a client more than 50 times at once!", invokerId)
                    return false
                }
                for (i in 0 until count) {
                    if (cmd.paramCount() == 4)
                        manager.api?.pokeClient(Integer.parseInt(cmd.getParam(1)), cmd.getParam(3))
                    else
                        manager.api?.pokeClient(Integer.parseInt(cmd.getParam(1)), "")
                }
                return true
            } catch (e: NumberFormatException) {
                manager.sendMessage(
                    "You specified an invalid number!\nTry '!help poke' for more information about the correct syntax.",
                    invokerId
                )
                return false
            }

        } else if (cmd.paramCount() == 4) {
            return try {
                for (i in 0 until Integer.parseInt(cmd.getParam(2)))
                    manager.api?.pokeClient(Integer.parseInt(cmd.getParam(1)), cmd.getParam(3))
                true
            } catch (e: NumberFormatException) {
                manager.sendMessage(
                    "You specified an invalid number!\nTry '!help poke' for more information about the correct syntax.",
                    invokerId
                )
                false
            }

        } else {
            manager.sendMessage("This isn't the right way of using this command!\nTry '!help poke'", invokerId)
            return false
        }
    }

    override fun setPluginManager(pluginManager: PluginManager) {
        this.manager = pluginManager
    }
}