package com.sapuseven.sneakybot.plugin.csgo

import com.sapuseven.sneakybot.plugins.PluggableCommand
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand
import kotlinx.serialization.json.Json
import kotlinx.serialization.parseMap
import org.slf4j.LoggerFactory


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

	@kotlinx.serialization.UnstableDefault
	override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
		return if (::manager.isInitialized) {
			manager.getConfiguration("PluginCsGo-SteamTsMapping").apply {
				manager.api?.let { api ->
					val steamId = cmd.getParam(1)
					val uid = get(steamId, "")
					val client = api.getClientByUId(uid)
					val rank = getRankForSteamId(steamId) ?: CsGoRank.NONE
					val sgid = getServerGroupForRank(rank.name)

					manager.sendMessage(
						"${client?.nickname} is rank ${rank.rankId} (${rank.name}) and should have server group $sgid",
						invokerId
					)

					Utils.updateRank(manager, steamId, rank)
				}
			}
			true
		} else {
			log.warn("PluginManager not initialized")
			false
		}
	}

	@kotlinx.serialization.UnstableDefault
	private fun getRankForSteamId(steamId: String): CsGoRank? {
		val ranks = Utils.loadRanksFromServer(manager)

		val rankId = Json.parseMap<String, Int>(ranks)[steamId]

		return CsGoRank.values().find { it.rankId == rankId }
	}

	private fun getServerGroupForRank(rank: String): Int {
		return manager.getConfiguration("PluginCsGo-RankGroupMapping").getInt(rank, 0)
	}

	override fun setPluginManager(pluginManager: PluginManager) {
		this.manager = pluginManager
	}
}
