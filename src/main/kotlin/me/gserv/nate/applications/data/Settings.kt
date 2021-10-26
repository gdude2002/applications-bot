package me.gserv.nate.applications.data

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    var channelId: Snowflake? = null,
    var secretToken: String
)
