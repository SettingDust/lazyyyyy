package settingdust.lazyyyyy.util.fastutil_wrappers;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.function.*;


public class WrappedObject2IntOpenHashMap<K> extends Object2IntOpenHashMap<K> {
    private record WrappedFastEntrySet<K>(ObjectSet<Object2IntMap.Entry<K>> wrapped)
        implements FastEntrySet<K>, ObjectSet<Object2IntMap.Entry<K>> {

        @Override
        public ObjectIterator<Entry<K>> fastIterator() {
            if (wrapped instanceof Object2IntMap.FastEntrySet) {
                return ((Object2IntMap.FastEntrySet<K>) wrapped).fastIterator();
            } else {
                return wrapped.iterator();
            }
        }

        @Override
        public ObjectIterator<Entry<K>> iterator() {return wrapped.iterator();}

        @Override
        public ObjectSpliterator<Entry<K>> spliterator() {return wrapped.spliterator();}

        @Override
        public int size() {return wrapped.size();}

        @Override
        public boolean isEmpty() {return wrapped.isEmpty();}

        @Override
        public boolean contains(final Object o) {return wrapped.contains(o);}

        @Override
        public @NotNull Object[] toArray() {return wrapped.toArray();}

        @Override
        public @NotNull <T> T[] toArray(@NotNull final T[] a) {return wrapped.toArray(a);}

        @Override
        public <T> T[] toArray(@NotNull final IntFunction<T[]> generator) {return wrapped.toArray(generator);}

        @Override
        public boolean add(final Entry<K> kvEntry) {return wrapped.add(kvEntry);}

        @Override
        public boolean remove(final Object o) {return wrapped.remove(o);}

        @Override
        public boolean containsAll(@NotNull final Collection<?> c) {return wrapped.containsAll(c);}

        @Override
        public boolean addAll(@NotNull final Collection<? extends Entry<K>> c) {return wrapped.addAll(c);}

        @Override
        public boolean removeAll(@NotNull final Collection<?> c) {return wrapped.removeAll(c);}

        @Override
        public boolean removeIf(@NotNull final Predicate<? super Entry<K>> filter) {return wrapped.removeIf(filter);}

        @Override
        public boolean retainAll(@NotNull final Collection<?> c) {return wrapped.retainAll(c);}

        @Override
        public void clear() {wrapped.clear();}
    }

    private final Object2IntMap<K> wrapped;
    private final WrappedFastEntrySet<K> wrappedEntries;

    public WrappedObject2IntOpenHashMap(Object2IntMap<K> wrapped) {
        this.wrapped = wrapped;
        this.wrappedEntries = new WrappedFastEntrySet<>(wrapped.object2IntEntrySet());
    }

    @Deprecated
    @Override
    public Integer merge(
        final K key,
        final Integer value,
        final BiFunction<? super Integer, ? super Integer, ? extends Integer> remappingFunction
    ) {return wrapped.merge(key, value, remappingFunction);}

    @Deprecated
    @Override
    public Integer replace(final K key, final Integer value) {return wrapped.replace(key, value);}

    @Deprecated
    @Override
    public boolean replace(final K key, final Integer oldValue, final Integer newValue) {
        return wrapped.replace(
            key,
            oldValue,
            newValue
        );
    }

    @Deprecated
    @Override
    public boolean remove(final Object key, final Object value) {return wrapped.remove(key, value);}

    @Deprecated
    @Override
    public Integer putIfAbsent(final K key, final Integer value) {return wrapped.putIfAbsent(key, value);}

    @Deprecated
    @Override
    public int mergeInt(
        final K key,
        final int value,
        final BiFunction<? super Integer, ? super Integer, ? extends Integer> remappingFunction
    ) {return wrapped.mergeInt(key, value, remappingFunction);}

    @Override
    public int mergeInt(
        final K key,
        final int value,
        final java.util.function.IntBinaryOperator remappingFunction
    ) {return wrapped.mergeInt(key, value, remappingFunction);}

