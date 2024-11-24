package settingdust.lazyyyyy.entity_sound_features

import it.unimi.dsi.fastutil.objects.Object2ReferenceMap
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectIterator
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import settingdust.lazyyyyy.Lazyyyyy
import traben.entity_sound_features.ESFVariantSupplier
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function

class WrappedFastEntrySet<K, V>(val wrapped: ObjectSet<Object2ReferenceMap.Entry<K, V>>) :
    Object2ReferenceMap.FastEntrySet<K, V>, ObjectSet<Object2ReferenceMap.Entry<K, V>> by wrapped {
    override fun fastIterator(): ObjectIterator<Object2ReferenceMap.Entry<K, V>> =
        if (wrapped is Object2ReferenceMap.FastEntrySet<K, V>) wrapped.fastIterator()
        else wrapped.iterator()
}

class WrappedObject2ReferenceOpenHashMap<K, V>(val wrapped: Object2ReferenceMap<K, V>) :
    Object2ReferenceOpenHashMap<K, V>() {
    private val wrappedEntries by lazy { WrappedFastEntrySet(wrapped.object2ReferenceEntrySet()) }

    override fun putAll(from: Map<out K?, V?>) = wrapped.putAll(from)

    override fun put(key: K?, value: V?) = wrapped.put(key, value)

    override fun remove(key: K?) = wrapped.remove(key)

    override fun get(key: K?) = wrapped[key]

    override fun containsKey(key: K?) = wrapped.containsKey(key)

    override fun containsValue(value: V?) = wrapped.containsValue(value)

    override fun getOrDefault(key: Any?, defaultValue: V?) = wrapped.getOrDefault(key, defaultValue)

    override fun putIfAbsent(key: K?, value: V?) = wrapped.putIfAbsent(key, value)

    override fun remove(key: K?, value: V?) = wrapped.remove(key, value)

    override fun replace(key: K?, oldValue: V?, newValue: V?) = wrapped.replace(key, oldValue, newValue)

    override fun replace(key: K?, value: V?) = wrapped.replace(key, value)

    override fun computeIfAbsent(key: K?, mappingFunction: Function<in K, out V?>) =
        wrapped.computeIfAbsent(key, mappingFunction)

    override fun computeIfPresent(key: K?, remappingFunction: BiFunction<in K, in V & Any, out V?>) =
        wrapped.computeIfPresent(key, remappingFunction)

    override fun compute(key: K?, remappingFunction: BiFunction<in K, in V?, out V?>) =
        wrapped.compute(key, remappingFunction)

    override fun merge(key: K?, value: V & Any, remappingFunction: BiFunction<in V & Any, in V & Any, out V?>) =
        wrapped.merge(key, value, remappingFunction)

    override fun defaultReturnValue(v: V) = wrapped.defaultReturnValue(v)

    override fun defaultReturnValue() = wrapped.defaultReturnValue()

    override fun clear() = wrapped.clear()

    override val size
        get() = wrapped.size

    override fun isEmpty() = wrapped.isEmpty()

    override fun object2ReferenceEntrySet() = wrappedEntries

    override val keys
        get() = wrapped.keys

    override val values
        get() = wrapped.values

    override fun hashCode() = wrapped.hashCode()

    override fun equals(other: Any?) = wrapped == other

    override val entries
        get() = wrapped.entries
}

fun asyncGetVariantSupplier(
    wrapped: () -> ESFVariantSupplier?,
    consumer: Consumer<ESFVariantSupplier?>
): Job {
    val loading = Lazyyyyy.scope.launch(Dispatchers.IO, start = CoroutineStart.LAZY) {
        consumer.accept(wrapped())
    }

    Lazyyyyy.scope.launch { Lazyyyyy.clientLaunched.collectLatest { loading.start() } }

    return loading
}

fun Job.joinBlocking() = runBlocking { join() }