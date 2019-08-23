package com.sapuseven.sneakybot.utils

import java.util.*
import java.util.regex.Pattern

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

    fun getParam(index: Int): String {
        return params[index]
    }

    fun paramCount(): Int {
        return params.size
    }

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