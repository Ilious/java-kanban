package ru.yandex.model;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.model.enums.TaskStatus;
import ru.yandex.model.interfaces.ITaskManager;
import ru.yandex.service.Managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class EpicTest {
    ITaskManager taskManager;
    Task task;
    Epic epic;

    @BeforeEach
    void init() {
        taskManager = Managers.getDefault();

        String description = "test description";
        String label = "simple task";
        task = new Task(description, label, 1, TaskStatus.NEW);
        epic = new Epic(task);
        taskManager.createTask(epic);
    }

    @Test
    void testEqualsTest() {
        Task taskById = taskManager.getTaskById(1);

        assertEquals(1, taskById.getId(), "ids aren't equal in Epics");
        assertEquals(epic, taskById, "Epics aren't equal");
    }

    @Test
    void ShouldNotAddSubtaskEpic() {
        Task epicToAdd = new Epic(task);

        assertThrows(ClassCastException.class, () -> {
            epic.addSubtask((Subtask) epicToAdd);
        });
        assertEquals(0, epic.getSubtasks().size(), "size of Subtasks should be 0");
    }
}