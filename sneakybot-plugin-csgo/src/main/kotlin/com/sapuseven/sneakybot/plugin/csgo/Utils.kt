package com.sapuseven.sneakybot.plugin.csgo

import com.sapuseven.sneakybot.plugins.PluginManager
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.*

object Utils {
	private lateinit var serverUrl: URL

	@Throws(IOException::class)
	fun loadRanksFromServer(manager: PluginManager): String {
		if (!::serverUrl.isInitialized)
			serverUrl = manager.getConfiguration("PluginCsGo-RankProviderApi").run {
				URI(
					get("scheme", "http"),
					get("userInfo", null),
					get("host", "localhost"),
					getInt("port", 7300),
					get("path", "/ranks"),
					get("query", null),
					get("fragment", null)
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
			manager.getConfiguration("PluginCsGo-TsSteamMapping").apply {
				this.keys().filter { tsUid ->
					get(tsUid, "") == steamId
				}.forEach { tsUid ->
					val sgid = getServerGroupForRank(manager, rank.name)
					val tsDbId =
						api.clients.find { client -> client.uniqueIdentifier.trimEnd('=') == tsUid }?.databaseId
							?: return@forEach

					val existingRankGroups = api.getServerGroupsByClientId(tsDbId).filter { sg ->
						with(manager.getConfiguration("PluginCsGo-RankGroupMapping")) {
							keys().any { get(it, "")?.toIntOrNull() == sg.id }
						}
					}

					existingRankGroups.forEach {
						if (it.id != sgid)
							api.removeClientFromServerGroup(it.id, tsDbId)
					}

					if (rank != CsGoRank.NONE)
						if (existingRankGroups.find { it.id == sgid } == null)
							api.addClientToServerGroup(sgid, tsDbId)
				}
			}
		}
	}

	private fun getServerGroupForRank(manager: PluginManager, rank: String): Int {
		return manager.getConfiguration("PluginCsGo-RankGroupMapping").getInt(rank, 0)
	}
}
