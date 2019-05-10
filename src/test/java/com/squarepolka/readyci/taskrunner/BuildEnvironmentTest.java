package com.squarepolka.readyci.taskrunner;

import com.squarepolka.readyci.configuration.PipelineConfiguration;
import com.squarepolka.readyci.util.PropertyMissingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class BuildEnvironmentTest {

    private static final String TEST_KEY = "testKey";
    private static final String TEST_KEY_STRING = "testKeyString";
    private static final String TEST_KEY_STRING_LIST = "testKeyStringList";
    private static final String TEST_KEY_BOOLEAN = "testKeyBoolean";
    private static final String TEST_VALUE_STRING = "testValue";
    private static final Boolean TEST_VALUE_BOOLEAN_TRUE = true;
    private static final Boolean TEST_VALUE_BOOLEAN_FALSE = false;
    private static final String TEST_VALUE_DEFAULT = "defaultValue";

    @Mock
    private PipelineConfiguration pipelineConfiguration;

    private BuildEnvironment subject;

    @Before
    public void setUp() {
        subject = new BuildEnvironment(pipelineConfiguration);
    }

    @Test
    public void addStringObject() {
        subject.addObject(TEST_KEY, TEST_VALUE_STRING);
        String value = (String) subject.getObject(TEST_KEY);
        assertEquals("buildParameters contains the string test value", TEST_VALUE_STRING, value);
    }

    @Test
    public void addBoolTrueObject() {
        subject.addObject(TEST_KEY, TEST_VALUE_BOOLEAN_FALSE);
        Boolean value = (Boolean) subject.getObject(TEST_KEY);
        assertEquals("buildParameters contains the true test value", TEST_VALUE_BOOLEAN_FALSE, value);
    }

    @Test
    public void addBoolFalseObject() {
        subject.addObject(TEST_KEY, TEST_VALUE_BOOLEAN_TRUE);
        Boolean value = (Boolean) subject.getObject(TEST_KEY);
        assertEquals("buildParameters contains the false test value", TEST_VALUE_BOOLEAN_TRUE, value);
    }

    @Test(expected = PropertyMissingException.class)
    public void getObjectEmpty() {
        List<String> returnedList = (List<String>) subject.getObject(TEST_KEY);
    }

    @Test
    public void getObjectPopulated() {
        subject.addObject(TEST_KEY, TEST_VALUE_STRING);
        String returnedValue = (String) subject.getObject(TEST_KEY);
        assertEquals("The returned value is populated when a single value is requested", TEST_VALUE_STRING, returnedValue);
    }

    @Test
    public void getObjectPopulatedWithSingleBoolean() {
        subject.addObject(TEST_KEY, TEST_VALUE_STRING);
        String returnedValue = (String) subject.getObject(TEST_KEY);
        assertEquals("The returned value is populated when a single value is requested", TEST_VALUE_STRING, returnedValue);
    }

    @Test(expected = PropertyMissingException.class)
    public void getObjectsEmpty() {
        subject.getObjects(TEST_KEY);
    }

    @Test
    public void getObjectsPopulated() {
        subject.addObject(TEST_KEY, TEST_VALUE_STRING);
        List<Object> returnedList = (List<Object>) subject.getObjects(TEST_KEY);
        String returnedValue = (String) returnedList.get(0);
        assertEquals("The returned list is populated when a list of values is requested", TEST_VALUE_STRING, returnedValue);
    }

    @Test(expected = PropertyMissingException.class)
    public void getPropertiesEmpty() {
        subject.getProperties(TEST_KEY);
    }

    @Test
    public void getProperties() {
        subject.addProperty(TEST_KEY, TEST_VALUE_STRING);
        List<String> returnedList = subject.getProperties(TEST_KEY);
        String returnedValue = returnedList.get(0);
        assertEquals("The returned list is populated when a list of string properties is requested", TEST_VALUE_STRING, returnedValue);
    }

    @Test(expected = PropertyMissingException.class)
    public void getPropertyEmpty() {
        subject.getProperty(TEST_KEY);
    }

    @Test
    public void getPropertyPopulated() {
        subject.addProperty(TEST_KEY, TEST_VALUE_STRING);
        String returnedValue = (String) subject.getProperty(TEST_KEY);
        assertEquals("The returned value is populated when a single string is requested", TEST_VALUE_STRING, returnedValue);
    }

    @Test
    public void getPropertyWithDefaultValueEmpty() {
        String returnedValue = subject.getProperty(TEST_KEY, TEST_VALUE_DEFAULT);
        assertEquals("The returned value is set to the default", TEST_VALUE_DEFAULT, returnedValue);
    }

    @Test
    public void getPropertyWithDefaultValuePopulated() {
        subject.addProperty(TEST_KEY, TEST_VALUE_STRING);
        String returnedValue = subject.getProperty(TEST_KEY, TEST_VALUE_DEFAULT);
        assertEquals("The returned value is set to the stored value", TEST_VALUE_STRING, returnedValue);
    }

    @Test
    public void addProperty() {
        subject.addProperty(TEST_KEY, TEST_VALUE_STRING);
        String returnedValue = subject.getProperty(TEST_KEY);
        assertEquals("The returned value is set to the stored value", TEST_VALUE_STRING, returnedValue);
    }

    @Test
    public void addPropertyList() {
        List<String> testData = new ArrayList<String>();
        testData.add(TEST_VALUE_STRING);
        subject.addProperty(TEST_KEY, testData);
        List<String> returnedList = subject.getProperties(TEST_KEY);
        String returnedValue = returnedList.get(0);
        assertEquals("The returned list is populated", TEST_VALUE_STRING, returnedValue);
    }

    @Test
    public void testSetBuildParameters() {
        List<String> testStringList = new LinkedList<>();
        testStringList.add("string1");
        testStringList.add("string2");
        testStringList.add("string3");

        Map.Entry<String, Object> stringEntry = new AbstractMap.SimpleEntry<String, Object>(TEST_KEY_STRING, TEST_VALUE_STRING);
        Map.Entry<String, Object> stringListEntry = new AbstractMap.SimpleEntry<String, Object>(TEST_KEY_STRING_LIST, testStringList);
        Map.Entry<String, Object> booleanEntry = new AbstractMap.SimpleEntry<String, Object>(TEST_KEY_BOOLEAN, TEST_VALUE_BOOLEAN_TRUE);

        Set<Map.Entry<String, Object>> set = new HashSet<>();
        set.add(stringEntry);
        set.add(stringListEntry);
        set.add(booleanEntry);
        Mockito.when(pipelineConfiguration.getParameters()).thenReturn(set);

        subject.setBuildParameters(pipelineConfiguration);
        String returnedString = subject.getProperty(TEST_KEY_STRING);
        List<String> returnedStringList = subject.getProperties(TEST_KEY_STRING_LIST);
        Boolean returnedBoolean = subject.getSwitch(TEST_KEY_BOOLEAN);

        assertEquals("Correct String value has been added and is returned", TEST_VALUE_STRING, returnedString);
        assertEquals("Correct String list value has been added and is returned", testStringList, returnedStringList);
        assertEquals("Correct Boolean value has been added and is returned", TEST_VALUE_BOOLEAN_TRUE, returnedBoolean);
    }

    @Test
    public void getProjectFolderFromConfigurationSpecified() {
        Mockito.when(pipelineConfiguration.hasParameter(PipelineConfiguration.PIPELINE_PROJECT_PATH)).thenReturn(true);
        Mockito.when(pipelineConfiguration.getParameter(PipelineConfiguration.PIPELINE_PROJECT_PATH)).thenReturn("testPath");
        subject.getProjectFolderFromConfiguration(pipelineConfiguration);
        assertEquals("The projectFolder should be set", "testPath", subject.getProjectFolder());
    }

    @Test
    public void getProjectFolderFromConfigurationNotSpecified() {
        subject.getProjectFolderFromConfiguration(pipelineConfiguration);
        assertEquals("The projectFolder should be empty", "", subject.getProjectFolder());
    }


    @Test
    public void configureProjectPath() {
        subject.setCodePath("codepath");
        subject.setProjectFolder("projectfolder");
        subject.configureProjectPath();
        assertEquals("The projectPath is configured correctly", "/codepath/projectfolder", subject.getProjectPath());
    }

    @Test
    public void resolveEnvironmentVariableWithMissingEnvironmentVariable() {
        String result = subject.resolveEnvironmentVariable("envName", "propertyName");
        assertEquals("The provided value is returned", "envName", result);
    }

    @Test
    public void resolveEnvironmentVariableWithPresentEnvironmentVariable() {
        String result = subject.resolveEnvironmentVariable("${SHELL}", "propertyName");
        assertEquals("The configured environment variable value is returned", "/bin/bash", result);
    }

    @Test
    public void isValueAVaribleFalse() {
        boolean result = subject.isValueAVarible("notAVar");
        assertEquals("The parameter is classed as not a variable", false, result);
    }

    @Test
    public void isValueAVaribleTrue() {
        boolean result = subject.isValueAVarible("${isAVar}");
        assertEquals("The parameter is classed as a variable", true, result);
    }

    @Test
    public void getNameFromEnvironmentVariable() {
        String result = subject.getNameFromEnvironmentVariable("${variableName}");
        assertEquals("The correct variable name is parsed", "variableName", result);
    }

}
