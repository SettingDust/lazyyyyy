package settingdust.lazyyyyy.toomanyplayers

import kotlinx.coroutines.launch
import settingdust.lazyyyyy.Lazyyyyy

fun executeOffThread(block: Runnable) {
    Lazyyyyy.scope.launch { block.run() }
}