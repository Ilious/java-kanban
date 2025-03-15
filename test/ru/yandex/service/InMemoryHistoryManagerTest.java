package ru.yandex.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.model.enums.TaskStatus;
import ru.yandex.model.interfaces.ITaskManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    ITaskManager manager;
    Task task2, task;
    final int MAX_SIZE = 10;

    @BeforeEach
    void init() {
        manager = Managers.getDefault();
        task = new Task("task description", "simple task", 1, TaskStatus.NEW);
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
    void getHistory_ShouldNotReturnDuplicatesTest() {
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

    @Test
    void getHistory_HistoryIsEmpty_ShouldReturnEmptyListTest() {
        List<Task> history = manager.getHistory();

        assertNotNull(history, "history is null");
        assertTrue(history.isEmpty(), "history is empty");
    }

    @Test
    void removeFromHistory_RemoveFirstTest() {
        Epic epic = new Epic(task.getDescription(), "epic", 3);
        manager.createTask(epic);
        Subtask subtask = new Subtask(new Task(task.getDescription(), "subtask", 4, TaskStatus.DONE),
                epic.getId());
        manager.createTask(subtask);
        manager.getTaskById(1);
        manager.getTaskById(2);
        manager.getTaskById(3);
        manager.getTaskById(4);

        manager.removeById(1);
        List<Task> history = manager.getHistory();
        Task taskFromHistory = history.get(0);
        Epic epicFromHistory = (Epic) history.get(1);
        Subtask subtaskFromHistory = (Subtask) history.get(2);

        assertEquals(3, manager.getHistory().size(), "history size should be 3");
        assertAll("remove first task from history doesn't work",
                () -> assertEquals(task2, taskFromHistory),
                () -> assertEquals(epic, epicFromHistory),
                () -> assertEquals(subtaskFromHistory, subtask)
        );
    }

    @Test
    void removeFromHistory_RemoveMiddleTest() {
        Task task3 = new Task(task.getDescription(), "task3", 3, TaskStatus.NEW);
        manager.createTask(task3);
        Subtask subtask = new Subtask(new Task(task.getDescription(), "subtask", 4, TaskStatus.DONE),
                task3.getId());
        manager.createTask(subtask);
        manager.getTaskById(1);
        manager.getTaskById(2);
        manager.getTaskById(3);
        manager.getTaskById(4);

        manager.removeById(2);
        manager.removeById(3);
        List<Task> history = manager.getHistory();
        Task taskFromHistory = history.get(0);
        Subtask subtaskFromHistory = (Subtask) history.get(1);

        assertEquals(2, history.size(), "history size should be 2");
        assertAll("remove first task from history doesn't work",
                () -> assertEquals(task, taskFromHistory),
                () -> assertEquals(subtaskFromHistory, subtask)
        );
    }

    @Test
    void removeFromHistory_RemoveLastTest() {
        Epic epic = new Epic(task.getDescription(), "epic", 3);
        manager.createTask(epic);
        Subtask subtask = new Subtask(new Task(task.getDescription(), "subtask", 4, TaskStatus.DONE),
                epic.getId());
        manager.createTask(subtask);
        manager.getTaskById(1);
        manager.getTaskById(2);
        manager.getTaskById(3);
        manager.getTaskById(4);

        manager.removeById(4);
        List<Task> history = manager.getHistory();
        Task taskFromHistory = history.get(0);
        Task taskFromHistory2 = history.get(1);
        Epic epicFromHistory = (Epic) history.get(2);

        assertEquals(3, history.size(), "history size should be 2");
        assertAll("remove first task from history doesn't work",
                () -> assertEquals(task, taskFromHistory),
                () -> assertEquals(task2, taskFromHistory2),
                () -> assertEquals(epicFromHistory, epic)
        );
    }
}