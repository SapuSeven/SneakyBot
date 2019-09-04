package com.sapuseven.sneakybot.utils

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.MissingValueException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SneakyBotConfigTest {
    @Test
    fun parse_exceptionOnMissingRequiredParameters() {
        val args = emptyArray<String>()
        assertThrows<MissingValueException> {
            ArgParser(args).parseInto(::SneakyBotConfig)
        }
    }

    @Test
    fun parse_portParsing() {
        val validPortArgs = "-s localhost -p 123 -q 10010".split(" ").toTypedArray()
        val invalidPortArgs = "-s localhost -p 123 -q 99999".split(" ").toTypedArray()

        val botConfig = ArgParser(validPortArgs).parseInto(::SneakyBotConfig)
        Assertions.assertEquals(10010, botConfig.port)

        assertThrows<InvalidArgumentException> {
            ArgParser(invalidPortArgs).parseInto(::SneakyBotConfig)
        }
    }

    @Test
    fun parse_correctValues_shortOptions() {
        val args = "-s localhost -q 10010 -u TestUser -p TestPassword -f -d -g 123".split(" ").toTypedArray()
        val botConfig = ArgParser(args).parseInto(::SneakyBotConfig)

        Assertions.assertEquals("localhost", botConfig.host)
        Assertions.assertEquals(10010, botConfig.port)
        Assertions.assertEquals("TestUser", botConfig.username)
        Assertions.assertEquals("TestPassword", botConfig.password)
        Assertions.assertEquals(true, botConfig.unlimitedFloodRate)
        Assertions.assertEquals(true, botConfig.debug)
        Assertions.assertEquals(123, botConfig.templateServerGroupId)
    }

    @Test
    fun parse_correctValues_longOptions() {
        val args = ("--host localhost " +
                "--port 10010 " +
                "--user TestUser " +
                "--password TestPassword " +
                "--unlimited " +
                "--debug " +
                "--vserver 2 " +
                "--servergroup 123 " +
                "--data-dir bot/data " +
                "--plugin-dir bot/plugins " +
                "--console-password ConsolePassword " +
                "--console-name ConsoleName"
                ).split(" ").toTypedArray()
        val botConfig = ArgParser(args).parseInto(::SneakyBotConfig)

        Assertions.assertEquals("localhost", botConfig.host)
        Assertions.assertEquals(10010, botConfig.port)
        Assertions.assertEquals("TestUser", botConfig.username)
        Assertions.assertEquals("TestPassword", botConfig.password)
        Assertions.assertEquals(true, botConfig.unlimitedFloodRate)
        Assertions.assertEquals(true, botConfig.debug)
        Assertions.assertEquals(2, botConfig.virtualServerId)
        Assertions.assertEquals(123, botConfig.templateServerGroupId)
        Assertions.assertEquals("bot/data", botConfig.dataDir)
        Assertions.assertEquals("bot/plugins", botConfig.pluginDir)
        Assertions.assertEquals("ConsolePassword", botConfig.consolePassword)
        Assertions.assertEquals("ConsoleName", botConfig.consoleName)
    }
}