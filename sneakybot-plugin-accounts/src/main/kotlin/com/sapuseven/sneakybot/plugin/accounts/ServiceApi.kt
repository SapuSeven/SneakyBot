package com.sapuseven.sneakybot.plugin.accounts

import com.github.theholywaffle.teamspeak3.api.event.BaseEvent
import com.sapuseven.sneakybot.plugin.accounts.ApiConstants.ERROR_NOT_FOUND
import com.sapuseven.sneakybot.plugin.accounts.models.ApiError
import com.sapuseven.sneakybot.plugins.PluggableService
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.ConsoleCommand
import io.javalin.Javalin
import io.javalin.plugin.json.JsonMapper
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

@Suppress("unused")
class ServiceApi : PluggableService {
	private lateinit var app: Javalin

	override fun preInit(pluginManager: PluginManager) {
		val port = pluginManager.getConfiguration("PluginAccounts-Server").getInt("port", 9900)
		app = Javalin.create { config ->
			config.showJavalinBanner = false
			config.jsonMapper(kotlinxSerializationMapper)
		}.start(port)
	}

	override fun postInit(pluginManager: PluginManager) {
		app.get("/clients/{name}") { ctx ->
			ctx.json(ApiError(ERROR_NOT_FOUND))
			//ctx.result("Hello: " + ctx.pathParam("name"))
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

	@OptIn(ExperimentalSerializationApi::class)
	val kotlinxSerializationMapper = object : JsonMapper {
		override fun <T> fromJsonString(json: String, targetClass: Class<T>): T {
			@Suppress("UNCHECKED_CAST")
			val deserializer = serializer(targetClass) as KSerializer<T>
			return Json.decodeFromString(deserializer, json)
		}

		override fun toJsonString(obj: Any): String {
			val serializer = serializer(obj.javaClass)
			return Json.encodeToString(serializer, obj)
		}
	}
}
