package com.sapuseven.sneakybot.plugin.r6siege.models

import kotlinx.serialization.Serializable

@Serializable
data class PlayerInfoModel(
	val players: Map<String, PlayerInfoPvpRankedModel>
)