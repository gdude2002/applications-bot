package me.gserv.nate.applications.data

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
@Suppress("DataClassShouldBeImmutable")
data class Application(
    val applicationId: String,
    val userId: Snowflake,
    val text: String,

    var applicationMessageId: Snowflake? = null,
    var state: ApplicationState = ApplicationState.OPEN,
)
