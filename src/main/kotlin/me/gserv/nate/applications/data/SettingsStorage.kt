package me.gserv.nate.applications.data

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import kotlin.io.path.createDirectory
import kotlin.io.path.exists
import kotlin.io.path.outputStream
import kotlin.io.path.readText
import kotlin.random.Random

const val TOKEN_SIZE = 12

class SettingsStorage {
    lateinit var current: Settings

    fun load() {
        if (!dataDirectory.exists()) {
            dataDirectory.createDirectory()
        }

        if (!configDataFile.exists()) {
            current = Settings(
                secretToken = getToken(TOKEN_SIZE)
            )

            save()
        } else {
            current = Json.decodeFromString(configDataFile.readText())
        }
    }

    fun save() {
        Json.encodeToStream(current, configDataFile.outputStream())
    }

    fun getToken(length: Int) = (1..length)
        .map { Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")
}
