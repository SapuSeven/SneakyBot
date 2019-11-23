package com.sapuseven.sneakybot

import com.github.theholywaffle.teamspeak3.TS3Api
import com.github.theholywaffle.teamspeak3.TS3Query
import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode
import com.github.theholywaffle.teamspeak3.api.event.ClientMovedEvent
import com.github.theholywaffle.teamspeak3.api.event.TextMessageEvent
import com.github.theholywaffle.teamspeak3.api.exception.TS3CommandFailedException
import com.github.theholywaffle.teamspeak3.api.wrapper.*
import com.sapuseven.sneakybot.exceptions.NoSuchClientException
import com.sapuseven.sneakybot.plugins.PluggableCommand
import com.sapuseven.sneakybot.plugins.PluginManager
import com.sapuseven.sneakybot.utils.Command
import com.sapuseven.sneakybot.utils.ConsoleCommand
import com.sapuseven.sneakybot.utils.EventListenerImplementation
import com.sapuseven.sneakybot.utils.SneakyBotConfig
import com.xenomachina.argparser.ArgParser
import io.mockk.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.Logger
import java.io.File

class SneakyBotTest {
    companion object {
        private const val VIRTUAL_SERVER_HOST = "localhost"
        private const val VIRTUAL_SERVER_USERNAME = "SneakyBOT-TEST"
        private const val VIRTUAL_SERVER_PASSWORD = "123"
        private const val VIRTUAL_SERVER_ARGS_DEFAULT = "-s $VIRTUAL_SERVER_HOST -p $VIRTUAL_SERVER_PASSWORD"

        private const val VIRTUAL_SERVER_BOT_USER_NICKNAME = "1"
        private const val VIRTUAL_SERVER_BOT_USER_ID = "1"
        private const val VIRTUAL_SERVER_BOT_CHANNEL_ID = "-1"

        private const val VIRTUAL_SERVER_CHANNEL_CONSOLE_ID = "2"

        private const val VIRTUAL_SERVER_GROUP_ID = "3"

        private const val VIRTUAL_SERVER_TEST_USER_NICKNAME = "TestUser"
        private const val VIRTUAL_SERVER_TEST_USER_ID = "4"
        private const val VIRTUAL_SERVER_TEST_USER_DATABASE_ID = "5"
        private const val VIRTUAL_SERVER_TEST_USER_UNIQUE_ID = "TestUserUniqueIdentifier"

        private const val VIRTUAL_SERVER_INVALID_USER_ID = "6"
    }

    @Test
    fun run_connectThrowsExceptionOnInvalidLogin() {
        // TODO: Find a way to check for exitProcess() instead of this questionable approach
        val args = VIRTUAL_SERVER_ARGS_DEFAULT.split(" ").toTypedArray()
        val botConfig = ArgParser(args).parseInto(::SneakyBotConfig)

        val mockedBot = spyk(SneakyBot(botConfig))

        mockkConstructor(TS3Query::class)

        every { mockedBot["logCommandFailed"](any<TS3CommandFailedException>(), any<String>()) } throws Exception()

        every { anyConstructed<TS3Query>().connect() } throws mockk<TS3CommandFailedException>()
        assertThrows<Exception> { mockedBot.run() }
    }

    @Test
    fun run_withExistingConsoleChannel() {
        val args = VIRTUAL_SERVER_ARGS_DEFAULT.split(" ").toTypedArray()
        val botConfig = ArgParser(args).parseInto(::SneakyBotConfig)

        val mockedApi = mockk<TS3Api>(relaxed = true)

        every { mockedApi.whoAmI() } returns createBotMock()
        every { mockedApi.channels } returns listOf(
            createChannelConsole(botConfig)
        )

        mockkConstructor(TS3Query::class)

        every { anyConstructed<TS3Query>().connect() } just Runs
        every { anyConstructed<TS3Query>().api } returns mockedApi

        SneakyBot(botConfig).run()

        verify { mockedApi.moveQuery(VIRTUAL_SERVER_CHANNEL_CONSOLE_ID.toInt()) }
    }

