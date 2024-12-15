package ru.yandex.model;

public interface ITask {
    void removeAllTasks();
    Task getTaskById(Integer id);
    Task removeById(Integer id);
    Task createTask(Task task);
    Task updateTask(Task task);
    Task getSubtasksByEpic(Epic epic);
}
//    a. Менеджер сам не выбирает статус для задачи. Информация о нём приходит менеджеру вместе с информацией о самой задаче. По этим данным в одних случаях он будет сохранять статус, в других будет рассчитывать.
//        b. Для эпиков:
//        если у эпика нет подзадач или все они имеют статус NEW, то статус должен быть NEW.
//        если все подзадачи имеют статус DONE, то и эпик считается завершённым — со статусом DONE.
//        во всех остальных случаях статус должен быть IN_PROGRESS.