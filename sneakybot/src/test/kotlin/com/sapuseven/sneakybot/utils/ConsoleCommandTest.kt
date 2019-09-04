package com.sapuseven.sneakybot.utils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ConsoleCommandTest {
    @Test
    fun correctValues_noParameters_correctValues() {
        val command = ConsoleCommand("!test")

        Assertions.assertEquals("test", command.commandName)
        Assertions.assertEquals(1, command.paramCount())
        Assertions.assertEquals("test", command.getParam(0))
    }

    @Test
    fun correctValues_oneParameter_correctValues() {
        val command = ConsoleCommand("!test 1")

        Assertions.assertEquals("test", command.commandName)
        Assertions.assertEquals(2, command.paramCount())
        Assertions.assertEquals("test", command.getParam(0))
        Assertions.assertEquals("1", command.getParam(1))
    }

    @Test
    fun correctValues_twoParameters_correctValues() {
        val command = ConsoleCommand("!test 1 a")

        Assertions.assertEquals("test", command.commandName)
        Assertions.assertEquals(3, command.paramCount())
        Assertions.assertEquals("test", command.getParam(0))
        Assertions.assertEquals("1", command.getParam(1))
        Assertions.assertEquals("a", command.getParam(2))
    }

    @Test
    fun correctValues_stringWithSpacesAsParameter_correctValues() {
        val command = ConsoleCommand("!test \"a b c\"")

        Assertions.assertEquals("test", command.commandName)
        Assertions.assertEquals(2, command.paramCount())
        Assertions.assertEquals("test", command.getParam(0))
        Assertions.assertEquals("a b c", command.getParam(1))
    }

    @Test
    fun correctValues_noParameters_toString() {
        val command = ConsoleCommand("!test")

        Assertions.assertEquals("test()", command.toString())
    }

    @Test
    fun correctValues_oneParameter_toString() {
        val command = ConsoleCommand("!test 1")

        Assertions.assertEquals("test(1)", command.toString())
    }

    @Test
    fun correctValues_twoParameters_toString() {
        val command = ConsoleCommand("!test 1 a")

        Assertions.assertEquals("test(1, a)", command.toString())
    }

    @Test
    fun correctValues_stringWithSpacesAsParameter_toString() {
        val command = ConsoleCommand("!test \"a b c\"")

        Assertions.assertEquals("test(a b c)", command.toString())
    }
}