package org.opencds.cqf.tooling.common;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A thread-safe and ordered implementation of a linked hash map.
 * This data structure combines the features of a {@link java.util.HashMap}
 * with the order-preserving characteristics of a {@link java.util.LinkedHashMap}.
 *
 * <p>Instances of this class allow concurrent access and modification
 * from multiple threads while preserving the insertion order of elements.
 * It is suitable for scenarios where you need to maintain elements in
 * the order they were added, while ensuring thread safety.
 *
 * <p>Operations such as {@code get}, {@code put}, and {@code remove} are
 * thread-safe and can be performed by multiple threads concurrently.
 *
 * <p>This class can be used as a drop-in replacement for a standard
 * {@link java.util.LinkedHashMap} when thread safety is required.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 *
 * @see java.util.LinkedHashMap
 * @see java.util.concurrent.ConcurrentHashMap
 */
public class ConcurrentLinkedHashMap<K, V> extends AbstractMap<K, V> implements Map<K, V> {
    private final LinkedHashMap<K, V> map;
    private final Lock lock = new ReentrantLock();

    public ConcurrentLinkedHashMap() {
        this.map = new LinkedHashMap<>();
    }

    @Override
    public V get(Object key) {
        try {
            lock.lock();
            return map.get(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public V put(K key, V value) {
        try {
            lock.lock();
            return map.put(key, value);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public V remove(Object key) {
        try {
            lock.lock();
            return map.remove(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        try {
            lock.lock();
            return map.entrySet();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        try {
            lock.lock();
            return map.size();
        } finally {
            lock.unlock();
        }
    }

}

