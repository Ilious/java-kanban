package ru.yandex.model.interfaces;

import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;

import java.util.List;
import java.util.Optional;

public interface ITaskManager {
    Optional<Task> getTaskById(Integer id);

    Task removeById(Integer id);

    Task createTask(Task task);

    Task updateTask(Task task);

    List<Subtask> getListSubTasks();

    List<Epic> getListEpics();

    List<Task> getListTasks();

    List<Task> getListAllTasks();

    List<Task> getHistory();

    void deleteTasks();

    void deleteSubtasks();

    void deleteEpics();

    List<Task> getPrioritizedTasks();
}
