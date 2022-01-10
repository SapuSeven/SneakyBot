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
					val ubisoftUids = pluginManager.getConfiguration("PluginR6Siege-TsUbisoftMapping").run {
						keys().map { get(it, "") }.distinct()
					}

					loadPlayerRanks(ubisoftUids, Platform.UPLAY, Region.EU).forEach { rank ->
						if (rankCache[rank.key] != rank.value)
							updateRank(pluginManager, rank.key, rank.value)
						rankCache[rank.key] = rank.value
					}
				}
			}
		} catch (e: IOException) {
			pluginManager.sendMessage("An error occurred while trying to connect to the rank provider server (${e.javaClass.simpleName}: ${e.message}).")
			throw e
		}
	}

	private fun updateRank(pluginManager: PluginManager, tsUid: String, rank: Rank) {
		log.debug("TeamSpeak user $tsUid should now be ${rank.displayName}")
	}
}
