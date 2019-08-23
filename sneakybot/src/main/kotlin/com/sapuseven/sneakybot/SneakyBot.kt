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
import com.sapuseven.sneakybot.exceptions.NoSuchClientException
import com.sapuseven.sneakybot.plugins.PluggableCommand
import com.sapuseven.sneakybot.plugins.PluggableService
import com.sapuseven.sneakybot.plugins.PluginLoader
import com.sapuseven.sneakybot.plugins.Timer
import com.sapuseven.sneakybot.utils.SneakyBotConfig
import com.sapuseven.sneakybot.utils.ConsoleCommand
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
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, if (botConfig.debug) "DEBUG" else "INFO") // TODO: Allow the user to specify any log level
    SneakyBot(botConfig).run()
}

class SneakyBot(internal val botConfig: SneakyBotConfig) {
    internal lateinit var query: TS3Query
    internal var mode: Int = MODE_CHANNEL
    private lateinit var whoAmI: ServerQueryInfo
    private var consoleChannelId: Int = -1
    private var serverGroupId: Int = -1
    private val directClients = ArrayList<Int>()
    private val timers = ArrayList<Thread>()
    private val commands = ArrayList<PluggableCommand>()
    private val services = ArrayList<PluggableService>()
    private lateinit var manager: PluginManagerImpl

    private val log = LoggerFactory.getLogger(SneakyBot::class.java)

    companion object {
        const val MODE_DIRECT = 0x0001
        const val MODE_CHANNEL = 0x0002
    }

