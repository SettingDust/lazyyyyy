package settingdust.lazyyyyy

import java.util.*

interface PlatformService {
    companion object : PlatformService by ServiceLoader.load(PlatformService::class.java).first()

    fun isModLoaded(modId: String): Boolean

    fun hasEarlyError(): Boolean
}