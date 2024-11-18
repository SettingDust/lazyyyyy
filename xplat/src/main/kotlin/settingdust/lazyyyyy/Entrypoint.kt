package settingdust.lazyyyyy

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import net.minecraft.client.Minecraft

object Lazyyyyy {
    const val ID = "lazyyyyy"
    val scope = CoroutineScope(SupervisorJob())
    val mainThreadContext by lazy { Minecraft.getInstance().asCoroutineDispatcher() }
    val mainThreadScope by lazy { CoroutineScope(mainThreadContext) }
}

fun init() {

}