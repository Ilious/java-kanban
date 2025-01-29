package ru.yandex.model.interfaces;

import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;

import java.util.ArrayList;
import java.util.List;

public interface ITaskManager {
    Task getTaskById(Integer id);

    Task removeById(Integer id);

    Task createTask(Task task);

    Task updateTask(Task task);

    ArrayList<Subtask> getListSubTasks();

    ArrayList<Epic> getListEpics();

    ArrayList<Task> getListTasks();

    List<Task> getHistory();

    void deleteTasks();

    void deleteSubtasks();

    void deleteEpics();
}
