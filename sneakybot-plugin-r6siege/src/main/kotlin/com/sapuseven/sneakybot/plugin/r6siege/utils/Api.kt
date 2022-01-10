package com.sapuseven.sneakybot.plugin.r6siege.utils

import com.github.kittinunf.fuel.serialization.responseObject
import com.github.kittinunf.result.Result
import com.sapuseven.sneakybot.plugin.r6siege.enums.Platform
import com.sapuseven.sneakybot.plugin.r6siege.enums.Rank
import com.sapuseven.sneakybot.plugin.r6siege.enums.Region
import com.sapuseven.sneakybot.plugin.r6siege.models.PlayerInfoModel
import com.sapuseven.sneakybot.plugin.r6siege.models.PlayerProfileModel
import com.sapuseven.sneakybot.plugin.r6siege.models.PlayerSearchResponseModel
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
			"https://public-ubiservices.ubi.com/v1/spaces/${platform.spaceId}/sandboxes/${platform.url}/r6karma/players?board_id=pvp_ranked&profile_ids=${
				uids.joinToString(
					","
				)
			}&region_id=${region.regionName}&season_id=$season"
		)
			.responseObject<PlayerInfoModel>()

		when (result) {
			is Result.Failure -> {
				throw result.getException()
			}
			is Result.Success -> {
				val data = result.get()
				return data.players.mapValues { player -> Rank.values()[player.value.rank] }
			}
		}
	}
}
