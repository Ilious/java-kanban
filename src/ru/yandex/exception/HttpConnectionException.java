package ru.yandex.exception;

public class HttpConnectionException extends RuntimeException {
    public HttpConnectionException(Throwable cause) {
        super(cause);
    }
}
