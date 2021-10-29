package me.gserv.nate.applications.extensions

import com.kotlindiscord.kord.extensions.DISCORD_BLURPLE
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.types.editingPaginator
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.dm
import com.kotlindiscord.kord.extensions.utils.respond
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.channel.withTyping
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.followUpEphemeral
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.entity.channel.TopGuildMessageChannel
import dev.kord.core.entity.channel.createInvite
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.kord.rest.builder.message.modify.actionRow
import dev.kord.rest.builder.message.modify.embed
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import me.gserv.nate.applications.GUILD_ID
import me.gserv.nate.applications.data.*

const val MAX_DENIAL_REASONS = 10

const val INVITE_TOKEN = "{INVITE}"
const val NEWLINE_TOKEN = "{N}"
const val REASON_TOKEN = "{REASON}"

class ApplicationsExtension : Extension() {
    override val name: String = "applications"

    val applications = ApplicationsStorage()
    val configStorage = SettingsStorage()

    val config: Settings get() = configStorage.current
    val denialReasonCount: Int get() = config.denialReasons.size

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

    var initialResponse: String
        get() = config.initialResponse
        set(value) {
            config.initialResponse = value; configStorage.save()
        }

    var alreadyRepliedResponse: String
        get() = config.alreadyRepliedResponse
        set(value) {
            config.alreadyRepliedResponse = value; configStorage.save()
        }

    var acceptedResponseTemplate: String
        get() = config.acceptedResponseTemplate
        set(value) {
            config.acceptedResponseTemplate = value; configStorage.save()
        }

    var deniedResponseTemplate: String
        get() = config.deniedResponseTemplate
        set(value) {
            config.deniedResponseTemplate = value; configStorage.save()
        }

