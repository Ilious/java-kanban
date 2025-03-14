package ru.yandex.service.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.exception.task.NotFoundException;
import ru.yandex.exception.task.TaskHasInteractionsException;
import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.model.enums.TaskType;
import ru.yandex.model.interfaces.ITaskManager;
import ru.yandex.service.interfaces.IHttpMethods;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class EpicHandler extends TaskHandler implements HttpHandler, IHttpMethods {

    public EpicHandler(Gson gsonMapper, ITaskManager taskManager) {
        super(gsonMapper, taskManager);
    }

    @Override
    public void processGet(String path, HttpExchange exchange) throws IOException {
        String[] urlParts = path.split("/");
        if (urlParts.length == 2) {
            List<Epic> allTasks = taskManager.getListEpics();

            String json = jsonMapper.toJson(allTasks, new TypeToken<List<Epic>>() {
            }.getType());
            sendText(exchange, json, 200);
        }

        if (urlParts.length == 3) {
            int id = Integer.parseInt(urlParts[2]);

            Task task = taskManager.getTaskById(id);

            if (super.checkTaskClassMatch(task, TaskType.EPIC)) {
                Epic epic = (Epic) task;
                String json = jsonMapper.toJson(epic);
                sendText(exchange, json, 200);
                return;
            }
            throw new NotFoundException("Excepted Epic class! But not found!");
        }

        if (urlParts.length == 4) {
            int id = Integer.parseInt(urlParts[2]);

            Task task = taskManager.getTaskById(id);

            if (super.checkTaskClassMatch(task, TaskType.EPIC)) {
                Epic epic = (Epic) task;
                String json = jsonMapper.toJson(epic.getSubtasks(),
                        new TypeToken<List<Subtask>>() {
                        }.getType());
                sendText(exchange, json, 200);
                return;
            }
            throw new NotFoundException("Excepted Epic class! But not found!");
        }
    }

    @Override
    public void processPost(String path, HttpExchange exchange) throws IOException {
        String[] urlParts = path.split("/");

        String request = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (urlParts.length == 2) {
            Epic task = jsonMapper.fromJson(request, Epic.class);
            taskManager.createTask(task);

            String json = jsonMapper.toJson(task);
            sendText(exchange, json, 201);
        }

        if (urlParts.length == 3) {
            Epic task = jsonMapper.fromJson(request, Epic.class);
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

            Task task = taskManager.getTaskById(id);
            if (Objects.isNull(task) || !checkTaskClassMatch(task, TaskType.EPIC)) {
                throw new TaskHasInteractionsException("Task wasn't found!");
            }

            String json = jsonMapper.toJson(task);
            sendText(exchange, json, 200);
        }
    }
}
