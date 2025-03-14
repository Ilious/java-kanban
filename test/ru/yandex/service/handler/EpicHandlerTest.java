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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EpicHandlerTest {

    private final ITaskManager taskManager = new InMemoryTaskManager(Managers.getDefaultHistory());

    private final String URL = "http://localhost";

    private final int PORT = 8080;

    private final Gson gson = BaseHttpHandler.getJsonMapper();

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
    void getEpics_ShouldReturnEpicsTest() throws IOException, InterruptedException {
        httpClient = HttpClient.newHttpClient();
        Epic task = new Epic(new Task("simple desc", "just task", TaskStatus.NEW));
        taskManager.createTask(task);
        String jsonTask = gson.toJson(taskManager.getListEpics(), new TypeToken<List<Epic>>() {
        }.getType());

        URI uri = URI.create(String.format("%s:%d%s", URL, PORT, "/epics"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(response.body(), jsonTask);
    }


    @Test
    void getEpic_TaskFound_ShouldReturnTaskTest() throws IOException, InterruptedException {
        httpClient = HttpClient.newHttpClient();
        Task task = new Epic(new Task("simple desc", "just task", TaskStatus.NEW));
        taskManager.createTask(task);
        String jsonTask = gson.toJson(task, Epic.class);

        URI uri = URI.create(String.format("%s:%d%s/%d", URL, PORT, "/epics", 1));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(response.body(), jsonTask);
    }

    @Test
    void getSubtasksFromEpic_TaskFound_ShouldReturnSubtasksTest() throws IOException, InterruptedException {
        httpClient = HttpClient.newHttpClient();
        Task task = new Epic(new Task("simple desc", "just task", TaskStatus.NEW));
        Task subtask = new Subtask(new Task("task1", "just task", TaskStatus.NEW), 1);
        Task subtask2 = new Subtask(new Task("task2", "just task", TaskStatus.NEW), 1);
        taskManager.createTask(task);
        taskManager.createTask(subtask);
        taskManager.createTask(subtask2);

        Epic taskById = ((Epic) taskManager.getTaskById(1));
        String jsonTask = gson.toJson(taskById.getSubtasks(), new TypeToken<List<Subtask>>() {
        }.getType());

        URI uri = URI.create(String.format("%s:%d%s/%d/%s", URL, PORT, "/epics", 1, "subtasks"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(response.body(), jsonTask);
        assertEquals(2, taskById.getSubtasks().size());
    }

    @Test
    void getEpic_TaskNotFound_ThrowErrorTest() throws IOException, InterruptedException {
        httpClient = HttpClient.newHttpClient();

        URI uri = URI.create(String.format("%s:%d%s/%d", URL, PORT, "/epics", 1));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void removeEpic_ShouldRemoveTaskTest() throws IOException, InterruptedException {
        httpClient = HttpClient.newHttpClient();
        Task task = new Epic(new Task("simple desc", "just task", TaskStatus.NEW));
        taskManager.createTask(task);
        String jsonTask = gson.toJson(task, Epic.class);

        URI uri = URI.create(String.format("%s:%d%s/%d", URL, PORT, "/epics", 1));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(jsonTask, response.body());
    }

    @Test
    void postEpic_CreateTaskWithNoId_ShouldCreateTaskTest() throws IOException, InterruptedException {
        Task task = new Epic(new Task("simple desc", "just task", TaskStatus.NEW));
        task.setId(1);
        String jsonTask = gson.toJson(task, Epic.class);

        URI uri = URI.create(String.format("%s:%d%s", URL, PORT, "/epics"));
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
        Task task = new Epic(new Task("simple desc", "just task", TaskStatus.NEW));
        taskManager.createTask(task);
        Task editedTask = new Epic(new Task(task.getLabel(), "the same but secret task", TaskStatus.IN_PROGRESS));
        int id = 1;
        editedTask.setId(id);
        String jsonTask = gson.toJson(editedTask, Epic.class);

        URI uri = URI.create(String.format("%s:%d%s/%d", URL, PORT, "/epics", id));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(jsonTask, response.body());
    }
}