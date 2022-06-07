package com.sapuseven.sneakybot.plugin.accounts.models

@kotlinx.serialization.Serializable
data class ApiClient(
	val uuid: String,
	val displayName: String,
	val linkedAccounts: List<ApiAccount>
)