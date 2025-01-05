package ru.yandex.service;

import ru.yandex.model.Task;
import ru.yandex.model.interfaces.ITaskHistory;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements ITaskHistory {
    private final List<Task> history = new ArrayList<>();

    @Override
    public void addToHistory(Task task) {
        if (history.size() < 10)
            history.add(0, task);
        else {
            history.remove(9);
            history.add(0, task);
        }
    }

    @Override
    public List<Task> getHistory() {
        return history;
    }
}
