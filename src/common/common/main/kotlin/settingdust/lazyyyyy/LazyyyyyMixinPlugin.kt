package settingdust.lazyyyyy

import org.apache.logging.log4j.LogManager
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo
import settingdust.lazyyyyy.util.LoaderAdapter

class LazyyyyyMixinPlugin : IMixinConfigPlugin {
    companion object {
        private val PLATFORM_PREFIXES = setOf("fabric", "forge", "neoforge")
    }
    
    lateinit var mixinPackage: String
        private set

    val logger = LogManager.getLogger()

    private val mixinConfig = LazyyyyyMixinConfig(logger)

    init {
        // Features will be registered later
        // Load config
        mixinConfig.load()
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
        
        return mixinConfig.shouldApplyFeature(relativeName)
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
