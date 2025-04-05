package settingdust.lazyyyyy

import java.nio.file.Path
import java.util.*

interface PlatformService {
    companion object : PlatformService by ServiceLoader.load(PlatformService::class.java).first()

    val configDir: Path

    fun isModLoaded(modId: String): Boolean

    fun hasEarlyError(): Boolean
}