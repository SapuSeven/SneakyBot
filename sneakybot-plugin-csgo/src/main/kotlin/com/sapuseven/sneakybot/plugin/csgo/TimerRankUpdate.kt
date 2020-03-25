package com.sapuseven.sneakybot.plugin.csgo

import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.plugins.Timer
import kotlinx.serialization.json.Json
import kotlinx.serialization.parseMap
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class TimerRankUpdate : Timer {
    private val rankCache: MutableMap<String, CsGoRank> = mutableMapOf()

    @kotlinx.serialization.ImplicitReflectionSerializer
    @kotlinx.serialization.UnstableDefault
    override fun actionPerformed(pluginManager: PluginManager) {
        val ranks = loadRanksFromServer()
        Json.parseMap<String, Int>(ranks).forEach { rank ->
            val rankValue = CsGoRank.values().find { it.rankId == rank.value } ?: CsGoRank.NONE
            if (rankCache[rank.key] != rankValue)
                updateRank(pluginManager, rank.key, rankValue)
            rankCache[rank.key] = rankValue
        }
    }

    private fun updateRank(manager: PluginManager, steamId: String, rank: CsGoRank) {
        manager.api?.let { api ->
            val uid = manager.getConfiguration("PluginCsGo-SteamTsMapping").get(steamId, "")
            val client = api.getDatabaseClientByUId(uid)
            val sgid = getServerGroupForRank(manager, rank.name)

            val existingRankGroups = api.getServerGroupsByClientId(client.databaseId).filter { sg ->
                with(manager.getConfiguration("PluginCsGo-RankGroupMapping")) {
                    keys().any { get(it, "")?.toIntOrNull() == sg.id }
                }
            }

            existingRankGroups.forEach {
                if (it.id != sgid)
                    api.removeClientFromServerGroup(it.id, client.databaseId)
            }

            if (existingRankGroups.find { it.id == sgid } == null)
                api.addClientToServerGroup(sgid, client.databaseId)
        }
    }

    private fun getServerGroupForRank(manager: PluginManager, rank: String): Int {
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
}
