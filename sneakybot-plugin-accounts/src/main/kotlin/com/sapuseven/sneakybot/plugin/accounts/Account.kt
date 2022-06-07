package com.sapuseven.sneakybot.plugin.accounts

import java.util.*

class Account {
	private val codeCharPool = "bcdefhjkmnprtvwxy2345689"
	private var code: String? = null

	fun generateCode(): String {
		val generated = StringBuilder()
		val random = Random()

		repeat(4) {
			generated.append(codeCharPool[random.nextInt(codeCharPool.length)])
		}
		generated.append('-')
		repeat(4) {
			generated.append(codeCharPool[random.nextInt(codeCharPool.length)])
		}

		code = generated.toString()
		return code as String
	}
}
