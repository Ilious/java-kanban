package ru.yandex.service;

import ru.yandex.model.Task;
import ru.yandex.model.interfaces.ITaskHistory;

import java.util.*;

public class InMemoryHistoryManager implements ITaskHistory {

    Node first, last;
    private final Map<Integer, Node> history = new HashMap<>();

    private static class Node {

        Node next, prev;
        Task value;

        public Node(Node next, Task value) {
            this.next = next;
            this.value = value;
        }
    }

    @Override
    public void addToHistory(Task task) {
        if (task == null) return;

        Node current = new Node(last, task);;

        if (first == null)
            first = current;

        last.next = current;
        last = last.next;
        history.put(task.getId(), current);
    }

    @Override
    public void remove(int id) {
        if (!history.containsKey(id))
            return;

        Node node = history.remove(id);

        node.next.prev = node.prev;
        node.prev.next = node.next;
    }

    @Override
    public List<Task> getHistory() {
        if (first == null) return new ArrayList<>();

        Node dummy = first;
        List<Task> listTasks = new ArrayList<>();

        while(dummy.next != null) {
            listTasks.add(dummy.value);
            dummy = dummy.next;
        }

        return new ArrayList<>(listTasks);
    }
}
