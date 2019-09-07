package com.sapuseven.sneakybot.plugin.ping

import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.ConsoleCommand
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CommandPingTest {
    @Test
    fun getCommand_returnsCommandPing() {
        val cmd = CommandPing()
        Assertions.assertEquals("ping", cmd.command.commandName)
    }

    @Test
    fun getCommand_callsCorrectManagerMethods() {
        val cmd = CommandPing()
        Assertions.assertEquals(false, cmd.execute(ConsoleCommand("ping"), 1))

        val managerMock: PluginManager = mockk()
        every {
            managerMock.sendMessage(any(), any())
        } returns
        cmd.setPluginManager(managerMock)
        Assertions.assertEquals(true, cmd.execute(ConsoleCommand("ping"), 2))
        verifySequence {
            managerMock.sendMessage("pong", 2)
        }
    }
}
