package com.sapuseven.sneakybot.commands

import com.sapuseven.sneakybot.SneakyBot
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand

internal class CommandHelp(private val bot: SneakyBot) : BuiltinCommand() {
    override val command: Command = Command()

    init {
        command.commandName = "help"
    }

    override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
        return when (cmd.paramCount()) {
            1 -> {
                bot.manager.sendMessage(
                    "Available Commands:${listHelp()}\n\n[***] ... REQUIRED\n<***> ... OPTIONAL",
                    invokerId
                )
                true
            }
            2 -> {
                bot.getCommandByName(cmd.getParam(1))?.let {
                    bot.manager.sendMessage("Usage:\n${listSingleHelp(it.command)}", invokerId)
                    true
                } ?: run {
                    bot.manager.sendMessage("This commands help page doesn't exist in my database!", invokerId)
                    false
                }
            }
            else -> sendHelpMessage(invokerId)
        }
    }

    private fun sendHelpMessage(invokerId: Int): Boolean {
        bot.manager.sendMessage(
            "This isn't the right way of using this command!\nTry '!help <COMMAND>'",
            invokerId
        )
        return false
    }

    private fun formatParam(param: Command.Parameter): String {
        // TODO: Don't capitalize strings between quotes
        return if (param.isRequired)
            "[${param.parameter?.toUpperCase()}]"
        else
            "<${param.parameter?.toUpperCase()}>"
    }

    private fun listHelp(): String {
        var message = ""
        for (command in bot.builtinCommands + bot.commands) {
            if (command.command.commandName == this.command.commandName) continue
            message += "\n| \n| ${listSingleHelp(command.command).replace("\n", "\n|     ")}"
        }
        return message
    }

    private fun listSingleHelp(command: Command): String {
        var message = "!${command.commandName} "

        for (param in command.parameters)
            message += "${formatParam(param)} "

        message += ": ${command.help}"

        return message
    }
}
