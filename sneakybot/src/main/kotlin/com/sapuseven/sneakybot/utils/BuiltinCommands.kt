package com.sapuseven.sneakybot.utils

import com.sapuseven.sneakybot.SneakyBot
import kotlin.system.exitProcess

class BuiltinCommands {
    companion object {
        fun mode(bot: SneakyBot, cmd: ConsoleCommand, invokerId: Int) {
            if (cmd.paramCount() == 2) {
                when (cmd.getParam(1)) {
                    "direct", "d" -> if (bot.mode != SneakyBot.MODE_DIRECT)
                        bot.setupMode(SneakyBot.MODE_DIRECT)
                    else
                        bot.manager.sendMessage("Mode is already set to DIRECT!", invokerId)
                    "channel", "console", "c" -> if (bot.mode != SneakyBot.MODE_CHANNEL)
                        bot.setupMode(SneakyBot.MODE_CHANNEL)
                    else
                        bot.manager.sendMessage("Mode is already set to CHANNEL!", invokerId)
                    else -> bot.manager.sendMessage("Unknown mode: " + cmd.getParam(1), invokerId)
                }
            } else {
                bot.manager.sendMessage("This isn't the right way of using this command!\nTry '!help mode'", invokerId)
            }
        }

        fun directs(bot: SneakyBot, cmd: ConsoleCommand, invokerId: Int) {
            when {
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
                            clientListMsg.append("\n - ${bot.query.api.getClientInfo(client).nickname}")
                        bot.manager.sendMessage(clientListMsg.toString(), invokerId)
                    } else {
                        bot.manager.sendMessage("This user is already in the direct clients list.", invokerId)
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
                    } else {
                        bot.manager.sendMessage("This user is not in the direct clients list!", invokerId)
                    }
                    else -> bot.manager.sendMessage("Unknown option: " + cmd.getParam(1), invokerId)
                }
                cmd.paramCount() == 1 -> {
                    val clientListMsg = StringBuilder("\nClients that can contact me:")
                    for (client in bot.directClients)
                        clientListMsg.append("\n - ${bot.query.api.getClientInfo(client).nickname}")
                    bot.manager.sendMessage(clientListMsg.toString(), invokerId)
                }
                else -> bot.manager.sendMessage(
                    "This isn't the right way of using this command!\nTry '!help directs'",
                    invokerId
                )
            }
        }

        fun reload(bot: SneakyBot) {
            bot.log.info("Reloading plugins...")

            bot.stopPlugins()
            bot.loadPlugins()
            try {
                bot.preInit()
            } catch (e: Exception) {
                // TODO: Better error handling
                e.printStackTrace()
                exitProcess(1)
            }

            bot.postInit()

            bot.log.info("All plugins reloaded.")
            val msg = "Plugins reloaded (${bot.commands.size} commands and ${bot.services.size} services active)."
            if (bot.mode == SneakyBot.MODE_CHANNEL)
                bot.sendChannelMessage(msg)
            else if (bot.mode == SneakyBot.MODE_DIRECT)
                bot.sendDirectMessage(msg)
        }

        fun help(bot: SneakyBot, cmd: ConsoleCommand, invokerId: Int) {
            when {
                cmd.paramCount() == 1 -> bot.manager.sendMessage(
                    "\n" +
                            "Available Commands:\n" +
                            "\n" +
                            "| !kill ALIAS !stop : Stops the SneakyBOT.\n" +
                            "| \n" +
                            "| !mode [channel|direct <PASSWORD>] : Switches between direct and channel mode. Use the PASSWORD parameter to start direct mode using a private message without using the console channel.\n" +
                            "| \n" +
                            "| !directs <[add|del] [CLIENT_ID]> : Lists all clients that can use the direct chat, or add/remove clients to the direct client list.\n" +
                            "| \n" +
                            "| !reload : Reload all plugins.\n" +
                            bot.listHelp() +
                            "\n" +
                            "[***] ... REQUIRED\n" +
                            "<***> ... OPTIONAL", invokerId
                )
                cmd.paramCount() == 2 -> when (cmd.getParam(1)) {
                    "stop", "kill" -> bot.manager.sendMessage("Usage:\n!kill ALIAS !stop : Stops the SneakyBOT.", invokerId)
                    "mode" -> bot.manager.sendMessage(
                        "Usage:\n!mode [channel|direct <PASSWORD>] : Switches between direct and channel mode. Use the PASSWORD parameter to start direct mode using a private message without using the console channel.",
                        invokerId
                    )
                    "directs" -> bot.manager.sendMessage(
                        "Usage:\n!directs <[add|del] [CLIENT_ID]> : Lists all clients that can use the direct chat, or add/remove clients to the direct client list.",
                        invokerId
                    )
                    "reload" -> bot.manager.sendMessage("Usage:\n!reload : Reload all plugins.", invokerId)
                    "help" -> bot.manager.sendMessage(
                        "Usage:\n!help [COMMAND_NAME] : Display useful information about all the commands or a specific command.",
                        invokerId
                    )
                    else -> {
                        val pluggableCommand = bot.getCommandByName(cmd.getParam(1))
                        if (pluggableCommand == null) {
                            bot.manager.sendMessage("This commands help page doesn't exist in my database!", invokerId)
                        } else {
                            val builder = StringBuilder("Usage:\n!")
                            builder
                                .append(pluggableCommand.command.commandName)
                                .append(" : ")

                            for (param in pluggableCommand.command.parameters) {
                                builder
                                    .append(bot.formatParam(param))
                                    .append(" ")
                            }

                            builder
                                .append(pluggableCommand.command.help)
                                .append("\n")

                            bot.manager.sendMessage(builder.toString(), invokerId)
                        }
                    }
                }
                else -> bot.manager.sendMessage(
                    "This isn't the right way of using this command!\nUsage: '!help <COMMAND>'",
                    invokerId
                )
            }
        }
    }
}
