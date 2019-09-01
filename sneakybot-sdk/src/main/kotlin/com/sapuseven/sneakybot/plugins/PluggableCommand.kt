package com.sapuseven.sneakybot.plugins

import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand

/**
 * The default structure for any command plugin.
 */
interface PluggableCommand {
    /**
     * Override this method and return a custom [Command] object representing the implemented command.<br></br>
     * <br></br>
     *
     * Example:
     *
     * <pre>
     * [Command] cmd = new [Command]();
     * cmd.setCommandName("myCommand");
     * cmd.setHelp("A description for your custom command.");
     * return cmd;
    </pre> *
     *
     * @return Your custom [Command] object.
     */
    val command: Command

    /**
     * This method is being executed when the command is issued at the TeamSpeak server.
     *
     * @param cmd A [ConsoleCommand] object representing the issued command, including all parameters.
     * You have to validate the prameters by yourself.
     * @param invokerId UserID of the command issuer.
     * @return `true` if the command was executed without any errors, `false` otherwise.
     */
    fun execute(cmd: ConsoleCommand, invokerId: Int): Boolean

    /**
     * Use ths method to obtain a [PluginManager] object which allows you to interact with the bot.
     *
     * @param pluginManager The [PluginManager] of the main SneakyBOT instance.
     */
    fun setPluginManager(pluginManager: PluginManager)
}
