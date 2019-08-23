@file:Suppress("UNUSED_PARAMETER")

package com.sapuseven.sneakybot.utils

import java.util.ArrayList

class Command {
    lateinit var commandName: String
    val parameters: ArrayList<Parameter> = ArrayList()
    var help: String = ""

    fun addParameter(parameter: String) {
        // This gets implemented by the SneakyBOT client
    }

    inner class Parameter {
        lateinit var parameter: String
        var isRequired: Boolean = false
    }
}
