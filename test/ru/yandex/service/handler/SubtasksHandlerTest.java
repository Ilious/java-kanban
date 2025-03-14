package ru.yandex.service.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.model.enums.TaskStatus;
import ru.yandex.model.interfaces.ITaskManager;
import ru.yandex.service.HttpTaskServer;
import ru.yandex.service.Managers;
import ru.yandex.service.Tasks.InMemoryTaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubtasksHandlerTest {

    private final ITaskManager taskManager = new InMemoryTaskManager(Managers.getDefaultHistory());

    private final String URL = "http://localhost";

    private final int PORT = 8080;

    private final Gson gson = BaseHttpHandler.getJsonMapper();

    private HttpClient httpClient;

    private Epic epic;

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newHttpClient();
        taskManager.deleteTasks();
        taskManager.deleteEpics();
        taskManager.deleteSubtasks();

        epic = new Epic("base epic", "epic");

        new HttpTaskServer(gson, taskManager);
    }

    @AfterEach
    void tearDown() {
        HttpTaskServer.stopServer();
    }

    @Test
    void getSubtasks_ShouldReturnTasksTest() throws IOException, InterruptedException {
        taskManager.createTask(epic);
        httpClient = HttpClient.newHttpClient();
        Task task = new Subtask(new Task("simple desc", "just task", TaskStatus.NEW), epic.getId());
        Task task2 = new Subtask(new Task("simple desc2", "just task", TaskStatus.NEW), epic.getId());
        taskManager.createTask(task);
        taskManager.createTask(task2);
        String jsonTask = gson.toJson(taskManager.getListSubTasks(), new TypeToken<List<Subtask>>() {
        }.getType());

        URI uri = URI.create(String.format("%s:%d%s", URL, PORT, "/subtasks"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(response.body(), jsonTask);
    }

    @Test
    void getSubtask_TaskFound_ShouldReturnTaskTest() throws IOException, InterruptedException {
        taskManager.createTask(epic);
        httpClient = HttpClient.newHttpClient();
        Task task = new Subtask(new Task("simple desc", "just task", TaskStatus.NEW), epic.getId());
        taskManager.createTask(task);
        String jsonTask = gson.toJson(task, Subtask.class);

        URI uri = URI.create(String.format("%s:%d%s/%d", URL, PORT, "/subtasks", 2));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(response.body(), jsonTask);
    }

    @Test
    void getSubtask_TaskNotFound_ThrowErrorTest() throws IOException, InterruptedException {
        httpClient = HttpClient.newHttpClient();

        URI uri = URI.create(String.format("%s:%d%s/%d", URL, PORT, "/subtasks", 1));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void removeSubTask_ShouldRemoveTaskTest() throws IOException, InterruptedException {
        taskManager.createTask(epic);
        httpClient = HttpClient.newHttpClient();
        Task task = new Subtask(new Task("simple desc", "just task", TaskStatus.NEW), epic.getId());
        taskManager.createTask(task);
        String jsonTask = gson.toJson(task, Subtask.class);

        URI uri = URI.create(String.format("%s:%d%s/%d", URL, PORT, "/subtasks", 2));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(jsonTask, response.body());
    }

    @Test
    void postTask_CreateTaskWithNoId_ShouldCreateTaskTest() throws IOException, InterruptedException {
        taskManager.createTask(epic);
        Task task = new Subtask(new Task("simple desc", "just task", TaskStatus.NEW), epic.getId());
        task.setId(2);
        String jsonTask = gson.toJson(task, Subtask.class);

        URI uri = URI.create(String.format("%s:%d%s", URL, PORT, "/subtasks"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(jsonTask, response.body());
    }

    @Test
    void postTask_UpdateTaskWithExistsId_ShouldUpdateTaskTest() throws IOException, InterruptedException {
        taskManager.createTask(epic);
        Task task = new Subtask(new Task("simple desc", "just task", TaskStatus.NEW), epic.getId());
        taskManager.createTask(task);
        Task editedTask = new Subtask(new Task(task.getLabel(), "the same but secret task",
                TaskStatus.IN_PROGRESS), epic.getId());
        int id = 2;
        editedTask.setId(id);
        String jsonTask = gson.toJson(editedTask, Subtask.class);

        URI uri = URI.create(String.format("%s:%d%s/%d", URL, PORT, "/subtasks", id));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(jsonTask, response.body());
    }

    @Test
    void postTask_CreateTaskWithNoInteractions_ShouldCreateTasksTest() throws IOException, InterruptedException {
        taskManager.createTask(epic);
        Task task = new Subtask(new Task("simple desc", "just task", TaskStatus.NEW, Instant.now(),
                Duration.ofMinutes(20)), epic.getId());
        taskManager.createTask(task);
        Task anotherTask = new Subtask(new Task(task.getLabel(), "the same but secret task", TaskStatus
                .IN_PROGRESS, Instant.now().plus(30, ChronoUnit.MINUTES), Duration.ofMinutes(10)),
                epic.getId());
        anotherTask.setId(2);
        String jsonTask = gson.toJson(anotherTask, Subtask.class);

        URI uri = URI.create(String.format("%s:%d%s", URL, PORT, "/subtasks"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(2, taskManager.getListSubTasks().size());
    }

    @Test
    void postTask_CreateTaskWithInteractions_ShouldNotCreateTaskTest() throws IOException, InterruptedException {
        Task task = new Task("simple desc", "just task", TaskStatus.NEW, Instant.now(),
                Duration.ofMinutes(20));
        taskManager.createTask(task);
        Task anotherTask = new Task(task.getLabel(), "the same but secret task", TaskStatus.IN_PROGRESS,
                Instant.now().plus(10, ChronoUnit.MINUTES), Duration.ofMinutes(10));
        anotherTask.setId(2);
        String jsonTask = gson.toJson(anotherTask, Task.class);

        URI uri = URI.create(String.format("%s:%d%s", URL, PORT, "/tasks"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
        assertEquals(1, taskManager.getListTasks().size());
    }


    @Test
    void postSubtask_TaskHasInteractions_ShouldNotUpdateTaskTest() throws IOException,
            InterruptedException {
        taskManager.createTask(epic);
        Task task = new Subtask("simple desc", "just task", TaskStatus.NEW, Instant.now(),
                Duration.ofMinutes(20), epic.getId());
        taskManager.createTask(task);
        Task anotherTask = new Subtask(task.getLabel(), "the same but secret task", TaskStatus
                .IN_PROGRESS, Instant.now().plus(30, ChronoUnit.MINUTES), Duration.ofMinutes(10),
                epic.getId());
        taskManager.createTask(anotherTask);
        Task anotherUpdTask = new Subtask(new Task(task.getLabel(), "the same but secret task", TaskStatus
                .IN_PROGRESS, Instant.now().plus(10, ChronoUnit.MINUTES), Duration.ofMinutes(10)), epic.getId());
        int id = 2;
        anotherUpdTask.setId(id);

        String jsonTask = gson.toJson(anotherUpdTask, Subtask.class);

        URI uri = URI.create(String.format("%s:%d%s/%d", URL, PORT, "/subtasks", id));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
        assertEquals(2, taskManager.getListSubTasks().size());
    }

    @Test
    void postSubtask_UpdateTaskWithNoInteractions_ShouldUpdateTaskTest() throws IOException, InterruptedException {
        taskManager.createTask(epic);
        Task task = new Subtask(new Task("simple desc", "just task", TaskStatus.NEW, Instant.now(),
                Duration.ofMinutes(20)), epic.getId());
        taskManager.createTask(task);
        Task anotherTask = new Subtask(new Task(task.getLabel(), "the same but secret task", TaskStatus
                .IN_PROGRESS, Instant.now().plus(30, ChronoUnit.MINUTES), Duration.ofMinutes(10)),
                epic.getId());
        taskManager.createTask(anotherTask);
        Task anotherUpdTask = new Subtask(new Task(task.getLabel(), "the same but secret task", TaskStatus
                .IN_PROGRESS, Instant.now().plus(40, ChronoUnit.MINUTES), Duration.ofMinutes(10)), epic.getId());
        int id = 2;
        anotherUpdTask.setId(id);

        String jsonTask = gson.toJson(anotherUpdTask, Subtask.class);

        URI uri = URI.create(String.format("%s:%d%s/%d", URL, PORT, "/tasks", id));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(2, taskManager.getListSubTasks().size());
    }
}