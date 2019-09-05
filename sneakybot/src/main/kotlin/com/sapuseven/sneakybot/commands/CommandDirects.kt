package com.sapuseven.sneakybot.commands

import com.sapuseven.sneakybot.SneakyBot
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand

internal class CommandDirects(private val bot: SneakyBot) : BuiltinCommand() {
    override val command: Command = Command()

    init {
        command.commandName = "directs"
        command.addParameter("<[\"add\"|\"del\"] [client_id]>")
        command.help = "Lists all clients that can use the direct chat, or add/remove clients to the direct client list."
    }

    // TODO: Reduce the references to the bot and try to handle most logic directly within this class
    override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
        return when {
            cmd.paramCount() == 3 -> when (cmd.getParam(1)) {
                "add" -> if (!bot.directClients.contains(Integer.parseInt(cmd.getParam(2)))) {
                    bot.directClients.add(Integer.parseInt(cmd.getParam(2)))
                    bot.query.api.sendPrivateMessage(
                        Integer.parseInt(cmd.getParam(2)),
                        "You are now using the direct chat to communicate with me."
                    )
                    bot.query.api.addClientToServerGroup(
                        bot.serverGroupId,
                        bot.query.api.getClientInfo(Integer.parseInt(cmd.getParam(2))).databaseId
                    )
                    val clientListMsg =
                        StringBuilder("User added to direct client list.\n\nClients that can contact me:")
                    for (client in bot.directClients)
                        clientListMsg.append("\n - ").append(bot.query.api.getClientInfo(client).nickname)
                    bot.manager.sendMessage(clientListMsg.toString(), invokerId)
                    true
                } else {
                    bot.manager.sendMessage("This user is already in the direct clients list.", invokerId)
                    false
                }
                "del" -> if (bot.directClients.contains(Integer.parseInt(cmd.getParam(2)))) {
                    bot.directClients.remove(Integer.parseInt(cmd.getParam(2)))
                    bot.query.api.sendPrivateMessage(
                        Integer.parseInt(cmd.getParam(2)),
                        "Direct chat is now closed. You can send me commands via the SneakyBOT Console channel."
                    )
                    bot.query.api.removeClientFromServerGroup(
                        bot.serverGroupId,
                        bot.query.api.getClientInfo(Integer.parseInt(cmd.getParam(2))).databaseId
                    )
                    bot.manager.sendMessage("User removed from direct client list.", invokerId)
                    true
                } else {
                    bot.manager.sendMessage("This user is not in the direct clients list!", invokerId)
                    false
                }
                else -> {
                    bot.manager.sendMessage("Unknown option: " + cmd.getParam(1), invokerId)
                    false
                }
            }
            cmd.paramCount() == 1 -> {
                val clientListMsg = StringBuilder("\nClients that can contact me:")
                for (client in bot.directClients)
                    clientListMsg.append("\n - ").append(bot.query.api.getClientInfo(client).nickname)
                bot.manager.sendMessage(clientListMsg.toString(), invokerId)
                true
            }
            else -> {
                bot.manager.sendMessage(
                    "This isn't the right way of using this command!\nTry '!help directs'",
                    invokerId
                )
                false
            }
        }
    }
}
