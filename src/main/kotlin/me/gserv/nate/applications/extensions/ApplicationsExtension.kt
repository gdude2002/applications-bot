package me.gserv.nate.applications.extensions

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
import me.gserv.nate.applications.GUILD_ID
import me.gserv.nate.applications.data.Settings
import me.gserv.nate.applications.data.SettingsStorage

const val TOKEN_SIZE = 12
internal val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

class ApplicationsExtension : Extension() {
    override val name: String = "applications"

    val configStorage = SettingsStorage()
    val config: Settings get() = configStorage.current

    var secret: String
        get() = config.secretToken
        set(value) {
            config.secretToken = value; configStorage.save()
        }

    var channelId: Snowflake?
        get() = config.channelId
        set(value) {
            config.channelId = value; configStorage.save()
        }

    override suspend fun setup() {
        configStorage.load()

        ephemeralSlashCommand(::ChannelArgs) {
            name = "application-channel"
            description = "Pick a channel to send applications to"

            guild(GUILD_ID)

            check { hasPermission(Permission.ManageChannels) }

            action {
                channelId = arguments.channel.id

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

    inner class ChannelArgs : Arguments() {
        val channel by channel("channel", "Channel to send applications to")
    }
}
