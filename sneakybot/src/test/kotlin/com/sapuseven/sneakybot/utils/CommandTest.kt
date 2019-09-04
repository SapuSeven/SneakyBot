package com.sapuseven.sneakybot.utils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CommandTest {
    @Test
    fun defaultConstructor() {
        val command = Command()

        Assertions.assertNull(command.commandName)
        Assertions.assertEquals(0, command.parameters.size)
        Assertions.assertNotNull(command.help)
    }

    @Test
    fun addParameter_requiredParameter() {
        val command = Command()

        command.addParameter("[test]")
        Assertions.assertEquals("test", command.parameters[0].parameter)
        Assertions.assertEquals(true, command.parameters[0].isRequired)
    }

    @Test
    fun addParameter_optionalParameter() {
        val command = Command()

        command.addParameter("<test>")
        Assertions.assertEquals("test", command.parameters[0].parameter)
        Assertions.assertEquals(false, command.parameters[0].isRequired)
    }
}