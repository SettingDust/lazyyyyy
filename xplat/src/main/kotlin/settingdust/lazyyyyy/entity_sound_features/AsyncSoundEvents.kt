package settingdust.lazyyyyy.entity_sound_features

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import traben.entity_sound_features.ESFVariantSupplier
import java.util.function.Consumer

fun asyncGetVariantSupplier(
    wrapped: () -> ESFVariantSupplier?,
    consumer: Consumer<ESFVariantSupplier?>
) = CoroutineScope(Dispatchers.IO + CoroutineName("Lazy ESF Variant")).launch(start = CoroutineStart.LAZY) {
    consumer.accept(wrapped())
}

fun Job.joinBlocking() = runBlocking { join() }