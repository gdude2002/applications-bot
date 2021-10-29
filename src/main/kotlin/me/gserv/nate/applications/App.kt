/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package me.gserv.nate.applications

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import dev.kord.common.entity.Snowflake
import me.gserv.nate.applications.extensions.ApplicationsExtension

private val TOKEN = env("TOKEN")   // Get the bot' token from the env vars or a .env file
internal val GUILD_ID = Snowflake(env("GUILD_ID"))

suspend fun main() {
    val bot = ExtensibleBot(TOKEN) {
        extensions {
            add(::ApplicationsExtension)
        }
    }

    bot.start()
}