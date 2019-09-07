@file:Suppress("unused", "UNUSED_PARAMETER")
package com.sapuseven.sneakybot.utils

abstract class ConsoleCommand(command: String) {
    abstract val commandName: String

    abstract fun getParam(index: Int): String

    abstract fun paramCount(): Int
}
