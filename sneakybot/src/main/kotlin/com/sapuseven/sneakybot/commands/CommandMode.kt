package com.sapuseven.sneakybot.commands

import com.sapuseven.sneakybot.SneakyBot
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand


class CommandMode(private val bot: SneakyBot) : BuiltinCommand() {
    override val command: Command = Command()

    init {
        command.commandName = "mode"
        command.addParameter("[\"channel\"|\"direct\"]")
        command.addParameter("<password>")
        command.help = "Switches between direct and channel mode. Use the PASSWORD parameter to start direct mode using a private message without using the console channel." // TODO: command help
    }

    override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
        return if (cmd.paramCount() == 2) {
            when (cmd.getParam(1)) {
                "direct", "d" -> return if (bot.mode != SneakyBot.MODE_DIRECT) {
                    // TODO: Verify password
                    //bot.setupMode(SneakyBot.MODE_DIRECT)
                    false
                } else {
                    bot.manager.sendMessage("Mode is already set to DIRECT!", invokerId)
                    false
                }
                "channel", "console", "c" -> return if (bot.mode != SneakyBot.MODE_CHANNEL) {
                    bot.setupMode(SneakyBot.MODE_CHANNEL)
                    true
                } else {
                    bot.manager.sendMessage("Mode is already set to CHANNEL!", invokerId)
                    false
                }
                else -> {
                    bot.manager.sendMessage("Unknown mode: " + cmd.getParam(1), invokerId)
                    false
                }
            }
        } else {
            bot.manager.sendMessage("This isn't the right way of using this command!\nTry '!help mode'", invokerId)
            false
        }
    }
}