package com.sapuseven.sneakybot.plugin.csgo

import com.sapuseven.sneakybot.plugins.PluggableCommand
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

@kotlinx.serialization.ImplicitReflectionSerializer
class CommandCsGoRank : PluggableCommand {
    private lateinit var manager: PluginManager
    private val log = LoggerFactory.getLogger(CommandCsGoRank::class.java)

    override val command: Command = Command()

    init {
        command.commandName = "csgorank"
        command.addParameter("[steam_id]")
        command.help = "Queries the rank of a client using the specified steam id."
    }

    override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
        return if (::manager.isInitialized) {
            manager.getConfiguration("PluginCsGo-SteamTsMapping").apply {
                manager.api?.let { api ->
                    val steamId = cmd.getParam(1)
                    val uid = get(steamId, "")
                    val client = api.getClientByUId(uid)
                    val rank = getRankForSteamId(steamId)
                    val sgid = getServerGroupForRank(rank?.name ?: "")

                    manager.sendMessage(
                        "${client?.nickname} is rank ${rank?.rankId} (${rank?.name}) and should have server group $sgid",
                        invokerId
                    )

                    api.addClientToServerGroup(sgid, client.databaseId)
                }
            }
            true
        } else {
            log.warn("PluginManager not initialized")
            false
        }
    }

    private fun getRankForSteamId(steamId: String): CsGoRank? {
        val ranks = loadRanksFromServer()

        val rankId = Json.parseMap<String, Int>(ranks)[steamId]

        return CsGoRank.values().find { it.rankId == rankId }
    }

    private fun getServerGroupForRank(rank: String): Int {
        return manager.getConfiguration("PluginCsGo-RankGroupMapping").getInt(rank, 0)
    }

    @Throws(IOException::class)
    private fun loadRanksFromServer(): String {
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
