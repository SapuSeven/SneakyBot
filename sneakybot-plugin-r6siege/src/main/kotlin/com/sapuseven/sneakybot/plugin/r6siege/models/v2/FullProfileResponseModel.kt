package com.sapuseven.sneakybot.plugin.r6siege.models.v2

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FullProfileResponseModel(
	@SerialName("platform_families_full_profiles") val platformFamiliesFullProfiles: List<PlatformFamilyModel>
)
