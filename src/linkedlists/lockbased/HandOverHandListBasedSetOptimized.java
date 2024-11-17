package linkedlists.lockbased;

import contention.abstractions.AbstractCompositionalIntSet;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class HandOverHandListBasedSetOptimized extends AbstractCompositionalIntSet {

    // sentinel nodes
    private Node head;
    private Node tail;

    public HandOverHandListBasedSetOptimized() {
        head = new Node(Integer.MIN_VALUE, true);
        tail = new Node(Integer.MAX_VALUE, false);
        head.next = tail;
    }

    /*
     * Insert
     *
     * @see contention.abstractions.CompositionalIntSet#addInt(int)
     */
    @Override
    public boolean addInt(int item) {
        head.lock();
        Node pred = head;
        Node curr = head.next;
        try {
            curr.lock();
            try {
                while (curr.key < item) {
                    pred.unlock();
                    pred = curr;
                    curr = pred.next;
                    curr.lock();
                }
                if (curr.key == item) {
                    return false;
                } else {
                    Node node = new Node(item, false);
                    node.next = curr;
                    pred.next = node;
                    return true;
                }
            } finally {
                curr.unlock();
            }
        } finally {
            pred.unlock();
        }
    }

    /*
     * Remove
     *
     * @see contention.abstractions.CompositionalIntSet#removeInt(int)
     */
    @Override
    public boolean removeInt(int item) {
        head.lock();
        Node pred = head;
        Node curr = head.next;
        try {
            curr.lock();
            try {
                while (curr.key < item) {
                    pred.unlock();
                    pred = curr;
                    curr = pred.next;
                    curr.lock();
                }
                if (curr.key == item) {
                    pred.next = curr.next;
                    return true;
                } else {
                    return false;
                }
            } finally {
                curr.unlock();
            }
        } finally {
            pred.unlock();
        }
    }

    /*
     * Contains
     *
     * @see contention.abstractions.CompositionalIntSet#containsInt(int)
     */
    @Override
    public boolean containsInt(int item) {
        head.lock();
        Node pred = head;
        Node curr = head.next;
        try {
            curr.lock();
            try {
                while (curr.key < item) {
                    pred.unlock();
                    pred = curr;
                    curr = pred.next;
                    curr.lock();
                }
                return curr.key == item;
            } finally {
                curr.unlock();
            }
        } finally {
            pred.unlock();
        }
    }

    private static class Node {
        Node(int item, Boolean generalLock) {
            key = item;
            next = null;
            lock = new CustomLock(generalLock);
        }

        public void lock() {
            lock.lock();
        }

        public void unlock() {
            lock.unlock();
        }

        public int key;
        public Node next;

        private CustomLock lock = null;
    }

    private static class CustomLock {
        CustomLock(Boolean general) {
            if (general) {
                generalLock = new ReentrantLock();
            } else {
                isLocked = new AtomicBoolean(false);
            }
        }

        public void lock() {
            if (generalLock != null) {
                generalLock.lock();
                return;
            }

            while (isLocked.get()) {
                Thread.yield();
            }
            isLocked.set(true);
        }

        public void unlock() {
            if (generalLock != null) {
                generalLock.unlock();
                return;
            }

            isLocked.set(false);
        }

        private Lock generalLock = null;
        private AtomicBoolean isLocked = null;
    }

    @Override
    public void clear() {
        head = new Node(Integer.MIN_VALUE, true);
        head.next = new Node(Integer.MAX_VALUE, false);
    }

    /**
     * Non atomic and thread-unsafe
     */
    @Override
    public int size() {
        int count = 0;
        Node curr = head.next;
        while (curr.key != Integer.MAX_VALUE) {
            curr = curr.next;
            count++;
        }

        return count;
    }
}
