package com.sapuseven.sneakybot.utils

import java.util.ArrayList

class Command {
    var commandName: String? = null
    val parameters: ArrayList<Parameter> = ArrayList()
    var help: String = "(No help text found.)"

    fun addParameter(parameter: String) {
        if (!parameter.matches(PARAMETER_PATTERN.toRegex()))
            throw IllegalArgumentException("Invalid parameter format: $parameter!")
        val p = Parameter()
        p.parameter = parameter.replace(PARAMETER_PATTERN.toRegex(), "$1$2")
        p.isRequired = parameter[0] == '['
        parameters.add(p)
    }

    inner class Parameter {
        var parameter: String? = null
        var isRequired: Boolean = false
    }

    companion object {
        private const val PARAMETER_PATTERN = "^<([a-zA-Z_]*)>$|^\\[([a-zA-Z_]*)]$"
    }
}