    @Override
    public int merge(
        final K key,
        final int value,
        final BiFunction<? super Integer, ? super Integer, ? extends Integer> remappingFunction
    ) {return wrapped.merge(key, value, remappingFunction);}

    @Override
    public int computeInt(
        final K key,
        final BiFunction<? super K, ? super Integer, ? extends Integer> remappingFunction
    ) {return wrapped.computeInt(key, remappingFunction);}

    @Override
    public int computeIntIfPresent(
        final K key,
        final BiFunction<? super K, ? super Integer, ? extends Integer> remappingFunction
    ) {return wrapped.computeIntIfPresent(key, remappingFunction);}

    @Deprecated
    @Override
    public int computeIntIfAbsentPartial(
        final K key,
        final Object2IntFunction<? super K> mappingFunction
    ) {return wrapped.computeIntIfAbsentPartial(key, mappingFunction);}

    @Override
    public int computeIfAbsent(
        final K key,
        final Object2IntFunction<? super K> mappingFunction
    ) {return wrapped.computeIfAbsent(key, mappingFunction);}

    @Deprecated
    @Override
    public int computeIntIfAbsent(
        final K key,
        final ToIntFunction<? super K> mappingFunction
    ) {return wrapped.computeIntIfAbsent(key, mappingFunction);}

    @Override
    public int computeIfAbsent(
        final K key,
        final ToIntFunction<? super K> mappingFunction
    ) {return wrapped.computeIfAbsent(key, mappingFunction);}

    @Override
    public int replace(final K key, final int value) {return wrapped.replace(key, value);}

    @Override
    public boolean replace(final K key, final int oldValue, final int newValue) {
        return wrapped.replace(
            key,
            oldValue,
            newValue
        );
    }

    @Override
    public boolean remove(final Object key, final int value) {return wrapped.remove(key, value);}

    @Override
    public int putIfAbsent(final K key, final int value) {return wrapped.putIfAbsent(key, value);}

    @Deprecated
    @Override
    public Integer getOrDefault(final Object key, final Integer defaultValue) {
        return wrapped.getOrDefault(
            key,
            defaultValue
        );
    }

    @Override
    public int getOrDefault(final Object key, final int defaultValue) {return wrapped.getOrDefault(key, defaultValue);}

    @Override
    public void forEach(final BiConsumer<? super K, ? super Integer> consumer) {wrapped.forEach(consumer);}

    @Deprecated
    @Override
    public boolean containsValue(final Object value) {return wrapped.containsValue(value);}

    @Override
    public boolean containsValue(final int value) {return wrapped.containsValue(value);}

    @Override
    public boolean containsKey(final Object key) {return wrapped.containsKey(key);}

    @Override
    public IntCollection values() {return wrapped.values();}

    @Override
    public ObjectSet<K> keySet() {return wrapped.keySet();}

    @Deprecated
    @Override
    public Integer remove(final Object key) {return wrapped.remove(key);}

    @Deprecated
    @Override
    public Integer get(final Object key) {return wrapped.get(key);}

    @Deprecated
    @Override
    public Integer put(final K key, final Integer value) {return wrapped.put(key, value);}

    @Deprecated
    @Override
    public ObjectSet<Map.Entry<K, Integer>> entrySet() {return wrapped.entrySet();}

    @Override
    public FastEntrySet<K> object2IntEntrySet() {return wrappedEntries;}

    @Override
    public int defaultReturnValue() {return wrapped.defaultReturnValue();}

    @Override
    public void defaultReturnValue(final int rv) {wrapped.defaultReturnValue(rv);}

    @Override
    public void clear() {wrapped.clear();}

    @Override
    public int size() {return wrapped.size();}

    @Override
    public int applyAsInt(final K operand) {return wrapped.applyAsInt(operand);}

    @Override
    public int put(final K key, final int value) {return wrapped.put(key, value);}

    @Override
    public int getInt(final Object key) {return wrapped.getInt(key);}

    @Override
    public int removeInt(final Object key) {return wrapped.removeInt(key);}
}
