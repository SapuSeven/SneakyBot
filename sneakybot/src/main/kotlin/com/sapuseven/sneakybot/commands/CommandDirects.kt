package com.sapuseven.sneakybot.commands

import com.sapuseven.sneakybot.SneakyBot
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand

internal class CommandDirects(private val bot: SneakyBot) : BuiltinCommand() {
    override val command: Command = Command()

    init {
        command.commandName = "directs"
        command.addParameter("<[\"add\"|\"del\"] [client_id]>")
        command.help =
            "Lists all clients that can use the direct chat, or add/remove clients to the direct client list."
    }

    override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
        return when (cmd.paramCount()) {
            1 -> listDirectUsers(invokerId)
            3 -> {
                val targetUserId = cmd.getParam(2).toIntOrNull() ?: return sendHelpMessage(invokerId)

                when (cmd.getParam(1)) {
                    "add" -> addDirectUser(targetUserId, invokerId)
                    "del" -> removeDirectUser(targetUserId, invokerId)
                    else -> {
                        bot.manager.sendMessage("Unknown option: " + cmd.getParam(1), invokerId)
                        false
                    }
                }
            }
            else -> sendHelpMessage(invokerId)
        }
    }

    private fun sendHelpMessage(invokerId: Int): Boolean {
        bot.manager.sendMessage(
            "This isn't the right way of using this command!\nTry '!help ${command.commandName}'",
            invokerId
        )
        return false
    }

    private fun listDirectUsers(invokerId: Int): Boolean {
        val clientListMsg = StringBuilder("\nClients that can contact me:")
        for (client in bot.directClients)
            clientListMsg.append("\n - ").append(bot.query.api.getClientInfo(client).nickname)
        bot.manager.sendMessage(clientListMsg.toString(), invokerId)
        return true
    }

    private fun removeDirectUser(targetUserId: Int, invokerId: Int): Boolean {
        return if (bot.directClients.contains(targetUserId)) {
            bot.directClients.remove(targetUserId)
            bot.query.api.sendPrivateMessage(
                targetUserId,
                "Direct chat is now closed. You can send me commands via the ${bot.botConfig.consoleName} channel."
            )
            bot.query.api.removeClientFromServerGroup(
                bot.serverGroupId,
                bot.query.api.getClientInfo(targetUserId).databaseId
            )
            bot.manager.sendMessage("User removed from direct client list.", invokerId)
            true
        } else {
            bot.manager.sendMessage("This user is not in the direct clients list!", invokerId)
            false
        }
    }

    private fun addDirectUser(targetUserId: Int, invokerId: Int): Boolean {
        return if (!bot.directClients.contains(targetUserId)) {
            bot.directClients.add(targetUserId)
            bot.query.api.sendPrivateMessage(targetUserId, "You are now using the direct chat to communicate with me.")
            bot.query.api.addClientToServerGroup(
                bot.serverGroupId,
                bot.query.api.getClientInfo(targetUserId).databaseId
            )
            val clientListMsg = StringBuilder("User added to direct client list.\n\nClients that can contact me:")
            for (client in bot.directClients) clientListMsg.append("\n - ").append(bot.query.api.getClientInfo(client).nickname)
            bot.manager.sendMessage(clientListMsg.toString(), invokerId)
            true
        } else {
            bot.manager.sendMessage("This user is already in the direct clients list.", invokerId)
            false
        }
    }
}
