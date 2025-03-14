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

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class EpicHandler extends TaskHandler implements HttpHandler {

    public EpicHandler(Gson gsonMapper, ITaskManager taskManager) {
        super(gsonMapper, taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        URI requestURI = exchange.getRequestURI();
        String path = requestURI.getPath();

        String[] urlParts = path.split("/");
        try {
            switch (method) {
                case "GET":
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
                    break;
                case "POST":
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

                    break;
                case "DELETE":
                    if (urlParts.length == 3) {
                        int id = Integer.parseInt(urlParts[2]);

                        Task task = taskManager.getTaskById(id);
                        if (Objects.isNull(task) || !checkTaskClassMatch(task, TaskType.EPIC)) {
                            throw new TaskHasInteractionsException("Task wasn't found!");
                        }

                        String json = jsonMapper.toJson(task);
                        sendText(exchange, json, 200);
                        return;
                    }
                default:
                    sendText(exchange, "Bad request", 400);
            }
        } catch (NotFoundException e) {
            sendText(exchange, "Task not found", 404);
        } catch (TaskHasInteractionsException e) {
            sendText(exchange, "Task not found", 406);
        } catch (Exception e) {
            sendText(exchange, "Internal Server error", 500);
        } finally {
            exchange.close();
        }
    }
}
