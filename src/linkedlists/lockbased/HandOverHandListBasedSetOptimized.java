package linkedlists.lockbased;

import contention.abstractions.AbstractCompositionalIntSet;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class HandOverHandListBasedSetOptimized extends AbstractCompositionalIntSet {

    // sentinel nodes
    private Node<GeneralLock> head;
    private Node<SimpleLock> tail;

    public HandOverHandListBasedSetOptimized() {
        head = Node.general(Integer.MIN_VALUE);
        tail = Node.simple(Integer.MAX_VALUE);
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
        Node<?> pred = head;
        Node<?> curr = head.next;
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
                    Node<?> node = Node.simple(item);
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
        Node<?> pred = head;
        Node<?> curr = head.next;
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
        Node<?> pred = head;
        Node<?> curr = head.next;
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

    private static class Node<L extends ISimpleLock> {
        Node(int item, L lock_) {
            key = item;
            next = null;
            lock = lock_;
        }

        public static Node<SimpleLock> simple(int item) {
            return new Node<>(item, new SimpleLock());
        }

        public static Node<GeneralLock> general(int item) {
            return new Node<>(item, new GeneralLock());
        }

        public void lock() {
            lock.lock();
        }

        public void unlock() {
            lock.unlock();
        }

        public int key;
        public Node<?> next;

        private final L lock;
    }

    interface ISimpleLock {
        void lock();
        void unlock();
    }

    private static class SimpleLock implements ISimpleLock {
        public void lock() {
            while (isLocked) {
                Thread.onSpinWait();
            }
            isLocked = true;
        }

        public void unlock() {
            isLocked = false;
        }

        private volatile Boolean isLocked = false;
    }

    private static class GeneralLock implements ISimpleLock {
        private final Lock lock = new ReentrantLock();

        public void lock() {
            lock.lock();
        }

        public void unlock() {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        head = Node.general(Integer.MIN_VALUE);
        head.next = Node.simple(Integer.MAX_VALUE);
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
