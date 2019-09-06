package com.sapuseven.sneakybot.plugins

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileFilter
import java.io.IOException

class PluginLoaderTest {
    @Test
    fun loadCommands() {
        val pluginDir: File = mockk()

        every { pluginDir.listFiles(any<FileFilter>()) } returns arrayOf(File("/TestPlugin.jar"))

        mockkObject(PluginLoader, recordPrivateCalls = true)
        every { PluginLoader["extractClassesFromJAR"](any<File>(), any<ClassLoader>(), any<Class<*>>()) } returns listOf(
            mockk<PluggableCommand> {}::class.java
        )

        val commands = PluginLoader.loadPlugins<PluggableCommand>(pluginDir, PluggableCommand::class.java)

        Assertions.assertEquals(1, commands.size)
    }

    @Test
    fun loadCommands_returnsEmptyOnException() {
        val pluginDir: File = mockk()

        every { pluginDir.listFiles(any<FileFilter>()) } returns arrayOf(File("/TestPlugin.jar"))

        mockkObject(PluginLoader, recordPrivateCalls = true)
        every {
            PluginLoader["extractClassesFromJAR"](
                any<File>(),
                any<ClassLoader>(),
                any<Class<*>>()
            )
        } throws IOException()

        val commands = PluginLoader.loadPlugins<PluggableCommand>(pluginDir, PluggableCommand::class.java)
        Assertions.assertEquals(0, commands.size)
    }

    @Test
    fun loadServices() {
        val pluginDir: File = mockk()

        every { pluginDir.listFiles(any<FileFilter>()) } returns arrayOf(File("/TestPlugin.jar"))

        mockkObject(PluginLoader, recordPrivateCalls = true)
        every { PluginLoader["extractClassesFromJAR"](any<File>(), any<ClassLoader>(), any<Class<*>>()) } returns listOf(
            mockk<PluggableService>{}::class.java
        )

        val services = PluginLoader.loadPlugins<PluggableService>(pluginDir, PluggableService::class.java)

        Assertions.assertEquals(1, services.size)
    }

    @Test
    fun loadServices_returnsEmptyOnException() {
        val pluginDir: File = mockk()

        every { pluginDir.listFiles(any<FileFilter>()) } returns arrayOf(File("/TestPlugin.jar"))

        mockkObject(PluginLoader, recordPrivateCalls = true)
        every {
            PluginLoader["extractClassesFromJAR"](
                any<File>(),
                any<ClassLoader>(),
                any<Class<*>>()
            )
        } throws IOException()

        val services = PluginLoader.loadPlugins<PluggableService>(pluginDir, PluggableService::class.java)
        Assertions.assertEquals(0, services.size)
    }
}
