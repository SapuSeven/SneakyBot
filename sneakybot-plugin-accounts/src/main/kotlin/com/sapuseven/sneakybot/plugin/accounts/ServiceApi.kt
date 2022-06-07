package com.sapuseven.sneakybot.plugin.accounts

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.theholywaffle.teamspeak3.api.event.BaseEvent
import com.github.theholywaffle.teamspeak3.api.exception.TS3CommandFailedException
import com.github.theholywaffle.teamspeak3.api.wrapper.Client
import com.sapuseven.sneakybot.plugin.accounts.ApiConstants.ERROR_API_UNREACHABLE
import com.sapuseven.sneakybot.plugin.accounts.ApiConstants.ERROR_NOT_FOUND
import com.sapuseven.sneakybot.plugin.accounts.ApiConstants.ERROR_RATE_LIMIT
import com.sapuseven.sneakybot.plugin.accounts.ApiConstants.ERROR_UNKNOWN
import com.sapuseven.sneakybot.plugin.accounts.ApiConstants.PLATFORM_STEAM
import com.sapuseven.sneakybot.plugin.accounts.ApiConstants.PLATFORM_TS
import com.sapuseven.sneakybot.plugin.accounts.models.ApiAccount
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
	private lateinit var steamApiRoot: String
	private val steamApiSearch = "/steam/search/"
	private val steamApiInvite = "/steam/message/"
	private val rateLimiter = RateLimiter(TimeUnit.MINUTES)
	private val accounts = AccountStorage()

	override fun preInit(pluginManager: PluginManager) {
		pluginManager.getConfiguration("PluginAccounts-SteamApi").let { config ->
			steamApiRoot = "http://" + config.get("host", "localhost") + ":" + config.getInt("port", 7300)
		}
		pluginManager.getConfiguration("PluginAccounts-Server").let { config ->
			val port = config.getInt("port", 9900)
			app = Javalin.create { cfg ->
				cfg.showJavalinBanner = false
				cfg.jsonMapper(kotlinxSerializationMapper)
			}.start(port)
		}
	}

	override fun postInit(pluginManager: PluginManager) {
		app.get("/search/<name>") { ctx ->
			if (rateLimit(ctx)) return@get

			when (ctx.queryParam("platform")) {
				"steam" -> {
					try {
						val (_, _, result) = Fuel.get(steamApiRoot + steamApiSearch + ctx.pathParam("name"))
							.responseString()

						if (result.get().isBlank()) {
							ctx.json(ApiError(ERROR_NOT_FOUND))
						} else {
							ctx.json(
								ApiAccount(
									id = result.get(),
									platform = PLATFORM_STEAM
								)
							)
						}
					} catch (e: FuelError) {
						ctx.json(ApiError(ERROR_API_UNREACHABLE))
					}
				}
				else -> {
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
							ApiAccount(
								id = client.uniqueIdentifier,
								platform = PLATFORM_TS
							)
						)
					}
				}
			}
		}

		app.get("/invite/<id>") { ctx ->
			if (rateLimit(ctx)) return@get

			when (ctx.queryParam("platform")) {
				"steam" -> {
					val code = accounts[ctx.pathParam("id")]?.generateCode()

					val (_, _, _) = Fuel.post(steamApiRoot + steamApiInvite + ctx.pathParam("id"))
						.body("Hi there! Please use the following code to verify your Steam account: $code")
						.response()
				}
				else -> try {
					pluginManager.api?.let { api ->
						val client = api.getClientByUId(ctx.pathParam("id"))
						val code = accounts[client.uniqueIdentifier]?.generateCode()

						if (code == null) {
							ctx.json(ApiError(ERROR_UNKNOWN))
							return@get
						}

						api.sendPrivateMessage(
							client.id,
							"Hi there! Please use the following code to verify your TeamSpeak account: $code"
						)
					}
				} catch (e: TS3CommandFailedException) {
					ctx.json(ApiError(ERROR_NOT_FOUND))
				}
			}
		}

		app.post("/link/<uuid>") { ctx ->
			if (rateLimit(ctx)) return@post

			try {
				pluginManager.api?.let { api ->
					val client = api.getClientByUId(ctx.pathParam("uuid"))
					val code = accounts[client.uniqueIdentifier]?.generateCode()

					if (code == null) {
						ctx.json(ApiError(ERROR_UNKNOWN))
						return@post
					}

					api.sendPrivateMessage(
						client.id,
						"Hi there! Please use the following code to verify your TeamSpeak account: $code"
					)
				}
			} catch (e: TS3CommandFailedException) {
				ctx.json(ApiError(ERROR_NOT_FOUND))
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
