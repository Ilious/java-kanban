package ru.yandex.service.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.model.interfaces.ITaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {

    public HistoryHandler(Gson jsonMapper, ITaskManager taskManager) {
        super(jsonMapper, taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (method.equals("GET") && path.equals("/history")) {
            String response = jsonMapper.toJson(taskManager.getHistory());
            sendText(exchange, response, 200);
        } else if (method.equals("GET")) {
            sendText(exchange, "Bad request", 400);
        } else
            sendText(exchange, "Method Not Allowed", 405);
    }
}
