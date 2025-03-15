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

public abstract class AbstractTaskManagerTest<T extends ITaskManager> {
    protected T manager;

    abstract T getManager();

    Task task;
    Epic epic;
    Subtask subtask;

    @BeforeEach
    void setUpTasks() {
        task = new Task("simple task description", "default Task", TaskStatus.NEW);
        epic = new Epic(new Task("simple task description", "default Epic", TaskStatus.NEW));
        subtask = new Subtask(new Task("simple task description", "default Subtask", TaskStatus.NEW),
                1);
    }

    @Test
    void getListTasksTest() {
        manager.createTask(task);

        assertEquals(1, manager.getListTasks().size(), "size of ListTask is not correct");
    }

    @Test
    void getListSubTasksTest() {
        manager.createTask(subtask);

        assertEquals(1, manager.getListSubTasks().size(), "size of ListSubTasks is not correct");
    }

    @Test
    void getListEpicsTest() {
        manager.createTask(epic);

        assertEquals(1, manager.getListEpics().size(), "size of ListEpics is not correct");
    }

    @Test
    void deleteTasksTest() {
        manager.deleteTasks();
        assertEquals(0, manager.getListTasks().size(), "deleteTasksTest is not working correct");
    }

    @Test
    void deleteSubTasksTest() {
        manager.createTask(subtask);
        manager.createTask(epic);
        String message = "deleteTasksTest is not working correct";

        manager.deleteSubtasks();
        List<Epic> epics = manager.getListEpics();

        assertEquals(0, manager.getListSubTasks().size(), message + "for ListSubtasks");
        for (Epic epic : epics) {
            assertEquals(0, epic.getSubtasks().size(), message + "for ListEpics");
        }
    }

    @Test
    void deleteEpicsTest() {
        manager.createTask(epic);
        manager.createTask(subtask);
        String message = "deleteTasksTest is not working correct";

        manager.deleteEpics();

        assertEquals(0, manager.getListEpics().size(), message + " for ListEpics");
        assertEquals(0, manager.getListSubTasks().size(), message + " for ListSubtasks");
    }

    @Test
    void getTaskById_ShouldReturnTaskTest() {
        Task taskToCreate = new Task(task.getDescription(), "default Task", TaskStatus.NEW);
        manager.createTask(taskToCreate);

        Task taskById = manager.getTaskById(1);

        assertNotNull(taskById, "taskById should not be null");
        assertSame(Task.class, taskById.getClass(), "getTask didn't work for Task");
    }

    @Test
    void getTaskById_ShouldReturnSubtaskTest() {
        manager.createTask(subtask);

        Task taskById = manager.getTaskById(1);

        assertNotNull(taskById, "taskById should not be null");
        assertSame(Subtask.class, taskById.getClass(), "getTask didn't work for Subtask");
    }

    @Test
    void getTaskById_ShouldReturnEpicTest() {
        manager.createTask(epic);

        Task taskById = manager.getTaskById(1);

        assertNotNull(taskById, "taskById should not be null");
        assertSame(Epic.class, taskById.getClass(), "getTask didn't work for Epic");
    }

    @Test
    void removeById_ShouldRemoveTaskTest() {
        manager.createTask(task);
        manager.removeById(1);

        assertEquals(0, manager.getListTasks().size(), "removeById didn't work for Task");
    }

    @Test
    void removeById_ShouldRemoveSubTaskTest() {
        manager.createTask(subtask);
        manager.removeById(1);

        assertEquals(0, manager.getListSubTasks().size(), "removeById didn't work for Subtask");
    }

    @Test
    void removeById_ShouldRemoveEpicTest() {
        manager.createTask(epic);
        manager.removeById(1);

        assertEquals(0, manager.getListEpics().size(), "removeById didn't work for Epic");
    }

    @Test
    void createTaskTest() {
        manager.createTask(task);
        Task taskById = manager.getTaskById(1);

        assertNotNull(taskById, "createTask didn't work for Task");
        assertEquals(task.getStatus(), taskById.getStatus());
        assertEquals(task.getLabel(), taskById.getLabel());
        assertEquals(task.getDescription(), taskById.getDescription());
    }

