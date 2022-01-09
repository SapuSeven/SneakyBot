package com.sapuseven.sneakybot.plugin.r6siege

import com.sapuseven.sneakybot.plugin.r6siege.enums.Platform
import com.sapuseven.sneakybot.plugin.r6siege.utils.Api
import com.sapuseven.sneakybot.plugins.PluggableCommand
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand
import org.slf4j.LoggerFactory

class CommandR6Link : PluggableCommand {
	private lateinit var manager: PluginManager
	private val log = LoggerFactory.getLogger(CommandR6Link::class.java)

	override val command: Command = Command()

	init {
		command.commandName = "r6link"
		command.addParameter("[username]")
		command.help = "Adds a link between your TeamSpeak and Ubisoft account."
	}

	override fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean {
		return if (::manager.isInitialized) {
			if (cmd.paramCount() <= 1) {
				manager.sendMessage("Please provide your Ubisoft account name.", invokerId)
				return false
			}

			manager.getConfiguration("PluginR6Siege-Auth").apply {
				try {
					Api(get("email", ""), get("password", "")).apply {
						val searchResult = searchPlayersByUsername(cmd.getParam(1), Platform.UPLAY)
						if (searchResult.isEmpty())
							manager.sendMessage("Username not found! (Platform: PC)", invokerId)
						else if (searchResult.size > 1)
							manager.sendMessage("Too many results. This is not supported yet.", invokerId)
						else {
							val profile = searchResult.first()
							val tsUid = manager.getClientById(invokerId).uniqueIdentifier
							manager.getConfiguration("PluginR6Siege-TsUbisoftMapping").put(tsUid, profile.userId)
							manager.sendMessage(
								"Success! Your TeamSpeak account has been successfully linked to the following Ubisoft account:\n" +
										"${profile.nameOnPlatform} (${profile.userId})", invokerId
							)
							manager.sendMessage("You will receive your rank shortly.", invokerId)
						}
					}
				} catch (e: Exception) {
					manager.sendMessage(
						"There has been an error linking your account. Please contact the server administrator.",
						invokerId
					)
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
