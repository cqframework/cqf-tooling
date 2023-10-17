package org.opencds.cqf.tooling.common;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * A custom thread-safe and ordered implementation of a linked hash set.
 * This class provides a combination of the features of a set with
 * the order-preserving characteristics of a linked list.
 *
 * <p>Instances of this class allow concurrent access and modification
 * from multiple threads while preserving the order in which elements were added.
 * It is suitable for scenarios where you need to maintain elements in the
 * order they were inserted, while ensuring thread safety.
 *
 * <p>Operations such as {@code add}, {@code remove}, and {@code contains}
 * are thread-safe and can be performed by multiple threads concurrently.
 *
 * <p>This custom class is not part of the standard Java Collections Framework
 * but can be used as a thread-safe alternative to a regular HashSet when
 * order preservation is required.
 *
 * @param <E> the type of elements maintained by this set
 *
 * @see java.util.HashSet
 * @see java.util.concurrent.ConcurrentHashMap
 * @see java.util.concurrent.ConcurrentLinkedQueue
 */
public class ConcurrentLinkedHashSet<E> extends AbstractSet<E> implements Set<E> {
    private final ConcurrentMap<E, Boolean> map = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<E> queue = new ConcurrentLinkedQueue<>();

    @Override
    public Iterator<E> iterator() {
        return queue.iterator();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean add(E e) {
        if (map.putIfAbsent(e, Boolean.TRUE) == null) {
            queue.add(e);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        if (map.remove(o) != null) {
            queue.remove(o);
            return true;
        }
        return false;
    }

    // You can override other methods like addAll, containsAll, etc. if needed
}
