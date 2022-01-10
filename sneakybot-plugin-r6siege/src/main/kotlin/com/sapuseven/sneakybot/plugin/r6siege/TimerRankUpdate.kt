package com.sapuseven.sneakybot.plugin.r6siege

import com.sapuseven.sneakybot.plugin.r6siege.enums.Platform
import com.sapuseven.sneakybot.plugin.r6siege.enums.Rank
import com.sapuseven.sneakybot.plugin.r6siege.enums.Region
import com.sapuseven.sneakybot.plugin.r6siege.utils.Api
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.plugins.Timer
import org.slf4j.LoggerFactory
import java.io.IOException


class TimerRankUpdate : Timer {
	private val log = LoggerFactory.getLogger(TimerRankUpdate::class.java)
	private val rankCache: MutableMap<String, Rank> = mutableMapOf()

	override fun actionPerformed(pluginManager: PluginManager) {
		try {
			pluginManager.getConfiguration("PluginR6Siege-Auth").apply {
				Api(get("email", ""), get("password", "")).apply {
					val tsUidToUbisoftUidMapping = pluginManager.getConfiguration("PluginR6Siege-TsUbisoftMapping")
					val onlineTsUidToUbisoftUidMapping = pluginManager.api?.clients?.map { client ->
						val tsUid = client.uniqueIdentifier.trimEnd('=')
						tsUid to tsUidToUbisoftUidMapping.get(tsUid, "")
					}?.filter { it.second.isNotBlank() } ?: emptyList()

					val onlineUbisoftUids = onlineTsUidToUbisoftUidMapping.map { it.second }.distinct()
					val ubisoftUidToRank = loadPlayerRanks(onlineUbisoftUids, Platform.UPLAY, Region.EU)
					onlineTsUidToUbisoftUidMapping.map { tsUidToUbisoftUid -> tsUidToUbisoftUid.first to ubisoftUidToRank[tsUidToUbisoftUid.second] }
						.forEach { tsUidToRank ->
							updateRank(pluginManager, tsUidToRank.first, tsUidToRank.second ?: Rank.UNRANKED)
						}
				}
			}
		} catch (e: IOException) {
			pluginManager.sendMessage("An error occurred while trying to connect to the rank provider server (${e.javaClass.simpleName}: ${e.message}).")
			throw e
		}
	}

	private fun updateRank(pluginManager: PluginManager, tsUid: String, rank: Rank) {
		pluginManager.api?.let { api ->
			pluginManager.getConfiguration("PluginR6Siege-TsSteamMapping").apply {
				val sgid = getServerGroupForRank(pluginManager, rank)
				val tsDbId =
					api.clients.find { client -> client.uniqueIdentifier.trimEnd('=') == tsUid }?.databaseId
						?: return

				val existingRankGroups = api.getServerGroupsByClientId(tsDbId).filter { sg ->
					with(pluginManager.getConfiguration("PluginR6Siege-RankGroupMapping")) {
						keys().any { get(it, "")?.toIntOrNull() == sg.id }
					}
				}

				existingRankGroups.forEach {
					if (it.id != sgid)
						api.removeClientFromServerGroup(it.id, tsDbId)
				}

				if (existingRankGroups.find { it.id == sgid } == null)
					api.addClientToServerGroup(sgid, tsDbId)
			}
		}
	}

	private fun getServerGroupForRank(pluginManager: PluginManager, rank: Rank): Int {
		return pluginManager.getConfiguration("PluginR6Siege-RankGroupMapping").getInt(rank.name, 0)
	}
}
