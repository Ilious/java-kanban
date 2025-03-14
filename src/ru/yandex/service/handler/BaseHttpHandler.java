package ru.yandex.service.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.exception.task.TaskHasInteractionsException;
import ru.yandex.model.Task;
import ru.yandex.model.enums.TaskType;
import ru.yandex.model.interfaces.ITaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler {

    protected ITaskManager taskManager;

    protected Gson jsonMapper;

    public BaseHttpHandler(Gson jsonMapper, ITaskManager taskManager) {
        this.jsonMapper = jsonMapper;
        this.taskManager = taskManager;
    }

    public void sendText(HttpExchange exchange, String text, int code) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, text.length());
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    public void sendNotFound(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text/plain");
        exchange.sendResponseHeaders(404, 0);
        exchange.getResponseBody().write("Not Found".getBytes());
        exchange.close();
    }

    public void sendHasInteractions(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text/plain");
        sendText(exchange, "Task already has interactions", 406);
        exchange.getResponseBody().write("Not Found".getBytes());
        exchange.close();
    }

    public boolean checkTaskClassMatch(Task task, TaskType expected) {
        if (task.getType().equals(expected)) {
            return true;
        }
        throw new TaskHasInteractionsException("Task class mismatch!");
    }
}