    @Test
    void createEpicTest() {
        manager.createTask(epic);
        Task taskById = manager.getTaskById(1);

        assertNotNull(taskById);
        assertSame(Epic.class, taskById.getClass(), "createEpic didn't work for Epic");
        Epic epicById = ((Epic) taskById);
        assertEquals(0, epicById.getSubtasks().size(), "createEpic didn't work for list of subtasks");
        assertEquals(epic.getStatus(), taskById.getStatus());
        assertEquals(epic.getLabel(), taskById.getLabel());
        assertEquals(epic.getDescription(), taskById.getDescription());
    }

    @Test
    void createSubtaskTest() {
        manager.createTask(subtask);
        Task taskById = manager.getTaskById(1);

        assertNotNull(taskById);
        assertSame(Subtask.class, taskById.getClass(), "createEpic didn't work for Epic");
        Subtask epicById = ((Subtask) taskById);
        assertEquals(1, epicById.getEpicId(), "createEpic didn't work for list of subtasks");
        assertEquals(subtask.getStatus(), taskById.getStatus());
        assertEquals(subtask.getLabel(), taskById.getLabel());
        assertEquals(subtask.getDescription(), taskById.getDescription());
    }

    @Test
    void updateTask_TaskShouldUpdateFieldsTest() {
        manager.createTask(task);

        Task taskEdited = new Task(task.getDescription(), task.getLabel(), TaskStatus.IN_PROGRESS);

        manager.updateTask(taskEdited, task.getId());

        Task taskById = manager.getTaskById(task.getId());

        assertNotNull(taskById, "updateTask didn't work for Task");
        assertEquals(TaskStatus.IN_PROGRESS, taskById.getStatus(), "updateTask didn't work for Task");
    }

    @Test
    void updateTask_SubtaskShouldUpdateFieldsTest() {
        manager.createTask(epic);
        manager.createTask(subtask);

        Subtask subtaskEdited = new Subtask(
                new Task(subtask.getDescription(), subtask.getLabel(), TaskStatus.DONE),
                subtask.getEpicId()
        );

        manager.updateTask(subtaskEdited, 2);
        Task subtaskById = manager.getTaskById(2);

        assertNotNull(subtaskById, "updateTask didn't work for Task");
        assertEquals(TaskStatus.DONE, subtaskById.getStatus(), "updateTask didn't work for Epic");
    }

    @Test
    void updateTask_EpicShouldUpdateFieldsTest() {
        manager.createTask(epic);

        String newLabel = epic.getLabel() + " new super label";
        Epic epicEdited = new Epic(
                new Task(epic.getDescription(), newLabel, TaskStatus.DONE)
        );

        manager.updateTask(epicEdited, 1);
        Task epicById = manager.getTaskById(1);

        assertNotNull(epicById, "updateTask didn't work for Task");
        assertEquals(newLabel, epicById.getLabel(), "updateTask didn't work for Epic");
    }

    @Test
    void createSubtask_BindToEpicTest() {
        Epic epic = new Epic("epic desc", "epic");
        Subtask subtask = new Subtask(new Task("subTask desc", "subTask", TaskStatus.IN_PROGRESS), 1);

        manager.createTask(epic);
        manager.createTask(subtask);
        Task taskById = manager.getTaskById(epic.getId());

        assertNotNull(taskById, "epic not found");
        Epic epicById = (Epic) taskById;
        assertEquals(1, epicById.getSubtasks().size(), "Wrong number of subtasks");
    }

    @Test
    void bindSubtasksToEpic_ShouldReturnSubtasksTest() {
        Epic epic = new Epic("simple desc", "epic");
        manager.createTask(epic);
        Subtask subtask = new Subtask(new Task("desc for task", "name", TaskStatus.NEW), epic.getId());
        Subtask subtask2 = new Subtask(new Task("desc for task2", "name2", TaskStatus.NEW), epic.getId());

        manager.createTask(subtask);
        manager.createTask(subtask2);
        Task taskById = manager.getTaskById(1);
        Epic epicById = (Epic) taskById;

        assertNotNull(epicById, "epic not found");
        assertEquals(2, epicById.getSubtasks().size(), "Wrong number of subtasks");
    }

