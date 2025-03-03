package ru.yandex.model;

import ru.yandex.model.enums.TaskStatus;
import ru.yandex.model.enums.TaskType;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Epic extends Task {
    private ArrayList<Subtask> subtasks = new ArrayList<>();

    public Epic(Task task) {
        super(task.getDescription(), task.getLabel(), task.getId(), TaskStatus.NEW);
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    public Task addSubtask(Subtask task) {
        subtasks.add(task);
        return task;
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    public void updateStatus() {
        AtomicInteger newStat = new AtomicInteger();
        this.getSubtasks().forEach(subtask -> {
            if (subtask.getStatus() == TaskStatus.IN_PROGRESS) {
                this.updateStatus(TaskStatus.IN_PROGRESS);
                return;
            }
            if (subtask.getStatus() == TaskStatus.NEW) {
                newStat.getAndIncrement();
            }
        });

        if (newStat.get() == this.getSubtasks().size()) {
            this.updateStatus(TaskStatus.NEW);
        } else {
            this.updateStatus(TaskStatus.DONE);
        }
    }
}
