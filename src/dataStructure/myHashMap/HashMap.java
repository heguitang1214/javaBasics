package dataStructure.myHashMap;

import dataStructure.LinkedList.singleLinked.Node;

import java.util.Hashtable;

/**
 * Created by 11256 on 2018/8/30.
 * HashMap
 */
public class HashMap<K, V> implements Map<K, V> {

    //定义一个存放数据的Node类型的数组
    private Node<K, V> hashTable[] = null;
    //集合中元素的个数
    private int size;

    //设置集合数组的默认长度
    private static int defaultCapacity = 1 << 4;
    //设置默认的加载因子 ***
    private static float defaultLoadFactor = 0.75f;
    //阀值
    private int threshold;

//    public HashMap() {
//
//    }

    /**
     * 减少乘除法的计算,提升性能
     */
    public HashMap() {
        this.threshold = (int)(defaultCapacity * defaultLoadFactor);
    }



    @Override
    public V put(K key, V value) {
        if (hashTable == null) {
            hashTable = new Node[this.defaultCapacity];
        }
        //1.通过Hash算法,得到index的值
        int index = getIndex(key, this.hashTable.length);

        //2.判断是否是修改
        Node<K, V> node = hashTable[index];
        for (; node != null; node = node.next) {
            if (node.key == key || (node.key != null && node.key.equals(key))) {
                return node.setValue(value);
            }
        }

        //扩容
        if (size >= threshold){
            resize();
        }

        //3.创建Node元素,存放在table中的index上
        hashTable[index] = new Node<>(key, value, hashTable[index]);
        ++size;

        return value;
    }

    /**
     * 扩容,消耗时间和空间
     */
    private void resize() {
        System.out.println("扩容操作....");
        Node<K, V> newHashTable[] = new Node[hashTable.length << 1];
        //循环数组
        for (int i = 0; i < hashTable.length; i++){
            //循环链表
            Node<K, V> node = hashTable[i];
            for (; node != null; ) {
                //Key在新数组上的下标位置(重新hash计算),这个时候,链表上的元素也要重新分配
                int index = getIndex(node.key, newHashTable.length);
                Node<K, V> oldNext = node.next;
                node.next = newHashTable[index];//修改指向,将定位到index的节点像下链接
                newHashTable[index] = node;
                node = oldNext;
            }
        }
        hashTable = newHashTable;
        defaultCapacity = newHashTable.length;
        threshold = (int)(defaultCapacity * defaultLoadFactor);
    }

    /**
     * 与算法获取下标
     */
    private int getIndex(K key, int length) {
        if (key == null) return 0;
        return key.hashCode() & (length - 1);
    }

    @Override
    public V get(K key) {
        if (hashTable != null) {
            int index = getIndex(key, this.hashTable.length);
            Node<K, V> node = hashTable[index];
            for (; node != null; node = node.next) {
                if (node.key == key || (node.key != null && node.key.equals(key))) {
                    return node.value;
                }
            }
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }


    static class Node<K, V> implements Map.Entry<K, V> {
        K key;
        V value;
        Node<K, V> next;

        Node(K key, V value, Node<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        @Override
        public V setValue(V value) {

            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        @Override
        public Entry<K, V> setNext(Entry<K, V> entry) {
            Entry<K, V> oldEntry = this.next;
            this.next = (Node<K, V>) entry;
            return oldEntry;
        }
    }

}
