package settingdust.lazyyyyy

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import net.minecraft.client.Minecraft
import org.apache.logging.log4j.LogManager

object Lazyyyyy {
    const val ID = "lazyyyyy"

    val logger = LogManager.getLogger()

    val scope = CoroutineScope(SupervisorJob())
    val mainThreadContext by lazy { Minecraft.getInstance().asCoroutineDispatcher() }

    fun init() {
    }
}