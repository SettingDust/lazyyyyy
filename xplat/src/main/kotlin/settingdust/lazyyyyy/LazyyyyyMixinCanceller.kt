package settingdust.lazyyyyy

import com.bawnorton.mixinsquared.api.MixinCanceller

class LazyyyyyMixinCanceller : MixinCanceller {
    override fun shouldCancel(
        targetClassNames: List<String>,
        mixinClassName: String
    ) = when (mixinClassName) {
        "com.ishland.c2me.opts.allocs.mixin.MixinUtil" -> true
        else -> false
    }
}