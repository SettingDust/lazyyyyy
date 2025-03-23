package settingdust.lazyyyyy.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun executeOffThread(block: Runnable) {
    CoroutineScope(Dispatchers.IO).launch { block.run() }
}