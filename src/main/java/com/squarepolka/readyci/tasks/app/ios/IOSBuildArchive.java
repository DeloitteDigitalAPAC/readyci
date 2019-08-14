package com.squarepolka.readyci.tasks.app.ios;

import com.squarepolka.readyci.taskrunner.BuildEnvironment;
import com.squarepolka.readyci.tasks.Task;
import com.squarepolka.readyci.tasks.app.ios.provisioningprofile.IOSProvisioningProfileRead;
import com.squarepolka.readyci.tasks.readyci.TaskExecuteException;

import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class IOSBuildArchive extends Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(IOSBuildArchive.class);
    public static final String TASK_IOS_ARCHIVE = "ios_archive";
    public static final String BUILD_PROP_IOS_SCHEME = "scheme";
    public static final String BUILD_PROP_FORCE_PROVISIONING_PROFILE = "forceProvisioningProfile";
    
    String workspace;
    String scheme;
    String configuration;
    String devTeam;
    String profileName;
    String archivePath;

    @Override
    public String taskIdentifier() {
        return TASK_IOS_ARCHIVE;
    }

    @Override
    public void performTask(BuildEnvironment buildEnvironment) {
        boolean forceProvisioningProfile = buildEnvironment.getSwitch(BUILD_PROP_FORCE_PROVISIONING_PROFILE, false);
        workspace = String.format("%s.xcworkspace", buildEnvironment.getProperty("workspace"));
        scheme = buildEnvironment.getProperty(BUILD_PROP_IOS_SCHEME);
        configuration = buildEnvironment.getProperty("configuration");
        devTeam = buildEnvironment.getProperty(IOSProvisioningProfileRead.BUILD_PROP_DEV_TEAM);
        profileName = buildEnvironment.getProperty(IOSProvisioningProfileRead.BUILD_PROP_PROFILE_NAME);
        archivePath = String.format("%s/app.xcarchive", buildEnvironment.getScratchPath());
        try {
            executeCommand(buildCommand(forceProvisioningProfile, true), buildEnvironment.getProjectPath());
        } catch (TaskExecuteException e) {
            LOGGER.debug("Failed using .xcworkspace file, trying .xcodeproj", BUILD_PROP_IOS_SCHEME);
            executeCommand(buildCommand(forceProvisioningProfile, false), buildEnvironment.getProjectPath());
        }
    }
    
    protected String[] buildCommand(boolean forceProvisioningProfile, boolean useWorkspace) {        
    	ArrayList<String> command = new ArrayList<String>(
    			Arrays.asList("/usr/bin/xcodebuild",
                "DEVELOPMENT_TEAM=" + devTeam,
                "-scheme", scheme,
                "-sdk", "iphoneos",
                "-configuration", configuration,
                "-archivePath", archivePath));
    	if (forceProvisioningProfile) {
    		command.add("PROVISIONING_PROFILE_SPECIFIER=" + profileName);
    	}
    	if (useWorkspace) {
    		command.add("-workspace");
    		command.add(workspace);
    	} else {
    		command.add("-project");
    		command.add(workspace.replace(".xcworkspace", ".xcodeproj"));
    	}
    	command.add("archive");
    	
    	return (String[]) command.toArray();
    }
}
