package ru.yandex.service.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.model.interfaces.ITaskManager;

import java.io.IOException;

public class PriorityTasksHandler extends BaseHttpHandler implements HttpHandler {

    public PriorityTasksHandler(Gson jsonMapper, ITaskManager taskManager) {
        super(jsonMapper, taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (method.equals("GET") && path.equals("/prioritized")) {
            processGet(path, exchange);
        } else if (method.equals("GET")) {
            sendText(exchange, "Bad request", 400);
        } else
            sendText(exchange, "Method Not Allowed", 405);
    }

    @Override
    public void processGet(String path, HttpExchange exchange) throws IOException {
        String response = jsonMapper.toJson(taskManager.getPrioritizedTasks());
        sendText(exchange, response, 200);
    }

    @Override
    public void processDelete(String path, HttpExchange exchange) throws IOException {
        sendText(exchange, "Method Not Allowed", 405);
    }


    @Override
    public void processPost(String path, HttpExchange exchange) throws IOException {
        sendText(exchange, "Method Not Allowed", 405);
    }
}
