package com.sapuseven.sneakybot.plugin.r6siege

import com.sapuseven.sneakybot.plugin.r6siege.enums.Platform
import com.sapuseven.sneakybot.plugin.r6siege.enums.Region
import com.sapuseven.sneakybot.plugin.r6siege.utils.Api
import com.sapuseven.sneakybot.plugin.r6siege.utils.PlayerMapping
import com.sapuseven.sneakybot.plugins.PluggableCommand
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand
import org.slf4j.LoggerFactory

class CommandR6Rank : PluggableCommand {
	private lateinit var manager: PluginManager
	private val log = LoggerFactory.getLogger(CommandR6Rank::class.java)

	override val command: Command = Command()

	init {
		command.commandName = "r6rank"
		command.addParameter("<username>")
		command.help = "Queries the Rainbow Six: Siege rank of yourself if linked. Specify a user to override."
	}

	override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
		return if (::manager.isInitialized) {
			manager.getConfiguration("PluginR6Siege-Auth").apply {
				Api(get("email", ""), get("password", "")).apply {
					var ubisoftUid: String? = null

					if (cmd.paramCount() > 1)
						ubisoftUid = cmd.getParam(1)
					else {
						val clientUid = manager.getClientById(invokerId).uniqueIdentifier
						if (PlayerMapping.isTeamspeakUidMapped(clientUid))
							ubisoftUid = PlayerMapping.getUbisoftUidForTeamspeakUid(clientUid)
						else {
							manager.sendMessage(
								"Your TeamSpeak account is not associated with an Ubisoft account.\n" +
										"Please link your accounts first using !${CommandR6Link().command.commandName}."
							)
							return false
						}
					}

					ubisoftUid?.let { uid ->
						val rank = loadPlayerRanks(uid, Platform.UPLAY, Region.EU)[ubisoftUid]
						manager.sendMessage("Rank: ${rank?.displayName}")
					} ?: manager.sendMessage(
						"Your TeamSpeak account could not be mapped to your Ubisoft account.\n" +
								"Please contact the server administrator."
					) // This serves as a fallback error message
				}
			}
			true
		} else {
			log.warn("PluginManager not initialized")
			false
		}
	}

	override fun setPluginManager(pluginManager: PluginManager) {
		this.manager = pluginManager
	}
}