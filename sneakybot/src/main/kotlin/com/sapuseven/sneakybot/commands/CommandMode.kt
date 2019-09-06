package com.sapuseven.sneakybot.commands

import com.sapuseven.sneakybot.SneakyBot
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand


internal class CommandMode(private val bot: SneakyBot) : BuiltinCommand() {
    override val command: Command = Command()

    init {
        command.commandName = "mode"
        command.addParameter("[\"channel\"|\"direct\"]")
        command.help = "Switches between direct and channel mode."
    }

    override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
        return if (cmd.paramCount() == 2) {
            when (cmd.getParam(1)) {
                "direct", "d" -> switchToDirect(invokerId)
                "channel", "console", "c" -> switchToChannel(invokerId)
                else -> {
                    bot.manager.sendMessage("Unknown mode: " + cmd.getParam(1), invokerId)
                    false
                }
            }
        } else {
            bot.manager.sendMessage("This isn't the right way of using this command!\nTry '!help ${command.commandName}'", invokerId)
            false
        }
    }

    private fun switchToChannel(invokerId: Int): Boolean {
        return if (bot.mode != SneakyBot.MODE_CHANNEL) {
            bot.setupChannelMode()
            true
        } else {
            bot.manager.sendMessage("Mode is already set to CHANNEL!", invokerId)
            false
        }
    }

    private fun switchToDirect(invokerId: Int): Boolean {
        return if (bot.mode != SneakyBot.MODE_DIRECT) {
            bot.setupDirectMode()
            false
        } else {
            bot.manager.sendMessage("Mode is already set to DIRECT!", invokerId)
            false
        }
    }
}