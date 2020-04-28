@file:Suppress("unused", "UNUSED_PARAMETER")

package com.sapuseven.sneakybot.utils

/**
 * This class gets implemented at runtime by the main bot code.
 *
 * These methods are just a reference to the actual code.
 */
class ConsoleCommand(command: String) {
    val commandName: String = ""

    /**
     * Returns the value of a parameter supplied to an issued command.
     * [index] is `1`-indexed, as the first parameter (index `0`) is always the command name.
     *
     * @param index The target parameter index.
     * @return The supplied value of the parameter.
     */
    fun getParam(index: Int): String = ""

    /**
     * Returns the total parameter count for an issued command.
     * The command itself counts as a parameter, so this is always greater than one.
     *
     * @return The total parameter count.
     */
    fun paramCount(): Int = 0
}
