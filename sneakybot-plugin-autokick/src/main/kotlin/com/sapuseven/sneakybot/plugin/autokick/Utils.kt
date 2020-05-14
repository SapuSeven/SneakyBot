package com.sapuseven.sneakybot.plugin.autokick

import com.sapuseven.sneakybot.plugins.PluginManager

object Utils {
	fun kickClones(manager: PluginManager) {
		manager.api?.let { api ->
			val clientsByUid = api.clients.groupBy { it.uniqueIdentifier }.toMutableMap()
			clientsByUid.remove(api.whoAmI().uniqueIdentifier)
			clientsByUid.values.forEach { clients ->
				if (clients.size > 1) {
					clients.sortedBy { it.id }.drop(1).forEach {
						api.kickClientFromServer(it)
					}
				}
			}
		}
	}
}
