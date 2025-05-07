package settingdust.lazyyyyy

import com.bawnorton.mixinsquared.api.MixinCanceller
import org.apache.logging.log4j.LogManager

class LazyyyyyMixinCanceller : MixinCanceller {
    val logger = LogManager.getLogger()

    override fun shouldCancel(
        targetClassNames: List<String>,
        mixinClassName: String
    ) = when {
        mixinClassName == "com.ishland.c2me.opts.allocs.mixin.MixinUtil" -> true
        mixinClassName.startsWith("org.embeddedt.modernfix.common.mixin.perf.dynamic_entity_renderers") -> {
            logger.info("Disabled ModernFix perf.dynamic_entity_renderers")
            true
        }
        mixinClassName.startsWith("org.embeddedt.modernfix.common.mixin.perf.resourcepacks") -> {
            logger.info("Disabled ModernFix perf.resourcepacks")
            true
        }
        mixinClassName.startsWith("me.thosea.badoptimizations.mixin.renderer") -> {
            logger.info("Disabled BadOptimizations `enable_entity_renderer_caching` and `enable_block_entity_renderer_caching`")
            true
        }
        else -> false
    }
}