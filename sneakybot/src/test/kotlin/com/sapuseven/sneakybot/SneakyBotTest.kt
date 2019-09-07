package com.sapuseven.sneakybot

import com.github.theholywaffle.teamspeak3.TS3Api
import com.github.theholywaffle.teamspeak3.TS3Query
import com.github.theholywaffle.teamspeak3.api.wrapper.Channel
import com.github.theholywaffle.teamspeak3.api.wrapper.Client
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerGroup
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerGroupClient
import com.sapuseven.sneakybot.exceptions.NoSuchClientException
import com.sapuseven.sneakybot.utils.SneakyBotConfig
import com.xenomachina.argparser.ArgParser
import io.mockk.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SneakyBotTest {
    @Test
    fun run_withExistingConsoleChannel() {
        val args = "-s localhost -p 123".split(" ").toTypedArray()
        val botConfig = ArgParser(args).parseInto(::SneakyBotConfig)

        val mockedApi = mockk<TS3Api>(relaxed = true)

        every { mockedApi.whoAmI() } returns mockk {
            every { nickname } returns "SneakyBOT"
            every { id } returns 1
            every { channelId } returns 1
        }
        every { mockedApi.channels } returns listOf(
            Channel(
                mapOf(
                    "channel_name" to botConfig.consoleName,
                    "cid" to "1"
                )
            )
        )

        mockkConstructor(TS3Query::class)

        every { anyConstructed<TS3Query>().connect() } returns mockk()
        every { anyConstructed<TS3Query>().api } returns mockedApi

        SneakyBot(botConfig).run()
    }

    @Test
    fun run_withSpecifiedUsername() {
        val args = "-s localhost -p 123 -u SneakyBOT-TEST".split(" ").toTypedArray()
        val botConfig = ArgParser(args).parseInto(::SneakyBotConfig)

        val mockedApi = mockk<TS3Api>(relaxed = true)

        every { mockedApi.whoAmI() } returns mockk {
            every { nickname } returns "SneakyBOT"
            every { id } returns 1
            every { channelId } returns 1
        }
        every { mockedApi.channels } returns listOf(
            Channel(
                mapOf(
                    "channel_name" to botConfig.consoleName,
                    "cid" to "1"
                )
            )
        )

        mockkConstructor(TS3Query::class)

        every { anyConstructed<TS3Query>().connect() } returns mockk()
        every { anyConstructed<TS3Query>().api } returns mockedApi

        SneakyBot(botConfig).run()

        verify { mockedApi.setNickname("SneakyBOT-TEST") }
    }

    @Test
    fun run_withoutExistingConsoleChannel() {
        val args = "-s localhost -p 123".split(" ").toTypedArray()
        val botConfig = ArgParser(args).parseInto(::SneakyBotConfig)

        val mockedApi = mockk<TS3Api>(relaxed = true)

        every { mockedApi.whoAmI() } returns mockk {
            every { nickname } returns "SneakyBOT"
            every { id } returns 1
            every { channelId } returns -1
        }

        mockkConstructor(TS3Query::class)

        every { anyConstructed<TS3Query>().connect() } returns mockk()
        every { anyConstructed<TS3Query>().api } returns mockedApi

        SneakyBot(botConfig).run()

        verify { mockedApi.createChannel(botConfig.consoleName, any()) }
    }

    @Test
    fun run_withExistingClientsInServerGroup() {
        val args = "-s localhost -p 123".split(" ").toTypedArray()
        val botConfig = ArgParser(args).parseInto(::SneakyBotConfig)

        val mockedApi = mockk<TS3Api>(relaxed = true)

        every { mockedApi.whoAmI() } returns mockk {
            every { nickname } returns "SneakyBOT"
            every { id } returns 1
            every { channelId } returns -1
        }
        every { mockedApi.serverGroups } returns listOf(
            ServerGroup(
                mapOf(
                    "name" to botConfig.username,
                    "sgid" to "1"
                )
            )
        )
        every { mockedApi.clients } returns listOf(
            Client(
                mapOf(
                    "client_nickname" to "TestUser",
                    "client_unique_identifier" to "TestUserUniqueIdentifier",
                    "clid" to "2"
                )
            )
        )
        every { mockedApi.getServerGroupClients(1) } returns listOf(
            ServerGroupClient(
                mapOf(
                    "cldbid" to "2",
                    "client_nickname" to "TestUser",
                    "client_unique_identifier" to "TestUserUniqueIdentifier"
                )
            )
        )

        mockkConstructor(TS3Query::class)

        every { anyConstructed<TS3Query>().connect() } returns mockk()
        every { anyConstructed<TS3Query>().api } returns mockedApi

        SneakyBot(botConfig).run()

        verify { mockedApi.sendPrivateMessage(2, any()) }
    }

    @Test
    fun getClientById() {
        val args = "-s localhost -p 123".split(" ").toTypedArray()
        val botConfig = ArgParser(args).parseInto(::SneakyBotConfig)

        val mockedBot = spyk(SneakyBot(botConfig))
        val mockedApi = mockk<TS3Api>(relaxed = true)

        val client = Client(
            mapOf(
                "client_nickname" to "TestUser",
                "client_unique_identifier" to "TestUserUniqueIdentifier",
                "clid" to "2"
            )
        )

        every { mockedApi.clients } returns listOf(
            client
        )

        mockedBot.query = mockk {
            every { api } returns mockedApi
        }

        Assertions.assertEquals(client, mockedBot.getClientById(2))
        assertThrows<NoSuchClientException> { mockedBot.getClientById(3) }
    }
}
