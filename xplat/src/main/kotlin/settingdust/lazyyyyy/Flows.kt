package settingdust.lazyyyyy

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

fun <T> Flow<T>.partition(
    scope: CoroutineScope,
    predicate: (T) -> Boolean
): Pair<Flow<T>, Flow<T>> {
    val sharedFlow = this.map { item ->
        Pair(item, predicate(item))
    }.shareIn(scope, SharingStarted.Lazily)

    val matchingFlow = sharedFlow.filter { it.second }.map { it.first }
    val nonMatchingFlow = sharedFlow.filter { !it.second }.map { it.first }

    return Pair(matchingFlow, nonMatchingFlow)
}