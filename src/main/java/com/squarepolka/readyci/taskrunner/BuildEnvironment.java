package com.squarepolka.readyci.taskrunner;

import com.squarepolka.readyci.configuration.PipelineConfiguration;
import com.squarepolka.readyci.util.PropertyMissingException;
import com.squarepolka.readyci.util.PropertyTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BuildEnvironment {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildEnvironment.class);

    private String pipelineName;
    private String buildUUID;
    private String codePath;
    private String projectFolder;
    private String projectPath;
    private String credentialsPath;
    private String scratchPath;
    private String realCIRunPath;
    private String username;
    private Map<String, Object> buildParameters;

    public BuildEnvironment(PipelineConfiguration configuration) {
        this.pipelineName = configuration.getName();
        this.buildUUID = UUID.randomUUID().toString();
        this.scratchPath = String.format("%s/%s", PipelineConfiguration.PIPELINE_PATH_PREFIX_BUILD, buildUUID);
        this.codePath = String.format("%s/%s", scratchPath, PipelineConfiguration.PIPELINE_PATH_PREFIX_CODE);
        this.credentialsPath = String.format("%s./build_credentials", this.codePath);
        this.realCIRunPath = System.getProperty("user.dir");
        this.username = System.getProperty("user.name");
        this.buildParameters = new HashMap<>();

        this.projectFolder = (String) configuration.getParameter(PipelineConfiguration.PIPELINE_PROJECT_PATH);
        getProjectFolderFromConfiguration(configuration);
        configureProjectPath();
        setBuildParameters(configuration);
    }

    public void addObject(String propertyName, Object propertyValue) {
        List<Object> values = (List<Object>) buildParameters.get(propertyName);
        if (null == values) {
            values = new ArrayList<>();
        }
        values.add(propertyValue);
        buildParameters.put(propertyName, values);
    }

    public Object getObject(String propertyName) {
        List<Object> values = getObjects(propertyName);
        return values.get(0);
    }

    public List<Object> getObjects(String propertyName) {
        List<Object> values = (List<Object>) buildParameters.get(propertyName);
        if (null == values || values.isEmpty()) {
            throw new PropertyMissingException(propertyName);
        }
        return values;
    }

    /**
     * Fetch a list of environment properties
     * @param propertyName
     * @return list of String property values
     * @throws PropertyMissingException if the property does not exist
     */
    public List<String> getProperties(String propertyName) {
        List<String> values = (List<String>) buildParameters.get(propertyName);
        if (null == values || values.isEmpty()) {
            throw new PropertyMissingException(propertyName);
        }
        return values;
    }

    /**
     * Fetch a list of environment properties
     * @param propertyName
     * @return list of String property values
     * @throws PropertyMissingException if the property does not exist
     */
    public List<String> getProperties(String propertyName, List<String> defaultValues) {
        List<String> values = (List<String>) buildParameters.get(propertyName);
        if (null == values || values.isEmpty()) {
            return defaultValues;
        }
        return values;
    }

    /**
     * Fetch a single environment property
     * @param propertyName
     * @return a single String property value
     * @throws PropertyMissingException if the property does not exist
     */
    public String getProperty(String propertyName) {
        List<String> values = getProperties(propertyName);
        return values.get(0);
    }

    /**
     * Fetch a single environment property, specifying a default if the property does not exist
     * @param propertyName
     * @param defaultValue
     * @return The stored property or defaultValue if the property does not exist
     */
    public String getProperty(String propertyName, String defaultValue) {
        try {
            return getProperty(propertyName);
        } catch (PropertyMissingException e) {
            return defaultValue;
        }
    }

    /**
     * Add an environment property to this build environment
     * @param propertyName
     * @param propertyValue
     */
    public void addProperty(String propertyName, String propertyValue) {
        List<String> values = (List<String>) buildParameters.get(propertyName);
        if (null == values) {
            values = new ArrayList<>();
        }
        values.add(propertyValue);
        buildParameters.put(propertyName, values);
    }

    /**
     * Add a list of environment properties to this build environment
     * @param propertyName
     * @param propertyValues
     */
    public void addProperty(String propertyName, List<String> propertyValues) {
        List<String> values = (List<String>) buildParameters.get(propertyName);
        if (null == values) {
            values = new ArrayList<>();
        }
        values.addAll(propertyValues);
        buildParameters.put(propertyName, values);
    }

    /**
     * Add a boolean switch value to this build environment
     * @param switchName
     * @param switchValue
     */
    public void addSwitch(String switchName, Boolean switchValue) {
        List<Boolean> values = (List<Boolean>) buildParameters.get(switchName);
        if (null == values) {
            values = new ArrayList<>();
        }
        values.add(switchValue);
        buildParameters.put(switchName, values);
    }

    /**
     * Return a configured boolean value
     * @param switchName
     * @return boolean value of the switch
     */
    public boolean getSwitch(String switchName) {
        List<Boolean> values = (List<Boolean>) buildParameters.get(switchName);
        if (null == values) {
            throw new PropertyMissingException(switchName);
        } else {
            Boolean switchValue = values.get(0);
            if (null == switchValue) {
                throw new PropertyMissingException(switchName);
            }
            return switchValue.booleanValue();
        }
    }

    /**
     * Fetch a list of environment properties
     * @param propertyName
     * @return list of LinkedHashMap<String, String> property values
     * @throws PropertyMissingException if the property does not exist
     */
    public List<LinkedHashMap<String, String>> getListOfHashMaps(String propertyName) {
        List<LinkedHashMap<String, String>> values = (List<LinkedHashMap<String, String>>) buildParameters.get(propertyName);
        if (null == values || values.isEmpty()) {
            throw new PropertyMissingException(propertyName);
        }
        return values;
    }

    /**
     * This method copies the pipeline build parameters loaded from the yml file into the
     * buildParameters object. It needs to do type checking and store strings within List<String>
     * so that we can support both String and List<String> values.
     *
     * @param configuration
     */
    public void setBuildParameters(PipelineConfiguration configuration) {
        for (Map.Entry<String, Object> configParameter : configuration.getParameters()) {
            String propertyName = configParameter.getKey();
            Object objectValue = configParameter.getValue();
            if (objectValue instanceof String) {
                // Attempt to resolve an environment variable or capture the configured value
                String stringValue = (String) objectValue;
                String resolvedVariable = resolveEnvironmentVariable(stringValue, propertyName);
                addProperty(propertyName, resolvedVariable);
            } else if (objectValue instanceof List) {
                List<String> listValue = (List<String>) objectValue;
                addProperty(propertyName, listValue);
            } else if (objectValue instanceof Boolean) {
                Boolean booleanValue = (Boolean) objectValue;
                addSwitch(propertyName, booleanValue);
            } else {
                throw new PropertyTypeException(propertyName);
            }
        }
    }

    public void getProjectFolderFromConfiguration(PipelineConfiguration configuration) {
        if (configuration.hasParameter(PipelineConfiguration.PIPELINE_PROJECT_PATH)) {
            String configuredProjectFolder = (String) configuration.getParameter(PipelineConfiguration.PIPELINE_PROJECT_PATH);
            this.projectFolder = configuredProjectFolder;
        } else {
            this.projectFolder = "";
        }
    }

    public void configureProjectPath() {
        this.projectPath = String.format("/%s/%s", codePath, projectFolder);
    }

    @Override
    public String toString() {
        return "BuildEnvironment{" +
                "pipelineName='" + pipelineName + '\'' +
                ", buildUUID='" + buildUUID + '\'' +
                ", codePath='" + codePath + '\'' +
                ", projectFolder='" + projectFolder + '\'' +
                ", projectPath='" + projectPath + '\'' +
                ", scratchPath='" + scratchPath + '\'' +
                ", realCIRunPath='" + realCIRunPath + '\'' +
                ", username='" + username + '\'' +
                ", buildParameters=" + buildParameters +
                '}';
    }

    public String resolveEnvironmentVariable(String propertyValue, String propertyName) {
        if (isValueAVarible(propertyValue)) {
            try {
                String environmentVariableName = getNameFromEnvironmentVariable(propertyValue);
                String environmentValue = System.getenv(environmentVariableName);
                if (null != environmentValue) {
                    return environmentValue;
                } else {
                    LOGGER.error("I couldn't resolve the environment variable {} defined in the {} parameter", propertyValue, propertyName);
                }
            } catch (NullPointerException | SecurityException e) {
                LOGGER.error("An exception was thrown while trying to resolve the environment variable {} defined in the {} parameter", propertyValue, propertyName, e);
            }
        }
        return propertyValue;
    }

    /**
     * Check if a configured parameter value is an environment variable
     * in the format ${variableName}
     * @param propertyValue - a string value or environment variable placeholder
     * @return the environment variable value, or the configured value if the environment variable doesn't exist.
    */
    public boolean isValueAVarible(String propertyValue) {
        return propertyValue.matches("^\\$\\{[a-zA-Z]+}");
    }

    public String getNameFromEnvironmentVariable(String propertyValue) {
        String propertyName = propertyValue.replace("${", "");
        propertyName = propertyName.replace("}", "");
        return propertyName;
    }

    // Getters
    public String getPipelineName() {
        return pipelineName;
    }

    public String getBuildUUID() {
        return buildUUID;
    }

    public String getCodePath() {
        return codePath;
    }

    public String getProjectFolder() {
        return projectFolder;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public String getCredentialsPath() {
        return credentialsPath;
    }

    public String getScratchPath() {
        return scratchPath;
    }

    public String getRealCIRunPath() {
        return realCIRunPath;
    }

    // Setters
    public void setCodePath(String codePath) {
        this.codePath = codePath;
    }

    public void setProjectFolder(String projectFolder) {
        this.projectFolder = projectFolder;
    }

    public void setScratchPath(String scratchPath) {
        this.scratchPath = scratchPath;
    }

    public void setRealCIRunPath(String realCIRunPath) {
        this.realCIRunPath = realCIRunPath;
    }
}
