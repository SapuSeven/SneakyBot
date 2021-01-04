package com.sapuseven.sneakybot.plugins

import com.github.theholywaffle.teamspeak3.TS3Api
import com.sapuseven.sneakybot.exceptions.NoSuchClientException
import java.io.File
import java.util.prefs.Preferences

/**
 * The main PluginManager structure.
 */
interface PluginManager {

    /**
     * This method is used to obtain the SneakyBOTs main [TS3Api] object.
     *
     * @return A [TS3Api] object for accessing the TeamSpeak ServerQuery api.
     */
    val api: TS3Api?

    /**
     * Send a chat message to bot users.
     *
     * Channel or direct messages will be used according to the bot's current mode.
     *
     * @param msg The message to be sent.
     * @param clientId The target user ID.
     * Used to only target a single user when in direct mode or if [forcePrivateChat] is `true`.
     * If `null`, the message will be sent to every user that has control of the bot.
     * @param forcePrivateChat Set to `true` to send a private message, regardless of the current mode.
     */
    fun sendMessage(msg: String, clientId: Int? = null, forcePrivateChat: Boolean = false)

    /**
     * Use this method to access a file in the plugins managed data directory.
     *
     * @param name The name of the file.
     * @return A [TS3Api] object for accessing the TeamSpeak ServerQuery api.
     */
    fun getDataFile(name: String): File

    /**
     * Resolve a client id to the corresponding name.
     *
     * The client id can change as clients connect and disconnect!
     *
     * @param clientId The id of the client to find.
     * @return The clients name.
     * @throws NoSuchClientException When the client cannot be found.
     */
    @Throws(NoSuchClientException::class)
    fun getClientNameById(clientId: Int): String

    /**
     * Setup a timer.
     *
     * @param interval The interval in seconds in which the timers action should be executed
     */
    fun addTimer(timer: Timer, interval: Int, restartTimeout: Int = 0)

    fun getConfiguration(name: String): Preferences
}
