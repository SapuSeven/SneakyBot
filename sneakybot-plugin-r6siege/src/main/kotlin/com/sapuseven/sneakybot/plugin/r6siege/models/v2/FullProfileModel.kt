package com.sapuseven.sneakybot.plugin.r6siege.models.v2

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FullProfileModel(
	@SerialName("profile") val profile: ProfileModel,
	@SerialName("season_statistics") val seasonStatistics: SeasonStatisticsModel
)
