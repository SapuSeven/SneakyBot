package com.sapuseven.sneakybot.plugin.r6siege.models.v2

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SeasonStatisticsModel(
	@SerialName("deaths") val deaths: Int,
	@SerialName("kills") val kills: Int,
	@SerialName("match_outcomes") val matchOutcomes: Map<String, Int>
)
