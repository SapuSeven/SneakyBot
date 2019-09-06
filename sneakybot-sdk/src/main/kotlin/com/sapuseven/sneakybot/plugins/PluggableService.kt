package com.sapuseven.sneakybot.plugins

import com.github.theholywaffle.teamspeak3.api.event.BaseEvent
import com.sapuseven.sneakybot.utils.ConsoleCommand

/**
 * The default structure for any service plugin.
 */
interface PluggableService {
    /**
     * This method is being executed before any other action happens.
     *
     * @param pluginManager The [PluginManager] of the main SneakyBOT instance.
     */
    @Throws(Exception::class)
    fun preInit(pluginManager: PluginManager)

    /**
     * This method is being executed after SneakyBOT set up and loaded all components.
     *
     * @param pluginManager The [PluginManager] of the main SneakyBOT instance.
     */
    fun postInit(pluginManager: PluginManager)

    /**
     * This method is being executed when a user issues the !kill / !stop command.
     *
     * @param pluginManager The [PluginManager] of the main SneakyBOT instance.
     */
    fun stop(pluginManager: PluginManager)

    /**
     * This method is being executed when any event occurs.
     */
    fun onEventReceived(e: BaseEvent)

    /**
     * This method is being executed when a user executes a command.
     */
    fun onCommandExecuted(cmd: ConsoleCommand, invokerId: Int)

    /**
     * Use ths method to obtain a [PluginManager] object which allows you to interact with the bot.
     *
     * @param pluginManager The [PluginManager] of the main SneakyBOT instance.
     */
    fun setPluginManager(pluginManager: PluginManager)
}
