package com.sapuseven.sneakybot.plugin.accounts

import com.github.theholywaffle.teamspeak3.api.event.BaseEvent
import com.sapuseven.sneakybot.plugins.PluggableService
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.ConsoleCommand
import io.javalin.Javalin

@Suppress("unused")
class ServiceApi : PluggableService {
	private lateinit var app: Javalin

	override fun preInit(pluginManager: PluginManager) {
		val port = pluginManager.getConfiguration("PluginAccounts-Server").getInt("port", 9900)
		app = Javalin.create { config ->
			config.showJavalinBanner = false
		}.start(port)
	}

	override fun postInit(pluginManager: PluginManager) {
		app.get("/") { ctx ->
			ctx.result("Hello World")
		}
	}

	override fun stop(pluginManager: PluginManager) {
		app.close()
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
