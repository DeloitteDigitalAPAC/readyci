package com.squarepolka.readyci.tasks.app.android;

import com.squarepolka.readyci.configuration.ReadyCIConfiguration;
import com.squarepolka.readyci.taskrunner.BuildEnvironment;
import com.squarepolka.readyci.tasks.Task;
import com.squarepolka.readyci.util.Util;
import com.squarepolka.readyci.util.appcenter.ACReleaseResponse;
import com.squarepolka.readyci.util.appcenter.ACUploadConfiguration;
import com.squarepolka.readyci.util.appcenter.ACUploadConfirmationResponse;
import com.squarepolka.readyci.util.appcenter.AppCenterHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

@Component
public class AndroidUploadAppCenter extends Task {

    //https://docs.microsoft.com/en-us/appcenter/distribution/uploading

    public static final String TASK_ANDROID_UPLOAD_APPCENTER = "android_upload_appcenter";
    public static final String BUILD_PROP_APPCENTER_TOKEN = "appcenterToken";
    public static final String BUILD_PROP_APPCENTER_OWNER = "appcenterOwner";
    public static final String BUILD_PROP_APPCENTER_APPNAME = "appcenterAppName";
    public static final String BUILD_PROP_APPCENTER_RELEASE_TAGS = "appcenterReleaseTags";
    public static final String BUILD_PROP_APPCENTER_RELEASE_NOTES = "appcenterReleaseNotes";

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadyCIConfiguration.class);

    @Override
    public String taskIdentifier() {
        return TASK_ANDROID_UPLOAD_APPCENTER;
    }

    @Override
    public void performTask(BuildEnvironment buildEnvironment) {

        String token = buildEnvironment.getProperty(BUILD_PROP_APPCENTER_TOKEN);
        String releaseTags = buildEnvironment.getProperty(BUILD_PROP_APPCENTER_RELEASE_TAGS, "");
        String appOwner = buildEnvironment.getProperty(BUILD_PROP_APPCENTER_OWNER, "");
        String appName = buildEnvironment.getProperty(BUILD_PROP_APPCENTER_APPNAME, "");
        String releaseNotes = buildEnvironment.getProperty(BUILD_PROP_APPCENTER_RELEASE_NOTES, "");

        AppCenterHelper helper = new AppCenterHelper(token, appOwner, appName);

        // upload all the apk builds that it finds
        Collection<File> files = Util.findAllByExtension(new File(buildEnvironment.getProjectPath()), ".apk");

        for (File apk : files) {
            if(apk.getAbsolutePath().contains("build")) {
                LOGGER.warn("uploading "+ apk.getAbsolutePath());
                ACUploadConfiguration configuration = helper.getUploadConfiguration();
                if(configuration != null) {
                    executeCommand(new String[]{"/usr/bin/curl",
                            configuration.getUploadUrl(),
                            "-F", "ipa=@" + apk.getAbsolutePath()});

                    ACUploadConfirmationResponse result = helper.markAsComplete(configuration.getUploadId());

                    if(result != null) {
                        ACReleaseResponse response = helper.releaseBuild(result.getReleaseID(), releaseNotes, releaseTags);
                        LOGGER.info("Build #"+result.getReleaseID()+" released to "+ releaseTags);
                    }
                }
                return;
            }
        }
    }
}