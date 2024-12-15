package ru.yandex.service;

import ru.yandex.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TaskManager implements ITask, IManageable {
    private final HashMap<Integer, Task> listTasks = new HashMap<>();

    private final HashMap<Integer, Subtask> listSubTasks = new HashMap<>();
    private final HashMap<Integer, Epic> listEpics = new HashMap<>();
    private static int idx = 0;

    public HashMap<Integer, Task> getListTasks() {
        return listTasks;
    }

    public HashMap<Integer, Subtask> getListSubTasks() {
        return listSubTasks;
    }
    public HashMap<Integer, Epic> getListEpics() {
        return listEpics;
    }

    public int getIdx() {
        return idx;
    }

    public void nextIdx() {
        idx++;
    }

    @Override
    public void printAllTasks() {
        printEpics();
        printSubtasks();
        printTasks();
        System.out.println("*".repeat(30));
    }

    @Override
    public void printSubtasks() {
        System.out.println("TaskManager.printSubtasks");
        for (Integer id : getListSubTasks().keySet()) {
            System.out.printf("id = %s, getListSubTasks().get(id) = %s\n", id, getListSubTasks().get(id));
        }
        System.out.println("-".repeat(30));
    }

    @Override
    public void printEpics() {
        System.out.println("TaskManager.printEpics");
        for (Integer id : getListEpics().keySet()) {
            System.out.printf("id = %s, getListEpics().get(id) = %s\n", id, getListEpics().get(id));
        }
        System.out.println("-".repeat(30));
    }

    @Override
    public void printTasks() {
        System.out.println("TaskManager.printTasks");
        for (Integer id : getListTasks().keySet()) {
            System.out.printf("id = %s, getListTasks().get(id) = %s\n", id, getListTasks().get(id));
        }
        System.out.println("-".repeat(30));
    }

    @Override
    public void removeAllTasks() {
        listSubTasks.clear();
        listEpics.clear();
        listTasks.clear();
    }

    @Override
    public Task getTaskById(Integer id) {
        Task task = null;
        if (listTasks.containsKey(id))
            task = listTasks.get(id);
        else if (listEpics.containsKey(id))
            task = listEpics.get(id);
        else if (listSubTasks.containsKey(id))
            task = listSubTasks.get(id);

        return task;
    }

    @Override
    public Task removeById(Integer id) {
        if (listEpics.containsKey(id)) {
            listSubTasks.entrySet()
                    .removeIf(entry -> entry.getValue().getEpicId() + 1 == id);
            return listEpics.remove(id);
        }
        else if (listSubTasks.containsKey(id))
            return listSubTasks.remove(id);
        else if (listTasks.containsKey(id))
            return listTasks.remove(id);

        else return null;
    }

    @Override
    public Task createTask(Task task) {
        nextIdx();
        Task newTask = new Task(task.getDescription(), task.getLabel(), task.getId(), task.getStatus());
        if (task instanceof Epic)
            listEpics.put(this.getIdx(), new Epic(newTask));
        else if (task instanceof Subtask) {
            Subtask subtask = new Subtask(newTask, ((Subtask) task).getEpicId());
            listSubTasks.put((this.getIdx()), subtask);
            Epic epic = listEpics.get((subtask).getEpicId() + 1);
            epic.addSubtask(subtask);
        } else
            listTasks.put(this.getIdx(), newTask);
        return task;
    }

    @Override
    public Task updateTask(Task task) {
        if (task instanceof Epic)
            return listEpics.put(task.getId(), new Epic(task));
        else if (task instanceof Subtask)
            return listSubTasks.put(this.getIdx(), new Subtask(task, ((Subtask) task).getEpicId()));
        else
            return listTasks.put(this.getIdx(), new Task(task.getDescription(), task.getLabel(), task.getId(),
                    task.getStatus()));
    }

    @Override
    public Task getSubtasksByEpic(Epic epic) {
        System.out.println("TaskManager.getSubtasksByEpic");
        for (Subtask subtask: getListSubTasks().values()) {
            if (subtask.getEpicId() == epic.getId())
                System.out.println(subtask);
        }
        System.out.println("-".repeat(30));
        return null;
    }
}
