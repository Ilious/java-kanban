package ru.yandex.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.model.Task;
import ru.yandex.model.enums.TaskStatus;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryTaskManagerTest extends AbstractTaskManagerTest<InMemoryTaskManager> {


    @Override
    InMemoryTaskManager getManager() {
        return new InMemoryTaskManager(Managers.getDefaultHistory());
    }

    @BeforeEach
    void init() {
        manager = getManager();
    }

    @Test
    void getHistoryTest() {
        manager.createTask(task);
        manager.createTask(epic);

        manager.getTaskById(1);
        manager.getTaskById(2);

        assertEquals(2, manager.getHistory().size(), "getHistory gives wrong size");
    }

    @Test
    void createTask_ShouldAddToEmptyPriorityList() {
        Instant now = Instant.now();
        Task firstTask = new Task(task.getDescription(), task.getLabel(), task.getId(), TaskStatus.IN_PROGRESS,
                now, Duration.of(20, ChronoUnit.MINUTES));

        manager.createTask(firstTask);

        assertEquals(1, manager.getPrioritizedTasks().size(), "size of prioritized ListTask is " +
                "not correct");
        assertEquals(manager.getPrioritizedTasks().get(0).getStartTime(), now,
                "getPrioritizedTasks doesn't work");
    }

    @Test
    void createTask_HasNoInteractions_ShouldAddTasks() {
        Task firstTask = new Task(task.getDescription(), task.getLabel(), task.getId(), TaskStatus.IN_PROGRESS,
                Instant.now(), Duration.of(20, ChronoUnit.MINUTES));

        Task secondTask = new Task(task.getDescription(), task.getLabel(), task.getId(), TaskStatus.IN_PROGRESS,
                Instant.now().plus(21, ChronoUnit.MINUTES), Duration.of(20, ChronoUnit.MINUTES));
        manager.createTask(firstTask);
        manager.createTask(secondTask);

        assertEquals(2, manager.getPrioritizedTasks().size(), "size of prioritized ListTask is " +
                "not correct");
    }

    @Test
    void createTask_HasBoundaryInteractions_ShouldAddTask() {
        Instant now = Instant.now();
        Task firstTask = new Task(task.getDescription(), task.getLabel(), task.getId(), TaskStatus.IN_PROGRESS,
                now, Duration.of(20, ChronoUnit.MINUTES));

        Task secondTask = new Task(task.getDescription(), task.getLabel(), task.getId(), TaskStatus.IN_PROGRESS,
                now.plus(20, ChronoUnit.MINUTES), Duration.of(20, ChronoUnit.MINUTES));
        manager.createTask(firstTask);
        manager.createTask(secondTask);

        assertEquals(2, manager.getPrioritizedTasks().size(), "size of prioritized ListTask is " +
                "not correct");
    }

    @Test
    void createTask_HasInteractions_ShouldNotAddTask() {
        Instant now = Instant.now();
        Task firstTask = new Task(task.getDescription(), task.getLabel(), task.getId(), TaskStatus.IN_PROGRESS,
                now, Duration.of(20, ChronoUnit.MINUTES));

        Task secondTask = new Task(task.getDescription(), task.getLabel(), task.getId(), TaskStatus.IN_PROGRESS,
                now.plus(17, ChronoUnit.MINUTES), Duration.of(20, ChronoUnit.MINUTES));
        manager.createTask(firstTask);
        manager.createTask(secondTask);

        assertEquals(1, manager.getPrioritizedTasks().size(), "size of prioritized ListTask is " +
                "not correct");
    }
}