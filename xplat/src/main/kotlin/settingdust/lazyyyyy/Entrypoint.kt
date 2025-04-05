package settingdust.lazyyyyy

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import net.minecraft.client.Minecraft
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import settingdust.lazyyyyy.minecraft.pack_resources_cache.PackResourcesCache
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.io.path.writeText

object Lazyyyyy {
    const val ID = "lazyyyyy"

    val logger = LogManager.getLogger()

    val mainThreadContext by lazy { Minecraft.getInstance().asCoroutineDispatcher() }

    fun init() {
        requireNotNull(Config.general)
    }

    @OptIn(ExperimentalSerializationApi::class)
    object Config {
        @Serializable
        data class GeneralConfig(
            val debug: Debug = Debug()
        ) {
            @Serializable
            data class Debug(val packCache: Boolean = false)
        }

        private val json = Json {
            prettyPrint = true
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

        var general: GeneralConfig = GeneralConfig()
            private set

        init {
            val generalPath = PlatformService.configDir.resolve("lazyyyyy.general.json")
            if (!generalPath.exists()) {
                generalPath.createFile()
                generalPath.writeText("{}")
            }
            general = json.decodeFromStream(generalPath.inputStream())
            json.encodeToStream(general, generalPath.outputStream())
        }
    }

    class DebugLogging(val logger: Logger, val condition: () -> Boolean) {
        companion object {
            val packCache = DebugLogging(PackResourcesCache.logger) { Config.general.debug.packCache }
        }

        fun whenDebug(block: Logger.() -> Unit) {
            if (condition()) {
                block(logger)
            }
        }
    }
}