package ru.yandex.service.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.model.Adapter.DurationAdapter;
import ru.yandex.model.Adapter.InstantAdapter;
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

class TaskHandlerTest {

    private final ITaskManager taskManager = new InMemoryTaskManager(Managers.getDefaultHistory());

    private final String URL = "http://localhost";

    private final int PORT = 8080;

    private final Gson gson = new Gson().newBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(Instant.class, new InstantAdapter())
            .create();

    private HttpClient httpClient;

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newHttpClient();
        taskManager.deleteTasks();
        taskManager.deleteEpics();
        taskManager.deleteSubtasks();
        new HttpTaskServer(gson, taskManager);
    }

    @AfterEach
    void tearDown() {
        HttpTaskServer.stopServer();
    }

    @Test
    void getTasks_ShouldReturnTasksTest() throws IOException, InterruptedException {
        httpClient = HttpClient.newHttpClient();
        Task task = new Task("simple desc", "just task", TaskStatus.NEW);
        taskManager.createTask(task);
        String jsonTask = gson.toJson(taskManager.getListTasks(), new TypeToken<List<Task>>() {}.getType());

        URI uri = URI.create(String.format("%s:%d%s", URL, PORT, "/tasks"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(response.body(), jsonTask);
    }


    @Test
    void getTask_TaskFound_ShouldReturnTaskTest() throws IOException, InterruptedException {
        httpClient = HttpClient.newHttpClient();
        Task task = new Task("simple desc", "just task", TaskStatus.NEW);
        taskManager.createTask(task);
        String jsonTask = gson.toJson(task, Task.class);

        URI uri = URI.create(String.format("%s:%d%s/%d", URL, PORT, "/tasks", 1));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(response.body(), jsonTask);
    }

    @Test
    void getTask_TaskNotFound_ThrowErrorTest() throws IOException, InterruptedException {
        httpClient = HttpClient.newHttpClient();

        URI uri = URI.create(String.format("%s:%d%s/%d", URL, PORT, "/tasks", 1));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void removeTask_ShouldRemoveTaskTest() throws IOException, InterruptedException {
        httpClient = HttpClient.newHttpClient();
        Task task = new Task("simple desc", "just task", TaskStatus.NEW);
        taskManager.createTask(task);
        String jsonTask = gson.toJson(task, Task.class);

        URI uri = URI.create(String.format("%s:%d%s/%d", URL, PORT, "/tasks", 1));
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
        Task task = new Task("simple desc", "just task", TaskStatus.NEW);
        task.setId(1);
        String jsonTask = gson.toJson(task, Task.class);

        URI uri = URI.create(String.format("%s:%d%s", URL, PORT, "/tasks"));
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
        Task task = new Task("simple desc", "just task", TaskStatus.NEW);
        taskManager.createTask(task);
        Task editedTask = new Task(task.getLabel(), "the same but secret task", TaskStatus.IN_PROGRESS);
        int id = 1;
        editedTask.setId(id);
        String jsonTask = gson.toJson(editedTask, Task.class);

        URI uri = URI.create(String.format("%s:%d%s/%d", URL, PORT, "/tasks", id));
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
        Task task = new Task("simple desc", "just task", TaskStatus.NEW, Instant.now(),
                Duration.ofMinutes(20));
        taskManager.createTask(task);
        Task anotherTask = new Task(task.getLabel(), "the same but secret task", TaskStatus.IN_PROGRESS,
                Instant.now().plus(30, ChronoUnit.MINUTES), Duration.ofMinutes(10));
        anotherTask.setId(2);
        String jsonTask = gson.toJson(anotherTask, Task.class);

        URI uri = URI.create(String.format("%s:%d%s", URL, PORT, "/tasks"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(2, taskManager.getListTasks().size());
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
    void postTask_UpdateTaskHasInteractions_ShouldNotUpdateTaskTest() throws IOException,
            InterruptedException {
        Task task = new Task("simple desc", "just task", TaskStatus.NEW, Instant.now(), Duration
                .ofMinutes(20));
        taskManager.createTask(task);
        Task anotherTask = new Task(task.getLabel(), "the same but secret task", TaskStatus.IN_PROGRESS,
                Instant.now().plus(30, ChronoUnit.MINUTES), Duration.ofMinutes(10));
        taskManager.createTask(anotherTask);
        Task anotherUpdTask = new Task(task.getLabel(), "the same but secret task", TaskStatus.IN_PROGRESS,
                Instant.now().plus(10, ChronoUnit.MINUTES), Duration.ofMinutes(10));
        int id = 2;
        anotherUpdTask.setId(id);

        String jsonTask = gson.toJson(anotherUpdTask, Task.class);

        URI uri = URI.create(String.format("%s:%d%s/%d", URL, PORT, "/tasks", id));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
        assertEquals(2, taskManager.getListTasks().size());
    }

    @Test
    void postTask_UpdateTaskWithNoInteractions_ShouldUpdateTaskTest() throws IOException, InterruptedException {
        Task task = new Task("simple desc", "just task", TaskStatus.NEW, Instant.now(),
                Duration.ofMinutes(20));
        taskManager.createTask(task);
        Task anotherTask = new Task(task.getLabel(), "the same but secret task", TaskStatus.IN_PROGRESS,
                Instant.now().plus(30, ChronoUnit.MINUTES), Duration.ofMinutes(10));
        taskManager.createTask(anotherTask);
        Task anotherUpdTask = new Task(task.getLabel(), "the same but secret task", TaskStatus.IN_PROGRESS,
                Instant.now().plus(40, ChronoUnit.MINUTES), Duration.ofMinutes(10));
        int id = 2;
        anotherUpdTask.setId(id);

        String jsonTask = gson.toJson(anotherUpdTask, Task.class);

        URI uri = URI.create(String.format("%s:%d%s/%d", URL, PORT, "/tasks", id));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(2, taskManager.getListTasks().size());
    }
}