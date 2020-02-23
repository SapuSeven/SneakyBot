package com.sapuseven.sneakybot.plugin.command.clients

import com.github.theholywaffle.teamspeak3.api.wrapper.Client
import com.sapuseven.sneakybot.plugins.PluggableCommand
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand

class CommandClients : PluggableCommand {
    private lateinit var manager: PluginManager

    override val command: Command
        get() {
            val cmd = Command()
            cmd.commandName = "clients"
            cmd.addParameter("<filter>")
            cmd.help =
                "Prints all connected clients with the according id. Use the optional FILTER parameter to filter the results by name or id (case insensitive)."
            return cmd
        }

    override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
        manager.api?.let { api ->
            var clientList = ""
            val clients = api.clients
            clients.sortWith(ClientComparator())
            for (client in clients)
                clientList += "\n#${client.id}: ${client.nickname}"

            if (cmd.paramCount() >= 2) {
                val searchString = allParams(cmd)
                clientList = clientList.lines().filter { line ->
                    line.isBlank() || line.contains(searchString, true)
                }.joinToString("\n")
            }

            manager.sendMessage("Client list:\n$clientList", invokerId)
            return true
        } ?: run {
            return false
        }
    }

    private fun allParams(cmd: ConsoleCommand): String {
        if (cmd.paramCount() < 2) return ""

        var allParams = cmd.getParam(1)

        for (i in 2 until cmd.paramCount())
            allParams += " ${cmd.getParam(i)}"

        return allParams
    }

    override fun setPluginManager(pluginManager: PluginManager) {
        this.manager = pluginManager
    }

    private inner class ClientComparator : Comparator<Client> {
        override fun compare(c1: Client, c2: Client): Int = c1.id.compareTo(c2.id)
    }
}
