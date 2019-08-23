package com.sapuseven.sneakybot.plugin.service.lock

import com.sapuseven.sneakybot.plugins.PluggableCommand
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand

class CommandUnock : PluggableCommand {
    private lateinit var manager: PluginManager

    override val command: Command
        get() {
            val cmd = Command()
            cmd.commandName = "unlock"
            cmd.addParameter("[client_id]")
            cmd.help = "Unlock a client to freely move him again."
            return cmd
        }

    override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
        return false
    }

    override fun setPluginManager(pluginManager: PluginManager) {
        this.manager = pluginManager
    }
}