    @Test
    fun run_withoutExistingConsoleChannel() {
        val args = VIRTUAL_SERVER_ARGS_DEFAULT.split(" ").toTypedArray()
        val botConfig = ArgParser(args).parseInto(::SneakyBotConfig)

        val mockedApi = mockk<TS3Api>(relaxed = true)

        every { mockedApi.whoAmI() } returns createBotMock()

        mockkConstructor(TS3Query::class)

        every { anyConstructed<TS3Query>().connect() } just Runs
        every { anyConstructed<TS3Query>().api } returns mockedApi

        SneakyBot(botConfig).run()

        verify { mockedApi.createChannel(botConfig.consoleName, any()) }
    }

    @Test
    fun run_withSpecifiedUsername() {
        val args = "$VIRTUAL_SERVER_ARGS_DEFAULT -u $VIRTUAL_SERVER_USERNAME".split(" ").toTypedArray()
        val botConfig = ArgParser(args).parseInto(::SneakyBotConfig)

        val mockedApi = mockk<TS3Api>(relaxed = true)

        every { mockedApi.whoAmI() } returns createBotMock()
        every { mockedApi.channels } returns listOf(
            createChannelConsole(botConfig)
        )

        mockkConstructor(TS3Query::class)

        every { anyConstructed<TS3Query>().connect() } just Runs
        every { anyConstructed<TS3Query>().api } returns mockedApi

        SneakyBot(botConfig).run()

        verify { mockedApi.setNickname(VIRTUAL_SERVER_USERNAME) }
    }

    @Test
    fun run_withExistingClientsInServerGroup() {
        val args = VIRTUAL_SERVER_ARGS_DEFAULT.split(" ").toTypedArray()
        val botConfig = ArgParser(args).parseInto(::SneakyBotConfig)

        val mockedApi = mockk<TS3Api>(relaxed = true)

        every { mockedApi.whoAmI() } returns createBotMock()
        every { mockedApi.serverGroups } returns listOf(
            createServerGroupSneakyBot(botConfig)
        )
        every { mockedApi.clients } returns listOf(
            createClient()
        )
        every { mockedApi.getServerGroupClients(VIRTUAL_SERVER_GROUP_ID.toInt()) } returns listOf(
            createServerGroupClient()
        )

        mockkConstructor(TS3Query::class)

        every { anyConstructed<TS3Query>().connect() } just Runs
        every { anyConstructed<TS3Query>().api } returns mockedApi

        SneakyBot(botConfig).run()

        verify { mockedApi.sendPrivateMessage(VIRTUAL_SERVER_TEST_USER_ID.toInt(), any()) }
    }

    @Test
    fun loadPlugins() {
        val args = VIRTUAL_SERVER_ARGS_DEFAULT.split(" ").toTypedArray()
        val botConfig = ArgParser(args).parseInto(::SneakyBotConfig)

        val mockedBot = spyk(SneakyBot(botConfig))
        val mockedApi = mockk<TS3Api>(relaxed = true)

        mockkConstructor(File::class)
        //every { anyConstructed<File>().exists() } returns true

        mockedBot.query = mockk {
            every { api } returns mockedApi
        }

        mockedBot.run()
    }

    @Test
    fun getClientById() {
        val args = VIRTUAL_SERVER_ARGS_DEFAULT.split(" ").toTypedArray()
        val botConfig = ArgParser(args).parseInto(::SneakyBotConfig)

        val mockedBot = spyk(SneakyBot(botConfig))
        val mockedApi = mockk<TS3Api>(relaxed = true)

        val client = createClient()

        every { mockedApi.clients } returns listOf(
            client
        )

        mockedBot.query = mockk {
            every { api } returns mockedApi
        }

        Assertions.assertEquals(client, mockedBot.getClientById(VIRTUAL_SERVER_TEST_USER_ID.toInt()))
        assertThrows<NoSuchClientException> { mockedBot.getClientById(VIRTUAL_SERVER_INVALID_USER_ID.toInt()) }
    }

