package com.sapuseven.sneakybot.commands

import com.sapuseven.sneakybot.SneakyBot
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand
import kotlin.system.exitProcess

internal class CommandStop(private val bot: SneakyBot) : BuiltinCommand() {
    override val command: Command = Command()

    init {
        command.commandName = "stop"
        command.help = "Stops the SneakyBot."
    }

    override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
        val msg = "Bye!"
        if (bot.mode == SneakyBot.MODE_CHANNEL)
            bot.sendChannelMessage(msg)
        else if (bot.mode == SneakyBot.MODE_DIRECT)
            bot.sendDirectMessage(msg)
        exitProcess(0)
    }
}
