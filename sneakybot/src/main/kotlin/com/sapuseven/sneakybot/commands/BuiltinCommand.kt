package com.sapuseven.sneakybot.commands

import com.sapuseven.sneakybot.plugins.PluggableCommand
import com.sapuseven.sneakybot.plugins.PluginManager

internal abstract class BuiltinCommand : PluggableCommand {
    override fun setPluginManager(pluginManager: PluginManager) {
        // the managers functionality is replaced with a direct reference to the bot
    }
}