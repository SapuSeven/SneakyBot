package com.sapuseven.sneakybot.plugin.r6siege.utils

import com.github.kittinunf.fuel.serialization.responseObject
import com.github.kittinunf.result.Result
import com.sapuseven.sneakybot.plugin.r6siege.enums.Platform
import com.sapuseven.sneakybot.plugin.r6siege.enums.Rank
import com.sapuseven.sneakybot.plugin.r6siege.enums.Region
import com.sapuseven.sneakybot.plugin.r6siege.models.v1.PlayerProfileModel
import com.sapuseven.sneakybot.plugin.r6siege.models.v1.PlayerSearchResponseModel
import com.sapuseven.sneakybot.plugin.r6siege.models.v2.FullProfileResponseModel
import com.sapuseven.sneakybot.plugin.r6siege.utils.AuthContext.authenticatedGet
import java.util.*

class Api {
	constructor(authToken: String) {
		AuthContext.saveAuthToken(authToken)
	}

	constructor(email: String, password: String) : this(
		Base64.getEncoder().encodeToString("$email:$password".toByteArray())
	)

	fun searchPlayersByUsername(username: String, platform: Platform): List<PlayerProfileModel> {
		val (_, _, result) = authenticatedGet("https://public-ubiservices.ubi.com/v3/profiles?nameOnPlatform=${username}&platformType=${platform.platformName}")
			.responseObject<PlayerSearchResponseModel>()

		when (result) {
			is Result.Failure -> {
				throw result.getException()
			}
			is Result.Success -> {
				return result.get().profiles
			}
		}
	}

	fun loadPlayerRanks(uids: List<String>, platform: Platform, region: Region, season: Int = -1): Map<String, Rank> {
		if (uids.isEmpty()) return emptyMap()

		val (_, _, result) = authenticatedGet(
			"https://public-ubiservices.ubi.com/v2/spaces/${platform.spaceId}/title/r6s/skill/full_profiles?platform_families=pc&profile_ids=${
				uids.joinToString(
					","
				)
			}"
		).responseObject<FullProfileResponseModel>()

		when (result) {
			is Result.Failure -> {
				throw result.getException()
			}
			is Result.Success -> {
				val data = result.get()
				return data.platformFamiliesFullProfiles.flatMap { platformFamily ->
					platformFamily.boardIdsFullProfiles.flatMap { boardId ->
						boardId.fullProfiles
					}
				}.associate { fullProfile ->
					fullProfile.profile.id to Rank.values()[fullProfile.profile.rank]
				}
			}
		}
	}
}