    @Test
    fun interpretTextMessage_ignoresNormalMessages() {
        val args = VIRTUAL_SERVER_ARGS_DEFAULT.split(" ").toTypedArray()
        val botConfig = ArgParser(args).parseInto(::SneakyBotConfig)

        val mockedApi = mockk<TS3Api>(relaxed = true)
        val mockedBot = spyk(SneakyBot(botConfig))
        val eventListener = EventListenerImplementation(mockedBot)

        every { mockedApi.whoAmI() } returns createBotMock()
        every { mockedApi.channels } returns listOf(
            createChannelConsole(botConfig)
        )

        mockkConstructor(TS3Query::class)

        every { anyConstructed<TS3Query>().connect() } just Runs
        every { anyConstructed<TS3Query>().api } returns mockedApi

        mockedBot.run()
        mockedApi.addTS3Listeners(eventListener)

        eventListener.onTextMessage(
            createTextMessageEvent(
                TextMessageTargetMode.CHANNEL,
                "test",
                VIRTUAL_SERVER_TEST_USER_ID
            )
        )
        eventListener.onTextMessage(
            createTextMessageEvent(
                TextMessageTargetMode.CHANNEL,
                "!",
                VIRTUAL_SERVER_TEST_USER_ID
            )
        )
        eventListener.onTextMessage(
            createTextMessageEvent(
                TextMessageTargetMode.CHANNEL,
                "!abc",
                VIRTUAL_SERVER_BOT_USER_ID
            )
        )
        eventListener.onTextMessage(
            createTextMessageEvent(
                TextMessageTargetMode.SERVER,
                "!abc",
                VIRTUAL_SERVER_TEST_USER_ID
            )
        )

        verify(exactly = 0) { mockedBot["interpretChannelMessage"](any<TextMessageEvent>()) }
        verify(exactly = 0) { mockedBot["interpretDirectMessage"](any<TextMessageEvent>()) }
    }

    @Test
    fun interpretChannelMessage_channelMode() {
        val args = VIRTUAL_SERVER_ARGS_DEFAULT.split(" ").toTypedArray()
        val botConfig = ArgParser(args).parseInto(::SneakyBotConfig)

        val mockedApi = mockk<TS3Api>(relaxed = true)
        val mockedBot = spyk(SneakyBot(botConfig), recordPrivateCalls = true)
        val eventListener = EventListenerImplementation(mockedBot)

        every { mockedApi.whoAmI() } returns createBotMock()
        every { mockedApi.channels } returns listOf(
            createChannelConsole(botConfig)
        )

        mockkConstructor(TS3Query::class)

        every { anyConstructed<TS3Query>().connect() } just Runs
        every { anyConstructed<TS3Query>().api } returns mockedApi

        mockedBot.run()
        mockedApi.addTS3Listeners(eventListener)

        eventListener.onTextMessage(
            createTextMessageEvent(
                TextMessageTargetMode.CHANNEL,
                "!test",
                VIRTUAL_SERVER_TEST_USER_ID
            )
        )

        verify(exactly = 1) { mockedBot["interpretCommand"](any<String>(), any<Int>()) }
        verify(exactly = 1) { mockedBot["interpretChannelMessage"](any<TextMessageEvent>()) }
        verify(exactly = 0) { mockedBot["interpretDirectMessage"](any<TextMessageEvent>()) }
    }

    @Test
    fun interpretChannelMessage_directMode() {
        val args = VIRTUAL_SERVER_ARGS_DEFAULT.split(" ").toTypedArray()
        val botConfig = ArgParser(args).parseInto(::SneakyBotConfig)

        val mockedApi = mockk<TS3Api>(relaxed = true)
        val mockedBot = spyk(SneakyBot(botConfig), recordPrivateCalls = true)
        val eventListener = EventListenerImplementation(mockedBot)

        every { mockedApi.whoAmI() } returns createBotMock()
        every { mockedApi.channels } returns listOf(
            createChannelConsole(botConfig)
        )
        every { mockedApi.serverGroups } returns listOf(
            createServerGroupSneakyBot(botConfig)
        )
        every { mockedApi.clients } returns listOf(
            createClient()
        )
        every { mockedApi.getServerGroupClients(VIRTUAL_SERVER_GROUP_ID.toInt()) } returns listOf(
            createServerGroupClient()
        )

        mockkConstructor(TS3Query::class)

        every { anyConstructed<TS3Query>().connect() } just Runs
        every { anyConstructed<TS3Query>().api } returns mockedApi

        mockedBot.run()
        mockedApi.addTS3Listeners(eventListener)

        eventListener.onTextMessage(
            createTextMessageEvent(
                TextMessageTargetMode.CHANNEL,
                "!test",
                VIRTUAL_SERVER_TEST_USER_ID
            )
        )

        verify(exactly = 0) { mockedBot["interpretCommand"](any<String>(), any<Int>()) }
        verify(exactly = 1) { mockedBot["interpretChannelMessage"](any<TextMessageEvent>()) }
        verify(exactly = 0) { mockedBot["interpretDirectMessage"](any<TextMessageEvent>()) }
    }

