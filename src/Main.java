import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.model.enums.TaskStatus;
import ru.yandex.model.interfaces.ITaskManager;
import ru.yandex.service.InMemoryHistoryManager;
import ru.yandex.service.InMemoryTaskManager;

public class Main {
    public static void main(String[] args) {
        ITaskManager tk = new InMemoryTaskManager(new InMemoryHistoryManager());

        Task task = new Task("create projectTasks", "tasks", 1, TaskStatus.NEW);
        tk.createTask(task);
        Task task2 = new Task("send the project[easy]", "send", 2, TaskStatus.NEW);
        tk.createTask(task2);

        Epic epicTask = new Epic(new Task("create projectTasks[complicated]", "epic task",
                3, TaskStatus.NEW));
        tk.createTask(epicTask);

        Subtask subtask = new Subtask(new Task(task.getLabel(), "subtask", 4, TaskStatus.DONE),
                epicTask.getId());
        tk.createTask(subtask);

        Subtask subtask2 = new Subtask(new Task(task2.getLabel(), task2.getLabel(), 5, TaskStatus
                .IN_PROGRESS), epicTask.getId());
        tk.createTask(subtask2);

        Epic epicTask2 = new Epic(new Task("pass the project", "epic task2", 6,
                TaskStatus.NEW));
        tk.createTask(epicTask2);
        Subtask subtaskEp2 = new Subtask(new Task("send the project[hard]", "send", 7,
                TaskStatus.DONE), epicTask2.getId());
        tk.createTask(subtaskEp2);

        ((Epic)tk.getTaskById(3)).updateStatus();
        ((Epic)tk.getTaskById(6)).updateStatus();
        for (Task tempSubtask: tk.getListTasks()) {
            System.out.println(tempSubtask);
        }
        System.out.println("*".repeat(30));
        for (Epic tempSubtask: tk.getListEpics()) {
            System.out.println(tempSubtask);
        }
        System.out.println("*".repeat(30));
        for (Subtask tempSubtask: tk.getListSubTasks()) {
            System.out.println(tempSubtask);
        }
        System.out.println("-".repeat(30));
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

        tk.removeById(1);
        tk.removeById(3);

        for (Task tempSubtask: tk.getListTasks()) {
            System.out.println(tempSubtask);
        }
        System.out.println("*".repeat(30));
        for (Epic tempSubtask: tk.getListEpics()) {
            System.out.println(tempSubtask);
        }
        System.out.println("*".repeat(30));
        for (Subtask tempSubtask: tk.getListSubTasks()) {
            System.out.println(tempSubtask);
        }

        System.out.println("&".repeat(30));
        printAllTasks(tk);
    }

    private static void printAllTasks(ITaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getListTasks()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Epic epic : manager.getListEpics()) {
            System.out.println(epic);

            for (Task task : epic.getSubtasks()) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : manager.getListSubTasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task.toString());
        }
    }
}
