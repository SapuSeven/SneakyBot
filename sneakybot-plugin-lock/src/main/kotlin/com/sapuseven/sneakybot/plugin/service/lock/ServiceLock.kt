package com.sapuseven.sneakybot.plugin.service.lock

import com.github.theholywaffle.teamspeak3.api.event.BaseEvent
import com.github.theholywaffle.teamspeak3.api.event.ClientJoinEvent
import com.github.theholywaffle.teamspeak3.api.event.ClientMovedEvent
import com.github.theholywaffle.teamspeak3.api.exception.TS3CommandFailedException
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerQueryInfo
import com.sapuseven.sneakybot.plugins.PluggableService
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.ConsoleCommand
import java.util.*

class ServiceLock : PluggableService {
    private lateinit var manager: PluginManager
    private lateinit var whoAmI: ServerQueryInfo
    private val lockedClients = ArrayList<ClientChannelPair>()

    companion object {
        private const val ERROR_ID_ALREADY_MEMBER_OF_CHANNEL = 770
    }

    override fun preInit(pluginManager: PluginManager) {
        // unused
    }

    override fun postInit(pluginManager: PluginManager) {
        whoAmI = pluginManager.api!!.whoAmI()
    }

    override fun stop(pluginManager: PluginManager) {
        // unused
    }

    override fun onEventReceived(e: BaseEvent) {
        if (e is ClientMovedEvent && e.invokerId != whoAmI.id) // Prevent conflicts with other automated moving plugins
            moveIfLocked(e.clientId)

        if (e is ClientJoinEvent) {
            refreshListWithClient(e.clientNickname, e.clientId)
            moveIfLocked(e.clientId)
        }
    }

    override fun onCommandExecuted(cmd: ConsoleCommand, invokerId: Int) {
        when (cmd.getParam(0)) {
            "lock" -> {
                val targetUserId = cmd.getParam(1).toIntOrNull() ?: return sendHelpMessage("lock", invokerId)
                val targetChannelId = cmd.getParam(2).toIntOrNull() ?: return sendHelpMessage("lock", invokerId)

                if (lockClient(targetUserId, targetChannelId, invokerId)) {
                    manager.sendMessage("Successfully locked client to channel.", invokerId)
                    moveIfLocked(targetUserId)
                } else
                    manager.sendMessage("An unknown error occurred.", invokerId)
            }
            "unlock" -> {
                val targetUserId = cmd.getParam(1).toIntOrNull() ?: return sendHelpMessage("unlock", invokerId)

                if (unlockClient(targetUserId, invokerId))
                    manager.sendMessage("Successfully unlocked client.", invokerId)
                else
                    manager.sendMessage("An unknown error occurred.", invokerId)
            }
        }
    }

    private fun sendHelpMessage(commandName: String, invokerId: Int) {
        manager.sendMessage(
            "This isn't the right way of using this command!\nTry '!help $commandName'",
            invokerId
        )
    }

    private fun moveIfLocked(clientId: Int) {
        for (lockedClient in lockedClients) {
            if (lockedClient.clientId == clientId)
                try {
                    manager.api?.moveClient(clientId, lockedClient.channelId)
                } catch (e: TS3CommandFailedException) {
                    if (e.error.id != ERROR_ID_ALREADY_MEMBER_OF_CHANNEL)
                        throw e
                }

        }
    }

    private fun refreshListWithClient(clientNickname: String, clientId: Int) {
        for (lockedClient in lockedClients) {
            if (lockedClient.clientName == clientNickname)
                lockedClient.clientId = clientId
        }
    }

    override fun setPluginManager(pluginManager: PluginManager) {
        this.manager = pluginManager
    }

    private fun lockClient(clientId: Int, channelId: Int, invokerId: Int): Boolean {
        // TODO: Check if client and channel actually exist
        return try {
            val clientName = manager.getClientNameById(clientId)
            lockedClients.add(ClientChannelPair(clientName, clientId, channelId))
            true
        } catch (e: TS3CommandFailedException) {
            manager.sendMessage("Client not found!", invokerId)
            false
        }

    }

    private fun unlockClient(clientId: Int, invokerId: Int): Boolean {
        var clientIndex = -1
        for (i in lockedClients.indices)
            if (lockedClients[i].clientId == clientId) {
                clientIndex = i
                break
            }
        return if (clientIndex >= 0) {
            lockedClients.removeAt(clientIndex)
            true
        } else {
            manager.sendMessage("Client not found!", invokerId)
            false
        }
    }
}