package me.gserv.nate.applications.data

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import kotlin.io.path.createDirectory
import kotlin.io.path.exists
import kotlin.io.path.outputStream
import kotlin.io.path.readText

class ApplicationsStorage {
    lateinit var current: MutableList<Application>

    fun load() {
        if (!dataDirectory.exists()) {
            dataDirectory.createDirectory()
        }

        if (!applicationsDataFile.exists()) {
            current = mutableListOf()

            save()
        } else {
            current = Json.decodeFromString(applicationsDataFile.readText())
        }
    }

    fun save() {
        Json.encodeToStream(current, applicationsDataFile.outputStream())
    }

    fun getApplication(id: String) =
        current.firstOrNull { it.applicationId == id }

    fun getApplicationsByState(state: ApplicationState) =
        current.filter { it.state == state }

    fun getApplicationsByUser(id: Snowflake, state: ApplicationState? = null) =
        current.filter { it.userId == id && (state == null || it.state == state) }

    fun getApplicationsByMessage(id: Snowflake, state: ApplicationState? = null) =
        current.filter { it.applicationMessageId == id && (state == null || it.state == state) }

    @Suppress("MagicNumber")
    fun createApplication(messageId: Snowflake, userId: Snowflake, text: String): Application {
        val application = Application(messageId.asString, userId, text)

        current.add(application)
        save()

        return application
    }
}
