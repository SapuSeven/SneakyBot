package com.sapuseven.sneakybot.plugin.csgo

import com.sapuseven.sneakybot.plugins.PluggableCommand
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class CommandCsGoRanksTest : PluggableCommand {
    private lateinit var manager: PluginManager
    private val log = LoggerFactory.getLogger(CommandCsGoRanksTest::class.java)

    override val command: Command = Command()

    init {
        command.commandName = "csgorankstest"
        command.help = "Queries the CS:GO rank server and prints the raw result."
    }

    override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
        return if (::manager.isInitialized) {
            try {
                val result = loadStringFromServer()
                manager.sendMessage(result, invokerId)
            } catch (e: IOException) {
                manager.sendMessage("Connection failed (${e.message})", invokerId)
            }
            true
        } else {
            log.warn("PluginManager not initialized")
            false
        }
    }

    @Throws(IOException::class)
    private fun loadStringFromServer(): String {
        val url = URL("http://localhost:7300/ranks")
        val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"

        conn.connect()

        val responseCode: Int = conn.responseCode

        if (responseCode != 200) throw IOException("HTTP$responseCode") else {
            Scanner(url.openStream()).apply {
                val line = nextLine()
                close()
                return line
            }
        }
    }

    override fun setPluginManager(pluginManager: PluginManager) {
        this.manager = pluginManager
    }
}
