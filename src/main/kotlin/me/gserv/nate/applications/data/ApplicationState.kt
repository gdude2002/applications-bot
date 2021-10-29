package me.gserv.nate.applications.data

import com.kotlindiscord.kord.extensions.DISCORD_BLACK
import com.kotlindiscord.kord.extensions.DISCORD_GREEN
import com.kotlindiscord.kord.extensions.DISCORD_RED
import dev.kord.common.Color
import dev.kord.common.entity.ButtonStyle
import kotlinx.serialization.Serializable

@Serializable
enum class ApplicationState(
    val readableName: String,
    val color: Color,
    val style: ButtonStyle
) {
    OPEN("Open", DISCORD_BLACK, ButtonStyle.Primary),

    ACCEPTED("Accepted", DISCORD_GREEN, ButtonStyle.Success),
    DENIED("Denied", DISCORD_RED, ButtonStyle.Secondary),
    DENIED_SILENTLY("Denied Silently", DISCORD_RED, ButtonStyle.Danger);

    companion object {
        fun byName(name: String) = when (name) {
            OPEN.readableName -> OPEN
            ACCEPTED.readableName -> ACCEPTED
            DENIED.readableName -> DENIED
            DENIED_SILENTLY.readableName -> DENIED_SILENTLY

            else -> null
        }
    }
}
