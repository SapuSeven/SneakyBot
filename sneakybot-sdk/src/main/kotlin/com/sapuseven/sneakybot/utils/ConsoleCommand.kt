package com.sapuseven.sneakybot.utils

import java.util.regex.Pattern

/**
 * This class gets implemented at runtime by the main bot code.
 *
 * These methods are just a reference to the actual code.
 */
class ConsoleCommand(command: String) {
	val commandName: String
	private val params = ArrayList<String>()

	init {
		val params = ArrayList<String>()
		commandName = command.substring(1).split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
		val m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(command.substring(1))
		while (m.find())
			params.add(m.group(1))
		for (param in params)
			if (param.startsWith("\"") && param.endsWith("\""))
				this.params.add(param.substring(1, param.length - 1))
			else
				this.params.add(param)
	}

	/**
	 * Returns the value of a parameter supplied to an issued command.
	 * [index] is `1`-indexed, as the first parameter (index `0`) is always the command name.
	 *
	 * @param index The target parameter index.
	 * @return The supplied value of the parameter.
	 */
	fun getParam(index: Int): String = params[index]

	/**
	 * Returns the total parameter count for an issued command.
	 * The command itself counts as a parameter, so this is always greater than one.
	 *
	 * @return The total parameter count.
	 */
	fun paramCount(): Int = params.size

	override fun toString(): String {
		val sb = StringBuilder(commandName)
		sb.append("(")
		for (i in 1 until paramCount()) {
			if (i > 1)
				sb.append(", ")
			sb.append(getParam(i))
		}
		return sb.append(")").toString()
	}
}
