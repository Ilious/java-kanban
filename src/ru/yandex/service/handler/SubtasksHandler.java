package ru.yandex.service.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.exception.task.NotFoundException;
import ru.yandex.exception.task.TaskHasInteractionsException;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.model.enums.TaskType;
import ru.yandex.model.interfaces.ITaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {

    public SubtasksHandler(Gson jsonMapper, ITaskManager taskManager) {
        super(jsonMapper, taskManager);
    }

    @Override
    public void processGet(String path, HttpExchange exchange) throws IOException {
        String[] urlParts = path.split("/");
        if (urlParts.length == 2) {
            List<Subtask> allTasks = taskManager.getListSubTasks();

            String json = jsonMapper.toJson(allTasks, new TypeToken<List<Subtask>>() {
            }.getType());
            sendText(exchange, json, 200);
            return;
        }

        if (urlParts.length == 3) {
            int id = Integer.parseInt(urlParts[2]);

            Task task = taskManager.getTaskById(id);
            if (checkTaskClassMatch(task, TaskType.SUBTASK)) {
                String json = jsonMapper.toJson(task);
                sendText(exchange, json, 200);
                return;

            }
            throw new NotFoundException("Excepted Epic class! But not found!");
        }

        sendText(exchange, "Invalid URL format", 400);
    }

    @Override
    public void processPost(String path, HttpExchange exchange) throws IOException {
        String[] urlParts = path.split("/");
        String request = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (urlParts.length == 2) {
            Task task = jsonMapper.fromJson(request, Subtask.class);
            if (taskManager.hasInteractions(task))
                throw new TaskHasInteractionsException("Task has interactions!");
            taskManager.createTask(task);

            String json = jsonMapper.toJson(task);
            sendText(exchange, json, 201);
        } else if (urlParts.length == 3) {
            Task task = jsonMapper.fromJson(request, Subtask.class);
            if (taskManager.hasInteractions(task))
                throw new TaskHasInteractionsException("Subtask has interactions!");

            taskManager.updateTask(task, Integer.parseInt(urlParts[2]));

            String json = jsonMapper.toJson(task);
            sendText(exchange, json, 201);
        }
    }

    @Override
    public void processDelete(String path, HttpExchange exchange) throws IOException {
        String[] urlParts = path.split("/");

        if (urlParts.length == 3) {
            int id = Integer.parseInt(urlParts[2]);

            Task task = taskManager.removeById(id);
            if (Objects.isNull(task)) {
                throw new NotFoundException("Task wasn't found!");
            }
            checkTaskClassMatch(task, TaskType.SUBTASK);

            String json = jsonMapper.toJson(task);
            sendText(exchange, json, 200);
        }
    }
}
