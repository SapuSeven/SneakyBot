package com.sapuseven.sneakybot

import com.github.theholywaffle.teamspeak3.TS3Config
import com.github.theholywaffle.teamspeak3.TS3Query
import com.github.theholywaffle.teamspeak3.api.ChannelProperty
import com.github.theholywaffle.teamspeak3.api.PermissionGroupDatabaseType
import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode
import com.github.theholywaffle.teamspeak3.api.event.*
import com.github.theholywaffle.teamspeak3.api.exception.TS3CommandFailedException
import com.github.theholywaffle.teamspeak3.api.exception.TS3ConnectionFailedException
import com.github.theholywaffle.teamspeak3.api.wrapper.Client
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerQueryInfo
import com.sapuseven.sneakybot.commands.*
import com.sapuseven.sneakybot.exceptions.NoSuchClientException
import com.sapuseven.sneakybot.plugins.PluggableCommand
import com.sapuseven.sneakybot.plugins.PluggableService
import com.sapuseven.sneakybot.plugins.PluginLoader
import com.sapuseven.sneakybot.plugins.Timer
import com.sapuseven.sneakybot.utils.ConsoleCommand
import com.sapuseven.sneakybot.utils.SneakyBotConfig
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import org.slf4j.LoggerFactory
import java.io.File
import java.net.MalformedURLException
import java.util.*
import kotlin.collections.set
import kotlin.system.exitProcess

fun main(args: Array<String>) = mainBody {
    val botConfig = ArgParser(args).parseInto(::SneakyBotConfig)
    System.setProperty(
        org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY,
        if (botConfig.debug) "DEBUG" else "INFO"
    ) // TODO: Allow the user to specify any log level
    SneakyBot(botConfig).run()
}

class SneakyBot(internal val botConfig: SneakyBotConfig) {
    internal lateinit var query: TS3Query
    internal var mode: Int = MODE_CHANNEL
    private lateinit var whoAmI: ServerQueryInfo
    private var consoleChannelId: Int = -1
    internal var serverGroupId: Int = -1
    internal val directClients = ArrayList<Int>()
    private val timers = ArrayList<Thread>()
    internal val builtinCommands = ArrayList<PluggableCommand>()
    internal val commands = ArrayList<PluggableCommand>()
    internal val services = ArrayList<PluggableService>()
    lateinit var manager: PluginManagerImpl

    private val log = LoggerFactory.getLogger(SneakyBot::class.java)

    companion object {
        const val MODE_DIRECT = 0x0001
        const val MODE_CHANNEL = 0x0002
    }

    fun run() {
        loadBuiltinCommands()

        log.info("Initializing plugins (Phase 1)...")
        loadPlugins()
        preInit()

        try {
            query = connect(generateConfig())
        } catch (e: TS3ConnectionFailedException) {
            log.error("Connection failed: ${e.message}")
            exitProcess(1)
        } catch (e: TS3CommandFailedException) {
            logCommandFailed(e, "Login failed")
            exitProcess(1)
        } catch (e: Exception) {
            log.error("Unknown error: ${e.message}")
            exitProcess(1)
        }

        consoleChannelId = getConsoleChannel()

        if (consoleChannelId == -1)
            consoleChannelId = createConsoleChannel()

        val whoAmI = query.api.whoAmI()
        log.debug("My client id is ${whoAmI.id}.")
        if (whoAmI.channelId != consoleChannelId) {
            log.debug("I am now moving to the console channel.")
            query.api.moveQuery(consoleChannelId)
        }

        serverGroupId = discoverServerGroup()

        // Look for clients in my server group; If there are any, switch to direct mode
        for (serverGroupClient in query.api.getServerGroupClients(serverGroupId)) {
            if (serverGroupClient.nickname != botConfig.username) {
                for (onlineClient in query.api.clients) {
                    if (onlineClient.nickname != botConfig.username && onlineClient.uniqueIdentifier == serverGroupClient.uniqueIdentifier) {
                        directClients.add(onlineClient.id)
                        mode = MODE_DIRECT
                    }
                }
            }
        }

        log.debug("Registering all event listeners...")
        query.api.registerAllEvents()

        log.info("Initializing plugins (Phase 2)...")
        postInit()

        log.info("Startup done.")
        log.debug(
            "I will now notify all users (in the console channel / direct contacts) that I have fully loaded all my components."
        )
        if (mode == MODE_CHANNEL)
            sendChannelMessage("SneakyBOT is now online.")
        else if (mode == MODE_DIRECT)
            sendDirectMessage("SneakyBOT is now online. Please use the direct chat for commands.")

        setupListeners()
    }

