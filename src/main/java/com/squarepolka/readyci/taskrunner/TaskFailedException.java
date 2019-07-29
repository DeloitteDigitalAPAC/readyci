package com.squarepolka.readyci.taskrunner;

public class TaskFailedException extends RuntimeException {

    public TaskFailedException(String message) {
        super(message);
    }
}
