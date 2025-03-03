import ru.yandex.model.Epic;
import ru.yandex.model.Subtask;
import ru.yandex.model.Task;
import ru.yandex.model.enums.TaskStatus;
import ru.yandex.model.interfaces.ITaskManager;
import ru.yandex.service.FileBackedTaskManager;
import ru.yandex.service.InMemoryHistoryManager;

public class Main {
    public static void main(String[] args) {
        FileBackedTaskManager tk = new FileBackedTaskManager(new InMemoryHistoryManager());

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

        printAllTasks(tk);

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

        printAllTasks(tk);

        System.out.println("&".repeat(30));
        printAllTasks(tk);
//        tk.fileUpload(new File("Tasks.csv"));
    }

    private static void printAllTasks(ITaskManager manager) {
        System.out.println("Задачи:");
        manager.getListTasks().forEach(System.out::println);

        System.out.println("Эпики:");
        manager.getListEpics().forEach(epic -> {
            System.out.println("epic = " + epic);

            epic.getSubtasks().forEach(subtask -> System.out.println("--> " + subtask));
        });

        System.out.println("Подзадачи:");
        manager.getListSubTasks().forEach(System.out::println);

        System.out.println("История:");
        manager.getHistory().forEach(System.out::println);
    }
}
