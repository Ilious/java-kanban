package ru.yandex.model;

import java.util.ArrayList;

public class Epic extends Task {
    ArrayList<Subtask> subtasks = new ArrayList<>();

    public Epic(Task task) {
        super(task.getDescription(), task.getLabel(), task.getId(), task.getStatus());
    }

    public Task addSubtask(Subtask task){
        subtasks.add(task);
        return task;
    }

    public void updateStatus(){
        if (subtasks.isEmpty()) super.updateStatus(TaskStatus.DONE);

        boolean allDone = subtasks.stream().allMatch(subtask -> subtask.getStatus() == TaskStatus.DONE);
        if (allDone) super.updateStatus(TaskStatus.DONE);
    }
}