    fun run() {
        val config = TS3Config()
        config.setHost(botConfig.host)
        config.setQueryPort(botConfig.port)
        config.setFloodRate(if (botConfig.unlimitedFloodRate) TS3Query.FloodRate.UNLIMITED else TS3Query.FloodRate.DEFAULT)
        config.setEnableCommunicationsLogging(botConfig.debug)

        log.info("Initializing plugins (Phase 1)...")

        loadPlugins()
        preInit()

        try {
            query = connect(config)
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
        //joinServerGroup()

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
        query.api.addTS3Listeners(object : TS3Listener {
            override fun onTextMessage(e: TextMessageEvent) {
                if (isCommand(e.message) && e.invokerId != whoAmI.id) {
                    if (e.targetMode != TextMessageTargetMode.SERVER) {
                        if (mode == MODE_DIRECT) {
                            if (e.targetMode == TextMessageTargetMode.CLIENT) {
                                if (directClients.contains(e.invokerId)) {
                                    log.info("User ${e.invokerName} executed a command via DIRECT chat: ${e.message}")
                                    interpretCommand(e.message, e.invokerId)
                                } else {
                                    log.info("User ${e.invokerName} tried to execute a command, but has no permissions to use DIRECT chat: " + e.message)
                                    query.api.sendPrivateMessage(
                                        e.invokerId,
                                        "You are not allowed to give me commands!"
                                    )
                                }
                            } else {
                                log.info("User ${e.invokerName} tried to execute a command, but used CONSOLE chat instead of DIRECT chat: " + e.message)
                                sendChannelMessage("Please use the direct chat to communicate with me.")
                            }
                        } else if (mode == MODE_CHANNEL) {
                            if (e.targetMode == TextMessageTargetMode.CHANNEL) {
                                log.info("User ${e.invokerName} executed a command via CONSOLE chat: " + e.message)
                                interpretCommand(e.message, e.invokerId)
                            } else if (e.targetMode == TextMessageTargetMode.CLIENT) {
                                if (e.message == "!mode direct ${botConfig.consolePassword}") {
                                    setupMode(MODE_DIRECT)
                                    directClients.add(e.invokerId)
                                    query.api.addClientToServerGroup(
                                        serverGroupId,
                                        query.api.getClientInfo(e.invokerId).databaseId
                                    )
                                    log.info("User ${e.invokerName} regained access to the DIRECT chat.")
                                    query.api.sendPrivateMessage(
                                        e.invokerId,
                                        "You are now allowed to give me commands."
                                    )
                                }
                            } else {
                                log.info("User ${e.invokerName} tried to execute a command, but didn't use CONSOLE chat: " + e.message)
                                query.api.sendPrivateMessage(
                                    e.invokerId,
                                    "Please use the SneakyBOT Console channel to communicate with me."
                                )
                            }
                        }
                    } else {
                        /*if (e.message != "!call") {
                            if (mode != MODE_DIRECT)
                                setupMode(MODE_DIRECT)
                            else
                                manager.sendMessage("Mode is already set to DIRECT!", e.invokerId)
                        }*/
                        log.info("User " + e.invokerName + " tried to execute a command via SERVER chat: " + e.message)
                        query.api.sendServerMessage("Sorry, I can only accept commands from the SneakyBot Console Channel!")
                    }
                }

                for (p in services)
                    p.onEventReceived(e)
            }

            override fun onServerEdit(e: ServerEditedEvent) {
                for (p in services)
                    p.onEventReceived(e)
            }

            override fun onClientMoved(e: ClientMovedEvent) {
                if (e.targetChannelId == consoleChannelId) {
                    val name = query.api.getClientInfo(e.clientId).nickname
                    log.info("User $name entered the SneakyBOT Console channel.")
                    if (mode == MODE_CHANNEL)
                        manager.sendMessage(
                            "\nWelcome, " + name + ", to the SneakyBOT Console!\n" +
                                    "If you are new, '!help' is usually a good command for getting started.", e.clientId
                        )
                }

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

        when (cmd.commandName) {
            "stop", "kill" -> quit()
            "mode" -> if (cmd.paramCount() == 2) {
                when (cmd.getParam(1)) {
                    "direct", "d" -> if (mode != MODE_DIRECT)
                        setupMode(MODE_DIRECT)
                    else
                        manager.sendMessage("Mode is already set to DIRECT!", invokerId)
                    "channel", "console", "c" -> if (mode != MODE_CHANNEL)
                        setupMode(MODE_CHANNEL)
                    else
                        manager.sendMessage("Mode is already set to CHANNEL!", invokerId)
                    else -> manager.sendMessage("Unknown mode: " + cmd.getParam(1), invokerId)
                }
            } else {
                manager.sendMessage("This isn't the right way of using this command!\nTry '!help mode'", invokerId)
            }
            "directs" -> when {
                cmd.paramCount() == 3 -> when (cmd.getParam(1)) {
                    "add" -> if (!directClients.contains(Integer.parseInt(cmd.getParam(2)))) {
                        directClients.add(Integer.parseInt(cmd.getParam(2)))
                        query.api.sendPrivateMessage(
                            Integer.parseInt(cmd.getParam(2)),
                            "You are now using the direct chat to communicate with me."
                        )
                        query.api.addClientToServerGroup(
                            serverGroupId,
                            query.api.getClientInfo(Integer.parseInt(cmd.getParam(2))).databaseId
                        )
                        val clientListMsg =
                            StringBuilder("User added to direct client list.\n\nClients that can contact me:")
                        for (client in directClients)
                            clientListMsg.append("\n - ").append(query.api.getClientInfo(client).nickname)
                        manager.sendMessage(clientListMsg.toString(), invokerId)
                    } else {
                        manager.sendMessage("This user is already in the direct clients list.", invokerId)
                    }
                    "del" -> if (directClients.contains(Integer.parseInt(cmd.getParam(2)))) {
                        directClients.remove(Integer.parseInt(cmd.getParam(2)))
                        query.api.sendPrivateMessage(
                            Integer.parseInt(cmd.getParam(2)),
                            "Direct chat is now closed. You can send me commands via the SneakyBOT Console channel."
                        )
                        query.api.removeClientFromServerGroup(
                            serverGroupId,
                            query.api.getClientInfo(Integer.parseInt(cmd.getParam(2))).databaseId
                        )
                        manager.sendMessage("User removed from direct client list.", invokerId)
                    } else {
                        manager.sendMessage("This user is not in the direct clients list!", invokerId)
                    }
                    else -> manager.sendMessage("Unknown option: " + cmd.getParam(1), invokerId)
                }
                cmd.paramCount() == 1 -> {
                    val clientListMsg = StringBuilder("\nClients that can contact me:")
                    for (client in directClients)
                        clientListMsg.append("\n - ").append(query.api.getClientInfo(client).nickname)
                    manager.sendMessage(clientListMsg.toString(), invokerId)
                }
                else -> manager.sendMessage(
                    "This isn't the right way of using this command!\nTry '!help directs'",
                    invokerId
                )
            }
            "reload" -> {
                log.info("Reloading plugins...")

                stopPlugins()
                loadPlugins()
                try {
                    preInit()
                } catch (e: Exception) {
                    // TODO: Better error handling
                    e.printStackTrace()
                    exitProcess(1)
                }

                postInit()

                log.info("All plugins reloaded.")
                val msg = "Plugins reloaded (${commands.size} commands and ${services.size} services active)."
                if (mode == MODE_CHANNEL)
                    sendChannelMessage(msg)
                else if (mode == MODE_DIRECT)
                    sendDirectMessage(msg)
            }
            "help" -> when {
                cmd.paramCount() == 1 -> manager.sendMessage(
                    "\n" +
                            "Available Commands:\n" +
                            "\n" +
                            "| !kill ALIAS !stop : Stops the SneakyBOT.\n" +
                            "| \n" +
                            "| !mode [channel|direct <PASSWORD>] : Switches between direct and channel mode. Use the PASSWORD parameter to start direct mode using a private message without using the console channel.\n" +
                            "| \n" +
                            "| !directs <[add|del] [CLIENT_ID]> : Lists all clients that can use the direct chat, or add/remove clients to the direct client list.\n" +
                            "| \n" +
                            "| !reload : Reload all plugins.\n" +
                            listHelp() +
                            "\n" +
                            "[***] ... REQUIRED\n" +
                            "<***> ... OPTIONAL", invokerId
                )
                cmd.paramCount() == 2 -> when (cmd.getParam(1)) {
                    "stop", "kill" -> manager.sendMessage("Usage:\n!kill ALIAS !stop : Stops the SneakyBOT.", invokerId)
                    "mode" -> manager.sendMessage(
                        "Usage:\n!mode [channel|direct <PASSWORD>] : Switches between direct and channel mode. Use the PASSWORD parameter to start direct mode using a private message without using the console channel.",
                        invokerId
                    )
                    "directs" -> manager.sendMessage(
                        "Usage:\n!directs <[add|del] [CLIENT_ID]> : Lists all clients that can use the direct chat, or add/remove clients to the direct client list.",
                        invokerId
                    )
                    "reload" -> manager.sendMessage("Usage:\n!reload : Reload all plugins.", invokerId)
                    "help" -> manager.sendMessage(
                        "Usage:\n!help [COMMAND_NAME] : Display useful information about all the commands or a specific command.",
                        invokerId
                    )
                    else -> {
                        val pluggableCommand = getCommandByName(cmd.getParam(1))
                        if (pluggableCommand == null) {
                            manager.sendMessage("This commands help page doesn't exist in my database!", invokerId)
                        } else {
                            val builder = StringBuilder("Usage:\n!")
                            builder
                                .append(pluggableCommand.command.commandName)
                                .append(" : ")

                            for (param in pluggableCommand.command.parameters) {
                                if (param.isRequired)
                                    builder
                                        .append("[")
                                        .append(param.parameter.toUpperCase())
                                        .append("] ")
                                else
                                    builder
                                        .append("<")
                                        .append(param.parameter.toUpperCase())
                                        .append("> ")
                            }

                            builder
                                .append(pluggableCommand.command.help)
                                .append("\n")

                            manager.sendMessage(builder.toString(), invokerId)
                        }
                    }
                }
                else -> manager.sendMessage(
                    "This isn't the right way of using this command!\nUsage: '!help <COMMAND>'",
                    invokerId
                )
            }
            else -> {
                val pluggableCommand = getCommandByName(cmd.commandName)
                if (pluggableCommand == null) {
                    manager.sendMessage("Sorry, I don't understand that!", invokerId)
                } else {
                    if (!pluggableCommand.execute(cmd, invokerId))
                        log.info("Command $cmd executed with errors.")
                }
            }
        }
    }

    private fun preInit() {
        for (p in services) {
            p.setPluginManager(manager)
            log.debug("PreInit: " + p.javaClass.simpleName)
            p.preInit(manager)
        }
    }

    private fun postInit() {
        for (p in services) {
            log.debug("PostInit: " + p.javaClass.simpleName)
            p.postInit(manager)
        }

        for (t in timers) {
            if (!t.isAlive)
                t.start()
        }
    }

    private fun loadPlugins() {
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

    private fun stopPlugins() {
        for (p in services) {
            log.debug("Stopping: " + p.javaClass.simpleName)
            p.stop(manager)
        }

        for (t in timers) {
            t.interrupt()
        }

        timers.clear()
    }

    private fun getCommandByName(name: String): PluggableCommand? {
        for (command in commands) {
            if (command.command.commandName == name)
                return command
        }

        return null
    }

    private fun listHelp(): String {
        val builder = StringBuilder()
        for (command in commands) {
            builder
                .append("| \n| !")
                .append(command.command.commandName)
                .append(" ")
            for (param in command.command.parameters) {
                if (param.isRequired)
                    builder
                        .append("[")
                        .append(param.parameter.toUpperCase())
                        .append("] ")
                else
                    builder
                        .append("<")
                        .append(param.parameter.toUpperCase())
                        .append("> ")
            }
            builder.append(": ")

            builder.append(command.command.help.replace("\n", "\n|     ")).append("\n")
        }
        return builder.toString()
    }

    private fun setupMode(mode: Int) {
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
            /*var isAdmin = false
            for (client in query.api.getServerGroupClients(templateServerGroupId))
                if (client.nickname == "SneakyBOT")
                    isAdmin = true
            if (!isAdmin)
                query.api.usePrivilegeKey(query.api.addPrivilegeKeyServerGroup(templateServerGroupId, null))*/
            serverGroupId =
                query.api.copyServerGroup(botConfig.templateServerGroupId, "SneakyBOT", PermissionGroupDatabaseType.REGULAR)
            query.api.deleteServerGroupPermission(serverGroupId, "i_icon_id") // Remove icon
            query.api.deleteServerGroupPermission(
                serverGroupId,
                "b_client_is_priority_speaker"
            ) // Remove priority speaker status
            query.api.addServerGroupPermission(
                serverGroupId,
                "i_client_kick_from_server_power",
                76,
                false,
                false
            ) // Increase kick from server power
            /*query.api.getServerGroupClients(templateServerGroupId).stream()
                .filter { client -> client.nickname == "SneakyBOT" }
                .forEach {
                    query.api.removeClientFromServerGroup(
                        templateServerGroupId,
                        query.api.whoAmI().databaseId
                    )
                }*/ // Remove myself from the template group
            log.debug("Finished, SneakyBOT Server Group created.")
        } else {
            log.info("Server group found.")
        }

        log.info("Server group discovery complete.")
        return serverGroupId
    }

    private fun checkIfInServerGroup(): Boolean {
        return query.api.getServerGroupClients(serverGroupId).find { it.uniqueIdentifier == whoAmI.uniqueIdentifier } != null
    }

    private fun joinServerGroup() {
        if (!checkIfInServerGroup()) {
            log.debug("I am not in my server group yet.")
            try {
                query.api.addClientToServerGroup(serverGroupId, whoAmI.databaseId)

                if (checkIfInServerGroup())
                    log.debug("Now I am.")
                else {
                    throw Exception()
                }
            } catch (e: TS3CommandFailedException) {
                logCommandFailed(e, "Failed to add SneakyBot to its server group")
                query.exit()
                exitProcess(1)
            } catch (e: Exception) {
                log.error("Failed to add SneakyBot to its server group.")
                query.exit()
                exitProcess(1)
            }
        } else {
            log.debug("I am already in my server group.")
        }
    }

    private fun quit() {
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

    private fun sendChannelMessage(msg: String) {
        query.api.sendChannelMessage(msg)
    }

    private fun sendDirectMessage(msg: String) {
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
