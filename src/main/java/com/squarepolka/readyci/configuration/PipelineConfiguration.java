package com.squarepolka.readyci.configuration;

import com.squarepolka.readyci.tasks.code.GitCheckout;

import java.util.*;

/**
 * Represents a single pipeline configuration.
 * The parameters Map allows arbitrary values to be stored.
 */
public class PipelineConfiguration {

    public static final String PIPELINE_PROJECT_PATH = "projectPath";
    public static final String GIT_NUMBER_REVISIONS = "gitLogHistory";

    public static final String PIPELINE_PATH_PREFIX_BUILD = "/tmp/readyci/";
    public static final String PIPELINE_PATH_PREFIX_CODE = "/code/";
    public static final String PIPELINE_NAME_DEFAULT = "unknown";

    private String name;
    private Map<String, Object> parameters;
    private List<TaskConfiguration> tasks;

    public PipelineConfiguration() {
        this.name = PIPELINE_NAME_DEFAULT;
        this.parameters = new HashMap<String, Object>();
        this.tasks = new ArrayList<TaskConfiguration>();
    }

    public boolean matchesRepositoryName(String repositoryName, String branch) {
        String gitPath = (String) parameters.get(GitCheckout.BUILD_PROP_GIT_PATH);
        String gitBranch = (String) parameters.get(GitCheckout.BUILD_PROP_GIT_BRANCH);
        return gitPath.toLowerCase().contains(repositoryName.toLowerCase()) &&
                branch.contains(gitBranch) &&
                repositoryName.length() > 0 &&
                branch.length() > 0;
    }

    public Object getParameter(String key) {
        return parameters.get(key);
    }

    public boolean hasParameter(String key) {
        return parameters.containsKey(key);
    }

    public void setParameter(String key, Object value) {
        parameters.put(key, value);
    }

    public Set<Map.Entry<String, Object>> getParameters() {
        return parameters.entrySet();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TaskConfiguration> getTasks() {
        return tasks;
    }
}
