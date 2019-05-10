package com.squarepolka.readyci.tasks.code;

import com.squarepolka.readyci.exceptions.TaskExitException;
import com.squarepolka.readyci.taskrunner.BuildEnvironment;
import com.squarepolka.readyci.tasks.Task;
import com.squarepolka.readyci.util.PropertyMissingException;
import com.squarepolka.readyci.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

import static com.squarepolka.readyci.tasks.code.GitCommit.SKIPCI_TAG;

@Component
public class GitCheckout extends Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitCheckout.class);
    public static final String TASK_CHECKOUT_GIT = "checkout_git";
    public static final String BUILD_PROP_GIT_PATH = "gitPath";
    public static final String BUILD_PROP_GIT_BRANCH = "gitBranch";
    public static final String CONST_UNKNOWN_GIT_BRANCH = "readyci_unknown_branch";
    private static final String COMMAND_GIT = "/usr/bin/git";

    public String taskIdentifier() {
        return TASK_CHECKOUT_GIT;
    }

    public void performTask(BuildEnvironment buildEnvironment) throws TaskExitException {

        try {
            String gitPath = buildEnvironment.getProperty(BUILD_PROP_GIT_PATH);
            LOGGER.debug("The gitPath parameter is specified, so I'll check out the code.");
            try {
                String gitBranch = buildEnvironment.getProperty(BUILD_PROP_GIT_BRANCH);
                buildEnvironment.addProperty(BUILD_PROP_GIT_BRANCH, gitBranch);
                executeCommand(new String[]{COMMAND_GIT, "clone", "-b", gitBranch, gitPath, buildEnvironment.getCodePath()});
            } catch (PropertyMissingException e) {
                LOGGER.debug("gitBranch not specified. Will clone from HEAD");
                executeCommand(new String[]{COMMAND_GIT, "clone", "--single-branch", gitPath, buildEnvironment.getCodePath()});
                String branchName = getCurrentBranchName(buildEnvironment);
                buildEnvironment.addProperty(BUILD_PROP_GIT_BRANCH, branchName);
                return;
            }
        } catch (PropertyMissingException e) {
            LOGGER.debug("The gitPath parameter was not specified, so I'll assume the code is already checked out and set the code path to the current directory and configure the build environment accordingly.");
            buildEnvironment.setCodePath(buildEnvironment.getRealCIRunPath());
            buildEnvironment.configureProjectPath();
            String branchName = getCurrentBranchName(buildEnvironment);
            buildEnvironment.addProperty(BUILD_PROP_GIT_BRANCH, branchName);
        }

        InputStream inputStream = executeCommand(new String[]{COMMAND_GIT, "log", "-1", "--pretty=%B"}, buildEnvironment.getProjectPath());
        try {
            String commitMessage = Util.readInputStream(inputStream);
            if(commitMessage.contains(SKIPCI_TAG))
            {
                throw new TaskExitException("Detected build skip parameter, skipping the build");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected String getCurrentBranchName(BuildEnvironment buildEnvironment) {
        InputStream inputStream = executeCommand(new String[]{COMMAND_GIT, "branch"}, buildEnvironment.getCodePath());
        java.util.Scanner scanner = new java.util.Scanner(inputStream).useDelimiter("\\A");
        String allBranches = scanner.hasNext() ? scanner.next() : "";
        return filterBranchName(allBranches);
    }

    protected String filterBranchName(String allBranches) {
        StringTokenizer stringTokenizer = new StringTokenizer(allBranches, "\n");
        while (stringTokenizer.hasMoreElements()) {
            String branchString = stringTokenizer.nextToken();
            if (branchString.contains("*")) {
                return branchString.replace("* ", "");
            }
        }
        return CONST_UNKNOWN_GIT_BRANCH;
    }

}