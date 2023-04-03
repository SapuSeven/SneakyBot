package com.sapuseven.sneakybot.plugin.r6siege.utils

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.serialization.responseObject
import com.github.kittinunf.result.Result
import com.sapuseven.sneakybot.plugin.r6siege.models.v1.AuthResponseModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.time.Instant

object AuthContext {
	private var savedAuthToken: String = ""
	private var key: String = ""
	private var sessionId: String = ""
	private var spaceId: String = ""
	private var expiration: Instant? = null

	private val EXPIRATION_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'")
	private const val RAINBOW_SIX_APPID = "e3d5ea9e-50bd-43b7-88bf-39794f4e3d40"

	internal fun saveAuthToken(authToken: String) {
		savedAuthToken = authToken
	}

	private fun authenticate(authToken: String = savedAuthToken) {
		savedAuthToken = authToken

		val (_, _, result) = Fuel.post("https://public-ubiservices.ubi.com/v3/profiles/sessions")
			.header(
				mapOf(
					"Ubi-AppId" to RAINBOW_SIX_APPID,
					"Authorization" to "Basic $authToken"
				)
			)
			.jsonBody(Json.encodeToString(mapOf("rememberMe" to true)))
			.responseObject<AuthResponseModel>()

		when (result) {
			is Result.Failure -> {
				throw result.getException()
			}
			is Result.Success -> {
				val data = result.get()
				key = data.ticket
				sessionId = data.sessionId
				spaceId = data.spaceId
				expiration = EXPIRATION_FORMAT.parse(data.expiration, ParsePosition(0)).toInstant()
			}
		}
	}

	fun authenticatedGet(path: String): Request {
		if (expiration?.isAfter(Instant.now()) != true) // If expiration is null or before now: authenticate
			authenticate()

		return Fuel.get(path)
			.header(
				mapOf(
					"Authorization" to "Ubi_v1 t=$key",
					"Ubi-AppId" to RAINBOW_SIX_APPID,
					"Ubi-SessionId" to sessionId,
					"Connection" to "keep-alive"
				)
			)
	}
}