package com.krickert.search.crawler.manager;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class represents a concurrent, unbounded, non-repeating queue.
 *
 * @param <E> The type of elements in the queue.
 */
public class ConcurrentUnboundedNonRepeatingQueue<E> {
    private final ConcurrentHashMap<E, Boolean> map = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<E> queue = new ConcurrentLinkedQueue<>();

    /**
     * Adds an element to the concurrent, unbounded, non-repeating queue if it does not already exist.
     *
     * @param e The element to be added to the queue.
     * @return {@code true} if the element was added to the queue, {@code false} otherwise.
     */
    public boolean add(E e) {
        // If the element was not in the map, add it to the queue
        return map.putIfAbsent(e, Boolean.TRUE) == null && queue.add(e);
    }

    /**
     * Retrieves and removes the head of this queue, or returns null if this queue is empty.
     *
     * @return The head of this queue, or null if this queue is empty.
     */
    public E poll() {
        E e = queue.poll();
        if (e != null) {
            map.remove(e);
        }
        return e;
    }

    /**
     * Retrieves, but does not remove, the head of this queue.
     *
     * @return the head of the queue, or {@code null} if the queue is empty.
     */
    public E peek() {
        return queue.peek();
    }

    /**
     * Adds all elements from the given collection to this queue.
     *
     * @param collection The collection containing the elements to be added.
     * @param <E>        The type of elements in the collection.
     */
    public void addAll(Collection<? extends E> collection) {
        for (E e : collection) {
            this.add(e);
        }
    }
}