import ru.yandex.model.*;
import ru.yandex.service.TaskManager;

public class Main {
    public static void main(String[] args) {
        TaskManager tk = new TaskManager();

        Task task = new Task("create projectTasks", "tasks", tk.getIdx(), TaskStatus.NEW);
        tk.createTask(task);
        Task task2 = new Task("send the project[easy]", "send", tk.getIdx(), TaskStatus.NEW);
        tk.createTask(task2);

        Epic epicTask = new Epic(new Task("create projectTasks[complicated]", "epic task",
                tk.getIdx(), TaskStatus.NEW));
        tk.createTask(epicTask);

        Subtask subtask = new Subtask(new Task(task.getLabel(), "subtask", tk.getIdx(), TaskStatus.IN_PROGRESS),
                epicTask.getId());
        tk.createTask(subtask);

        Subtask subtask2 = new Subtask(new Task(task2.getLabel(), task2.getLabel(), tk.getIdx(), TaskStatus.
                IN_PROGRESS), epicTask.getId());
        tk.createTask(subtask2);

        Epic epicTask2 = new Epic(new Task("pass the project", "epic task2", tk.getIdx(),
                TaskStatus.NEW));
        tk.createTask(epicTask2);
        Subtask subtaskEp2 = new Subtask(new Task("send the project[hard]", "send", tk.getIdx(),
                TaskStatus.IN_PROGRESS), epicTask2.getId());
        tk.createTask(subtaskEp2);

        tk.printAllTasks();

        tk.getTaskById(1).updateStatus(TaskStatus.DONE);
        tk.getTaskById(2).updateStatus(TaskStatus.DONE);

        Task taskEPIC = tk.getTaskById(3);

        ((Epic) taskEPIC).updateStatus();
        tk.getTaskById(4).updateStatus(TaskStatus.DONE);
        tk.getTaskById(5).updateStatus(TaskStatus.IN_PROGRESS);
        ((Epic) taskEPIC).updateStatus();

        Task taskEPIC2 = tk.getTaskById(6);
        ((Epic) taskEPIC2).updateStatus();
        tk.getTaskById(7).updateStatus(TaskStatus.DONE);
        ((Epic) taskEPIC2).updateStatus();

        tk.printAllTasks();

        tk.removeById(1);
        tk.removeById(3);
        tk.printAllTasks();
    }
}
