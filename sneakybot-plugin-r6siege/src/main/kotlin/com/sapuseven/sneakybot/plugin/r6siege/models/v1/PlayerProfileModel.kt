package com.sapuseven.sneakybot.plugin.r6siege.models.v1

import kotlinx.serialization.Serializable

@Serializable
data class PlayerProfileModel(
	val idOnPlatform: String,
	val nameOnPlatform: String,
	val platformType: String,
	val profileId: String,
	val userId: String
)