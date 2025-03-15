package ru.yandex.service.handler;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.model.Task;
import ru.yandex.model.enums.TaskStatus;
import ru.yandex.model.interfaces.ITaskManager;
import ru.yandex.service.HttpTaskServer;
import ru.yandex.service.Managers;
import ru.yandex.service.Tasks.InMemoryTaskManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class HistoryHandlerTest {

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
    void getHistoryTasks_WhenNoTasks_ShouldReturnEmptyListTest() throws Exception {
        URI uri = URI.create(String.format("%s:%d%s", URL, PORT, "/history"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void getHistoryTasks_WhenTasksExist_ShouldReturnTasksTest() throws Exception {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW,
                Instant.now(), Duration.ofMinutes(10));
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.NEW,
                Instant.now().plus(10, ChronoUnit.MINUTES), Duration.ofMinutes(15));
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.getTaskById(1);
        taskManager.getTaskById(2);

        URI uri = URI.create(String.format("%s:%d%s", URL, PORT, "/history"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Task 1"));
        assertTrue(response.body().contains("Task 2"));
    }

    @Test
    void getHistoryTasks_WhenTasksDuplicates_ShouldReturnTasksTest() throws Exception {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW,
                Instant.now(), Duration.ofMinutes(10));
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.NEW,
                Instant.now().plus(10, ChronoUnit.MINUTES), Duration.ofMinutes(15));
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.getTaskById(1);
        taskManager.getTaskById(1);

        URI uri = URI.create(String.format("%s:%d%s", URL, PORT, "/history"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Task 1"));
        assertFalse(response.body().contains("Task 2"));
    }

    @Test
    void getHistoryTasks_WhenInvalidPath_ShouldReturnBadRequestTest() throws Exception {
        URI uri = URI.create(String.format("%s:%d%s", URL, PORT, "/history/3213"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Bad request"));
    }

    @Test
    void getHistoryTasks_WhenMethodNotAllowed_ShouldReturnMethodNotAllowedTest() throws Exception {
        URI uri = URI.create(String.format("%s:%d%s", URL, PORT, "/history"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, response.statusCode());
        assertTrue(response.body().contains("Method Not Allowed"));
    }
}