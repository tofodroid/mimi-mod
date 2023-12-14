package io.github.tofodroid.mods.mimi.util;

public class NoStackTraceException extends RuntimeException {
    public NoStackTraceException(String message) {
        super(message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}