    @Test
    fun interpretDirectMessage_channelMode() {
        val args = VIRTUAL_SERVER_ARGS_DEFAULT.split(" ").toTypedArray()
        val botConfig = ArgParser(args).parseInto(::SneakyBotConfig)

        val mockedApi = mockk<TS3Api>(relaxed = true)
        val mockedBot = spyk(SneakyBot(botConfig), recordPrivateCalls = true)
        val eventListener = EventListenerImplementation(mockedBot)

        every { mockedApi.whoAmI() } returns createBotMock()
        every { mockedApi.channels } returns listOf(
            createChannelConsole(botConfig)
        )

        mockkConstructor(TS3Query::class)

        every { anyConstructed<TS3Query>().connect() } just Runs
        every { anyConstructed<TS3Query>().api } returns mockedApi

        mockedBot.run()
        mockedApi.addTS3Listeners(eventListener)

        eventListener.onTextMessage(
            createTextMessageEvent(
                TextMessageTargetMode.CLIENT,
                "!test",
                VIRTUAL_SERVER_TEST_USER_ID
            )
        )

        verify(exactly = 0) { mockedBot["interpretCommand"](any<String>(), any<Int>()) }
        verify(exactly = 0) { mockedBot["interpretChannelMessage"](any<TextMessageEvent>()) }
        verify(exactly = 1) { mockedBot["interpretDirectMessage"](any<TextMessageEvent>()) }
    }

    @Test
    fun interpretDirectMessage_directMode() {
        val args = VIRTUAL_SERVER_ARGS_DEFAULT.split(" ").toTypedArray()
        val botConfig = ArgParser(args).parseInto(::SneakyBotConfig)

        val mockedApi = mockk<TS3Api>(relaxed = true)
        val mockedBot = spyk(SneakyBot(botConfig), recordPrivateCalls = true)
        val eventListener = EventListenerImplementation(mockedBot)

        every { mockedApi.whoAmI() } returns createBotMock()
        every { mockedApi.channels } returns listOf(
            createChannelConsole(botConfig)
        )
        every { mockedApi.serverGroups } returns listOf(
            createServerGroupSneakyBot(botConfig)
        )
        every { mockedApi.clients } returns listOf(
            createClient()
        )
        every { mockedApi.getServerGroupClients(VIRTUAL_SERVER_GROUP_ID.toInt()) } returns listOf(
            createServerGroupClient()
        )

        mockkConstructor(TS3Query::class)

        every { anyConstructed<TS3Query>().connect() } just Runs
        every { anyConstructed<TS3Query>().api } returns mockedApi

        mockedBot.run()
        mockedApi.addTS3Listeners(eventListener)

        eventListener.onTextMessage(
            createTextMessageEvent(
                TextMessageTargetMode.CLIENT,
                "!test",
                VIRTUAL_SERVER_TEST_USER_ID
            )
        )

        verify(exactly = 1) { mockedBot["interpretCommand"](any<String>(), any<Int>()) }
        verify(exactly = 1) { mockedBot["interpretDirectMessage"](any<TextMessageEvent>()) }

        eventListener.onTextMessage(
            createTextMessageEvent(
                TextMessageTargetMode.CLIENT,
                "!test",
                VIRTUAL_SERVER_INVALID_USER_ID
            )
        )

        verify(exactly = 1) { mockedBot["interpretCommand"](any<String>(), any<Int>()) }
        verify(exactly = 2) { mockedBot["interpretDirectMessage"](any<TextMessageEvent>()) }
        verify(exactly = 0) { mockedBot["interpretChannelMessage"](any<TextMessageEvent>()) }
    }

