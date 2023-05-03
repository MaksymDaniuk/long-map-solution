package de.comparus.opensource.longmap;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LongMapImpl<V> implements LongMap<V> {
    private static final int DEFAULT_CAPACITY = 16;
    static final int MAXIMUM_CAPACITY = 1 << 20;
    private static final float RESIZE_THRESHOLD = 1.0f;
    private Node<V>[] table;
    private long size;

    public LongMapImpl() {
        this(DEFAULT_CAPACITY);
    }

    public LongMapImpl(int initialCapacity) {
        verifyCapacity(initialCapacity);
        this.table = new Node[initialCapacity <= MAXIMUM_CAPACITY ? initialCapacity : MAXIMUM_CAPACITY];
    }

    private void verifyCapacity(int capacity) {
        if(capacity <= 0){
            throw new IllegalArgumentException("Capacity must be positive");
        }
    }

    public static int calculateIndex(long key, int capacity){
        return (int) Math.abs(key % capacity);
    }

    @Override
    public V put(long key, V value) {
        resizeIfNeeded();
        return putOnTable(this.table, key, value);
    }

    private V putOnTable(Node<V>[] table, long key, V value) {
        Node<V> newNode = Node.newInstance(key, value);
        int index = calculateIndex(key, table.length);
        if(table[index] == null){
            table[index] = newNode;
            size++;
            return null;
        } else {
           return addOrChangeNodeInList(table[index], newNode);
        }
    }

    private V addOrChangeNodeInList(Node<V> head, Node<V> newNode) {
        Node<V> currentNode = head;
        while (true){
            if(currentNode.key == newNode.key){
                V previousValue = currentNode.value;
                currentNode.value = newNode.value;
                return previousValue;
            }
            if(currentNode.next == null) break;
             currentNode = currentNode.next;
        }
        currentNode.next = newNode;
        size++;
        return null;
    }

    private void resizeIfNeeded() {
        if(table.length != MAXIMUM_CAPACITY && size / (float) table.length >= RESIZE_THRESHOLD){
            resizeTable(2 * table.length <= MAXIMUM_CAPACITY ? 2 * table.length : MAXIMUM_CAPACITY);
        }
    }

    private void resizeTable(int newCapacity) {
        verifyCapacity(newCapacity);
        Node<V>[] newTable = new Node[newCapacity];
        size = 0;
        for(Node<V> head : table){
            Node<V> current = head;
            while(current != null){
                putOnTable(newTable, current.key, current.value);
                current = current.next;
            }
        }
        this.table = newTable;
    }

    @Override
    public V get(long key) {
        Objects.requireNonNull(key);
        int index = calculateIndex(key, table.length);
        Node<V> currentNode = table[index];
        while (currentNode != null){
            if(currentNode.key == key){
                return currentNode.value;
            }
            currentNode = currentNode.next;
        }
        return null;
    }

    @Override
    public V remove(long key) {
        Objects.requireNonNull(key);
        int index = calculateIndex(key, table.length);
        Node<V> currentNode = table[index];
        if(currentNode != null){
            if(currentNode.key == key){
                V value = currentNode.value;
                table[index] = currentNode.next;
                size--;
                return value;
            }
            while (currentNode.next != null){
                if(currentNode.next.key == key){
                    V value = currentNode.next.value;
                    currentNode.next = currentNode.next.next;
                    size--;
                    return value;
                }
                currentNode = currentNode.next;
            }
        }
        return null;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(long key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(V value) {
        for(Node<V> head : table){
            Node<V> currentNode = head;
            while (currentNode != null) {
                if(value == null && currentNode.value == null) return true;
                if (currentNode.value.equals(value)) return true;
                currentNode = currentNode.next;
            }
        }
        return false;
    }

    @Override
    public long[] keys() {
        List<Long> keyList = new ArrayList<>();
        for (int i = 0; i < table.length; i++) {
            Node<V> currentNode = table[i];
            while (currentNode != null){
                keyList.add(currentNode.key);
                currentNode = currentNode.next;
            }
        }
        return keyList.stream()
                .mapToLong(Long::longValue)
                .toArray();
    }

    @Override
    public V[] values() {
        List<V> valueList = new ArrayList<>();
        for (int i = 0; i < table.length; i++) {
            Node<V> currentNode = table[i];
            while(currentNode != null){
                valueList.add(currentNode.value);
                currentNode = currentNode.next;
            }
        }
        if(valueList.isEmpty()) return (V[]) null;
        Class vClass = valueList.get(0).getClass();
        V[] vArray = (V[]) Array.newInstance(vClass, valueList.size());
        for (int i = 0; i < vArray.length; i++) {
            vArray[i] = valueList.get(i);
        }
        return vArray;
    }



    @Override
    public long size() {
        return size;
    }

    @Override
    public void clear() {
        Node<V>[] newTable = new Node[DEFAULT_CAPACITY];
        if(table != null && size > 0){
            size = 0;
            for (int i = 0; i < table.length; i++) {
                table[i] = null;
            }
        }
        table = newTable;
    }

    private static class Node<V>{
        long key;
        V value;
        Node<V> next;

        private Node(long key, V value) {
            this.key = key;
            this.value = value;
        }

        public static <V> Node<V> newInstance(long key, V value) {
            return new Node<>(Objects.requireNonNull(key), value);
        }
    }
}
