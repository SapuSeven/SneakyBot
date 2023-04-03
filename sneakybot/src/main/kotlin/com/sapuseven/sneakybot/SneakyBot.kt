package com.sapuseven.sneakybot

import com.github.theholywaffle.teamspeak3.TS3Config
import com.github.theholywaffle.teamspeak3.TS3Query
import com.github.theholywaffle.teamspeak3.api.PermissionGroupDatabaseType
import com.github.theholywaffle.teamspeak3.api.exception.TS3CommandFailedException
import com.github.theholywaffle.teamspeak3.api.exception.TS3ConnectionFailedException
import com.github.theholywaffle.teamspeak3.api.reconnect.ConnectionHandler
import com.github.theholywaffle.teamspeak3.api.reconnect.ReconnectStrategy
import com.github.theholywaffle.teamspeak3.api.wrapper.Client
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerQueryInfo
import com.sapuseven.sneakybot.commands.*
import com.sapuseven.sneakybot.exceptions.NoSuchClientException
import com.sapuseven.sneakybot.plugins.PluggableCommand
import com.sapuseven.sneakybot.plugins.PluggableService
import com.sapuseven.sneakybot.plugins.Timer
import com.sapuseven.sneakybot.services.ServiceChannelMode
import com.sapuseven.sneakybot.services.ServiceDirectMode
import com.sapuseven.sneakybot.utils.ConsoleCommand
import com.sapuseven.sneakybot.utils.EventListenerImplementation
import com.sapuseven.sneakybot.utils.PluginLoader
import com.sapuseven.sneakybot.utils.SneakyBotConfig
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import org.ini4j.IniPreferences
import org.ini4j.Wini
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.impl.SimpleLogger
import java.io.File
import java.io.FileOutputStream
import java.util.prefs.Preferences
import kotlin.system.exitProcess


fun main(args: Array<String>) = mainBody {
	val botConfig = ArgParser(args).parseInto(::SneakyBotConfig)
	System.setProperty(
		SimpleLogger.DEFAULT_LOG_LEVEL_KEY,
		if (botConfig.debug) "DEBUG" else "INFO"
	) // TODO: Allow the user to specify any log level

	val bot = SneakyBot(botConfig)

	Runtime.getRuntime().addShutdownHook(object : Thread() {
		override fun run() {
			try {
				bot.quit()
			} catch (e: Exception) {
				bot.logException(e, "Exception on shutdown")
				exitProcess(1)
			}
		}
	})

	try {
		bot.run()
	} catch (e: Exception) {
		bot.logException(e, "Exception during runtime")
		exitProcess(1)
	}
}

class SneakyBot(internal val botConfig: SneakyBotConfig) {
	internal lateinit var query: TS3Query
	internal var mode: Int = MODE_CHANNEL
	internal lateinit var whoAmI: ServerQueryInfo
	internal var serverGroupId: Int = -1
	internal val directClients = mutableSetOf<Int>()

	internal val builtinCommands = ArrayList<PluggableCommand>()
	internal val commands = ArrayList<PluggableCommand>()
	internal val builtinServices = ArrayList<PluggableService>()
	internal val services = ArrayList<PluggableService>()

	internal val timers = ArrayList<TimerThread>()
	internal lateinit var config: Wini
	internal lateinit var configPrefs: Preferences
	internal lateinit var manager: PluginManagerImpl

	companion object {
		const val MODE_DIRECT = 1
		const val MODE_CHANNEL = 2

		const val EXIT_CODE_COMMAND_ERROR = 1
		const val EXIT_CODE_CONNECTION_ERROR = 2

		val log: Logger = LoggerFactory.getLogger(SneakyBot::class.java)
	}

	fun run() {
		manager = PluginManagerImpl(this)

		loadBuiltinServices()
		loadBuiltinCommands()

		loadPlugins()
		preInit()

		try {
			query = connect(generateConfig())
		} catch (e: TS3ConnectionFailedException) {
			log.error("Connection failed: ${e.message}")
			exitProcess(EXIT_CODE_CONNECTION_ERROR)
		} catch (e: TS3CommandFailedException) {
			logException(e, "Login failed")
			exitProcess(EXIT_CODE_COMMAND_ERROR)
		}

		log.debug(
			"I will now notify all users (in the console channel / direct contacts) that I have fully loaded all my components."
		)
		if (mode == MODE_CHANNEL)
			sendChannelMessage("SneakyBot is now online.")
		else if (mode == MODE_DIRECT)
			sendDirectMessage("SneakyBot is now online. Please use the direct chat for commands.")

		query.api.addTS3Listeners(EventListenerImplementation(this))
	}

