package ru.yandex.service;

import ru.yandex.model.interfaces.ITaskHistory;
import ru.yandex.model.interfaces.ITaskManager;

public class Managers {
    public static ITaskManager getDefault() {
        ITaskHistory historyManager = getDefaultHistory();
        return new InMemoryTaskManager(historyManager);
    }

    public static ITaskHistory getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
