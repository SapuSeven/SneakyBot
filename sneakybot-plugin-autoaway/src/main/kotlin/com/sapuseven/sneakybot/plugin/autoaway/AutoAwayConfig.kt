package com.sapuseven.sneakybot.plugin.autoaway

import com.github.theholywaffle.teamspeak3.api.wrapper.Channel

data class AutoAwayConfig(
    val afkChannel: Channel,
    val idleTimeThreshold: Int,
    val idleTimeMutedThreshold: Int,
    val idleTimeResponseThreshold: Int,
    val ignoreIfAway: Boolean,
    val excludedChannels: Set<Int>
)
