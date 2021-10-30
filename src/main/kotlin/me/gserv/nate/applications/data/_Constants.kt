package me.gserv.nate.applications.data

import com.kotlindiscord.kord.extensions.utils.envOrNull
import java.nio.file.Path
import kotlin.io.path.div

internal val charPool: List<Char> = ('0'..'9') + ('a'..'z') + ('A'..'Z')

internal val dataDirectory = Path.of(envOrNull("DATA_DIR") ?: "./data/")

internal val applicationsDataFile = dataDirectory / "applications.json"
internal val configDataFile = dataDirectory / "config.json"
