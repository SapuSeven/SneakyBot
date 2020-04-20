package com.sapuseven.sneakybot.plugin.autoaway

import com.github.theholywaffle.teamspeak3.api.event.BaseEvent
import com.sapuseven.sneakybot.plugins.PluggableService
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.ConsoleCommand

class ServiceAutoAway : PluggableService {
	companion object {
		private const val DEFAULT_AFK_CHANNEL_NAME = "AFK"
		private const val DEFAULT_IDLE_TIME_THRESHOLD = 60 * 60 * 1000 // 1 hour
		private const val DEFAULT_IDLE_TIME_MUTED_THRESHOLD = 20 * 60 * 1000 // 20 minutes
		private const val DEFAULT_IDLE_TIME_RESPONSE_THRESHOLD = 60 * 1000 // 1 minute
		private const val DEFAULT_IGNORE_IF_AWAY = false
	}

	@Throws(Exception::class)
	override fun preInit(pluginManager: PluginManager) {
		// unused
	}

	override fun postInit(pluginManager: PluginManager) {
		pluginManager.api?.let { api ->
			val config = pluginManager.getConfiguration("PluginAutoAway")

            val afkChannelName = config.get("AfkChannelName", DEFAULT_AFK_CHANNEL_NAME)
			val timer = TimerIdleCheck(
				api.whoAmI(), AutoAwayConfig(
					api.channels.find { it.name == afkChannelName } ?: return,
					config.getInt("IdleTimeThreshold", DEFAULT_IDLE_TIME_THRESHOLD),
					config.getInt("IdleTimeMutedThreshold", DEFAULT_IDLE_TIME_MUTED_THRESHOLD),
					config.getInt("IdleTimeResponseThreshold", DEFAULT_IDLE_TIME_RESPONSE_THRESHOLD),
					config.getBoolean("IgnoreIfAway", DEFAULT_IGNORE_IF_AWAY),
					config.get("ExcludedChannels", "").split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
				)
			)
			pluginManager.addTimer(timer, 10)
		}
	}

	override fun stop(pluginManager: PluginManager) {
		// unused
	}

	override fun onEventReceived(e: BaseEvent) {
		// unused
	}

	override fun onCommandExecuted(cmd: ConsoleCommand, invokerId: Int) {
		// unused
	}

	override fun setPluginManager(pluginManager: PluginManager) {
		// unused
	}
}
