package settingdust.lazyyyyy.util.fastutil_wrappers;

import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;

public class WrappedObject2ReferenceOpenHashMap<K, V> extends Object2ReferenceOpenHashMap<K, V> {

    private record WrappedFastEntrySet<K, V>(ObjectSet<Entry<K, V>> wrapped)
        implements FastEntrySet<K, V>, ObjectSet<Entry<K, V>> {

        @Override
        public ObjectIterator<Entry<K, V>> fastIterator() {
            if (wrapped instanceof Object2ReferenceMap.FastEntrySet) {
                return ((FastEntrySet<K, V>) wrapped).fastIterator();
            } else {
                return wrapped.iterator();
            }
        }

        @Override
        public ObjectIterator<Entry<K, V>> iterator() {return wrapped.iterator();}

        @Override
        public ObjectSpliterator<Entry<K, V>> spliterator() {return wrapped.spliterator();}

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
        public boolean add(final Entry<K, V> kvEntry) {return wrapped.add(kvEntry);}

        @Override
        public boolean remove(final Object o) {return wrapped.remove(o);}

        @Override
        public boolean containsAll(@NotNull final Collection<?> c) {return wrapped.containsAll(c);}

        @Override
        public boolean addAll(@NotNull final Collection<? extends Entry<K, V>> c) {return wrapped.addAll(c);}

        @Override
        public boolean removeAll(@NotNull final Collection<?> c) {return wrapped.removeAll(c);}

        @Override
        public boolean removeIf(@NotNull final Predicate<? super Entry<K, V>> filter) {return wrapped.removeIf(filter);}

        @Override
        public boolean retainAll(@NotNull final Collection<?> c) {return wrapped.retainAll(c);}

        @Override
        public void clear() {wrapped.clear();}
    }

    private final Object2ReferenceMap<K, V> wrapped;
    private final WrappedFastEntrySet<K, V> wrappedEntries;

    public WrappedObject2ReferenceOpenHashMap(Object2ReferenceMap<K, V> wrapped) {
        this.wrapped = wrapped;
        this.wrappedEntries = new WrappedFastEntrySet<>(wrapped.object2ReferenceEntrySet());
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> from) {
        wrapped.putAll(from);
    }

    @Override
    public V put(K key, V value) {
        return wrapped.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return wrapped.remove(key);
    }

    @Override
    public V get(Object key) {
        return wrapped.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        return wrapped.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return wrapped.containsValue(value);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return wrapped.getOrDefault(key, defaultValue);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return wrapped.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return wrapped.remove(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return wrapped.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(K key, V value) {
        return wrapped.replace(key, value);
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return wrapped.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return wrapped.computeIfPresent(key, remappingFunction);
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return wrapped.compute(key, remappingFunction);
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return wrapped.merge(key, value, remappingFunction);
    }

    @Override
    public void defaultReturnValue(V rv) {
        wrapped.defaultReturnValue(rv);
    }

    @Override
    public V defaultReturnValue() {
        return wrapped.defaultReturnValue();
    }

    @Override
    public void clear() {
        wrapped.clear();
    }

    @Override
    public int size() {
        return wrapped.size();
    }

    @Override
    public boolean isEmpty() {
        return wrapped.isEmpty();
    }

    @Override
    public Object2ReferenceMap.FastEntrySet<K, V> object2ReferenceEntrySet() {
        return wrappedEntries;
    }

    @Override
    public ObjectSet<K> keySet() {
        return wrapped.keySet();
    }

    @Override
    public ReferenceCollection<V> values() {
        return wrapped.values();
    }

    @Override
    public int hashCode() {
        return wrapped.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return wrapped.equals(other);
    }

    @Override
    public ObjectSet<Map.Entry<K, V>> entrySet() {
        return wrapped.entrySet();
    }
}
