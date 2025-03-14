package ru.yandex.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.model.enums.TaskStatus;
import ru.yandex.service.Tasks.FileBackedTaskManager;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

class FileBackedTaskManagerTest extends AbstractTaskManagerTest<FileBackedTaskManager> {

    private File file;
    private Task task;

    @Override
    FileBackedTaskManager getManager() {
        FileBackedTaskManager backedTaskManager;
        backedTaskManager = new FileBackedTaskManager(file, Managers.getDefaultHistory());
        return backedTaskManager;
    }

    @BeforeEach
    void setUp() throws IOException {
        file = File.createTempFile("Data", ".csv");
        manager = getManager();
        task = new Task("simple task for test", "simple task", TaskStatus.NEW);
    }

    @Test
    void uploadEmptyFile() {
        FileBackedTaskManager emptyFile = FileBackedTaskManager.fileUpload(file);

        Assertions.assertEquals(emptyFile.getListAllTasks(), Collections.emptyList());
    }

    @Test
    void updateTask() {
        String updatedDescription = task.getDescription() + "updated";
        Task updatedTask = new Task(updatedDescription, task.getLabel(), task.getStatus());

        manager.createTask(task);
        manager.updateTask(updatedTask, 1);

        Assertions.assertNotNull(manager.getTaskById(task.getId()));
        Assertions.assertEquals(updatedDescription, manager.getTaskById(1).getDescription());
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
        Subtask subtask = new Subtask(new Task(task.getDescription(), task.getLabel(), task.getStatus()), epic.getId());
        manager.createTask(epic);
        manager.createTask(subtask);
        int subtaskId = subtask.getId();

        Task removeById = manager.removeById(subtaskId);

        Assertions.assertEquals(subtaskId, removeById.getId());
        Assertions.assertEquals(0, manager.getListSubTasks().size());
        Assertions.assertEquals(1, manager.getListAllTasks().size());
    }

    @Test
    void deleteEpics() {
        Epic epic1 = new Epic(task);
        manager.createTask(epic1);

        Task removedById = manager.removeById(epic1.getId());

        Assertions.assertEquals(removedById.getId(), epic1.getId());
        Assertions.assertEquals(0, manager.getListEpics().size());
    }
}