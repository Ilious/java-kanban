package ru.yandex.service.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.exception.task.NotFoundException;
import ru.yandex.exception.task.TaskHasInteractionsException;
import ru.yandex.model.Adapter.DurationAdapter;
import ru.yandex.model.Adapter.InstantAdapter;
import ru.yandex.model.Task;
import ru.yandex.model.enums.TaskType;
import ru.yandex.model.interfaces.ITaskManager;
import ru.yandex.service.interfaces.IHttpMethods;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

public abstract class BaseHttpHandler implements HttpHandler, IHttpMethods {

    protected ITaskManager taskManager;

    protected Gson jsonMapper;

    public BaseHttpHandler(Gson jsonMapper, ITaskManager taskManager) {
        this.jsonMapper = jsonMapper;
        this.taskManager = taskManager;
    }

    public abstract void processPost(String path, HttpExchange exchange) throws IOException;

    public abstract void processGet(String path, HttpExchange exchange) throws IOException;

    public abstract void processDelete(String path, HttpExchange exchange) throws IOException;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        URI requestURI = exchange.getRequestURI();
        String path = requestURI.getPath();

        try {
            switch (method) {
                case "GET":
                    processGet(path, exchange);
                    break;
                case "POST":
                    processPost(path, exchange);
                    break;
                case "DELETE":
                    processDelete(path, exchange);
                    break;
                default:
                    sendText(exchange, "Method is not allowed", 405);
            }
        } catch (NotFoundException e) {
            sendNotFound(exchange);
        } catch (TaskHasInteractionsException e) {
            sendHasInteractions(exchange);
        } catch (Exception e) {
            sendText(exchange, "Internal Server error", 500);
        } finally {
            exchange.close();
        }
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

    public static Gson getJsonMapper() {
        return new Gson().newBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .create();
    }
}
