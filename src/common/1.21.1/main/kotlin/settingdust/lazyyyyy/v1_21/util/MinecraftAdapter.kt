package settingdust.lazyyyyy.v1_21.util

import net.minecraft.resources.ResourceLocation
import settingdust.lazyyyyy.util.MinecraftAdapter

class MinecraftAdapter : MinecraftAdapter {
    override fun id(namespace: String, path: String) = ResourceLocation.fromNamespaceAndPath(namespace, path)
}