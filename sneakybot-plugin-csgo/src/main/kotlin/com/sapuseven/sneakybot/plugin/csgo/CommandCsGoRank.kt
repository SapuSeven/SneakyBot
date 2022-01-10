package com.sapuseven.sneakybot.plugin.csgo

import com.sapuseven.sneakybot.plugins.PluggableCommand
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory


class CommandCsGoRank : PluggableCommand {
	private lateinit var manager: PluginManager
	private val log = LoggerFactory.getLogger(CommandCsGoRank::class.java)

	override val command: Command = Command()

	init {
		command.commandName = "csgorank"
		command.addParameter("[steam_id]")
		command.help = "Queries your rank if linked. Specify a Steam id to override."
	}

	override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
		return if (::manager.isInitialized) {
			var steamId: String? = null

			if (cmd.paramCount() > 1)
				steamId = cmd.getParam(1)
			else {
				val clientUid = manager.getClientById(invokerId).uniqueIdentifier.trimEnd('=')

				manager.getConfiguration("PluginCsGo-TsSteamMapping").apply {
					steamId = get(clientUid, "")
					if ((steamId ?: "").isBlank()) {
						manager.sendMessage(
							"Your TeamSpeak account is not associated with a Steam account."
						)
						return false
					}
				}
			}

			steamId?.let { id ->
				val rank = getRankForSteamId(id)?.let { rank ->
					val sgid = getServerGroupForRank(rank.name)
					manager.sendMessage("Rank: ${rank.name} (server group $sgid)")
				} ?: {
					manager.sendMessage(
						"Your rank could not be determined.\n" +
								"Please contact the server administrator."
					)
				}
			} ?: run {
				manager.sendMessage(
					"Your TeamSpeak account could not be mapped to your Steam account.\n" +
							"Please contact the server administrator."
				) // This serves as a fallback error message
				return false
			}
			true
		} else {
			log.warn("PluginManager not initialized")
			false
		}
	}

	private fun getRankForSteamId(steamId: String): CsGoRank? {
		val ranks = Utils.loadRanksFromServer(manager)

		val rankId = Json.decodeFromString<Map<String, Int>>(ranks)[steamId]

		return CsGoRank.values().find { it.rankId == rankId }
	}

	private fun getServerGroupForRank(rank: String): Int {
		return manager.getConfiguration("PluginCsGo-RankGroupMapping").getInt(rank, 0)
	}

	override fun setPluginManager(pluginManager: PluginManager) {
		this.manager = pluginManager
	}
}
