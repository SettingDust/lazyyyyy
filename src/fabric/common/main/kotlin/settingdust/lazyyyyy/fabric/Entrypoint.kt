package settingdust.lazyyyyy.fabric

import settingdust.lazyyyyy.Lazyyyyy
import settingdust.lazyyyyy.util.Entrypoint

object LazyyyyyFabric {
    init {
        requireNotNull(Lazyyyyy)
        Entrypoint.construct()
    }

    fun init() {
        Entrypoint.init()
    }

    fun clientInit() {
        Entrypoint.clientInit()
    }
}
