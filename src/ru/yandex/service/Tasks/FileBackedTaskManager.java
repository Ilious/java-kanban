package ru.yandex.service.Tasks;

import ru.yandex.exception.FileException;
import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.model.enums.TaskStatus;
import ru.yandex.model.enums.TaskType;
import ru.yandex.model.interfaces.ITaskHistory;
import ru.yandex.service.Managers;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;
    public final String pathToFile = "data";
    public final String nameOfFile = "tasks.txt";

    public FileBackedTaskManager(ITaskHistory historyManager) {
        super(historyManager);
        this.file = new File(pathToFile + File.separatorChar + nameOfFile);
    }

    public FileBackedTaskManager(File file, ITaskHistory historyManager) {
        super(historyManager);
        this.file = file;
    }

    @Override
    public Task createTask(Task task) {
        Task addedTask = super.createTask(task);
        save();
        return addedTask;
    }

    @Override
    public Task updateTask(Task task, int id) {
        Task updateTask = super.updateTask(task, id);
        save();
        return updateTask;
    }

    @Override
    public Task removeById(Integer id) {
        Task task = super.removeById(id);
        save();
        return task;
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }

    public static FileBackedTaskManager fileUpload(File file) throws FileException {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(Managers.getDefaultHistory());
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),
                StandardCharsets.UTF_8))) {

            while (reader.ready()) {
                String string = reader.readLine();
                lines.add(string);
            }
        } catch (IOException exception) {
            throw new FileException("Error occurred in reading file", exception);
        }

        lines.forEach(line -> {
            Task taskFromCsv = getTaskFromCsv(line);
            if (taskFromCsv == null) {
                throw new FileException("Error parsing file");
            }

            putTaskInList(fileBackedTaskManager, taskFromCsv);
        });

        return fileBackedTaskManager;
    }

    private static void putTaskInList(FileBackedTaskManager taskManager, Task task) {
        int idx = taskManager.idx;
        if (task.getType() == TaskType.EPIC) {
            taskManager.listEpics.put(taskManager.idx, (Epic) task);
        } else if (task.getType() == TaskType.SUBTASK) {
            taskManager.listSubTasks.put(taskManager.idx, (Subtask) task);
        } else {
            taskManager.listTasks.put(taskManager.idx, task);
        }

        taskManager.idx = Math.max(idx, task.getId());
    }

    private static Task getTaskFromCsv(String line) {
        Task task = null;
        String[] data = line.split(",");

        TaskStatus status;


        switch (data[3]) {
            case "IN_PROGRESS" -> status = TaskStatus.IN_PROGRESS;
            case "DONE" -> status = TaskStatus.DONE;
            default -> status = TaskStatus.NEW;
        }

        final Instant parsedInstance = data[5].equals("null") ? null : Instant.parse(data[5]);
        final Duration parsedDuration = data[6].equals("null") ? null : Duration.parse(data[6]);
        switch (data[1]) {
            case "TASK" -> task = new Task(data[4], data[2], status,
                    parsedInstance, parsedDuration);
            case "EPIC" -> task = new Epic(new Task(data[4], data[2], status,
                    parsedInstance, parsedDuration));
            case "SUBTASK" -> task = new Subtask(
                    new Task(data[4], data[2], status,
                            parsedInstance, parsedDuration), Integer.parseInt(data[7]));
        }
        if (task != null)
            task.setId(Integer.parseInt(data[0]));

        return task;
    }

    private void save() throws FileException {
        List<Task> allTasks = super.getListAllTasks();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (Task task : allTasks) {
                writer.write(String.format("%s%n", convertToCsvFormat(task)));
            }

        } catch (IOException exception) {
            throw new FileException("Error occurred in writing file", exception);
        }
    }

    private String convertToCsvFormat(Task task) {
        String epicId = task.getType() == TaskType.SUBTASK ? "" + ((Subtask) task).getEpicId() : "";

        String startTime = task.getStartTime() != null ? task.getStartTime().toString() : "null";
        String duration = task.getDuration() != null ? task.getDuration().toString() : "null";

        List<String> fields = new ArrayList<>(Arrays.asList(
                String.valueOf(task.getId()),
                task.getClass().getSimpleName().toUpperCase(),
                task.getLabel(),
                task.getStatus().toString(),
                task.getDescription(),
                startTime,
                duration
        ));

        if (!epicId.isEmpty())
            fields.add(epicId);

        return String.join(",", fields);
    }
}
