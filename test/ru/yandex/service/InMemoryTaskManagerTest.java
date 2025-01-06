package ru.yandex.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.model.enums.TaskStatus;
import ru.yandex.model.interfaces.ITaskManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InMemoryTaskManagerTest {

    ITaskManager taskManager;
    Task task;
    Epic epic;
    Subtask subtask;

    @BeforeEach
    void init() {
        taskManager = Managers.getDefault();

        task = new Task("simple task description", "default Task", 1, TaskStatus.NEW);
        epic = new Epic(new Task("simple task description", "default Epic", 2, TaskStatus.NEW));
        subtask = new Subtask(new Task("simple task description", "default Subtask", 3, TaskStatus.NEW),
                epic.getId()
        );

        taskManager.createTask(task);
        taskManager.createTask(epic);
        taskManager.createTask(subtask);
    }

    @Test
    void getListTasksTest() {
        assertEquals(1, taskManager.getListTasks().size(), "size of ListTask is not correct");
    }

    @Test
    void getListSubTasksTest() {
        assertEquals(1, taskManager.getListSubTasks().size(), "size of ListSubTasks is not correct");
    }

    @Test
    void getListEpicsTest() {
        assertEquals(1, taskManager.getListEpics().size(), "size of ListEpics is not correct");
    }

    @Test
    void deleteTasksTest() {
        taskManager.deleteTasks();
        assertEquals(0, taskManager.getListTasks().size(), "deleteTasksTest is not working correct");
    }

    @Test
    void deleteSubTasksTest() {
        String message = "deleteTasksTest is not working correct";

        taskManager.deleteSubtasks();
        List<Epic> epics = taskManager.getListEpics();

        assertEquals(0, taskManager.getListSubTasks().size(), message + "for ListSubtasks");
        for (Epic epic :
                epics) {
            assertEquals(0, epic.getSubtasks().size(), message + "for ListEpics");
        }
    }

    @Test
    void deleteEpicsTest() {
        String message = "deleteTasksTest is not working correct";

        taskManager.deleteEpics();

        assertEquals(0, taskManager.getListEpics().size(), message + " for ListEpics");
        assertEquals(0, taskManager.getListSubTasks().size(), message + " for ListSubtasks");
    }

    @Test
    void getHistoryTest() {
        taskManager.getTaskById(1);
        taskManager.getTaskById(2);

        assertEquals(2, taskManager.getHistory().size(), "getHistory gives wrong size");
    }

    @Test
    void getTaskByIdTest() {
        Task taskById = taskManager.getTaskById(1);
        Task epicById = taskManager.getTaskById(2);
        Task SubtaskById = taskManager.getTaskById(3);

        assertEquals(Task.class, taskById.getClass(),"getTask didn't work for Task");
        assertEquals(Epic.class, epicById.getClass(),"getTask didn't work for Epic");
        assertEquals(Subtask.class, SubtaskById.getClass(),"getTask didn't work for Subtask");
    }

    @Test
    void removeByIdTest() {
        taskManager.removeById(1);
        taskManager.removeById(3);
        taskManager.removeById(2);

        assertEquals(0, taskManager.getListTasks().size(),"removeById didn't work for Task");
        assertEquals(0, taskManager.getListSubTasks().size(),"removeById didn't work for SubTask");
        assertEquals(0, taskManager.getListEpics().size(),"removeById didn't work for Epic");
    }

    @Test
    void createTaskTest() {
        Task taskById = taskManager.getTaskById(1);

        assertEquals(task.getStatus(), taskById.getStatus());
        assertEquals(task.getLabel(), taskById.getLabel());
        assertEquals(task.getDescription(), taskById.getDescription());
    }

    @Test
    void updateTaskTest() {
        Task taskEdited = new Task(task.getDescription(), task.getLabel(), task.getId(), TaskStatus.IN_PROGRESS);
        Subtask subtaskEdited = new Subtask(
                new Task(subtask.getDescription(), subtask.getLabel(), subtask.getId(), TaskStatus.DONE),
                subtask.getEpicId()
        );

        taskManager.updateTask(taskEdited);
        taskManager.updateTask(subtaskEdited);

        Task taskById = taskManager.getTaskById(1);
        Task epicById = taskManager.getTaskById(2);
        Task subtaskById = taskManager.getTaskById(3);

        assertEquals(TaskStatus.IN_PROGRESS, taskById.getStatus(),"updateTask didn't work for Task");
        assertEquals(TaskStatus.DONE, epicById.getStatus(), "updateTask didn't work for SubTask");
        assertEquals(TaskStatus.DONE, subtaskById.getStatus(), "updateTask didn't work for Epic");

    }
}