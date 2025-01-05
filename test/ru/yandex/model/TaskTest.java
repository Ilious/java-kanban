package ru.yandex.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.model.enums.TaskStatus;
import ru.yandex.model.interfaces.ITaskManager;
import ru.yandex.service.Managers;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    ITaskManager taskManager;

    @BeforeEach
    void init() {
        taskManager = Managers.getDefault();
    }

    @Test
    void testEqualsTest() {
        String description = "test description";
        String label = "simple task";
        Task task = new Task(description, label, 1, TaskStatus.NEW);

        taskManager.createTask(task);
        Task taskById = taskManager.getTaskById(1);

        assertEquals(1, taskById.getId());
        assertEquals(task, taskById);
    }
}