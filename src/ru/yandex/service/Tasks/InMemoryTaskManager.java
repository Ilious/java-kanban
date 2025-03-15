package ru.yandex.service.Tasks;

import ru.yandex.exception.task.NotFoundException;
import ru.yandex.exception.task.TaskHasInteractionsException;
import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.model.enums.TaskType;
import ru.yandex.model.interfaces.ITaskHistory;
import ru.yandex.model.interfaces.ITaskManager;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class InMemoryTaskManager implements ITaskManager {

    protected final HashMap<Integer, Task> listTasks = new HashMap<>();

    protected final HashMap<Integer, Subtask> listSubTasks = new HashMap<>();

    protected final HashMap<Integer, Epic> listEpics = new HashMap<>();

    protected final ITaskHistory historyManager;

    protected TreeSet<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));

    protected int idx = 0;

    public InMemoryTaskManager(ITaskHistory historyManager) {
        this.historyManager = historyManager;
    }

    public int getIdx() {
        return idx;
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    public void nextIdx() {
        idx++;
    }

    @Override
    public List<Task> getListTasks() {
        return new ArrayList<>(listTasks.values());
    }

    @Override
    public List<Subtask> getListSubTasks() {
        return new ArrayList<>(listSubTasks.values());
    }

    @Override
    public List<Epic> getListEpics() {
        return new ArrayList<>(listEpics.values());
    }

    @Override
    public List<Task> getListAllTasks() {
        return Stream.of(getListSubTasks(), getListTasks(), getListEpics())
                .flatMap(List::stream)
                .collect(toList());
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(historyManager.getHistory());
    }

    @Override
    public void deleteTasks() {
        listTasks.keySet()
                .forEach(t -> {
                    prioritizedTasks.remove(listTasks.get(t));
                    historyManager.remove(t);
                });
        listTasks.clear();
    }

    @Override
    public void deleteSubtasks() {
        listEpics.values().forEach(epic -> {
            List<Subtask> subtasks = epic.getSubtasks();

            subtasks.stream()
                    .map(Task::getId)
                    .forEach(t -> {
                        prioritizedTasks.remove(subtasks.get(t));
                        historyManager.remove(t);
                    });

            subtasks.clear();
            epic.updateStatus();
        });
        listSubTasks.clear();
    }

    @Override
    public void deleteEpics() {
        listEpics.keySet()
                .forEach(t -> {
                    prioritizedTasks.remove(listEpics.get(t));
                    historyManager.remove(t);
                });

        listSubTasks.keySet()
                .forEach(t -> {
                    prioritizedTasks.remove(listSubTasks.get(t));
                    historyManager.remove(t);
                });

        listEpics.clear();
        listSubTasks.clear();
    }

    @Override
    public Task getTaskById(Integer id) throws NotFoundException {
        Task task = null;
        if (listTasks.containsKey(id)) task = listTasks.get(id);
        else if (listEpics.containsKey(id)) task = listEpics.get(id);
        else if (listSubTasks.containsKey(id)) task = listSubTasks.get(id);

        if (task == null) {
            throw new NotFoundException("Task wasn't found");
        }

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
                    .forEach(t -> {
                        prioritizedTasks.remove(listSubTasks.get(t));
                        historyManager.remove(t);
                    });

            listSubTasks.entrySet().removeIf(entry -> entry.getValue().getEpicId() == id);
            prioritizedTasks.remove(listEpics.get(id));

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
            prioritizedTasks.remove(listSubTasks.get(id));
            return listSubTasks.remove(id);
        } else if (listTasks.containsKey(id)) {
            prioritizedTasks.remove(listTasks.get(id));

            return listTasks.remove(id);
        }
        return null;
    }

    @Override
    public Task createTask(Task task) {
        nextIdx();
        Task newTask = new Task(task.getDescription(), task.getLabel(), task.getStatus(),
                task.getStartTime(), task.getDuration());
        newTask.setId(this.getIdx());
        task.setId(this.getIdx());

        if (task.getType() == TaskType.EPIC) {
            Epic epic = new Epic(newTask);
            listEpics.put(this.getIdx(), epic);
            epic.setId(this.getIdx());
        } else if (task.getType() == TaskType.SUBTASK) {
            Subtask subtask = new Subtask(newTask, ((Subtask) task).getEpicId());
            subtask.setId(this.getIdx());

            listSubTasks.put((this.getIdx()), subtask);

            Optional<Epic> epic = getListEpics().stream()
                    .filter(i -> i.getId() == ((Subtask) task).getEpicId())
                    .findFirst();

            if (epic.isPresent()) {
                Epic neededEpic = epic.get();
                neededEpic.addSubtask(subtask);
                neededEpic.updateStatus();
            }
        } else {
            listTasks.put(this.getIdx(), newTask);
        }

        addToPriorityList(newTask);

        return task;
    }

    @Override
    public Task updateTask(Task task, int id) {
        task.setId(getTaskById(id).getId());

        prioritizedTasks.remove(task);

        if (hasInteractions(task)) {
            throw new TaskHasInteractionsException("Task time overlaps with another task!");
        }

        addToPriorityList(task);

        Task updatedTask;
        if (task.getType() == TaskType.EPIC) {
            updatedTask = new Task(task.getDescription(), task.getLabel(), task.getStatus());
            updatedTask.setId(id);

            Epic epic = new Epic(updatedTask);
            listEpics.put(task.getId(), epic);
        } else if (task.getType() == TaskType.SUBTASK) {
            updatedTask = new Subtask(task, ((Subtask) task).getEpicId());
            updatedTask.setId(id);

            Subtask subtask = (Subtask) updatedTask;
            subtask.setEpicId(((Subtask) task).getEpicId());

            listSubTasks.put(task.getId(), subtask);

            Task epicTask = getTaskById(subtask.getEpicId());
            if (epicTask.getType() != TaskType.EPIC) {
                throw new TaskHasInteractionsException("EpicTask doesn't match task type");
            }
            Epic ep = (Epic) epicTask;

            ep.getSubtasks().removeIf(s -> s.getId() == subtask.getId());
            ep.addSubtask(subtask);
            ep.updateStatus();

        } else {
            updatedTask = new Task(task.getDescription(), task.getLabel(), task.getStatus());
            updatedTask.setId(id);

            listTasks.put(task.getId(), updatedTask);
        }
        return updatedTask;
    }

    protected void addToPriorityList(Task task) {
        if (task.getStartTime() != null) {
            boolean interactions = hasInteractions(task);

            if (!interactions)
                prioritizedTasks.add(task);
        }
    }

    public boolean hasInteractions(Task task) {
        return prioritizedTasks.stream()
                .anyMatch(t -> t.getEndTime().isAfter(task.getStartTime()) &&
                        t.getStartTime().isBefore(task.getEndTime()));
    }
}
