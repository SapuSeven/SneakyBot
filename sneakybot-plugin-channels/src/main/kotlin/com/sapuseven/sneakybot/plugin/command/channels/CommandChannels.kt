package com.sapuseven.sneakybot.plugin.command.channels

import com.github.theholywaffle.teamspeak3.api.wrapper.Channel
import com.sapuseven.sneakybot.plugins.PluggableCommand
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand

class CommandChannels : PluggableCommand {
    private lateinit var manager: PluginManager

    override val command: Command
        get() {
            val cmd = Command()
            cmd.commandName = "channels"
            cmd.addParameter("<filter>")
            cmd.help =
                "Prints all channels with the according id. Use the optional FILTER parameter to filter the results by name or id (case insensitive)."
            return cmd
        }

    override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
        return manager.api?.let { api ->
            var channelList = ""
            val channels = api.channels
            channels.sortWith(ChannelComparator())
            for (channel in channels)
                channelList += "\n#${channel.id}: ${channel.name}"

            if (cmd.paramCount() >= 2) {
                val searchString = allParams(cmd)
                channelList = channelList.lines().filter { line ->
                    line.isBlank() || line.contains(searchString, true)
                }.joinToString("\n")
            }

            manager.sendMessage("Channel list:\n$channelList", invokerId)
            true
        } ?: false
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

    private inner class ChannelComparator : java.util.Comparator<Channel> {
        override fun compare(c1: Channel, c2: Channel): Int = c1.id.compareTo(c2.id)
    }
}