	private fun generateConfig(): TS3Config {
		val config = TS3Config()
		config.setHost(botConfig.host)
		config.setQueryPort(botConfig.port)
		config.setFloodRate(if (botConfig.unlimitedFloodRate) TS3Query.FloodRate.UNLIMITED else TS3Query.FloodRate.DEFAULT)
		config.setEnableCommunicationsLogging(botConfig.debug)

		config.setReconnectStrategy(ReconnectStrategy.exponentialBackoff())
		config.setConnectionHandler(object : ConnectionHandler {
			override fun onConnect(ts3Query: TS3Query) {
				query = ts3Query

				log.debug("Logging in...")
				query.api.login(botConfig.username, botConfig.password)

				log.debug("Selecting virtual server #${botConfig.virtualServerId}...")
				query.api.selectVirtualServerById(botConfig.virtualServerId)

				whoAmI = query.api.whoAmI()
				log.debug("Current nickname: ${whoAmI.nickname}")
				val newName = if (botConfig.debug) botConfig.nickname + "-Dev" else botConfig.nickname
				if (whoAmI.nickname != newName) {
					log.debug("Changing nickname to $newName")
					query.api.setNickname(newName)
				}

				log.info("Successfully connected and logged in.")

				val whoAmI = query.api.whoAmI()
				log.debug("My client id is ${whoAmI.id}.")

				serverGroupId = discoverServerGroup()
				postInit()

				log.debug("Registering all event listeners...")
				query.api.registerAllEvents()

				log.info("Startup done.")
			}

			override fun onDisconnect(ts3Query: TS3Query) {
				stopPlugins()
			}
		})

		return config
	}

	internal fun logException(e: Exception, msg: String = "") {
		e.message?.let { message ->
			if (message.indexOf('\n') != -1) {
				if (msg.isNotBlank())
					log.error("$msg: ${message.subSequence(0, message.indexOf('\n'))}")
				else
					log.error(message.subSequence(0, message.indexOf('\n')).toString())

				message.split('\n').drop(1).forEach {
					log.error(it)
				}
			} else {
				if (msg.isNotBlank())
					log.error("$msg: $message")
				else
					log.error(message)
			}
		} ?: run {
			log.error("$msg.")
		}
	}

	internal fun interpretCommand(command: String, invokerId: Int) {
		val cmd = ConsoleCommand(command)

		for (p in services + builtinServices)
			p.onCommandExecuted(cmd, invokerId)

		val pluggableCommand = getCommandByName(cmd.commandName)
		if (pluggableCommand == null) {
			manager.sendMessage("Sorry, I don't understand that!", invokerId)
		} else {
			if (!pluggableCommand.execute(cmd, invokerId))
				log.info("Command $cmd executed with errors.")
		}
	}

	internal fun preInit() {
		log.info("Loading configuration...")

		config = loadConfig()
		configPrefs = IniPreferences(config)

		log.info("Initializing plugins (Phase 1)...")

		for (p in services + builtinServices) {
			p.setPluginManager(manager)
			log.debug("PreInit: " + p.javaClass.simpleName)
			try {
				p.preInit(manager)
			} catch (e: java.lang.Exception) {
				logException(e, p.javaClass.simpleName + " threw an Exception during preInit()")
			}
		}
	}

	internal fun postInit() {
		log.info("Initializing plugins (Phase 2)...")

		for (p in services + builtinServices) {
			log.debug("PostInit: " + p.javaClass.simpleName)
			try {
				p.postInit(manager)
			} catch (e: java.lang.Exception) {
				logException(e, p.javaClass.simpleName + " threw an Exception during postInit()")
			}
		}

		for (t in timers) {
			if (!t.thread.isAlive)
				t.thread.start()
		}
	}

	private fun loadConfig(): Wini {
		val file = File(botConfig.config)

		if (!file.exists())
			FileOutputStream(file).close() // Create file

		return Wini(file)
	}

