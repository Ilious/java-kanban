package ru.yandex.service;

import ru.yandex.model.*;
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

    public ArrayList<Task> getListTasks() {
        return new ArrayList<>(listTasks.values());
    }

    public ArrayList<Subtask> getListSubTasks() {
        return new ArrayList<>(listSubTasks.values());
    }

    public ArrayList<Epic> getListEpics() {
        return new ArrayList<>(listEpics.values());
    }

    public List<Task> getHistory() {
        return new ArrayList<>(historyManager.getHistory());
    }

    public int getIdx() {
        return idx;
    }

    public void nextIdx() {
        idx++;
    }

    public void deleteTasks() {
        getListTasks().clear();
    }

    public void deleteSubtasks() {
        for (Epic epic : getListEpics()) {
            epic.getSubtasks().clear();
            epic.updateStatus();
        }
        getListSubTasks().clear();
    }

    public void deleteEpics() {
        getListEpics().clear();
        getListSubTasks().clear();
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
        if (listEpics.containsKey(id)) {
            listSubTasks.entrySet().removeIf(entry -> entry.getValue().getEpicId() + 1 == id);
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
            return listTasks.put(task.getId(), new Task(task.getDescription(), task.getLabel(), task.getId(), task.getStatus()));
    }
}
