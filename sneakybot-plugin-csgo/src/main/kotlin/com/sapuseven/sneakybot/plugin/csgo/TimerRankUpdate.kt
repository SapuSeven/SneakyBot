package com.sapuseven.sneakybot.plugin.csgo

import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.plugins.Timer
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.parseMap
import java.io.IOException


class TimerRankUpdate : Timer {
	private val rankCache: MutableMap<String, CsGoRank> = mutableMapOf()

	@OptIn(ImplicitReflectionSerializer::class)
	override fun actionPerformed(pluginManager: PluginManager) {
		try {
			val ranks = Utils.loadRanksFromServer(pluginManager)

			Json.parseMap<String, Int>(ranks).forEach { rank ->
				val rankValue = CsGoRank.values().find { it.rankId == rank.value } ?: CsGoRank.NONE
				if (rankCache[rank.key] != rankValue)
					Utils.updateRank(pluginManager, rank.key, rankValue)
				rankCache[rank.key] = rankValue
			}
		} catch (e: IOException) {
			pluginManager.sendMessage("An error occurred while trying to connect to the rank provider server (${e.javaClass.simpleName}: ${e.message}).")
			throw e
		}
	}
}
