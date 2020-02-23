package com.sapuseven.sneakybot.services

import com.sapuseven.sneakybot.plugins.PluggableService
import com.sapuseven.sneakybot.plugins.PluginManager

internal abstract class BuiltinService : PluggableService {
    override fun setPluginManager(pluginManager: PluginManager) {
        // the managers functionality is replaced with a direct reference to the bot
    }
}
