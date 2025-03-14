package ru.yandex.model;

import ru.yandex.model.enums.TaskStatus;
import ru.yandex.model.enums.TaskType;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class Epic extends Task {
    private ArrayList<Subtask> subtasks;

    public Epic(Task task) {
        super(task.getDescription(), task.getLabel(), TaskStatus.NEW);
        this.subtasks = new ArrayList<>();
    }

    public Epic(String description, String label) {
        super(description, label, TaskStatus.NEW);
        this.subtasks = new ArrayList<>();
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    public Task addSubtask(Subtask task) {
        subtasks.add(task);
        return task;
    }

    @Override
    public Duration getDuration() {
        return isNotValidate() ? null : subtasks.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);
    }

    @Override
    public Instant getEndTime() {
        return isNotValidate() ? null : subtasks.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(Comparator.comparing(Instant::getEpochSecond))
                .orElse(null);
    }

    @Override
    public Instant getStartTime() {
        return isNotValidate() ? null : subtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(Comparator.comparing(Instant::getEpochSecond))
                .orElse(null);
    }

    public boolean isNotValidate() {
        return subtasks == null || subtasks.isEmpty();
    }


    public ArrayList<Subtask> getSubtasks() {
        return isNotValidate() ? new ArrayList<>() : subtasks;
    }

    public void updateStatus() {
        int newStat = 0;
        int doneStat = 0;
        for (Subtask subtask : this.getSubtasks()) {
            if (subtask.getStatus() == TaskStatus.IN_PROGRESS) {
                this.updateStatus(TaskStatus.IN_PROGRESS);
                return;
            }
            if (subtask.getStatus() == TaskStatus.NEW) {
                newStat++;
            } else if (subtask.getStatus() == TaskStatus.DONE) {
                doneStat++;
            }
        }

        if (newStat == this.getSubtasks().size()) {
            this.updateStatus(TaskStatus.NEW);
        } else if (doneStat == this.getSubtasks().size()) {
            this.updateStatus(TaskStatus.DONE);
        } else {
            this.updateStatus(TaskStatus.IN_PROGRESS);
        }
    }
}
