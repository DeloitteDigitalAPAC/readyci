package com.squarepolka.readyci.tasks.app.ios;

import com.squarepolka.readyci.configuration.ReadyCIConfiguration;
import com.squarepolka.readyci.taskrunner.BuildEnvironment;
import com.squarepolka.readyci.tasks.Task;
import com.squarepolka.readyci.util.appcenter.ACReleaseResponse;
import com.squarepolka.readyci.util.appcenter.ACUploadConfiguration;
import com.squarepolka.readyci.util.appcenter.ACUploadConfirmationResponse;
import com.squarepolka.readyci.util.appcenter.AppCenterHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class IOSUploadAppCenter extends Task {

    //https://docs.microsoft.com/en-us/appcenter/distribution/uploading

    public static final String TASK_IOS_UPLOAD_APPCENTER = "ios_upload_appcenter";

    public static final String BUILD_PROP_APPCENTER_TOKEN = "appcenterToken";
    public static final String BUILD_PROP_APPCENTER_OWNER = "appcenterOwner";
    public static final String BUILD_PROP_APPCENTER_APPNAME = "appcenterAppName";
    public static final String BUILD_PROP_APPCENTER_RELEASE_TAGS = "appcenterReleaseTags";
    public static final String BUILD_PROP_APPCENTER_RELEASE_NOTES = "appcenterReleaseNotes";

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadyCIConfiguration.class);

    @Override
    public String taskIdentifier() {
        return TASK_IOS_UPLOAD_APPCENTER;
    }

    @Override
    public void performTask(BuildEnvironment buildEnvironment) {

        String token = buildEnvironment.getProperty(BUILD_PROP_APPCENTER_TOKEN);
        String releaseTags = buildEnvironment.getProperty(BUILD_PROP_APPCENTER_RELEASE_TAGS, "");
        String appOwner = buildEnvironment.getProperty(BUILD_PROP_APPCENTER_OWNER, "");
        String appName = buildEnvironment.getProperty(BUILD_PROP_APPCENTER_APPNAME, "");
        String releaseNotes = buildEnvironment.getProperty(BUILD_PROP_APPCENTER_RELEASE_NOTES, "");

        String scheme = buildEnvironment.getProperty(IOSBuildArchive.BUILD_PROP_IOS_SCHEME);
        String appBinaryPath = String.format("%s/%s.ipa", buildEnvironment.getScratchPath(), scheme);
        String dsymPath = String.format("%s/app.xcarchive/dSYMs", buildEnvironment.getScratchPath());
        String dsymPathZip = String.format("%s/app.xcarchive/dSYMs/dsym.zip", buildEnvironment.getScratchPath());

        // Zip the dSYM bundle
        executeCommand(new String[] {"zip",
                "-r",
                dsymPathZip,
                "."}, dsymPath);

        AppCenterHelper helper = new AppCenterHelper(token, appOwner, appName);

        ACUploadConfiguration configuration = helper.getUploadConfiguration();
        if(configuration != null) {
            executeCommand(new String[]{"/usr/bin/curl",
                    configuration.getUploadUrl(),
                    "-F", "ipa=@" + appBinaryPath});

            ACUploadConfirmationResponse result = helper.markAsComplete(configuration.getUploadId());

            if(result != null) {
                ACReleaseResponse response = helper.releaseBuild(result.getReleaseID(), releaseNotes, releaseTags);
                LOGGER.info("Build #"+result.getReleaseID()+" released to "+ releaseTags);
            }
        }
    }
}