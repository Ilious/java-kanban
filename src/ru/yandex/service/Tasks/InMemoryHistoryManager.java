package ru.yandex.service.Tasks;

import ru.yandex.model.Task;
import ru.yandex.model.interfaces.ITaskHistory;

import java.util.*;

public class InMemoryHistoryManager implements ITaskHistory {

    Node first, last;
    private final Map<Integer, Node> history = new HashMap<>();

    private static class Node {

        private Node next, prev;
        private Task value;

        public Node(Node prev, Task value) {
            this.prev = prev;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return Objects.equals(value, node.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    @Override
    public void addToHistory(Task task) {
        if (task == null) return;

        remove(task.getId());
        Node current = new Node(last, task);

        if (first == null)
            first = current;

        if (last == null) {
            last = current;
        } else {
            last.next = current;
            last = current;
        }

        history.put(task.getId(), current);
    }

    @Override
    public void remove(int id) {
        Node node = history.remove(id);

        if (node == null)
            return;

        if (node.equals(first)) {
            first = first.next;
            if (first != null) first.prev = null;
            return;
        }

        if (node.equals(last)) {
            last = last.prev;
            if (last != null) last.next = null;
            return;
        }

        node.next.prev = node.prev;
        node.prev.next = node.next;
    }

    @Override
    public List<Task> getHistory() {
        if (first == null) return new ArrayList<>();

        Node dummy = first;
        List<Task> listTasks = new ArrayList<>();

        while (dummy != null) {
            listTasks.add(dummy.value);
            dummy = dummy.next;
        }

        return new ArrayList<>(listTasks);
    }
}
