package ru.yandex.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.model.enums.TaskStatus;
import ru.yandex.model.enums.TaskType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    FileBackedTaskManager taskManager;
    private File file;
    private Task task;

    @BeforeEach
    void setUp() throws IOException {
        file = File.createTempFile("Data", ".csv");
        taskManager = new FileBackedTaskManager(file, Managers.getDefaultHistory());
        task = new Task("simple task for test", "simple task", 1, TaskStatus.NEW);
    }

    @Test
    void createTasks() {
        Epic task2 = new Epic(task);
        Task task3 = new Subtask(task, 2);

        Assertions.assertDoesNotThrow(() -> taskManager.createTask(task));
        Assertions.assertDoesNotThrow(() -> taskManager.createTask(task2));
        Assertions.assertDoesNotThrow(() -> taskManager.createTask(task3));

        Task taskById = taskManager.getTaskById(1).get();
        Task taskById2 = taskManager.getTaskById(2).get();
        Task taskById3 = taskManager.getTaskById(3).get();

        Assertions.assertNotNull(taskById);
        Assertions.assertNotNull(taskById2);
        Assertions.assertNotNull(taskById3);

        Assertions.assertEquals(task, taskById);
        Assertions.assertEquals(task2, taskById2);
        Assertions.assertEquals(task3, taskById3);

        Assertions.assertEquals(3, taskManager.getListAllTasks().size());
    }

    @Test
    void uploadEmptyFile() {
        FileBackedTaskManager emptyFile = FileBackedTaskManager.fileUpload(file);

        Assertions.assertEquals(emptyFile.getListAllTasks(), Collections.emptyList());
    }

    @Test
    void updateTask() {
        String updatedDescription = task.getDescription() + "updated";
        Task updatedTask = new Task(updatedDescription, task.getLabel(), task.getId(), task.getStatus());

        taskManager.createTask(task);
        taskManager.updateTask(updatedTask);

        Assertions.assertNotNull(taskManager.getTaskById(task.getId()));
        Assertions.assertEquals(updatedDescription, taskManager.getTaskById(1).get().getDescription());
        Assertions.assertEquals(1, taskManager.getListTasks().size());
    }

    @Test
    void uploadTasksFromAnotherFile() {
        taskManager = new FileBackedTaskManager(file, Managers.getDefaultHistory());
        taskManager.createTask(task);

        Assertions.assertEquals(1, taskManager.getListTasks().size());
        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.fileUpload(file);
        Assertions.assertEquals(1, fileBackedTaskManager.getListTasks().size());
    }

    @Test
    void removeById() {
        taskManager.createTask(task);

        Task removeById = taskManager.removeById(task.getId());

        Assertions.assertEquals(removeById.getId(), task.getId());
        Assertions.assertEquals(0, taskManager.getListAllTasks().size());
    }

    @Test
    void deleteTasks() {
        taskManager.createTask(task);

        Task removeById = taskManager.removeById(task.getId());

        Assertions.assertEquals(removeById.getId(), task.getId());
        Assertions.assertEquals(0, taskManager.getListTasks().size());
    }

    @Test
    void deleteSubtasks() {
        Epic epic = new Epic(task);
        int subtaskId = task.getId() + 1;
        Subtask subtask = new Subtask(new Task(task.getDescription(), task.getLabel(), subtaskId,
                task.getStatus()), epic.getId());
        taskManager.createTask(epic);
        taskManager.createTask(subtask);

        Task removeById = taskManager.removeById(subtaskId);

        Assertions.assertEquals(subtaskId, removeById.getId());
        Assertions.assertEquals(0, taskManager.getListSubTasks().size());
        Assertions.assertEquals(1, taskManager.getListAllTasks().size());
    }

    @Test
    void deleteEpics() {
        taskManager.createTask(new Epic(task));

        Task removeById = taskManager.removeById(task.getId());

        Assertions.assertEquals(removeById.getId(), task.getId());
        Assertions.assertEquals(0, taskManager.getListEpics().size());
    }
}