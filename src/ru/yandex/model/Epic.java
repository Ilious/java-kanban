package ru.yandex.model;

import ru.yandex.model.enums.TaskStatus;
import ru.yandex.model.enums.TaskType;

import java.util.ArrayList;

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
        int newStat = 0;
        for (Subtask subtask : this.getSubtasks()) {
            if (subtask.getStatus() == TaskStatus.IN_PROGRESS) {
                this.updateStatus(TaskStatus.IN_PROGRESS);
                return;
            }
            if (subtask.getStatus() == TaskStatus.NEW) {
                newStat++;
            }
        }
        if (newStat == this.getSubtasks().size()) {
            this.updateStatus(TaskStatus.NEW);
        } else {
            this.updateStatus(TaskStatus.DONE);
        }
    }
}
