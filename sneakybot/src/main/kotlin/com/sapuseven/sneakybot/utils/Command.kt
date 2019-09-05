package com.sapuseven.sneakybot.utils

class Command {
    var commandName: String? = null
    val parameters: ArrayList<Parameter> = ArrayList()
    var help: String = "(No help text found.)"

    companion object {
        private const val PARAMETER_PATTERN = "^<(.*)>$|^\\[(.*)]$"
    }

    fun addParameter(parameter: String) {
        require(parameter.matches(PARAMETER_PATTERN.toRegex())) { "Invalid parameter format: $parameter!" }
        val p = Parameter()
        p.parameter = parameter.replace(PARAMETER_PATTERN.toRegex(), "$1$2")
        p.isRequired = parameter[0] == '['
        parameters.add(p)
    }

    inner class Parameter {
        var parameter: String? = null
        var isRequired: Boolean = false
    }
}
