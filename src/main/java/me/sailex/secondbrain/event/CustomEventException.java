package me.sailex.secondbrain.event;

public class CustomEventException extends RuntimeException {
    public CustomEventException(String message, Exception cause) {
        super(message, cause);
    }
}
