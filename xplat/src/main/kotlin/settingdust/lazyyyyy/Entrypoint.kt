package settingdust.lazyyyyy

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.debug.DebugProbes
import net.minecraft.client.Minecraft
import org.apache.logging.log4j.LogManager

object Lazyyyyy {
    const val ID = "lazyyyyy"

    val logger = LogManager.getLogger()

    val mainThreadContext by lazy { Minecraft.getInstance().asCoroutineDispatcher() }

    fun init() {
        DebugProbes.install()
    }
}