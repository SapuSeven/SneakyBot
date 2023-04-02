package com.sapuseven.sneakybot.plugin.autoaway

import com.github.theholywaffle.teamspeak3.TS3Api
import com.github.theholywaffle.teamspeak3.api.wrapper.Client
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerQueryInfo
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.plugins.Timer
import org.slf4j.LoggerFactory
import java.lang.System.currentTimeMillis

class TimerIdleCheck internal constructor(
	private val whoAmI: ServerQueryInfo,
	private val config: AutoAwayConfig
) : Timer {
	companion object {
		private val log = LoggerFactory.getLogger(TimerIdleCheck::class.java)
	}

	private val responseRequests = mutableMapOf<Int, Long>()

	override fun actionPerformed(pluginManager: PluginManager) {
		pluginManager.api?.let { api ->
			for (c in api.clients) {
				log.debug(c.nickname + " is idle for " + c.idleTime / 1000 + " seconds")

				if (
					(c.id == whoAmI.id)
					|| (c.channelId == config.afkChannel.id)
					|| (config.ignoreIfAway && c.isAway)
					|| (config.excludedChannels.contains(c.channelId))
				) continue

				val idleTimeThresholdMillis =
					if (c.isOutputMuted) config.idleTimeMutedThreshold * 1000 else config.idleTimeThreshold * 1000

				if (responseRequests.containsKey(c.id) && c.idleTime < idleTimeThresholdMillis)
					responseRequests.remove(c.id)

				if (
					!responseRequests.containsKey(c.id)
					&& c.idleTime >= idleTimeThresholdMillis
				)
					autoAwayRequestResponse(api, c)
				else if (
					responseRequests.containsKey(c.id)
					&& currentTimeMillis() - (responseRequests[c.id] ?: 0) >= config.idleTimeResponseThreshold * 1000
				)
					autoAway(api, c)
			}
		}
	}

	private fun autoAway(api: TS3Api, c: Client) {
		if (responseRequests.containsKey(c.id))
			responseRequests.remove(c.id)

		api.sendPrivateMessage(c.id, "You have been inactive for too long")
		api.moveClient(c.id, config.afkChannel.id)
	}

	private fun autoAwayRequestResponse(api: TS3Api, c: Client) {
		api.sendPrivateMessage(
			c.id,
			"You are about to be moved for being inactive for too long. Please perform any action to show that you're still active."
		)
		responseRequests[c.id] = currentTimeMillis()
	}
}
