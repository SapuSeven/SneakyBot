package com.sapuseven.sneakybot.services

import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode
import com.github.theholywaffle.teamspeak3.api.event.BaseEvent
import com.github.theholywaffle.teamspeak3.api.event.ClientJoinEvent
import com.github.theholywaffle.teamspeak3.api.event.TextMessageEvent
import com.sapuseven.sneakybot.SneakyBot
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.ConsoleCommand

internal class ServiceDirectMode(private val bot: SneakyBot) : BuiltinService() {
    override fun preInit(pluginManager: PluginManager) {
    }

    override fun postInit(pluginManager: PluginManager) {
        lookForExistingDirectClients()
    }

    override fun stop(pluginManager: PluginManager) {
    }

    override fun onEventReceived(e: BaseEvent) {
        when (e) {
            is TextMessageEvent -> {
                if (e.invokerId == bot.whoAmI.id || !bot.isCommand(e.message)) return

                if (e.targetMode == TextMessageTargetMode.CLIENT) interpretDirectMessage(e)
            }
            is ClientJoinEvent -> {
                // If a user joins who is already in the SneakyBOT server group, setup direct mode or add to directs
                if (bot.query.api.getServerGroupsByClientId(e.clientDatabaseId).any { it.id == bot.serverGroupId }) {
                    if (bot.mode == SneakyBot.MODE_DIRECT) {
                        // Add client to directs
                        addDirectClient(e.clientId, e.clientDatabaseId)
                    } else {
                        // Switch mode to direct
                        setupDirectMode(e.clientId)
                    }
                }
            }
        }
    }

    override fun onCommandExecuted(cmd: ConsoleCommand, invokerId: Int) {
        if (cmd.getParam(0) == "mode" && cmd.paramCount() == 2) {
            when (cmd.getParam(1)) {
                "direct", "d" -> switchToDirectMode(invokerId)
            }
        }
    }

    private fun switchToDirectMode(invokerId: Int): Boolean {
        return if (bot.mode != SneakyBot.MODE_DIRECT) {
            setupDirectMode(invokerId)
            false
        } else {
            bot.manager.sendMessage("Mode is already set to DIRECT!", invokerId)
            false
        }
    }

    private fun interpretDirectMessage(event: TextMessageEvent) {
        if (bot.mode == SneakyBot.MODE_DIRECT) {
            if (bot.directClients.contains(event.invokerId)) {
                SneakyBot.log.info("User #${event.invokerId} (${event.invokerName}) executed a command via DIRECT chat: ${event.message}")
                bot.interpretCommand(event.message, event.invokerId)
            } else {
                SneakyBot.log.info("User #${event.invokerId} (${event.invokerName}) tried to execute a command, but has no permissions to use DIRECT chat: " + event.message)
                bot.query.api.sendPrivateMessage(
                    event.invokerId,
                    "You are not allowed to give me commands!"
                )
            }
        } else {
            SneakyBot.log.info("User #${event.invokerId} (${event.invokerName}) tried to execute a command, but didn't use CONSOLE chat: " + event.message)
            bot.query.api.sendPrivateMessage(
                event.invokerId,
                "Please use the ${bot.botConfig.consoleName} channel to communicate with me."
            )
        }
    }

    /**
     * Looks for clients in the SneakyBOT server group.
     * If so, switch to direct mode.
     */
    private fun lookForExistingDirectClients() {
        SneakyBot.log.info("Looking for existing direct clients...")

        for (client in bot.query.api.clients) {
            if (client.serverGroups.contains(bot.serverGroupId)) {
                bot.directClients.add(client.id)
                bot.mode = SneakyBot.MODE_DIRECT
            }
        }
    }

    private fun setupDirectMode(invokerId: Int) {
        SneakyBot.log.info("Switching to direct mode...")
        bot.directClients.clear()
        val clientInfo = bot.query.api.getClientInfo(invokerId)
        SneakyBot.log.info("Removing all users from the SneakyBOT server group...")
        bot.query.api.getServerGroupClients(bot.serverGroupId).forEach {
            if (clientInfo.databaseId != it.clientDatabaseId)
                bot.query.api.removeClientFromServerGroup(bot.serverGroupId, it.clientDatabaseId)
        }
        val consoleChannelId = clientInfo.channelId

        var clientListMsg = "I am now listening for commands on the direct chat.\n\nClients that can contact me:"
        bot.query.api.clients
            .filter { client ->
                client.channelId == consoleChannelId
                        && client.id != bot.whoAmI.id
            }
            .forEach { client ->
                SneakyBot.log.info("Adding client #${client.id} (${client.nickname}) to the directs list...")
                addDirectClient(client.id, client.databaseId, client.databaseId != clientInfo.databaseId)
                clientListMsg += "\n - ${client.nickname}"
            }
        bot.sendChannelMessage(clientListMsg)
        SneakyBot.log.info("Finished.")
        bot.mode = SneakyBot.MODE_DIRECT
    }

    private fun addDirectClient(clientId: Int, clientDatabaseId: Int, shouldAddToServerGroup: Boolean = true) {
        bot.directClients.add(clientId)
        bot.query.api.sendPrivateMessage(
            clientId,
            "You can now use the direct chat to communicate with me."
        )
        if (shouldAddToServerGroup)
            bot.query.api.addClientToServerGroup(bot.serverGroupId, clientDatabaseId)
    }
}
