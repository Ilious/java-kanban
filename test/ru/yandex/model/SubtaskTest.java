package ru.yandex.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.model.enums.TaskStatus;
import ru.yandex.model.interfaces.ITaskManager;
import ru.yandex.service.Managers;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    ITaskManager taskManager;

    @BeforeEach
    void init() {
        taskManager = Managers.getDefault();
    }

    @Test
    void testEqualsTest() {
        Task taskEpic = new Task("epic desc", "epic label", 1, TaskStatus.NEW);
        Epic epic = new Epic(taskEpic);
        taskManager.createTask(epic);

        String description = "test description";
        String label = "simple task";
        Task simpleTask = new Task(description, label, 2, TaskStatus.NEW);
        Subtask Subtask = new Subtask(simpleTask, epic.getId());

        taskManager.createTask(Subtask);
        Task taskById = taskManager.getTaskById(2).get();

        assertEquals(2, taskById.getId(), "ids aren't equal in Subtasks");
        assertEquals(Subtask, taskById, "Subtask's aren't equal");
    }
}