package com.sapuseven.sneakybot.plugin.accounts

import com.github.theholywaffle.teamspeak3.api.event.BaseEvent
import com.github.theholywaffle.teamspeak3.api.exception.TS3CommandFailedException
import com.github.theholywaffle.teamspeak3.api.wrapper.Client
import com.sapuseven.sneakybot.plugin.accounts.ApiConstants.ERROR_NOT_FOUND
import com.sapuseven.sneakybot.plugin.accounts.ApiConstants.ERROR_RATE_LIMIT
import com.sapuseven.sneakybot.plugin.accounts.models.ApiClient
import com.sapuseven.sneakybot.plugin.accounts.models.ApiError
import com.sapuseven.sneakybot.plugins.PluggableService
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.ConsoleCommand
import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.HttpResponseException
import io.javalin.http.util.RateLimiter
import io.javalin.plugin.json.JsonMapper
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.util.concurrent.TimeUnit

@Suppress("unused")
class ServiceApi : PluggableService {
	private lateinit var app: Javalin
	private val rateLimiter = RateLimiter(TimeUnit.MINUTES)

	override fun preInit(pluginManager: PluginManager) {
		val port = pluginManager.getConfiguration("PluginAccounts-Server").getInt("port", 9900)
		app = Javalin.create { config ->
			config.showJavalinBanner = false
			config.jsonMapper(kotlinxSerializationMapper)
		}.start(port)
	}

	override fun postInit(pluginManager: PluginManager) {
		app.get("/clients/<name>") { ctx ->
			if (rateLimit(ctx)) return@get

			var client: Client? = null

			try {
				client = pluginManager.api?.getClientByUId(ctx.pathParam("name"))
			} catch (_: TS3CommandFailedException) {
			}

			if (client == null) {
				try {
					client = pluginManager.api?.getClientByNameExact(ctx.pathParam("name"), true)
				} catch (_: TS3CommandFailedException) {
				}
			}

			if (client == null) {
				ctx.json(ApiError(ERROR_NOT_FOUND))
			} else {
				ctx.json(
					ApiClient(
						uuid = client.uniqueIdentifier,
						displayName = client.nickname,
						linkedAccounts = emptyList()
					)
				)
			}
		}
	}

	private fun rateLimit(ctx: Context): Boolean {
		return try {
			rateLimiter.incrementCounter(ctx, 5)
			false
		} catch (e: HttpResponseException) {
			ctx.json(ApiError(ERROR_RATE_LIMIT))
			true
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