    @Suppress("MagicNumber")
    override suspend fun setup() {
        applications.load()
        configStorage.load()

        ephemeralSlashCommand {
            name = "config"
            description = "Applications configuration commands"

            guild(GUILD_ID)

            check { hasPermission(Permission.Administrator) }

            ephemeralSubCommand {
                name = "help"
                description = "Learn how to configure the bot"

                action {
                    respond {
                        content = ">>> **__Applications Bot: Help__**\n\n" +

                                "Hello! This bot exists to provide a simple applications system, allowing your " +
                                "server to manually verify anyone that wants to join it. For this bot to work " +
                                "properly, you'll need to ensure the following:\n\n" +

                                "**»** That this bot has also been placed on a public-facing server, so that " +
                                "applicants can send it DMs and learn how it works and what your application " +
                                "criteria are\n\n" +

                                "**»** That the bot has permission to create invites on your target server, and it " +
                                "can send messages in the applications channel you've set up\n\n" +

                                "**»** That `/config check` doesn't return any errors\n\n" +

                                "**__Message Formatting__**\n\n" +

                                "When configuring the various messages the bot can send to applicants, take note " +
                                "of the following:\n\n" +

                                "**Newlines:** To add a new line in your message, use `$NEWLINE_TOKEN`. This will " +
                                "be replaced with a line break automatically.\n\n" +

                                "**Denial reasons:** For the denial message, you must include `$REASON_TOKEN` - " +
                                "this will be replaced with the text that was provided for the denial reason you " +
                                "provide when you deny an application.\n\n" +

                                "**Acceptance invites:** For the acceptance message, you must include " +
                                "`$INVITE_TOKEN` - this will be replaced with the invite that was created for " +
                                "the applicant to join the server.\n\n" +

                                "If you have any further issues, feel free to ask Nate to bug me!\n" +
                                "-- **gdude#2002**"
                    }
                }
            }

            ephemeralSubCommand {
                name = "check"
                description = "Check whether everything is ready to go"

                requireBotPermissions(Permission.CreateInstantInvite)

                action {
                    var failures = false

                    if (config.channelId == null) {
                        respond {
                            content = "**Error:** No applications channel has been set - set one with " +
                                    "`/config channel`"
                        }

                        failures = true
                    } else {
                        @Suppress("TooGenericExceptionCaught")
                        try {
                            getApplicationsChannel()
                        } catch (e: Exception) {
                            respond {
                                content =
                                    "**Error:** Unable to get configured applications channel - set another " +
                                            "with `/config channel`\n\n" +

                                            "**Exception thrown:** ```\n$e\n```"
                            }

                            failures = true
                        }
                    }

                    if (denialReasonCount <= 0) {
                        respond {
                            content = "**Error:** No denial reasons have been configured - add up to " +
                                    "$MAX_DENIAL_REASONS with `/config add-denial`"
                        }

                        failures = true
                    }

                    if (failures) {
                        respond {
                            content = "One or more errors have been detected - please address them and run this " +
                                    "command again.\n\n" +

                                    "For more information on how this bot is configured, please use `/config help`"
                        }
                    } else {
                        respond {
                            content = "No issues detected - you're good to go!"
                        }
                    }
                }
            }

            ephemeralSubCommand(::TextArg) {
                name = "initial-response"
                description = "Check or provide the message to send when someone first applies"

                action {
                    if (arguments.text == null) {
                        respond {
                            content = "**Current response:**\n\n>>> $initialResponse"
                        }
                    } else {
                        initialResponse = arguments.text!!.replace(NEWLINE_TOKEN, "\n")

                        respond {
                            content = "Initial response message updated."
                        }
                    }
                }
            }

            ephemeralSubCommand(::TextArg) {
                name = "already-applied-response"
                description = "Check or provide the message to send when someone applies with an application open " +
                        "already"

                action {
                    if (arguments.text == null) {
                        respond {
                            content = "**Current response:**\n\n>>> $alreadyRepliedResponse"
                        }
                    } else {
                        alreadyRepliedResponse = arguments.text!!.replace(NEWLINE_TOKEN, "\n")

                        respond {
                            content = "\"Already applied\" message updated."
                        }
                    }
                }
            }

            ephemeralSubCommand(::TextArg) {
                name = "accepted-response"
                description = "Check or provide the message to send when someone's application is accepted"

                action {
                    if (arguments.text == null) {
                        respond {
                            content = "**Current response:**\n\n>>> $acceptedResponseTemplate"
                        }
                    } else {
                        if (!arguments.text!!.contains(INVITE_TOKEN)) {
                            respond {
                                content = "**Error:** The denial message must contain `$INVITE_TOKEN`, which will " +
                                        "be replaced with the invite link that the applicant can use to join the " +
                                        "server."
                            }

                            return@action
                        }

                        acceptedResponseTemplate = arguments.text!!.replace(NEWLINE_TOKEN, "\n")

                        respond {
                            content = "Application acceptance message updated."
                        }
                    }
                }
            }

            ephemeralSubCommand(::TextArg) {
                name = "denied-response"
                description = "Check or provide the message to send when someone's application is denied"

                action {
                    if (arguments.text == null) {
                        respond {
                            content = "**Current response:**\n\n>>> $deniedResponseTemplate"
                        }
                    } else {
                        if (!arguments.text!!.contains(REASON_TOKEN)) {
                            respond {
                                content = "**Error:** The denial message must contain `$REASON_TOKEN`, which will " +
                                        "be replaced with the reason for denial."
                            }

                            return@action
                        }

                        deniedResponseTemplate = arguments.text!!.replace(NEWLINE_TOKEN, "\n")

                        respond {
                            content = "Application denial message updated."
                        }
                    }
                }
            }

            ephemeralSubCommand(::DenialArg) {
                name = "add-denial"
                description = "Add a denial reason"

                action {
                    val current = getDenialReason(arguments.reasonName)

                    if (denialReasonCount >= MAX_DENIAL_REASONS && current == null) {
                        respond {
                            content = "Failed: You may not have more than $MAX_DENIAL_REASONS denial reasons."
                        }

                        return@action
                    }

                    setDenialReason(arguments.reasonName, arguments.reason)

                    channel.withTyping {
                        updateApplicationsByState()
                    }

                    respond {
                        content = if (current == null) {
                            "Denial reason created: ${arguments.reasonName}"
                        } else {
                            "Existing denial reason updated: ${arguments.reasonName}"
                        }
                    }
                }
            }

            ephemeralSubCommand(::DenialKeyArg) {
                name = "remove-denial"
                description = "Remove a denial reason"

                action {
                    val result = removeDenialReason(arguments.reasonName)

                    respond {
                        content = if (result) {
                            channel.withTyping {
                                updateApplicationsByState()
                            }

                            "Denial reason removed."
                        } else {
                            "Unable to find denial reason with name: `${arguments.reasonName}`"
                        }
                    }
                }
            }

            ephemeralSubCommand {
                name = "list-denials"
                description = "List all configured denial reasons"

                action {
                    editingPaginator {
                        config.denialReasons.entries.chunked(3) {
                            page {
                                title = "Denial reasons"
                                color = DISCORD_BLURPLE

                                description = ""

                                it.forEach { (key, value) ->
                                    description += "**Name:** $key\n"
                                    description += value.lines().joinToString("\n") { "> $it" }
                                    description += "\n\n"
                                }
                            }
                        }
                    }.send()
                }
            }

            ephemeralSubCommand(::ChannelArgs) {
                name = "channel"
                description = "Set which channel should be used for submitted applications"

                action {
                    channelId = arguments.channel.id

                    respond {
                        content = "Applications channel set to ${arguments.channel.mention}"
                    }
                }
            }
        }

        event<ButtonInteractionCreateEvent> {
            check { failIf { channelId == null } }
            check { failIf { event.interaction.channelId != channelId } }
            check { failIf { !event.interaction.componentId.startsWith(secret) } }

            action {
                val resp = event.interaction.acknowledgeEphemeralDeferredMessageUpdate()
                val parts = event.interaction.componentId.split("/")

                val applicationId = parts[1]
                val actionTest = parts[2]
                val reason = if (parts.size > 3) parts[3] else null

                val app = applications.getApplication(applicationId)
                val action = ApplicationState.byName(actionTest)

                if (app == null) {
                    resp.followUpEphemeral {
                        content = "Unknown application ID: $applicationId"
                    }

                    return@action
                }

                if (action == null) {
                    resp.followUpEphemeral {
                        content = "Unknown action: $actionTest"
                    }

                    return@action
                }

                val user = kord.getUser(app.userId)

                if (user == null) {
                    resp.followUpEphemeral {
                        "Unknown user: `${app.userId.value}`"
                    }

                    return@action
                }

                when (action) {
                    ApplicationState.ACCEPTED -> {
                        val channel: TopGuildMessageChannel = getApplicationsChannel()
                            .getGuild()
                            .channels
                            .first { it is TopGuildMessageChannel } as TopGuildMessageChannel

                        val invite = channel.createInvite {
                            uses = 1

                            this.reason = "Application accepted: ${user.tag}"
                        }

                        val msg = user.dm {
                            content = acceptedResponseTemplate
                                .replace(INVITE_TOKEN, "https://discord.gg/${invite.code}")
                        }

                        app.state = ApplicationState.ACCEPTED
                        applications.save()

                        getApplicationsChannel().getMessage(app.applicationMessageId!!).edit {
                            addApplication(app)
                        }

                        if (msg == null) {
                            resp.followUpEphemeral {
                                content = "Unable to send the invite to ${user.mention} - they may have their DMs " +
                                        "disabled.\n\n" +

                                        "**Invite:** https://discord.gg/${invite.code}"
                            }
                        } else {
                            resp.followUpEphemeral { content = "Application accepted." }
                        }
                    }

                    ApplicationState.DENIED -> {
                        val reasonText = getDenialReason(reason!!)

                        if (reasonText == null) {
                            resp.followUpEphemeral { content = "Unknown denial reason: $reason" }

                            return@action
                        }

                        val msg = user.dm {
                            content = deniedResponseTemplate
                                .replace(REASON_TOKEN, reasonText)
                        }

                        app.state = ApplicationState.DENIED
                        applications.save()

                        getApplicationsChannel().getMessage(app.applicationMessageId!!).edit {
                            addApplication(app)
                        }

                        if (msg == null) {
                            resp.followUpEphemeral {
                                content = "Unable to tell ${user.mention} that their application was denied - " +
                                        "they may have their DMs disabled.\n\n"
                            }
                        } else {
                            resp.followUpEphemeral { content = "Application denied." }
                        }
                    }

                    ApplicationState.DENIED_SILENTLY -> {
                        app.state = ApplicationState.DENIED_SILENTLY
                        applications.save()

                        getApplicationsChannel().getMessage(app.applicationMessageId!!).edit {
                            addApplication(app)
                        }

                        resp.followUpEphemeral { content = "Application denied silently." }
                    }

                    else -> resp.followUpEphemeral {
                        content = "Unsupported action: ${action.name}"
                    }
                }
            }
        }

        event<MessageCreateEvent> {
            check { failIf { event.message.author == null } }
            check { failIf { event.guildId != null } }
            check { failIf { channelId == null } }

            action {
                val author = event.message.author!!

                if (getApplicationsChannel().getGuild().members.toList().firstOrNull { it.id == author.id } != null) {
                    return@action
                }

                val currentApps = applications.getApplicationsByUser(author.id, ApplicationState.OPEN)

                if (currentApps.isNotEmpty()) {
                    event.message.respond {
                        content = alreadyRepliedResponse
                    }

                    return@action
                }

                val application = applications.createApplication(author.id, event.message.content)
                val embedMessage = getApplicationsChannel().createMessage { addApplication(application) }

                application.applicationMessageId = embedMessage.id
                applications.save()

                event.message.respond {
                    content = initialResponse
                }
            }
        }
    }

