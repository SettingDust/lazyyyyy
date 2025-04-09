package settingdust.lazyyyyy.minecraft.pack_resources_cache

import com.google.common.hash.HashCode

@JvmDefaultWithoutCompatibility
interface HashablePackResources {
    @Suppress("FunctionName")
    fun `lazyyyyy$getHash`(): HashCode?
}