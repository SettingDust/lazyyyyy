package settingdust.lazyyyyy

import kotlinx.coroutines.launch

fun executeOffThread(block: Runnable) {
    Lazyyyyy.scope.launch { block.run() }
}