package com.squarepolka.readyci.tasks.app.ios;

import org.springframework.stereotype.Component;

import com.squarepolka.readyci.taskrunner.BuildEnvironment;
import com.squarepolka.readyci.taskrunner.TaskFailedException;

@Component
public class IOSUploadItunesConnect extends IOSUploadAppleConnect {
	private static final String BUILD_PROP_ITUNES_PASSWORD = "iTunesPassword";
	private static final String BUILD_PROP_ITUNES_USERNAME = "iTunesUsername";
	public static final String TASK_UPLOAD_ITUNES_CONNECT = "ios_upload_itunes_connect";

    @Override
    public String taskIdentifier() {
        return TASK_UPLOAD_ITUNES_CONNECT;
    }

    @Override
    public void performTask(BuildEnvironment buildEnvironment) throws TaskFailedException {
    	if (!buildEnvironment.propertyExists(IOSUploadAppleConnect.BUILD_PROP_APPLE_CONNECT_USERNAME) &&
    			!buildEnvironment.propertyExists(IOSUploadAppleConnect.BUILD_PROP_APPLE_CONNECT_PASSWORD)) {
    		String iTunesUsername = buildEnvironment.getProperty(BUILD_PROP_ITUNES_USERNAME);
            String iTunesPassword = buildEnvironment.getProperty(BUILD_PROP_ITUNES_PASSWORD);
            buildEnvironment.addProperty(IOSUploadAppleConnect.BUILD_PROP_APPLE_CONNECT_USERNAME, iTunesUsername);
            buildEnvironment.addProperty(IOSUploadAppleConnect.BUILD_PROP_APPLE_CONNECT_PASSWORD, iTunesPassword);
    	}
        super.performTask(buildEnvironment);
    }
}
