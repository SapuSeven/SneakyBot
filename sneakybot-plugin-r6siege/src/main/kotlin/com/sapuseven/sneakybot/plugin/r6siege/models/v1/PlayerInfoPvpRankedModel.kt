package com.sapuseven.sneakybot.plugin.r6siege.models.v1

import kotlinx.serialization.Serializable

@Serializable
data class PlayerInfoPvpRankedModel(
	val abandons: Int,
	val board_id: String,
	val deaths: Int,
	val kills: Int,
	val last_match_mmr_change: Double,
	val last_match_result: Int,
	val last_match_skill_mean_change: Double,
	val last_match_skill_stdev_change: Double,
	val losses: Int,
	val max_mmr: Double,
	val max_rank: Int,
	val mmr: Double,
	val next_rank_mmr: Double,
	val past_seasons_abandons: Int,
	val past_seasons_losses: Int,
	val past_seasons_wins: Int,
	val previous_rank_mmr: Double,
	val profile_id: String,
	val rank: Int,
	val region: String,
	val season: Int,
	val skill_mean: Double,
	val skill_stdev: Double,
	val top_rank_position: Int,
	val update_time: String,
	val wins: Int
)