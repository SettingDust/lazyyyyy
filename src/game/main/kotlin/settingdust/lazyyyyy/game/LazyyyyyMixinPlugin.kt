package settingdust.lazyyyyy.game

import org.apache.logging.log4j.LogManager
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo
import settingdust.lazyyyyy.util.LoaderAdapter

class LazyyyyyMixinPlugin : IMixinConfigPlugin {
    companion object {
        private val PLATFORM_PREFIXES = setOf("fabric", "forge", "neoforge")
        private val MOD_REQUIRED_MIXINS = mapOf(
            "dynamic_trees" to "dynamictrees"
        )
    }

    lateinit var mixinPackage: String
        private set

    val logger = LogManager.getLogger()

    init {
        LazyyyyyMixinConfig.load()
    }

    override fun onLoad(mixinPackage: String) {
        this.mixinPackage = mixinPackage
    }

    override fun getRefMapperConfig() = null

    override fun shouldApplyMixin(targetClassName: String, mixinClassName: String): Boolean {
        if (LoaderAdapter.get().hasEarlyErrors()) return false

        if (!mixinClassName.startsWith(mixinPackage)) return true

        var relativeName = mixinClassName.removePrefix("${mixinPackage}.")
        // Remove platform prefixes
        for (prefix in PLATFORM_PREFIXES) {
            if (relativeName.startsWith("$prefix.")) {
                relativeName = relativeName.removePrefix("$prefix.")
                break
            }
        }

        // Check if mixin requires a specific mod to be loaded
        for ((pathSegment, modId) in MOD_REQUIRED_MIXINS) {
            if (relativeName.contains(pathSegment)) {
                if (!LoaderAdapter.get().isModLoaded(modId)) {
                    logger.debug("Skipping mixin '$mixinClassName': mod '$modId' is not loaded")
                    return false
                }
            }
        }

        return LazyyyyyMixinConfig.shouldApplyFeature(relativeName)
    }

    override fun acceptTargets(myTargets: Set<String>, otherTargets: Set<String>) {}

    override fun getMixins() = emptyList<String>()

    override fun preApply(
        targetClassName: String,
        targetClass: ClassNode,
        mixinClassName: String,
        mixinInfo: IMixinInfo
    ) {
    }

    override fun postApply(
        targetClassName: String,
        targetClass: ClassNode,
        mixinClassName: String,
        mixinInfo: IMixinInfo
    ) {
    }
}
