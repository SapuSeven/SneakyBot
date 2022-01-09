package com.sapuseven.sneakybot.plugin.r6siege.enums

enum class Platform(val platformName: String, val url: String, val spaceId: String) {
	UPLAY("uplay", "OSBOR_PC_LNCH_A", "5172a557-50b5-4665-b7db-e3f2e8c5041d"),
	PS4("psn", "OSBOR_PS4_LNCH_A", "05bfb3f7-6c21-4c42-be1f-97a33fb5cf66"),
	XBOX("xbl", "OSBOR_XBOXONE_LNCH_A", "98a601e5-ca91-4440-b1c5-753f601a2c90");

	companion object {
		fun getByName(name: String?): Platform? {
			for (platform in values()) {
				if (platform.name.equals(name, ignoreCase = true)) return platform
			}
			return null
		}
	}
}
