package com.sapuseven.sneakybot

import com.github.theholywaffle.teamspeak3.TS3Api
import com.github.theholywaffle.teamspeak3.api.wrapper.Client
import com.sapuseven.sneakybot.exceptions.NoSuchClientException
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.plugins.Timer
import org.slf4j.LoggerFactory
import java.io.File
import java.util.prefs.Preferences
import kotlin.math.min

class PluginManagerImpl internal constructor(private val bot: SneakyBot) : PluginManager {
    override val api: TS3Api?
        get() = bot.query.api
    private val log = LoggerFactory.getLogger(PluginManagerImpl::class.java)

    override fun sendMessage(msg: String, clientId: Int?) {
        api?.let { api ->
            val msgParts = ArrayList<String>()

            var i = 0
            var index: Int
            while (i < msg.length) {
                index = 510
                var part = msg.substring(i, min(i + index, msg.length))
                if (part.length == 510) {
                    index = part.lastIndexOf('\n')
                    if (index != -1)
                        part = msg.substring(i, i + index)
                    else
                        index = 510
                }

                msgParts.add(part)
                i += index
            }

            if (bot.mode == SneakyBot.MODE_DIRECT)
                for (msgPart in msgParts) {
                    if (clientId != null)
                        api.sendPrivateMessage(clientId, msgPart)
                    else
                        bot.sendDirectMessage(msgPart)
                }
            else if (bot.mode == SneakyBot.MODE_CHANNEL)
                for (msgPart in msgParts)
                    api.sendChannelMessage(msgPart)
        }
    }

    override fun getDataFile(name: String): File {
        val f = File(bot.botConfig.dataDir + "/" + Exception().stackTrace[1].className, name)
        log.debug("Data file full path: " + f.absolutePath)
        f.parentFile.mkdirs()
        return f
    }

    @Throws(NoSuchClientException::class)
    override fun getClientById(clientId: Int): Client = bot.getClientById(clientId)

    @Throws(NoSuchClientException::class)
    override fun getClientNameById(clientId: Int): String = bot.getClientById(clientId).nickname

    override fun addTimer(timer: Timer, interval: Int, restartTimeout: Int) =
        bot.addTimer(timer, interval, restartTimeout)

    override fun getConfiguration(name: String): Preferences = bot.getConfig(name)
}
