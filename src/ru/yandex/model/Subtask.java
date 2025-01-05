package ru.yandex.model;

import java.util.StringJoiner;

public class Subtask extends Task {
    private int epicId;

    public Subtask(Task task, int epicId) {
        super(task.getDescription(), task.getLabel(), task.getId(), task.getStatus());
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return super.toString() + new StringJoiner(", ", Subtask.class.getSimpleName() + "[", "]")
                .add("epicId=" + epicId);
    }
}
