package com.sapuseven.sneakybot.commands

import com.sapuseven.sneakybot.SneakyBot
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand
import kotlin.system.exitProcess

internal class CommandStop(private val bot: SneakyBot) : BuiltinCommand() {
    override val command: Command = Command()

    init {
        command.commandName = "stop"
        command.help = "Stops the SneakyBOT."
    }

    override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
        exitProcess(0)
    }
}