    private fun setupListeners() {
        query.api.addTS3Listeners(object : TS3Listener {
            override fun onTextMessage(e: TextMessageEvent) {
                interpretTextMessage(e)

                for (p in services)
                    p.onEventReceived(e)
            }

            override fun onServerEdit(e: ServerEditedEvent) {
                for (p in services)
                    p.onEventReceived(e)
            }

            override fun onClientMoved(e: ClientMovedEvent) {
                interpretClientMoved(e)

                for (p in services)
                    p.onEventReceived(e)
            }

            override fun onClientLeave(e: ClientLeaveEvent) {
                for (p in services)
                    p.onEventReceived(e)
            }

            override fun onClientJoin(e: ClientJoinEvent) {
                for (p in services)
                    p.onEventReceived(e)
            }

            override fun onChannelEdit(e: ChannelEditedEvent) {
                for (p in services)
                    p.onEventReceived(e)
            }

            override fun onChannelDescriptionChanged(e: ChannelDescriptionEditedEvent) {
                for (p in services)
                    p.onEventReceived(e)
            }

            override fun onChannelCreate(e: ChannelCreateEvent) {
                for (p in services)
                    p.onEventReceived(e)
            }

            override fun onChannelDeleted(e: ChannelDeletedEvent) {
                getConsoleChannel()

                for (p in services)
                    p.onEventReceived(e)
            }

            override fun onChannelMoved(e: ChannelMovedEvent) {
                for (p in services)
                    p.onEventReceived(e)
            }

            override fun onChannelPasswordChanged(e: ChannelPasswordChangedEvent) {
                for (p in services)
                    p.onEventReceived(e)
            }

            override fun onPrivilegeKeyUsed(e: PrivilegeKeyUsedEvent) {
                for (p in services)
                    p.onEventReceived(e)
            }
        })
    }

    private fun interpretClientMoved(event: ClientMovedEvent) {
        if (event.targetChannelId == consoleChannelId) {
            val name = query.api.getClientInfo(event.clientId).nickname
            log.info("User $name entered the SneakyBOT Console channel.")
            if (mode == MODE_CHANNEL)
                manager.sendMessage(
                    "\nWelcome, " + name + ", to the SneakyBOT Console!\n" +
                            "If you are new, '!help' is usually a good command for getting started.", event.clientId
                )
        }
    }

    private fun interpretTextMessage(event: TextMessageEvent) {
        if (isCommand(event.message) && event.invokerId != whoAmI.id) {
            if (event.targetMode != TextMessageTargetMode.SERVER) {
                if (mode == MODE_DIRECT) {
                    if (event.targetMode == TextMessageTargetMode.CLIENT) {
                        if (directClients.contains(event.invokerId)) {
                            log.info("User ${event.invokerName} executed a command via DIRECT chat: ${event.message}")
                            interpretCommand(event.message, event.invokerId)
                        } else {
                            log.info("User ${event.invokerName} tried to execute a command, but has no permissions to use DIRECT chat: " + event.message)
                            query.api.sendPrivateMessage(
                                event.invokerId,
                                "You are not allowed to give me commands!"
                            )
                        }
                    } else {
                        log.info("User ${event.invokerName} tried to execute a command, but used CONSOLE chat instead of DIRECT chat: " + event.message)
                        sendChannelMessage("Please use the direct chat to communicate with me.")
                    }
                } else if (mode == MODE_CHANNEL) {
                    if (event.targetMode == TextMessageTargetMode.CHANNEL) {
                        log.info("User ${event.invokerName} executed a command via CONSOLE chat: " + event.message)
                        interpretCommand(event.message, event.invokerId)
                    } else if (event.targetMode == TextMessageTargetMode.CLIENT) {
                        if (event.message == "!mode direct ${botConfig.consolePassword}") {
                            setupMode(MODE_DIRECT)
                            directClients.add(event.invokerId)
                            query.api.addClientToServerGroup(
                                serverGroupId,
                                query.api.getClientInfo(event.invokerId).databaseId
                            )
                            log.info("User ${event.invokerName} regained access to the DIRECT chat.")
                            query.api.sendPrivateMessage(
                                event.invokerId,
                                "You are now allowed to give me commands."
                            )
                        }
                    } else {
                        log.info("User ${event.invokerName} tried to execute a command, but didn't use CONSOLE chat: " + event.message)
                        query.api.sendPrivateMessage(
                            event.invokerId,
                            "Please use the SneakyBOT Console channel to communicate with me."
                        )
                    }
                }
            } else {
                /*if (event.message != "!call") {
                    if (mode != MODE_DIRECT)
                        setupMode(MODE_DIRECT)
                    else
                        manager.sendMessage("Mode is already set to DIRECT!", event.invokerId)
                }*/
                log.info("User " + event.invokerName + " tried to execute a command via SERVER chat: " + event.message)
                query.api.sendServerMessage("Sorry, I can only accept commands from the SneakyBot Console Channel!")
            }
        }
    }

