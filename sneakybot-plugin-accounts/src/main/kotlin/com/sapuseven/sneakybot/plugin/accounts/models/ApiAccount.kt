package com.sapuseven.sneakybot.plugin.accounts.models

@kotlinx.serialization.Serializable
data class ApiAccount(
	val platform: String,
	val id: String,
	val code: String? = null
)