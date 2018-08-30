package dataStructure.myHashMap;

/**
 * Created by 11256 on 2018/8/30.
 * HashMap
 */
public class HashMap implements Map {
    @Override
    public Object put(Object key, Object value) {
        return null;
    }

    @Override
    public Object get(Object key) {
        return null;
    }

    static class Node<K, V> implements Map.Entry{
        K key;
        V value;
        Node<K, V> next;

        @Override
        public Object setValue(Object value) {

            return null;
        }

        @Override
        public Entry setNext(Entry entry) {
            return null;
        }
    }

}
