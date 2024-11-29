package settingdust.lazyyyyy

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import org.apache.logging.log4j.LogManager

object Lazyyyyy {
    const val ID = "lazyyyyy"

    val logger = LogManager.getLogger()

    val scope = CoroutineScope(SupervisorJob())
    val mainThreadContext by lazy { Minecraft.getInstance().asCoroutineDispatcher() }
    val mainThreadScope by lazy { CoroutineScope(mainThreadContext) }

    val clientLaunched = MutableSharedFlow<Unit>(1)

    fun init() {
        ClientTickEvents.START_CLIENT_TICK.register {
            if (it.overlay == null) {
                scope.launch { clientLaunched.emit(Unit) }
            }
        }
    }
}