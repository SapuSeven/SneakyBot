package com.sapuseven.sneakybot.plugin.service.lock

import com.sapuseven.sneakybot.plugins.PluggableCommand
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand

class CommandLock : PluggableCommand {
    private lateinit var manager: PluginManager

    override val command: Command
        get() {
            val cmd = Command()
            cmd.commandName = "lock"
            cmd.addParameter("[client_id]")
            cmd.addParameter("[channel_id]")
            cmd.help =
                "Lock a client to a specific channel.\n" + "This client will not be able to switch channels and cannot be moved or channel-kicked."
            return cmd
        }

    override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
        return true
    }

    override fun setPluginManager(pluginManager: PluginManager) {
        this.manager = pluginManager
    }
}
