package com.sapuseven.sneakybot.plugin.r6siege.models.v1

import kotlinx.serialization.Serializable

@Serializable
data class PlayerSearchResponseModel(
	val profiles: List<PlayerProfileModel>
)