package com.squarepolka.readyci.tasks.app.ios;

import com.squarepolka.readyci.taskrunner.BuildEnvironment;
import com.squarepolka.readyci.taskrunner.TaskFailedException;
import com.squarepolka.readyci.tasks.Task;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class IOSUploadAppleConnect extends Task {

    public static final String BUILD_PROP_APPLE_CONNECT_PASSWORD = "appleConnectPassword";
	public static final String BUILD_PROP_APPLE_CONNECT_USERNAME = "appleConnectUsername";
	public static final String TASK_UPLOAD_APPLE_CONNECT = "ios_upload_apple_connect";
    private static final String COMMAND_XCODE_SELECT = "xcode-select";
    private static final String PARAM_XCODE_SELECT_PATH = "--print-path";
    private static final String PATH_XCODE_DEFAULT = "/Applications/Xcode.app/Contents";

    @Override
    public String taskIdentifier() {
        return TASK_UPLOAD_APPLE_CONNECT;
    }

    @Override
    public void performTask(BuildEnvironment buildEnvironment) throws TaskFailedException {

        String scheme = buildEnvironment.getProperty(IOSBuildArchive.BUILD_PROP_IOS_SCHEME);
        String exportPath = String.format("%s/%s.ipa", buildEnvironment.getScratchPath(), scheme);
        String appleUsername = buildEnvironment.getProperty(BUILD_PROP_APPLE_CONNECT_USERNAME);
        String applePassword = buildEnvironment.getProperty(BUILD_PROP_APPLE_CONNECT_PASSWORD);
        String xCodePath = getXCodeSelectPath(buildEnvironment);
        String altoolPath = xCodePath + "/Applications/Application Loader.app/Contents/Frameworks/ITunesSoftwareService.framework/Support/altool";

        executeCommand(new String[] {altoolPath,
        "--upload-app",
        "-f", exportPath,
        "-u", appleUsername,
        "-p", applePassword});
    }

    protected String getXCodeSelectPath(BuildEnvironment buildEnvironment) {
        InputStream inputStream = executeCommand(new String[]{COMMAND_XCODE_SELECT, PARAM_XCODE_SELECT_PATH});
        try (java.util.Scanner scanner = new java.util.Scanner(inputStream).useDelimiter("\\A")) {
        	String xCodeSelectPath = scanner.hasNext() ? scanner.next() : PATH_XCODE_DEFAULT;
            xCodeSelectPath = xCodeSelectPath.replace("\n", "");
            xCodeSelectPath = xCodeSelectPath.replace("\r", "");
            xCodeSelectPath = xCodeSelectPath.replace("/Developer", "");
            
            return xCodeSelectPath;
        }
    }
}