    suspend fun updateApplicationsByState(state: ApplicationState = ApplicationState.OPEN) {
        val apps = applications.getApplicationsByState(state)

        apps.forEach { app ->
            getApplicationsChannel().getMessage(app.applicationMessageId!!).edit {
                addApplication(app)
            }
        }
    }

    fun ActionRowBuilder.addButton(app: Application, toState: ApplicationState, denialReason: String? = null) {
        // token/application/action[/reason]

        var id = "${config.secretToken}/${app.applicationId}/${toState.readableName}"

        if (toState == ApplicationState.DENIED && denialReason != null) {
            id += "/$denialReason"
        }

        interactionButton(toState.style, id) {
            label = when (toState) {
                ApplicationState.OPEN -> "Reopen"
                ApplicationState.ACCEPTED -> "Accept"
                ApplicationState.DENIED_SILENTLY -> "Deny Silently"

                ApplicationState.DENIED -> "Deny: $denialReason"
            }
        }
    }

    @Suppress("MagicNumber")
    suspend fun MessageCreateBuilder.addApplication(app: Application) {
        embed {
            addApplicationEmbed(app)
        }

        if (app.state == ApplicationState.OPEN) {
            actionRow {
                addButton(app, ApplicationState.ACCEPTED)
                addButton(app, ApplicationState.DENIED_SILENTLY)
            }

            for (row in config.denialReasons.keys.chunked(5)) {
                actionRow {
                    for (key in row) {
                        addButton(app, ApplicationState.DENIED, key)
                    }
                }
            }
        }
    }

