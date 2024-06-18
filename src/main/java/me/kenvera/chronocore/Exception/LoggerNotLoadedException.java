package me.kenvera.chronocore.Exception;

public class LoggerNotLoadedException extends RuntimeException {
    public LoggerNotLoadedException(String message) {
        super(message);
    }

    public LoggerNotLoadedException(String message, Throwable cause) {
        super(message, cause);
    }
}
