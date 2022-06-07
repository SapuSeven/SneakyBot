package com.sapuseven.sneakybot.plugin.accounts

class AccountStorage {
	private val accounts: MutableMap<String, Account> = mutableMapOf()

	operator fun get(id: String): Account? {
		if (!accounts.containsKey(id))
			accounts[id] = Account()

		return accounts[id]
	}

	fun has(id: String): Boolean {
		return accounts.containsKey(id)
	}
}