    @Test
    void calculateEpicStatus_HasInProgress_ShouldReturnInProgress() {
        Epic epic = new Epic("simple desc", "epic");
        manager.createTask(epic);
        Subtask subtask = new Subtask(new Task("desc for task", "name", TaskStatus.IN_PROGRESS), epic.getId());
        Subtask subtask2 = new Subtask(new Task("desc for task2", "name2", TaskStatus.IN_PROGRESS), epic.getId());

        manager.createTask(subtask);
        manager.createTask(subtask2);
        Task taskById = manager.getTaskById(epic.getId());
        Epic epicById = (Epic) taskById;

        assertNotNull(epicById, "epic not found");
        assertEquals(TaskStatus.IN_PROGRESS, epicById.getStatus(), "Wrong status");
    }

    @Test
    void calculateEpicStatus_HasNew_ShouldReturnInProgress() {
        Epic epic = new Epic("simple desc", "epic");
        manager.createTask(epic);
        Subtask subtask = new Subtask(new Task("desc for task", "name", TaskStatus.NEW), epic.getId());
        Subtask subtask2 = new Subtask(new Task("desc for task2", "name2", TaskStatus.IN_PROGRESS), epic.getId());

        manager.createTask(subtask);
        manager.createTask(subtask2);
        Task taskById = manager.getTaskById(epic.getId());
        Epic epicById = (Epic) taskById;

        assertNotNull(epicById, "epic not found");
        assertEquals(TaskStatus.IN_PROGRESS, epicById.getStatus(), "Wrong status");
    }

    @Test
    void calculateEpicStatus_HasAllAreDone_ShouldReturnDone() {
        Epic epic = new Epic("simple desc", "epic");
        manager.createTask(epic);
        Subtask subtask = new Subtask(new Task("desc for task", "name", TaskStatus.DONE), epic.getId());
        Subtask subtask2 = new Subtask(new Task("desc for task2", "name2", TaskStatus.DONE), epic.getId());

        manager.createTask(subtask);
        manager.createTask(subtask2);
        Task taskById = manager.getTaskById(1);
        Epic epicById = (Epic) taskById;

        assertNotNull(epicById, "epic not found");
        assertEquals(TaskStatus.DONE, epicById.getStatus(), "Wrong status");
    }

    @Test
    void calculateEpicStatus_HasNotAllDone_ShouldReturnNotDone() {
        Epic epic = new Epic("simple desc", "epic");
        manager.createTask(epic);
        Subtask subtask = new Subtask(new Task("desc for task", "name", TaskStatus.NEW), epic.getId());
        Subtask subtask2 = new Subtask(new Task("desc for task2", "name2", TaskStatus.DONE), epic.getId());

        manager.createTask(subtask);
        manager.createTask(subtask2);
        Task taskById = manager.getTaskById(epic.getId());
        Epic epicById = (Epic) taskById;

        assertNotNull(epicById, "epic not found");
        assertNotEquals(TaskStatus.DONE, epicById.getStatus(), "Wrong status");
        assertEquals(TaskStatus.IN_PROGRESS, epicById.getStatus(), "Wrong status");
    }

    @Test
    void calculateEpicStatus_HasAllDone_ShouldReturnDone() {
        Epic epic = new Epic("simple desc", "epic");
        manager.createTask(epic);
        Subtask subtask = new Subtask(new Task("desc for task", "name", TaskStatus.DONE), epic.getId());
        Subtask subtask2 = new Subtask(new Task("desc for task2", "name2", TaskStatus.DONE), epic.getId());

        manager.createTask(subtask);
        manager.createTask(subtask2);
        Task taskById = manager.getTaskById(epic.getId());
        Epic epicById = (Epic) taskById;

        assertNotNull(epicById, "epic not found");
        assertEquals(TaskStatus.DONE, epicById.getStatus(), "Wrong status");
    }

    @Test
    void calculateEpicStatus_HasAllNew_ShouldReturnNew() {
        Epic epic = new Epic("simple desc", "epic");
        manager.createTask(epic);
        Subtask subtask = new Subtask(new Task("desc for task", "name", TaskStatus.NEW), epic.getId());
        Subtask subtask2 = new Subtask(new Task("desc for task2", "name2", TaskStatus.NEW), epic.getId());

        manager.createTask(subtask);
        manager.createTask(subtask2);
        Task taskById = manager.getTaskById(epic.getId());
        Epic epicById = (Epic) taskById;

        assertNotNull(epicById, "epic not found");
        assertEquals(TaskStatus.NEW, epicById.getStatus(), "Wrong status");
    }
}
