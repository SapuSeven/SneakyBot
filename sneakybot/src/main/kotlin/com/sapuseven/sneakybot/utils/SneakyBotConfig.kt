package com.sapuseven.sneakybot.utils

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.default

class SneakyBotConfig(parser: ArgParser) {
    val host: String by parser.storing(
        "-s",
        "--host",
        help = "hostname or IP address of the TeamSpeak3 Server"
    )

    val port: Int by parser.storing(
        "-q",
        "--port",
        help = "the ServerQuery port of the TeamSpeak3 Server"
    ) { toInt() }
        .default(10011)
        .addValidator {
            if (value < 1 || value > 65535)
                throw InvalidArgumentException("Invalid port (valid range: 1-65535)")
        }

    val username: String by parser.storing(
        "-u",
        "--user",
        help = "the ServerQuery username to use for connecting to the TeamSpeak3 Server"
    )
        .default("SneakyBOT")

    val password: String by parser.storing(
        "-p",
        "--password",
        help = "the ServerQuery password to use for connecting to the TeamSpeak3 Server"
    )

    val unlimitedFloodRate: Boolean by parser.flagging(
        "-f",
        "--unlimited",
        help = "enable unlimitedflood rate (ATTENTION: only use if your IP is whitelisted in the TeamSpeak3 server configuration)"
    )

    val debug: Boolean by parser.flagging(
        "-d",
        "--debug",
        help = "enable debug log (ATTENTION: will potentially log sensitive data)"
    )

    val virtualServerId: Int by parser.storing("--vserver", help = "virtual server id") { toInt() }
        .default(1)

    val templateServerGroupId: Int by parser.storing(
        "-g",
        "--servergroup",
        help = "id of the servergroup to copy for usage with SneakyBOT"
    ) { toInt() }
        .default(8 /* Guest */)

    val dataDir: String by parser.storing(
        "--data-dir",
        help = "absolute or relative path to a directory to store plugin data"
    )
        .default("data")

    val pluginDir: String by parser.storing(
        "--plugin-dir",
        help = "absolute or relative path to a directory containing SneakyBOT plugins in JAR format"
    )
        .default("plugins")

    val consolePassword: String by parser.storing(
        "--console-password",
        help = "password for the autogenerated SneakyBOT console channel"
    )
        .default("SneakyBOT-admin")

    val consoleName: String by parser.storing(
        "--console-name",
        help = "name for the autogenerated SneakyBOT console channel. If an existing channel with this name exists, it will be used instead"
    )
        .default("SneakyBOT Console")
}
