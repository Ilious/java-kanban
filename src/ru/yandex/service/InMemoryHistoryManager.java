package ru.yandex.service;

import ru.yandex.model.Task;
import ru.yandex.model.interfaces.ITaskHistory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InMemoryHistoryManager implements ITaskHistory {
    private final List<Task> history = new ArrayList<>();

    @Override
    public void addToHistory(Task task) {
        if (task == null) return;
        history.add(task);
        if (history.size() > 10)
            history.remove(0);
    }

    @Override
    public List<Task> getHistory() {
        return List.copyOf(history);
    }
}
