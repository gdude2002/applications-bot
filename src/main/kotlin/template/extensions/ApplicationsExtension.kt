package template.extensions

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import template.GUILD_ID
import template.dataFile
import java.util.*
import kotlin.io.path.exists
import kotlin.io.path.reader
import kotlin.io.path.writer
import kotlin.random.Random

const val TOKEN_SIZE = 12
internal val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

class ApplicationsExtension : Extension() {
    override val name: String = "applications"

    val config = Properties()

    val secret: String
        get() = config.getProperty("secret")!!

    val channelId: Snowflake?
        get() = run {
            val prop = config.getProperty("channel")

            if (prop == null) {
                null
            } else {
                Snowflake(prop)
            }
        }

    override suspend fun setup() {
        load()

        ephemeralSlashCommand(::ChannelArgs) {
            name = "application-channel"
            description = "Pick a channel to send applications to"

            guild(GUILD_ID)

            check { hasPermission(Permission.ManageChannels) }

            action {
                config["channel"] = arguments.channel.id.asString
                save()

                respond {
                    content = "Applications channel set to ${arguments.channel.mention}"
                }
            }
        }

        event<ButtonInteractionCreateEvent> {
            check { failIf { channelId == null } }
            check { failIf { event.interaction.channelId != channelId } }
            check { failIf { !event.interaction.componentId.startsWith(secret) } }

            action {

            }
        }

        event<MessageCreateEvent> {
            check { failIf { event.guildId != null } }
            check { failIf { channelId == null } }

            action {

            }
        }
    }

    fun load() {
        if (dataFile.exists()) {
            config.load(dataFile.reader())
        } else {
            config["secret"] = getToken(TOKEN_SIZE)
            save()
        }
    }

    fun save() {
        config.store(dataFile.writer(), "Application extension settings")
    }

    fun getToken(length: Int) = (1..length)
        .map { Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")

    inner class ChannelArgs : Arguments() {
        val channel by channel("channel", "Channel to send applications to")
    }
}
