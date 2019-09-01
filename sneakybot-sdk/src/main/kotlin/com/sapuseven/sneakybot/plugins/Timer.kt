package com.sapuseven.sneakybot.plugins

/**
 * A Timer will automatically be started after the *postInit* phase (phase 2) and stopped after the *stop*
 * phase.
 */
interface Timer {
    fun actionPerformed(pluginManager: PluginManager)
}
