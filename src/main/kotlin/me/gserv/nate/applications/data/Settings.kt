package me.gserv.nate.applications.data

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
@Suppress("DataClassShouldBeImmutable")
data class Settings(
    var channelId: Snowflake? = null,
    var secretToken: String,

    var initialResponse: String =
        "Thanks for your application - we'll get back to you as soon as possible!\n\n" +

                "Please avoid responding to this message - we'll contact you when we've looked over your application.",

    var alreadyRepliedResponse: String = "It looks like you've already applied. Please be patient - it can take " +
            "some time to review applications, and we'll get back to you as soon as possible.",

    var acceptedResponseTemplate: String =
        "Congratulations, you've been accepted!\n\n" +

                "Here's your invite: {INVITE}",

    var deniedResponseTemplate: String =
        "Unfortunately, your application has been denied.\n\n" +

                "Reason: **{REASON}**",

    val denialReasons: MutableMap<String, String> = mutableMapOf()
)
