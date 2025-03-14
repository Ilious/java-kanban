package ru.yandex.service;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.exception.HttpConnectionException;
import ru.yandex.model.Task;
import ru.yandex.model.enums.TaskStatus;
import ru.yandex.model.interfaces.ITaskManager;
import ru.yandex.service.Tasks.FileBackedTaskManager;
import ru.yandex.service.handler.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {

    private static final int PORT = 8080;

    private static final String ADDRESS = "localhost";

    private static Gson gson;

    private static HttpServer server;

    private static HttpTaskServer httpTaskServer;

    private ITaskManager taskManager;

    public HttpTaskServer(Gson gson, ITaskManager taskManager) {
        this.taskManager = taskManager;
        httpTaskServer = HttpTaskServer.configureServer(this.taskManager);
        startServer();
    }

    public static void main(String[] args) {
        ITaskManager taskManager = new FileBackedTaskManager(Managers.getDefaultHistory());
        Task task = new Task("simple desc", "just task", TaskStatus.NEW);
        taskManager.createTask(task);

        configureGson();
        configureServer(taskManager);
        startServer();
    }

    public static HttpTaskServer configureServer(ITaskManager taskManager) {
        try {
            server = HttpServer.create(new InetSocketAddress(ADDRESS, PORT), 0);
            configureGson();

            server.createContext("/tasks", new TaskHandler(HttpTaskServer.gson, taskManager));
            server.createContext("/epics", new EpicHandler(HttpTaskServer.gson, taskManager));
            server.createContext("/subtasks", new SubtasksHandler(HttpTaskServer.gson, taskManager));
            server.createContext("/history", new HistoryHandler(HttpTaskServer.gson, taskManager));
            server.createContext("/prioritized", new PriorityTasksHandler(HttpTaskServer.gson, taskManager));
        } catch (IOException e) {
            throw new HttpConnectionException(e);
        }
        return httpTaskServer;
    }

    public static void configureGson() {
        HttpTaskServer.gson = BaseHttpHandler.getJsonMapper();
    }

    public static void startServer() {
        server.start();
    }

    public static void stopServer() {
        server.stop(0);
    }
}