	private fun loadBuiltinServices() {
		builtinServices.clear()
		builtinServices.add(ServiceDirectMode(this))
		builtinServices.add(ServiceChannelMode(this))
	}

	private fun loadBuiltinCommands() {
		builtinCommands.clear()
		builtinCommands.add(CommandStop(this))
		builtinCommands.add(CommandMode(this))
		builtinCommands.add(CommandDirects(this))
		builtinCommands.add(CommandReload(this))
		builtinCommands.add(CommandHelp(this))
		builtinCommands.add(CommandTimers(this))
	}

	internal fun loadPlugins() {
		log.info("Loading plugins...")

		val pluginDir = File(botConfig.pluginDir)
		if (!pluginDir.exists()) {
			log.warn("Plugin directory not found!")
			return
		}

		commands.clear()
		commands.addAll(PluginLoader.loadPlugins(pluginDir, PluggableCommand::class.java))
		services.clear()
		services.addAll(PluginLoader.loadPlugins(pluginDir, PluggableService::class.java))

		for (p in commands)
			p.setPluginManager(manager)
	}

	internal fun stopPlugins() {
		for (p in services + builtinServices) {
			try {
				log.debug("Stopping: " + p.javaClass.simpleName)
				p.stop(manager)
			} catch (e: Exception) {
				log.error("Error stopping ${p.javaClass.simpleName}: $e")
			}
		}

		for (t in timers)
			t.thread.interrupt()

		timers.clear()
	}

	internal fun getCommandByName(name: String): PluggableCommand? {
		for (command in builtinCommands + commands)
			if (command.command.commandName == name)
				return command

		return null
	}

	private fun connect(config: TS3Config): TS3Query {
		log.info("Connecting...")
		return TS3Query(config).also(TS3Query::connect)
	}

	private fun discoverServerGroup(): Int {
		log.info("Searching for server group...")
		val name = botConfig.nickname
		var serverGroupId = query.api.serverGroups.find { it.name == name }?.id ?: -1

		if (serverGroupId == -1) {
			log.debug("Not found :(")
			log.info("Generating server group...")
			serverGroupId =
				query.api.copyServerGroup(
					botConfig.templateServerGroupId,
					"SneakyBot",
					PermissionGroupDatabaseType.REGULAR
				)
			log.debug("Finished, SneakyBot Server Group created.")
		} else {
			log.info("Server group found.")
		}

		return serverGroupId
	}

	internal fun quit() {
		log.info("Stopping plugins...")
		stopPlugins()

		if (this::config.isInitialized) {
			log.info("Saving configuration...")
			config.store()
		}

		if (::query.isInitialized && query.isConnected) {
			if (mode == MODE_DIRECT)
				sendDirectMessage("SneakyBot is now offline.")
			else
				sendChannelMessage("SneakyBot is now offline.")

			query.exit()
		}
	}

	internal fun sendChannelMessage(msg: String) {
		query.api.sendChannelMessage(msg)
	}

	internal fun sendDirectMessage(msg: String) {
		for (userId in directClients)
			query.api.sendPrivateMessage(userId, msg)
	}

	internal fun isCommand(msg: String): Boolean = msg.startsWith("!") && msg.length >= 2

	@Throws(NoSuchClientException::class)
	fun getClientById(clientId: Int): Client {
		for (c in query.api.clients)
			if (c.id == clientId)
				return c
		throw NoSuchClientException()
	}

	internal fun addTimer(timer: Timer, interval: Int, restartTimeout: Int) {
		timers.add(
			TimerThread(
				timer.javaClass.name,
				Thread {
					var running = true
					while (running) {
						try {
							timer.actionPerformed(manager)
							Thread.sleep((interval * 1000).toLong())
						} catch (e: InterruptedException) {
							log.debug("Timer stopped ({})", timer.javaClass)
							running = false
						} catch (e: Exception) {
							log.error("Timer threw an exception ({})", timer.javaClass, e)
							if (restartTimeout > 0)
								try {
									Thread.sleep((restartTimeout * 1000).toLong())
								} catch (e: InterruptedException) {
									log.debug("Timer stopped ({})", timer.javaClass)
									running = false
								}
							else
								running = false
						}
					}
				}
			)
		)
	}

	internal class TimerThread(val name: String, val thread: Thread)

	fun getConfig(name: String): Preferences {
		return configPrefs.node(name)
	}
}
