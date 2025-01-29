package ru.yandex.service;

import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.model.interfaces.ITaskHistory;
import ru.yandex.model.interfaces.ITaskManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements ITaskManager {
    private final HashMap<Integer, Task> listTasks = new HashMap<>();

    private final HashMap<Integer, Subtask> listSubTasks = new HashMap<>();
    private final HashMap<Integer, Epic> listEpics = new HashMap<>();
    private final ITaskHistory historyManager;

    private int idx = 0;

    public InMemoryTaskManager(ITaskHistory historyManager) {
        this.historyManager = historyManager;
    }

    public int getIdx() {
        return idx;
    }

    public void nextIdx() {
        idx++;
    }

    @Override
    public ArrayList<Task> getListTasks() {
        return new ArrayList<>(listTasks.values());
    }

    @Override
    public ArrayList<Subtask> getListSubTasks() {
        return new ArrayList<>(listSubTasks.values());
    }

    @Override
    public ArrayList<Epic> getListEpics() {
        return new ArrayList<>(listEpics.values());
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(historyManager.getHistory());
    }

    @Override
    public void deleteTasks() {
        listTasks.clear();
    }

    @Override
    public void deleteSubtasks() {
        for (Epic epic : listEpics.values()) {
            List<Subtask> subtasks = epic.getSubtasks();

            subtasks.stream()
                    .map(Task::getId)
                    .forEach(historyManager::remove);

            subtasks.clear();
            epic.updateStatus();
        }
        listSubTasks.clear();
    }

    @Override
    public void deleteEpics() {
        listEpics.values()
                .stream()
                .map(Task::getId)
                .forEach(historyManager::remove);

        listSubTasks.values()
                    .stream()
                    .map(Task::getId)
                    .forEach(historyManager::remove);

        listEpics.clear();
        listSubTasks.clear();
    }

    @Override
    public Task getTaskById(Integer id) {
        Task task = null;
        if (listTasks.containsKey(id)) task = listTasks.get(id);
        else if (listEpics.containsKey(id)) task = listEpics.get(id);
        else if (listSubTasks.containsKey(id)) task = listSubTasks.get(id);

        historyManager.addToHistory(task);
        return task;
    }

    @Override
    public Task removeById(Integer id) {
        historyManager.remove(id);

        if (listEpics.containsKey(id)) {
            listSubTasks.values().stream()
                    .filter(subtask -> subtask.getEpicId() == id)
                    .map(Subtask::getId)
                    .forEach(historyManager::remove);

            listSubTasks.entrySet().removeIf(entry -> entry.getValue().getEpicId() == id);
            return listEpics.remove(id);
        } else if (listSubTasks.containsKey(id)) {
            int epicId = listSubTasks.get(id).getEpicId();
            Epic epic = listEpics.get(epicId);

            Subtask neededSubtask = listSubTasks.get(id);
            if (epic != null) {
                ArrayList<Subtask> subtasksFromEpic = epic.getSubtasks();

                subtasksFromEpic.remove(neededSubtask);

                epic.updateStatus();
            }
            return listSubTasks.remove(id);
        } else if (listTasks.containsKey(id)) return listTasks.remove(id);

        else return null;
    }

    @Override
    public Task createTask(Task task) {
        nextIdx();
        Task newTask = new Task(task.getDescription(), task.getLabel(), task.getId(), task.getStatus());
        if (task instanceof Epic) listEpics.put(this.getIdx(), new Epic(newTask));
        else if (task instanceof Subtask) {
            Subtask subtask = new Subtask(newTask, ((Subtask) task).getEpicId());
            listSubTasks.put((this.getIdx()), subtask);
            Epic epic = listEpics.get((subtask).getEpicId());
            epic.addSubtask(subtask);
            epic.updateStatus();
        } else listTasks.put(this.getIdx(), newTask);
        return task;
    }

    @Override
    public Task updateTask(Task task) {
        if (task instanceof Epic) return listEpics.put(task.getId(), new Epic(task));
        else if (task instanceof Subtask) {
            Subtask subtask = new Subtask(task, ((Subtask) task).getEpicId());

            listSubTasks.put(task.getId(), subtask);
            Epic epic = (Epic) getTaskById(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtasks().removeIf(s -> s.getId() == subtask.getId());
                epic.addSubtask(subtask);
                epic.updateStatus();
            }

            return task;
        } else
            return listTasks.put(task.getId(),
                    new Task(task.getDescription(), task.getLabel(), task.getId(), task.getStatus()));
    }
}
