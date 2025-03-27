package settingdust.lazyyyyy.util

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.selects.select
import kotlin.coroutines.CoroutineContext

fun CoroutineContext.currentCoroutineName() = this[CoroutineName.Key]?.name

fun CoroutineContext.withCoroutineNameSuffix(name: String) =
    CoroutineName(currentCoroutineName() + name)

suspend fun CoroutineScope.withCoroutineNameSuffix(name: String) =
    currentCoroutineContext().withCoroutineNameSuffix(name)

suspend fun <T> race(vararg racers: suspend CoroutineScope.() -> T): T {
    require(racers.isNotEmpty()) { "A race needs racers." }
    return coroutineScope {
        @Suppress("RemoveExplicitTypeArguments")
        select<T> {
            @OptIn(ExperimentalCoroutinesApi::class)
            val racersAsyncList = racers.map {
                async(start = CoroutineStart.UNDISPATCHED, block = it)
            }
            for (racer in racersAsyncList) {
                racer.onAwait { resultOfWinner: T ->
                    for (deferred in racersAsyncList) {
                        deferred.cancel()
                    }
                    return@onAwait resultOfWinner
                }
            }
        }
    }
}