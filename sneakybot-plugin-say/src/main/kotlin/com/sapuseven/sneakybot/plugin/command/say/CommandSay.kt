package com.sapuseven.sneakybot.plugin.command.say

import com.sapuseven.sneakybot.plugins.PluggableCommand
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand

class CommandSay : PluggableCommand {
    private lateinit var manager: PluginManager

    override val command: Command
        get() {
            val cmd = Command()
            cmd.commandName = "say"
            cmd.addParameter("[message]")
            cmd.addParameter("[username]")
            cmd.help =
                "Sends MESSAGE as USERNAME.\n" + "USERNAME has to be a minimum of 3 chars long and cannot be the same as an existing username."
            return cmd
        }

    override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
        manager.api?.let { api ->
            return if (cmd.paramCount() == 3) {
                if (cmd.getParam(2).replace(" ", "").length >= 3) {
                    api.setNickname(cmd.getParam(2))
                    api.sendServerMessage(cmd.getParam(1))
                    api.setNickname("SneakyBOT")
                    true
                } else {
                    manager.sendMessage("USERNAME has to be a minimum of 3 chars long.", invokerId)
                    false
                }
            } else {
                manager.sendMessage("This isn't the right way of using this command!\nTry '!help ${command.commandName}'", invokerId)
                false
            }
        } ?: run {
            return false
        }
    }

    override fun setPluginManager(pluginManager: PluginManager) {
        this.manager = pluginManager
    }
}