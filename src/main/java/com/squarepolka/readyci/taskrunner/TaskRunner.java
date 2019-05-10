package com.squarepolka.readyci.taskrunner;

import com.squarepolka.readyci.exceptions.TaskExitException;
import com.squarepolka.readyci.tasks.Task;
import com.squarepolka.readyci.tasks.readyci.TaskExecuteException;
import com.squarepolka.readyci.util.time.TaskTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TaskRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskRunner.class);

    protected TaskRunnerFactory taskRunnerFactory;
    protected List<Task> defaultPreTasks;
    protected List<Task> defaultPostTasks;
    protected List<Task> configuredTasks;
    protected BuildEnvironment buildEnvironment;

    public TaskRunner(BuildEnvironment buildEnvironment, TaskRunnerFactory taskRunnerFactory) {
        this.defaultPreTasks = new ArrayList<Task>();
        this.defaultPostTasks = new ArrayList<Task>();
        this.configuredTasks = new ArrayList<Task>();
        this.buildEnvironment = buildEnvironment;
        this.taskRunnerFactory = taskRunnerFactory;
    }

    public void setConfiguredTasks(List<Task> tasks) {
        this.configuredTasks = tasks;
    }

    public void addConfiguredTask(Task task) {
        configuredTasks.add(task);
    }

    public void addDefaultPreTask(Task task) {
        defaultPreTasks.add(task);
    }

    public void addDefaultPostTask(Task task) {
        defaultPostTasks.add(task);
    }

    public void runAllTasks() {
        try {
            LOGGER.info("EXECUTING\tBUILD\t{}\t({})", buildEnvironment.getPipelineName(), buildEnvironment.getBuildUUID());
            runTaskList(defaultPreTasks);
            checkThatTasksExist();
            runTaskList(configuredTasks);
            runTaskList(defaultPostTasks);
            LOGGER.info("COMPLETED\tBUILD\t{}\t({})", buildEnvironment.getPipelineName(), buildEnvironment.getBuildUUID());
        }
        catch (TaskExitException e) {
            LOGGER.info("EXITED\tBUILD\t{}\t({})\t{}", buildEnvironment.getPipelineName(), buildEnvironment.getBuildUUID(), e.getMessage());
        }
        catch (RuntimeException e) {
            LOGGER.info("FAILED\tBUILD\t{}\t({})", buildEnvironment.getPipelineName(), buildEnvironment.getBuildUUID());
            throw e;
        }
    }

    private void checkThatTasksExist() {
        if (configuredTasks.size() <= 0) {
            throw new RuntimeException("There are no tasks to run. Add some tasks and then try again.");
        }
    }

    private void runTaskList(List<Task> tasks) throws TaskExitException {
        for (Task task : tasks) {
            try {
                task.setTaskRunner(this);
                runTask(task);
                task.setTaskRunner(null);
            } catch (TaskFailedException e) {
                handleTaskFailure(task, e);
            }
        }
    }

    private void runTask(Task task) throws TaskFailedException, TaskExitException {
        LOGGER.info("RUNNING\tTASK\t{}", task.taskIdentifier());
        TaskTimer taskTimer = TaskTimer.newStartedTimer();
        task.performTask(buildEnvironment);
        String formattedTime = taskTimer.stopAndGetElapsedTime();
        LOGGER.info("\t\t\tFINISHED IN {}", formattedTime);
    }

    private void handleTaskFailure(Task task, Exception e) {
        String errorMessage = String.format("FAILED\tTASK\t%s with exception: %s", task.taskIdentifier(), e.toString());
        LOGGER.error(errorMessage);
        if (task.shouldStopOnFailure()) {
            TaskExecuteException taskExecuteException = new TaskExecuteException(errorMessage);
            taskExecuteException.setStackTrace(e.getStackTrace());
            throw taskExecuteException;
        }
    }

    public TaskRunnerFactory getTaskRunnerFactory() {
        return taskRunnerFactory;
    }

}