package com.sapuseven.sneakybot.services

import com.github.theholywaffle.teamspeak3.api.ChannelProperty
import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode
import com.github.theholywaffle.teamspeak3.api.event.*
import com.sapuseven.sneakybot.SneakyBot
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.ConsoleCommand
import java.util.*

internal class ServiceChannelMode(private val bot: SneakyBot) : BuiltinService() {
    private var consoleChannelId: Int = -1

    override fun preInit(pluginManager: PluginManager) {
    }

    override fun postInit(pluginManager: PluginManager) {
        findNewConsoleChannel()

        val whoAmI = bot.query.api.whoAmI()
        if (whoAmI.channelId != consoleChannelId) {
            SneakyBot.log.debug("I am now moving to the console channel.")
            bot.query.api.moveQuery(consoleChannelId)
        }
    }

    override fun stop(pluginManager: PluginManager) {
    }

    override fun onEventReceived(e: BaseEvent) {
        when (e) {
            is ClientMovedEvent -> {
                if (e.targetChannelId == consoleChannelId) {
                    val name = bot.query.api.getClientInfo(e.clientId).nickname
                    SneakyBot.log.info("User $name entered the Console channel.")
                    if (bot.mode == SneakyBot.MODE_CHANNEL)
                        bot.manager.sendMessage(
                            "Welcome, " + name + ", to the SneakyBOT Console!\n" +
                                    "If you are new, '!help' is usually a good command for getting started.", e.clientId
                        )
                }
            }
            is TextMessageEvent -> {
                if (e.invokerId == bot.whoAmI.id || !bot.isCommand(e.message)) return

                if (e.targetMode == TextMessageTargetMode.CHANNEL) interpretChannelMessage(e)
            }
            is ChannelEditedEvent -> {
                if (e.channelId == consoleChannelId)
                    findNewConsoleChannel()
            }
            is ChannelDeletedEvent -> {
                if (e.channelId == consoleChannelId)
                    findNewConsoleChannel()
            }
            is ClientLeaveEvent -> {
                if (bot.mode != SneakyBot.MODE_DIRECT) return

                // Remove from directs list but keep in server group
                bot.directClients.removeIf { it == e.clientId }
                if (bot.directClients.isEmpty()) {
                    setupChannelMode()
                }
            }
        }
    }

    override fun onCommandExecuted(cmd: ConsoleCommand, invokerId: Int) {
        if (cmd.getParam(0) == "mode" && cmd.paramCount() == 2) {
            when (cmd.getParam(1)) {
                "channel", "console", "c" -> switchToChannelMode(invokerId)
            }
        }
    }

    private fun switchToChannelMode(invokerId: Int): Boolean {
        return if (bot.mode != SneakyBot.MODE_CHANNEL) {
            setupChannelMode()
            true
        } else {
            bot.manager.sendMessage("Mode is already set to CHANNEL!", invokerId)
            false
        }
    }

    private fun findNewConsoleChannel() {
        consoleChannelId = getConsoleChannel() ?: createConsoleChannel()
    }

    private fun interpretChannelMessage(event: TextMessageEvent) {
        if (bot.mode == SneakyBot.MODE_CHANNEL) {
            SneakyBot.log.info("User #${event.invokerId} (${event.invokerName}) executed a command via CONSOLE chat: " + event.message)
            bot.interpretCommand(event.message, event.invokerId)
        } else {
            SneakyBot.log.info("User #${event.invokerId} (${event.invokerName}) tried to execute a command, but didn't use DIRECT chat: " + event.message)
            bot.sendChannelMessage("Please use the direct chat to communicate with me.")
        }
    }

    private fun createConsoleChannel(): Int {
        SneakyBot.log.debug("Creating a new SneakyBOT Console channel...")
        val properties = HashMap<ChannelProperty, String>()
        properties[ChannelProperty.CHANNEL_FLAG_TEMPORARY] = "1"
        properties[ChannelProperty.CHANNEL_TOPIC] = "Console for SneakyBOT"
        properties[ChannelProperty.CHANNEL_NEEDED_TALK_POWER] = "100"
        properties[ChannelProperty.CHANNEL_PASSWORD] = bot.botConfig.consolePassword

        val channelId = bot.query.api.createChannel(bot.botConfig.consoleName, properties)

        SneakyBot.log.debug("Done. Channel ID: #$channelId")
        return channelId
    }

    private fun getConsoleChannel(): Int? {
        SneakyBot.log.debug("Searching for an existing console channel...")
        val consoleChannel = bot.query.api.channels.find { it.name == bot.botConfig.consoleName }
        if (consoleChannel == null)
            SneakyBot.log.debug("No SneakyBOT Console channel found.")
        else
            SneakyBot.log.debug("Using existing SneakyBOT Console channel with ID #${consoleChannel.id}")

        return consoleChannel?.id
    }

    private fun setupChannelMode() {
        if (bot.mode == SneakyBot.MODE_CHANNEL) return

        bot.directClients.forEach {
            bot.query.api.sendPrivateMessage(
                it,
                "Direct chat is now closed. You can send me commands via the ${bot.botConfig.consoleName} channel."
            )
            bot.query.api.removeClientFromServerGroup(
                bot.serverGroupId,
                bot.query.api.getClientInfo(it).databaseId
            )
        }
        findNewConsoleChannel()
        bot.sendChannelMessage("I am back from DIRECT mode! The ${bot.botConfig.consoleName} channel can be used again.")
        bot.mode = SneakyBot.MODE_CHANNEL
    }
}