    suspend fun EmbedBuilder.addApplicationEmbed(app: Application) {
        color = app.state.color
        title = "Application: ${app.applicationId}"

        description = app.text

        footer {
            text = "${app.state.readableName} | "

            val user = kord.getUser(app.userId)

            text += user?.tag
                ?: "Unknown/deleted user: ${app.userId.value}"
        }
    }

    @Suppress("MagicNumber")
    suspend fun MessageModifyBuilder.addApplication(app: Application) {
        embed {
            addApplicationEmbed(app)
        }

        if (app.state == ApplicationState.OPEN) {
            actionRow {
                addButton(app, ApplicationState.ACCEPTED)
                addButton(app, ApplicationState.DENIED_SILENTLY)
            }

            for (row in config.denialReasons.keys.chunked(5)) {
                actionRow {
                    for (key in row) {
                        addButton(app, ApplicationState.DENIED, key)
                    }
                }
            }
        } else {
            components = mutableListOf()
        }
    }

    suspend fun getApplicationsChannel() =
        kord.getChannelOf<GuildMessageChannel>(config.channelId!!)!!

    fun setDenialReason(key: String, text: String) {
        val actualKey = config.denialReasons.keys.firstOrNull { it.equals(key, true) }
            ?: key

        config.denialReasons[actualKey] = text.replace(NEWLINE_TOKEN, "\n")

        configStorage.save()
    }

    fun removeDenialReason(key: String): Boolean {
        val storedKey = config.denialReasons.keys.firstOrNull { it.equals(key, true) }
            ?: return false

        config.denialReasons.remove(storedKey)
        configStorage.save()

        return true
    }

    fun getDenialReason(key: String): String? =
        config.denialReasons.filterKeys { it.equals(key, true) }.values.firstOrNull()

    inner class ChannelArgs : Arguments() {
        val channel by channel("channel", "Channel to send applications to")
    }

    inner class DenialKeyArg : Arguments() {
        val reasonName by string("reason-name", "Short name for the denial reason")
    }

    inner class DenialArg : Arguments() {
        val reasonName by string("name", "Short name for the denial reason")
        val reason by string("reason", "Denial reason text to send to the applicant")
    }

    inner class TextArg : Arguments() {
        val text by optionalString("text", "Text to send to the user")
    }
}
