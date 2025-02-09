package ru.yandex.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void getDefaultTest() {
        assertNotNull(Managers.getDefault(), "ITaskManager is Null");
    }

    @Test
    void getDefaultHistoryTest() {
        assertNotNull(Managers.getDefaultHistory(), "ITaskHistory is Null");
    }
}