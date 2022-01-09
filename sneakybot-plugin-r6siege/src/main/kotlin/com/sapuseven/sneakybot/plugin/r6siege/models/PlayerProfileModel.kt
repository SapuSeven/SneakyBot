package com.sapuseven.sneakybot.plugin.r6siege.models

import kotlinx.serialization.Serializable

@Serializable
data class PlayerProfileModel(
	val idOnPlatform: String,
	val nameOnPlatform: String,
	val platformType: String,
	val profileId: String,
	val userId: String
)