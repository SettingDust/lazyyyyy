package settingdust.lazyyyyy

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft

object Lazyyyyy {
    const val ID = "lazyyyyy"
    val scope = CoroutineScope(SupervisorJob())
    val mainThreadContext by lazy { Minecraft.getInstance().asCoroutineDispatcher() }
    val mainThreadScope by lazy { CoroutineScope(mainThreadContext) }

    val clientLaunched = CompletableDeferred<Unit>()

    fun init() {
        ClientTickEvents.START_CLIENT_TICK.register {
            if (it.overlay == null) {
                clientLaunched.complete(Unit)
            }
        }
    }
}