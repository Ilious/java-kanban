package ru.yandex.exception;

import java.util.Arrays;

public class FileException extends RuntimeException {

    public FileException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileException(String message) {
        super(message);
    }
}
