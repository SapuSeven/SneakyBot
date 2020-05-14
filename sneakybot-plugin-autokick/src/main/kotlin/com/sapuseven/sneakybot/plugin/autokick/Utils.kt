package com.sapuseven.sneakybot.plugin.autokick

import com.github.theholywaffle.teamspeak3.TS3Api
import com.github.theholywaffle.teamspeak3.api.wrapper.Client
import com.sapuseven.sneakybot.plugins.PluginManager

object Utils {
	fun kickClones(manager: PluginManager) {
		manager.api?.let { api ->
			api.clients.removeSelf(api).groupBy { it.ip }.values.forEach { clients ->
				if (clients.size > 1) {
					clients.sortedBy { it.id }.drop(1).forEach {
						api.kickClientFromServer(it)
					}
				}
			}
		}
	}
}

private fun List<Client>.removeSelf(api: TS3Api): List<Client> {
	toMutableList().removeAll { it.id == api.whoAmI().id }
	return this
}
