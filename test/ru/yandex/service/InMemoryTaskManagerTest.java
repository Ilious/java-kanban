package ru.yandex.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.model.Task;
import ru.yandex.model.enums.TaskStatus;
import ru.yandex.model.interfaces.ITaskManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InMemoryTaskManagerTest {

    ITaskManager taskManager;

    @BeforeEach
    void init() {
        taskManager = Managers.getDefault();
    }

    @Test
    void getListTasks() {

    }

    @Test
    void getListSubTasks() {

    }

    @Test
    void getListEpics() {
    }

    @Test
    void getHistory() {
    }

    @Test
    void getTaskById() {
    }

    @Test
    void removeById() {
    }

    @Test
    void createTask() {
        String description = "simple description";
        String label = "newTask";
        Task task = new Task(description, label, 0, TaskStatus.NEW);

        taskManager.createTask(task);
        Task taskAdded = taskManager.getListTasks().get(0);

        assertEquals(taskAdded.getStatus(), TaskStatus.NEW);
        assertEquals(taskAdded.getLabel(), label);
        assertEquals(taskAdded.getDescription(), description);
        assertNotNull(taskAdded.getId());
    }

    @Test
    void updateTask() {
    }
}