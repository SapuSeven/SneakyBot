package com.sapuseven.sneakybot.plugin.r6siege.models.v2

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlatformFamilyModel(
	@SerialName("board_ids_full_profiles") val boardIdsFullProfiles: List<BoardIdModel>,
	@SerialName("platform_family") val platformFamily: String
)
