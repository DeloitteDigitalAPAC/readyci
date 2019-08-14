package com.squarepolka.readyci.tasks.app.ios;

import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListParser;
import com.squarepolka.readyci.taskrunner.BuildEnvironment;
import com.squarepolka.readyci.taskrunner.TaskFailedException;
import com.squarepolka.readyci.tasks.Task;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class IOSIncrementBuildNumber extends Task {

    public static final String TASK_IOS_INCREMENT_BUILD_NUMBER = "ios_increment_build_number";
    private static final String CFBUNDLEVERSION = "CFBundleVersion";
    private static final String BUILD_PROP_INC_BLD_PLIST_FILES = "iOSIncrementBuildNumbers";

    @Override
    public String taskIdentifier() {
        return TASK_IOS_INCREMENT_BUILD_NUMBER;
    }

    @Override
    public void performTask(BuildEnvironment buildEnvironment) throws TaskFailedException {
        String codePath = buildEnvironment.getCodePath();
        List<String> relativePListPaths = buildEnvironment.getProperties(BUILD_PROP_INC_BLD_PLIST_FILES);
        for (String relativepListPath : relativePListPaths) {
            incrementBuildNumberAtPath(relativepListPath, codePath);
        }
    }

    private void incrementBuildNumberAtPath(String relativepListPath, String codePath) throws TaskFailedException {
        try {
            String infoPlistPath = String.format("%s/%s", codePath, relativepListPath);
            NSDictionary infoDict = getInfoPlistDict(infoPlistPath);
            Integer buildNumber = getCurrentBuildNumber(infoDict) + 1;
            updateNewBuildNumber(infoDict, infoPlistPath, buildNumber);
        } catch (Exception e) {
            throw new TaskFailedException(e.getMessage());
        }
    }

    private NSDictionary getInfoPlistDict(String infoPlistPath) throws Exception {
        File infoPlistFile = new File(infoPlistPath);
        return (NSDictionary) PropertyListParser.parse(infoPlistFile);
    }

    private Integer getCurrentBuildNumber(NSDictionary infoDict) {
        String buildNumberStr = infoDict.objectForKey(CFBUNDLEVERSION).toString();
        Integer buildNumber = Integer.valueOf(buildNumberStr);
        return buildNumber;
    }

    private void updateNewBuildNumber(NSDictionary infoDict, String infoPlistPath, Integer buildNumber) throws IOException {
        File infoPlistFile = new File(infoPlistPath);
        infoDict.put(CFBUNDLEVERSION, buildNumber.toString());
        PropertyListParser.saveAsXML(infoDict, infoPlistFile);
    }
}
