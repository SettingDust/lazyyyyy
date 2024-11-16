package settingdust.lazyyyyy

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

object Lazyyyyy {
    const val ID = "lazyyyyy"
    val scope = CoroutineScope(SupervisorJob())
}

fun init() {

}