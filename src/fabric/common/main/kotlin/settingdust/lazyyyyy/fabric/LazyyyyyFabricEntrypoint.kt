package settingdust.lazyyyyy.fabric

import settingdust.lazyyyyy.config.LazyyyyyEarlyConfig
import settingdust.lazyyyyy.faster_mixin.FasterMixinEntrypoint
import settingdust.lazyyyyy.faster_module_resolver.FasterModuleResolverEntrypoint
import settingdust.preloading_tricks.api.PreloadingEntrypoint

class LazyyyyyFabricEntrypoint : PreloadingEntrypoint {
    init {
        LazyyyyyEarlyConfig.instance().registerDisableCondition(
            "faster_module_resolver",
            { true },  // always disabled on fabric
            "fabric has no usage of java module"
        )

        FasterModuleResolverEntrypoint()
        FasterMixinEntrypoint.init(javaClass.getClassLoader())
    }
}