package ru.yandex.model;

import ru.yandex.model.enums.TaskStatus;
import ru.yandex.model.enums.TaskType;

import java.time.Duration;
import java.time.Instant;
import java.util.StringJoiner;

public class Subtask extends Task {
    private int epicId;

    public Subtask(Task task, int epicId) {
        super(task.getDescription(), task.getLabel(), task.getStatus());
        super.setStartTime(task.getStartTime());
        super.setDuration(task.getDuration());

        this.epicId = epicId;
    }

    public Subtask(String description, String label, TaskStatus status, int epicId) {
        super(description, label, status);
        this.epicId = epicId;
    }

    public Subtask(String description, String label, TaskStatus status, Instant startTime, Duration duration,
                   int epicId) {
        super(description, label, status, startTime, duration);
        this.epicId = epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    @Override
    public String toString() {
        return super.toString() + new StringJoiner(", ", Subtask.class.getSimpleName() + "[", "]")
                .add("epicId=" + epicId);
    }
}
