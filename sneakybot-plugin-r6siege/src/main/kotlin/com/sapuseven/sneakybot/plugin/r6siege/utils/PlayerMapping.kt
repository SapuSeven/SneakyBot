package com.sapuseven.sneakybot.plugin.r6siege.utils

object PlayerMapping {
	private val playerMap: MutableMap<String, String> = mutableMapOf()

	fun addPlayerMapping(teamspeakUidToUbisoftUid: Pair<String, String>) =
		playerMap.put(teamspeakUidToUbisoftUid.first, teamspeakUidToUbisoftUid.second)

	fun getUbisoftUidForTeamspeakUid(teamspeakUid: String) = playerMap[teamspeakUid]

	fun isTeamspeakUidMapped(teamspeakUid: String): Boolean = playerMap.contains(teamspeakUid)
}
