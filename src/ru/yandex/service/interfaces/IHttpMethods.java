package ru.yandex.service.interfaces;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public interface IHttpMethods {

    void processPost(String path, HttpExchange exchange) throws IOException;

    void processGet(String path, HttpExchange exchange) throws IOException;

    void processDelete(String path, HttpExchange exchange) throws IOException;
}
