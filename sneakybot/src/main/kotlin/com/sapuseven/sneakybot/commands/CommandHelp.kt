package com.sapuseven.sneakybot.commands

import com.sapuseven.sneakybot.SneakyBot
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand

class CommandHelp(private val bot: SneakyBot) : BuiltinCommand() {
    override val command: Command = Command()

    init {
        command.commandName = "help"
        command.help = "" // TODO: command help
    }

    override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
        return when {
            cmd.paramCount() == 1 -> {
                bot.manager.sendMessage(
                    "\n" +
                            "Available Commands:\n" +
                            listHelp() +
                            "\n" +
                            "[***] ... REQUIRED\n" +
                            "<***> ... OPTIONAL", invokerId
                )
                true
            }
            cmd.paramCount() == 2 -> {
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
                            .append(formatParam(param))
                            .append(" ")
                    }

                    builder
                        .append(pluggableCommand.command.help)
                        .append("\n")

                    bot.manager.sendMessage(builder.toString(), invokerId)
                }
                true
            }
            else -> {
                bot.manager.sendMessage(
                    "This isn't the right way of using this command!\nUsage: '!help <COMMAND>'",
                    invokerId
                )
                false
            }
        }
    }

    private fun formatParam(param: Command.Parameter): String {
        // TODO: Don't capitalize strings between quotes
        return if (param.isRequired)
            "[${param.parameter?.toUpperCase()}]"
        else
            "<${param.parameter?.toUpperCase()}>"
    }

    private fun listHelp(): String {
        val builder = StringBuilder()
        for (command in bot.builtinCommands + bot.commands) {
            if (command.command.commandName == this.command.commandName) continue
            builder
                .append("| \n| !")
                .append(command.command.commandName)
                .append(" ")
            for (param in command.command.parameters) {
                builder
                    .append(formatParam(param))
                    .append(" ")
            }
            builder.append(": ")

            builder.append(command.command.help.replace("\n", "\n|     ")).append("\n")
        }
        return builder.toString()
    }
}
