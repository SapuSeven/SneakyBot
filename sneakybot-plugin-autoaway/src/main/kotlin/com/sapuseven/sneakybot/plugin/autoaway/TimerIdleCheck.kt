package com.sapuseven.sneakybot.plugin.autoaway

import com.github.theholywaffle.teamspeak3.api.wrapper.Channel
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerQueryInfo
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.plugins.Timer
import org.slf4j.LoggerFactory

class TimerIdleCheck internal constructor(
    private val whoAmI: ServerQueryInfo,
    private val defaultChannel: Channel?,
    private val afkChannel: Channel?
) : Timer {
    companion object {
        private val log = LoggerFactory.getLogger(TimerIdleCheck::class.java)

        private const val IDLE_TIME_THRESHOLD = 10 * 60 * 1000 // 10 Minutes
    }

    override fun actionPerformed(pluginManager: PluginManager) {
        pluginManager.api?.let { api ->
            for (c in api.clients) {
                log.debug(c.nickname + " is idle for " + c.idleTime / 1000 + " seconds")

                if (c.idleTime > IDLE_TIME_THRESHOLD
                    && c.id != whoAmI.id
                    && c.channelId != afkChannel?.id ?: -1
                    && c.channelId != defaultChannel?.id ?: -1
                ) {
                    if (afkChannel == null)
                        api.kickClientFromChannel("You have been inactive for too long", c)
                    else {
                        api.sendPrivateMessage(c.id, "You have been inactive for too long")
                        api.moveClient(c.id, afkChannel.id)
                    }
                }
            }
        }
    }
}
