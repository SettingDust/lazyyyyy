package settingdust.lazyyyyy.fabric

import com.pixelstorm.better_log4j_config.BetterLog4jConfig
import settingdust.lazyyyyy.Lazyyyyy
import settingdust.lazyyyyy.config.LazyyyyyEarlyConfig
import settingdust.lazyyyyy.fabric.config.FabricEarlyConfig
import settingdust.lazyyyyy.fabric.transformer.better_log4j_config.earlier_init.BetterLog4jConfigTransformer
import settingdust.lazyyyyy.faster_mixin.FasterMixinEntrypoint
import settingdust.lazyyyyy.faster_module_resolver.FasterModuleResolverEntrypoint
import settingdust.preloading_tricks.api.PreloadingEntrypoint
import settingdust.preloading_tricks.util.class_transform.ClassTransformBootstrap

class LazyyyyyFabricEntrypoint : PreloadingEntrypoint {
    init {
        val config = LazyyyyyEarlyConfig.instance()

        config.registerDisableCondition(
            LazyyyyyEarlyConfig.FASTER_MODULE_RESOLVER,
            { true },  // always disabled on fabric
            "fabric has no usage of java module"
        )

        // Load configuration after registering conditions
        config.load()

        // Initialize Fabric-specific config
        val fabricConfig = FabricEarlyConfig.instance()
        fabricConfig.load()

        if (fabricConfig.isFeatureEnabled(FabricEarlyConfig.BETTER_LOG4J_CONFIG_EARLIER_INIT)) {
            Lazyyyyy.LOGGER.info("Applying ${FabricEarlyConfig.BETTER_LOG4J_CONFIG_EARLIER_INIT}");
            BetterLog4jConfig().onPreLaunch()
            ClassTransformBootstrap.INSTANCE.transformerManager.addTransformer(BetterLog4jConfigTransformer::class.qualifiedName!!)
        }

        FasterModuleResolverEntrypoint()
        FasterMixinEntrypoint.init(javaClass.getClassLoader())
    }
}