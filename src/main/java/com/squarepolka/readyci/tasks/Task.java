package com.squarepolka.readyci.tasks;

import com.squarepolka.readyci.exceptions.TaskExitException;
import com.squarepolka.readyci.taskrunner.BuildEnvironment;
import com.squarepolka.readyci.taskrunner.TaskFailedException;
import com.squarepolka.readyci.taskrunner.TaskRunner;
import com.squarepolka.readyci.tasks.readyci.TaskCommand;
import com.squarepolka.readyci.tasks.readyci.TaskCommandHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;

public abstract class Task {

    protected TaskRunner taskRunner;
    @Autowired
    private TaskCommandHandler taskCommandHandler;

    protected InputStream executeCommand(String command) {
        return executeCommand(new String[] {command});
    }

    protected InputStream executeCommand(String command, String workingDirectory) {
        return executeCommand(new String[] {command}, workingDirectory);
    }

    protected InputStream executeCommand(String[] command) {
        return executeCommand(command, "/tmp/");
    }

    protected InputStream executeCommand(String[] command, String workingDirectory) {
        TaskCommand taskCommand = new TaskCommand(command);
        return executeCommand(taskCommand, workingDirectory);
    }

    protected InputStream executeCommand(TaskCommand taskCommand, String workingDirectory) {
        return taskCommandHandler.executeCommand(taskCommand.getCommandAndParams(), workingDirectory);
    }

    // Methods than can be implemented by subclasses
    public boolean shouldStopOnFailure() {
        return true;
    }

    // Methods that must be implemented by subclasses
    public abstract String taskIdentifier();
    public abstract void performTask(BuildEnvironment buildEnvironment) throws TaskFailedException, TaskExitException;

    // Getters and Setters
    public void setTaskRunner(TaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }

}
