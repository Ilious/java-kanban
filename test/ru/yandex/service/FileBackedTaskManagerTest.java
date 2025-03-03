package ru.yandex.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.model.enums.TaskStatus;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

class FileBackedTaskManagerTest extends AbstractTaskManagerTest<FileBackedTaskManager> {

    private File file;
    private Task task;

    @Override
    FileBackedTaskManager getManager() {
        return new FileBackedTaskManager(Managers.getDefaultHistory());
    }

    @BeforeEach
    void setUp() throws IOException {
        manager = getManager();
        file = File.createTempFile("Data", ".csv");
        task = new Task("simple task for test", "simple task", 1, TaskStatus.NEW);
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

        manager.createTask(task);
        manager.updateTask(updatedTask);

        Assertions.assertNotNull(manager.getTaskById(task.getId()));
        Assertions.assertEquals(updatedDescription, manager.getTaskById(1).get().getDescription());
        Assertions.assertEquals(1, manager.getListTasks().size());
    }

    @Test
    void uploadTasksFromAnotherFile() {
        manager = new FileBackedTaskManager(file, Managers.getDefaultHistory());
        manager.createTask(task);

        Assertions.assertEquals(1, manager.getListTasks().size());
        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.fileUpload(file);
        Assertions.assertEquals(1, fileBackedTaskManager.getListTasks().size());
    }

    @Test
    void removeById() {
        manager.createTask(task);

        Task removeById = manager.removeById(task.getId());

        Assertions.assertEquals(removeById.getId(), task.getId());
        Assertions.assertEquals(0, manager.getListAllTasks().size());
    }

    @Test
    void deleteTasks() {
        manager.createTask(task);

        Task removeById = manager.removeById(task.getId());

        Assertions.assertEquals(removeById.getId(), task.getId());
        Assertions.assertEquals(0, manager.getListTasks().size());
    }

    @Test
    void deleteSubtasks() {
        Epic epic = new Epic(task);
        int subtaskId = task.getId() + 1;
        Subtask subtask = new Subtask(new Task(task.getDescription(), task.getLabel(), subtaskId,
                task.getStatus()), epic.getId());
        manager.createTask(epic);
        manager.createTask(subtask);

        Task removeById = manager.removeById(subtaskId);

        Assertions.assertEquals(subtaskId, removeById.getId());
        Assertions.assertEquals(0, manager.getListSubTasks().size());
        Assertions.assertEquals(1, manager.getListAllTasks().size());
    }

    @Test
    void deleteEpics() {
        manager.createTask(new Epic(task));

        Task removeById = manager.removeById(task.getId());

        Assertions.assertEquals(removeById.getId(), task.getId());
        Assertions.assertEquals(0, manager.getListEpics().size());
    }
}