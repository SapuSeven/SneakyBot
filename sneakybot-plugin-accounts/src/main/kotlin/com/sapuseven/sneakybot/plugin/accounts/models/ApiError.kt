package com.sapuseven.sneakybot.plugin.accounts.models

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
	val error: String
)