package me.kenvera.chronocore.Exception;

public class GroupAdditionException extends Exception{
    public GroupAdditionException() {
        super();
    }

    public GroupAdditionException(String message) {
        super(message);
    }

    public GroupAdditionException(String message, Throwable cause) {
        super(message, cause);
    }

    public GroupAdditionException(Throwable cause) {
        super(cause);
    }
}
