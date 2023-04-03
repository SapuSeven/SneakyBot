package com.sapuseven.sneakybot.plugin.r6siege.models.v2

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BoardIdModel(
	@SerialName("board_id") val boardId: String,
	@SerialName("full_profiles") val fullProfiles: List<FullProfileModel>
)