    @Test
    fun interpretClientMoved_movedToConsole() {
        val args = VIRTUAL_SERVER_ARGS_DEFAULT.split(" ").toTypedArray()
        val botConfig = ArgParser(args).parseInto(::SneakyBotConfig)

        val mockedApi = mockk<TS3Api>(relaxed = true)
        val mockedBot = spyk(SneakyBot(botConfig))
        val eventListener = EventListenerImplementation(mockedBot)

        every { mockedApi.whoAmI() } returns createBotMock()
        every { mockedApi.channels } returns listOf(
            createChannelConsole(botConfig)
        )

        mockkConstructor(TS3Query::class)

        every { anyConstructed<TS3Query>().connect() } just Runs
        every { anyConstructed<TS3Query>().api } returns mockedApi

        mockedBot.run()

        mockedApi.addTS3Listeners(eventListener)

        eventListener.onClientMoved(
            createClientMovedEvent(
                VIRTUAL_SERVER_CHANNEL_CONSOLE_ID,
                VIRTUAL_SERVER_TEST_USER_ID
            )
        )

        verify { mockedApi.sendChannelMessage(any()) }
    }

    @Test
    fun interpretCommand_commandExecutedWithErrors() {
        val args = VIRTUAL_SERVER_ARGS_DEFAULT.split(" ").toTypedArray()
        val botConfig = ArgParser(args).parseInto(::SneakyBotConfig)

        val mockedApi = mockk<TS3Api>(relaxed = true)
        val mockedBot = spyk(SneakyBot(botConfig), recordPrivateCalls = true)
        val eventListener = EventListenerImplementation(mockedBot)

        every { mockedApi.whoAmI() } returns createBotMock()
        every { mockedApi.channels } returns listOf(
            createChannelConsole(botConfig)
        )

        every { mockedBot.getCommandByName(any()) } returns mockk {
            every { execute(any(), any()) } returns false
        }

        mockkConstructor(TS3Query::class)

        every { anyConstructed<TS3Query>().connect() } just Runs
        every { anyConstructed<TS3Query>().api } returns mockedApi

        mockedBot.run()
        mockedApi.addTS3Listeners(eventListener)

        eventListener.onTextMessage(
            createTextMessageEvent(
                TextMessageTargetMode.CHANNEL,
                "!test",
                VIRTUAL_SERVER_TEST_USER_ID
            )
        )

        verify(exactly = 1) { mockedBot["interpretCommand"](any<String>(), any<Int>()) }
        // TODO: Check for log.info
    }

    private fun createClientMovedEvent(targetChannelId: String, clientId: String) = ClientMovedEvent(
        mapOf(
            "ctid" to targetChannelId,
            "clid" to clientId
        )
    )

    private fun createTextMessageEvent(targetMode: TextMessageTargetMode, text: String, invokerId: String) =
        TextMessageEvent(
            mapOf(
                "targetmode" to targetMode.index.toString(),
                "msg" to text,
                "invokerid" to invokerId
                //"invokername"
                //"invokeruid"
            )
        )

    private fun createBotMock(): ServerQueryInfo = mockk {
        every { nickname } returns VIRTUAL_SERVER_BOT_USER_NICKNAME
        every { id } returns VIRTUAL_SERVER_BOT_USER_ID.toInt()
        every { channelId } returns VIRTUAL_SERVER_BOT_CHANNEL_ID.toInt()
    }

    private fun createChannelConsole(botConfig: SneakyBotConfig) = Channel(
        mapOf(
            "channel_name" to botConfig.consoleName,
            "cid" to VIRTUAL_SERVER_CHANNEL_CONSOLE_ID
        )
    )

    private fun createServerGroupSneakyBot(botConfig: SneakyBotConfig) = ServerGroup(
        mapOf(
            "name" to botConfig.username,
            "sgid" to VIRTUAL_SERVER_GROUP_ID
        )
    )

    private fun createClient() = Client(
        mapOf(
            "client_nickname" to VIRTUAL_SERVER_TEST_USER_NICKNAME,
            "client_unique_identifier" to VIRTUAL_SERVER_TEST_USER_UNIQUE_ID,
            "clid" to VIRTUAL_SERVER_TEST_USER_ID
        )
    )

    private fun createServerGroupClient() = ServerGroupClient(
        mapOf(
            "cldbid" to VIRTUAL_SERVER_TEST_USER_DATABASE_ID,
            "client_nickname" to VIRTUAL_SERVER_TEST_USER_NICKNAME,
            "client_unique_identifier" to VIRTUAL_SERVER_TEST_USER_UNIQUE_ID
        )
    )
}
