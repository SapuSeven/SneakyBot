package com.sapuseven.sneakybot.plugin.csgo

import com.sapuseven.sneakybot.plugins.PluginManager
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.*

object Utils {
	lateinit var serverUrl: URL

	@Throws(IOException::class)
	fun loadRanksFromServer(manager: PluginManager): String {
		if (!::serverUrl.isInitialized)
			serverUrl = manager.getConfiguration("PluginCsGo-RankGroupMapping").run {
				URI(
					get("apiScheme", "http"),
					get("apiUserInfo", null),
					get("apiHost", "localhost"),
					getInt("apiPort", 7300),
					get("apiPath", "/ranks"),
					get("apiQuery", null),
					get("apiFragment", null)
				).toURL()
			}

		val conn: HttpURLConnection = serverUrl.openConnection() as HttpURLConnection
		conn.requestMethod = "GET"

		conn.connect()

		val responseCode: Int = conn.responseCode

		if (responseCode != 200) throw IOException("HTTP$responseCode") else {
			Scanner(serverUrl.openStream()).apply {
				val line = nextLine()
				close()
				return line
			}
		}
	}

	fun updateRank(manager: PluginManager, steamId: String, rank: CsGoRank) {
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

			if (rank != CsGoRank.NONE)
				if (existingRankGroups.find { it.id == sgid } == null)
					api.addClientToServerGroup(sgid, client.databaseId)
		}
	}

	private fun getServerGroupForRank(manager: PluginManager, rank: String): Int {
		return manager.getConfiguration("PluginCsGo-RankGroupMapping").getInt(rank, 0)
	}
}
