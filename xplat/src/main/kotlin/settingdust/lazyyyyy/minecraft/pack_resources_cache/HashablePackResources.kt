package settingdust.lazyyyyy.minecraft.pack_resources_cache

import com.google.common.hash.HashCode

interface HashablePackResources {
    fun `lazyyyyy$getHash`(): HashCode
}