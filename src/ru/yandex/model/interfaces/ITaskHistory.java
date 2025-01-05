package ru.yandex.model.interfaces;

import ru.yandex.model.Task;

import java.util.List;

public interface ITaskHistory {

    void addToHistory(Task task);
    List<Task> getHistory();
}
