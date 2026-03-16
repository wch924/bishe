package com.chwww924.chwwwBackend.testCode.LRU;

import java.util.HashMap;
import java.util.Map;

public class Main {
    class Node {
        int key;
        int value;
        Node pre;
        Node next;
        public Node(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }
    private int capacity;
    private Node head;
    private Node tail;
    private Map<Integer, Node> map;
    private int size;
    public Main(int capacity) {
        this.capacity = capacity;
        head = new Node(-1, -1);
        tail = new Node(-1, -1);
        head.next = tail;
        tail.pre = head;
        size = 0;
        map = new HashMap<>();
    }
    private void addToHead(Node node) {
        node.pre = head;
        node.next = head.next;
        head.next.pre = node;
        head.next = node;
    }
    private void removeNode(Node node) {
        node.pre.next = node.next;
        node.next.pre = node.pre;
    }
    private void moveToHead(Node node) {
        removeNode(node);
        addToHead(node);
    }
    private Node removeTail() {
        Node node = tail.pre;
        removeNode(node);
        return node;
    }
    private int get(int key) {
        if(!map.containsKey(key)) {
            return -1;
        }else {
            Node node = map.get(key);
            moveToHead(node);
            return node.value;
        }
    }
    private void put(int key, int value) {
        Node node = map.get(key);
        if(node!=null) {
            node.value = value;
            moveToHead(node);
        }else {
            Node newNode = new Node(key, value);
            addToHead(newNode);
            map.put(key, newNode);
            ++size;
            if(size>capacity) {
                Node tail = removeTail();
                map.remove(tail.key);
                --size;
            }
        }
    }
}
