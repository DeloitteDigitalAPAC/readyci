package com.squarepolka.readyci.tasks.app.android;

import com.squarepolka.readyci.configuration.PipelineConfiguration;
import com.squarepolka.readyci.configuration.ReadyCIConfiguration;
import com.squarepolka.readyci.taskrunner.BuildEnvironment;
import com.squarepolka.readyci.taskrunner.TaskFailedException;
import com.squarepolka.readyci.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FilenameFilter;


@Component
public class AndroidFetchSigningCredentials extends Task {

    public static final String TASK_FETCH_BUILD_DEPENDENCIES = "android_fetch_build_credentials";
    private static final Logger LOGGER = LoggerFactory.getLogger(ReadyCIConfiguration.class);

    @Override
    public String taskIdentifier() {
        return TASK_FETCH_BUILD_DEPENDENCIES;
    }

    @Override
    public void performTask(BuildEnvironment buildEnvironment) throws TaskFailedException {

        String credentialsRepository = buildEnvironment.getProperty("credentialsRepository", "");

        if(credentialsRepository.isEmpty()) {
            throw new TaskFailedException("credentialsRepository was not provided. We cannot make a build without these artifacts");
        }

        String credentialsTarLocation = String.format("%s/credentials.tar.gz", buildEnvironment.getProjectPath());

        // download an archive of the repository under master
        executeCommand(new String[]{"/usr/bin/git", "archive", "--remote", credentialsRepository, "master", "-o", credentialsTarLocation});

        try {
            // copy the properties + jks files into the project path
            executeCommand(new String[]{"tar", "-xvf", credentialsTarLocation, "-C", buildEnvironment.getProjectPath(), "*.properties", "*.jks"});
        }
        catch (Exception e) {
            // do nothing. its not required if we are using the properties and jks files included in the project
        }

        // copy the rest of the credentials to a special folder
        // TODO - exclude the non properties and jks files
        executeCommand(new String[] {"/bin/mkdir", buildEnvironment.getCredentialsPath()});
        executeCommand(new String[]{"tar", "-xvf", credentialsTarLocation, "-C", buildEnvironment.getCredentialsPath()});
        executeCommand(new String[] {"/bin/rm", credentialsTarLocation});


        // read in the .build_credentials/*.yml files
        File[] files = new File(buildEnvironment.getCredentialsPath()).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".yml");
            }
        });

        if(files != null && files.length > 0) {
            for (File file : files) { // load and add to the configuration
                ReadyCIConfiguration credentialConfiguration = ReadyCIConfiguration.readConfigurationFile(file.getAbsolutePath());
                PipelineConfiguration repoPipelineConf = credentialConfiguration.getPipeline(buildEnvironment.getPipelineName());
                buildEnvironment.setBuildParameters(repoPipelineConf);
            }
        }
        else {
            throw new TaskFailedException("There were no yml files found in the provided credentials repository");
        }
    }
}
