package com.sapuseven.sneakybot.plugin.r6siege.models.v2

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileModel(
	@SerialName("board_id") val boardId: String,
	@SerialName("id") val id: String,
	@SerialName("max_rank") val maxRank: Int,
	@SerialName("max_rank_points") val maxRankPoints: Int,
	@SerialName("platform_family") val platformFamily: String,
	@SerialName("rank") val rank: Int,
	@SerialName("rank_points") val rankPoints: Int,
	@SerialName("season_id") val seasonId: Int,
	@SerialName("top_rank_position") val topRankPosition: Int
)
