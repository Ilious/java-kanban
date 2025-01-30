package ru.yandex.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.model.Task;
import ru.yandex.model.enums.TaskStatus;
import ru.yandex.model.interfaces.ITaskManager;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    ITaskManager manager;
    Task task2;
    final int MAX_SIZE = 10;

    @BeforeEach
    void init() {
        manager = Managers.getDefault();
        Task task = new Task("task description", "simple task", 1, TaskStatus.NEW);
        manager.createTask(task);

        String description = "simple task 2";
        String label = "another task";
        task2 = new Task(description, label, 2, TaskStatus.NEW);
        manager.createTask(task2);
    }

    @Test
    void addToHistoryTest() {
        manager.getTaskById(1);

        assertEquals(1, manager.getHistory().size(), "task wasn't added to to history");
    }

    @Test
    void getHistoryTest() {
        for (int i = 0; i < MAX_SIZE + 1; i++) {
            manager.getTaskById(1);
        }
        manager.getTaskById(2);
        Task taskFromHistory = manager.getHistory().get(1);

        assertEquals(2, manager.getHistory().size(), "there aren't 2 tasks in history");
        assertEquals(taskFromHistory.getDescription(), task2.getDescription(),
                "getHistory has problem in description");
        assertEquals(taskFromHistory.getLabel(), task2.getLabel(), "getHistory has problem in label");
        assertEquals(taskFromHistory.getId(), task2.getId(), "getHistory has problem in id");
        assertEquals(taskFromHistory.getStatus(), task2.getStatus(), "getHistory has problem in ");
    }
}