    private fun generateConfig(): TS3Config {
        val config = TS3Config()
        config.setHost(botConfig.host)
        config.setQueryPort(botConfig.port)
        config.setFloodRate(if (botConfig.unlimitedFloodRate) TS3Query.FloodRate.UNLIMITED else TS3Query.FloodRate.DEFAULT)
        config.setEnableCommunicationsLogging(botConfig.debug)
        return config
    }

    private fun logCommandFailed(e: TS3CommandFailedException, msg: String = "") {
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

    private fun interpretCommand(command: String, invokerId: Int) {
        val cmd = ConsoleCommand(command)

        for (p in services)
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
        for (p in services) {
            p.setPluginManager(manager)
            log.debug("PreInit: " + p.javaClass.simpleName)
            p.preInit(manager)
        }
    }

    internal fun postInit() {
        for (p in services) {
            log.debug("PostInit: " + p.javaClass.simpleName)
            p.postInit(manager)
        }

        for (t in timers) {
            if (!t.isAlive)
                t.start()
        }
    }

    private fun loadBuiltinCommands() {
        builtinCommands.clear()
        builtinCommands.add(CommandStop(this))
        builtinCommands.add(CommandMode(this))
        builtinCommands.add(CommandDirects(this))
        builtinCommands.add(CommandReload(this))
        builtinCommands.add(CommandHelp(this))
        // TODO: Add builtin commands
    }

    internal fun loadPlugins() {
        try {
            commands.clear()
            commands.addAll(PluginLoader.loadCommands(File(botConfig.pluginDir)))
            services.clear()
            services.addAll(PluginLoader.loadServices(File(botConfig.pluginDir)))
        } catch (e: MalformedURLException) {
            log.warn("Plugin directory not found!")
        } catch (e: NullPointerException) {
            log.warn("Plugin directory not found!")
        } finally {
            manager = PluginManagerImpl(this)
        }

        for (p in commands) {
            p.setPluginManager(manager)
        }
    }

    internal fun stopPlugins() {
        for (p in services) {
            log.debug("Stopping: " + p.javaClass.simpleName)
            p.stop(manager)
        }

        for (t in timers) {
            t.interrupt()
        }

        timers.clear()
    }

    internal fun getCommandByName(name: String): PluggableCommand? {
        for (command in builtinCommands + commands)
            if (command.command.commandName == name)
                return command

        return null
    }

    internal fun setupMode(mode: Int) {
        if (mode == MODE_DIRECT) {
            log.info("Switching to direct mode...")
            directClients.clear()
            log.info("Removing all users from the SneakyBOT server group...")
            for (groupMember in query.api.getServerGroupClients(serverGroupId))
                if (groupMember.uniqueIdentifier != whoAmI.uniqueIdentifier)
                    query.api.removeClientFromServerGroup(serverGroupId, groupMember.clientDatabaseId)
            val clients = query.api.clients
            val clientListMsg =
                StringBuilder("I am now listening for commands on the direct chat.\n\nClients that can contact me:")
            clients.stream()
                .filter { client -> client.channelId == consoleChannelId && client.id != whoAmI.id }
                .forEach { client ->
                    log.info("Adding " + client.nickname + " (#" + client.id + ") to the directs list...")
                    directClients.add(client.id)
                    query.api.sendPrivateMessage(
                        client.id,
                        "You are now using the direct chat to communicate with me."
                    )
                    log.info(
                        "Adding " + client.nickname + " (DBID:" + client.databaseId + ") to the SneakyBOT server group..."
                    )
                    query.api.addClientToServerGroup(serverGroupId, client.databaseId)
                    clientListMsg.append("\n - ").append(client.nickname)
                }
            sendChannelMessage(clientListMsg.toString())
            log.info("Finished.")
            this.mode = MODE_DIRECT
        } else if (mode == MODE_CHANNEL) {
            for (directClient in directClients) {
                query.api.sendPrivateMessage(
                    directClient,
                    "Direct chat is now closed. You can send me commands via the SneakyBOT Console channel."
                )
                query.api.removeClientFromServerGroup(
                    serverGroupId,
                    query.api.getClientInfo(directClient).databaseId
                )
            }
            getConsoleChannel()
            if (consoleChannelId == -1)
                consoleChannelId = createConsoleChannel()
            sendChannelMessage("I am back from DIRECT mode! The SneakyBOT Console channel can be used again.")
            this.mode = MODE_CHANNEL
        }
    }

    private fun connect(config: TS3Config): TS3Query {
        log.info("Connecting...")
        val query = TS3Query(config)
        query.connect()

        log.debug("Logging in...")
        query.api.login(botConfig.username, botConfig.password)

        log.debug("Selecting virtual server #${botConfig.virtualServerId}...")
        query.api.selectVirtualServerById(botConfig.virtualServerId)

        whoAmI = query.api.whoAmI()
        log.debug("Current nickname: ${whoAmI.nickname}")
        if (whoAmI.nickname != botConfig.username) {
            log.debug("Changing nickname to ${botConfig.username}")
            query.api.setNickname(botConfig.username)
        }

        log.info("Successfully connected and logged in.")

        return query
    }

    private fun discoverServerGroup(): Int {
        log.info("Searching for server group...")
        var serverGroupId = query.api.serverGroups.find { it.name == botConfig.username }?.id ?: -1

        if (serverGroupId == -1) {
            log.debug("Not found :(")
            log.info("Generating server group...")
            serverGroupId =
                query.api.copyServerGroup(
                    botConfig.templateServerGroupId,
                    "SneakyBOT",
                    PermissionGroupDatabaseType.REGULAR
                )
            log.debug("Finished, SneakyBOT Server Group created.")
        } else {
            log.info("Server group found.")
        }

        log.info("Server group discovery complete.")
        return serverGroupId
    }

    internal fun quit() {
        log.info("Stopping plugins...")
        stopPlugins()

        if (mode == MODE_DIRECT)
            sendDirectMessage("SneakyBOT is now offline.")
        else
            sendChannelMessage("SneakyBOT is now offline.")

        query.exit()
        exitProcess(0)
    }

    private fun createConsoleChannel(): Int {
        log.debug("Creating a new SneakyBOT Console channel...")
        val properties = HashMap<ChannelProperty, String>()
        properties[ChannelProperty.CHANNEL_FLAG_TEMPORARY] = "1"
        properties[ChannelProperty.CHANNEL_TOPIC] = "Console for SneakyBOT"
        properties[ChannelProperty.CHANNEL_NEEDED_TALK_POWER] = "100"
        properties[ChannelProperty.CHANNEL_PASSWORD] = botConfig.consolePassword

        val channelId = query.api.createChannel(botConfig.consoleName, properties)

        log.debug("Done. Channel ID: #$channelId")
        return channelId
    }

    private fun getConsoleChannel(): Int {
        log.debug("Searching for an existing console channel...")
        val consoleChannel = query.api.channels.find { it.name == botConfig.consoleName }
        if (consoleChannel == null)
            log.debug("No SneakyBOT Console channel found.")
        else
            log.debug("Using existing SneakyBOT Console channel with ID #${consoleChannel.id}")

        return consoleChannel?.id ?: -1
    }

    internal fun sendChannelMessage(msg: String) {
        query.api.sendChannelMessage(msg)
    }

    internal fun sendDirectMessage(msg: String) {
        for (userId in directClients)
            query.api.sendPrivateMessage(userId, msg)
    }

    private fun isCommand(msg: String): Boolean {
        return msg.startsWith("!")
    }

    @Throws(NoSuchClientException::class)
    fun getClientById(clientId: Int): Client {
        for (c in query.api.clients)
            if (c.id == clientId)
                return c
        throw NoSuchClientException()
    }

    fun addTimer(timer: Timer, interval: Int) {
        timers.add(
            Thread {
                var running = true
                while (running) {
                    timer.actionPerformed(manager)
                    try {
                        Thread.sleep((interval * 1000).toLong())
                    } catch (e: InterruptedException) {
                        log.debug("Timer stopped")
                        running = false
                    }
                }
            }
        )
    }
}
