package me.gserv.nate.applications.data

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import me.gserv.nate.applications.extensions.charPool
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.random.Random

internal val dataDirectory = Path.of("./data/")
internal val dataFile = dataDirectory / "config.json"

const val TOKEN_SIZE = 12

class SettingsStorage {
    lateinit var current: Settings

    fun load() {
        if (!dataDirectory.exists()) {
            dataDirectory.createDirectory()
        }

        if (!dataFile.exists()) {
            current = Settings(
                secretToken = getToken(TOKEN_SIZE)
            )

            save()
        } else {
            current = Json.decodeFromString(dataFile.readText())
        }
    }

    fun save() {
        Json.encodeToStream(current, dataFile.outputStream())
    }

    fun getToken(length: Int) = (1..length)
        .map { Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")
}
