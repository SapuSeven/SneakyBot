package com.sapuseven.sneakybot.plugin.r6siege.models.v1

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponseModel(
	val clientIp: String,
	val clientIpCountry: String,
	val environment: String,
	val expiration: String,
	val nameOnPlatform: String,
	val platformType: String,
	val profileId: String,
	val rememberMeTicket: String,
	val serverTime: String,
	val sessionId: String,
	val sessionKey: String,
	val spaceId: String,
	val ticket: String,
	val twoFactorAuthenticationTicket: String?,
	val userId: String
)
