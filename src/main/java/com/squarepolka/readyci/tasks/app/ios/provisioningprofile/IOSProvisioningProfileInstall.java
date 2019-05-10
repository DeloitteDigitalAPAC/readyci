package com.squarepolka.readyci.tasks.app.ios.provisioningprofile;

import com.squarepolka.readyci.taskrunner.BuildEnvironment;
import com.squarepolka.readyci.tasks.Task;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

@Component
public class IOSProvisioningProfileInstall extends Task {

    public static final String TASK_IOS_INSTALL_PROFILE = "ios_install_provisioning_profile";

    @Override
    public String taskIdentifier() {
        return TASK_IOS_INSTALL_PROFILE;
    }

    @Override
    public void performTask(BuildEnvironment buildEnvironment) {
        List<LinkedHashMap<String, String>> relativeProfilePaths = buildEnvironment.getListOfHashMaps(IOSProvisioningProfileRead.BUILD_PROP_PROFILE_PATHS);
        for (LinkedHashMap<String, String> relativeProfilePath : relativeProfilePaths) {
            installProfile(relativeProfilePath, buildEnvironment.getProjectPath());
        }
    }

    public void installProfile(LinkedHashMap<String, String> relativeProfilePath, String projectPath) {
        String profilePath = String.format("%s/%s", projectPath, relativeProfilePath.get("profilePath"));
        executeCommand(new String[] {"/usr/bin/open", profilePath});
    }
}
