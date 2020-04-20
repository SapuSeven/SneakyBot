package com.sapuseven.sneakybot.commands

import com.sapuseven.sneakybot.SneakyBot
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand

internal class CommandTimers(private val bot: SneakyBot) : BuiltinCommand() {
	override val command: Command = Command()

	init {
		command.commandName = "timers"
		command.help = "Lists all currently started timers."
	}

	override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
		val msg =
			if (bot.timers.isEmpty())
				"No timers are currently loaded."
			else
				bot.timers.joinToString(separator = "\n") {
					"${it.name} (running: ${it.thread.isAlive})"
				}
		if (bot.mode == SneakyBot.MODE_CHANNEL)
			bot.sendChannelMessage(msg)
		else if (bot.mode == SneakyBot.MODE_DIRECT)
			bot.sendDirectMessage(msg)
		return true
	}
}
