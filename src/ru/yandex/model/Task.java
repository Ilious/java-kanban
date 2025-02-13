package ru.yandex.model;

import ru.yandex.model.enums.TaskStatus;
import ru.yandex.model.enums.TaskType;

import java.util.Objects;
import java.util.StringJoiner;

public class Task {
    private final String description, label;

    private final int id;

    private TaskStatus status;

    public Task(String description, String label, int id, TaskStatus status) {
        this.description = description;
        this.label = label;
        this.id = id;
        this.status = status;
    }

    public void updateStatus(TaskStatus status) {
        this.status = status;
    }

    public String getLabel() {
        return label;
    }

    public int getId() {
        return id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;

        Task task = (Task) obj;
        return this.hashCode() == task.hashCode();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Task.class.getSimpleName() + "[", "]")
                .add("description='" + description + "'")
                .add("label='" + label + "'")
                .add("id=" + id)
                .add("status=" + status)
                .toString();
    }

    public TaskType getType() {
        return TaskType.TASK;
    }

}
