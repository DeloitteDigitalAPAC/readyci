package com.squarepolka.readyci.tasks.app.android

import com.squarepolka.readyci.configuration.AndroidPropConstants
import com.squarepolka.readyci.configuration.ReadyCIConfiguration
import com.squarepolka.readyci.taskrunner.BuildEnvironment
import com.squarepolka.readyci.tasks.Task
import com.squarepolka.readyci.util.Util
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

import java.io.File

@Component
class AndroidUploadHockeyapp : Task() {
    companion object {
        private const val TASK_UPLOAD_HOCKEYAPP = "android_upload_hockeyapp"
        private val LOGGER = LoggerFactory.getLogger(ReadyCIConfiguration::class.java)
    }

    override fun taskIdentifier(): String = TASK_UPLOAD_HOCKEYAPP

    override fun performTask(buildEnvironment: BuildEnvironment) {
        val hockappToken = buildEnvironment.getProperty(AndroidPropConstants.BUILD_PROP_HOCKEYAPP_TOKEN)
        val releaseTags = buildEnvironment.getProperty(AndroidPropConstants.BUILD_PROP_HOCKEYAPP_RELEASE_TAGS, "")
        val releaseNotes = buildEnvironment.getProperty(AndroidPropConstants.BUILD_PROP_HOCKEYAPP_RELEASE_NOTES, "")

        // upload all the apk builds that it finds
        val files = Util.findAllByExtension(File(buildEnvironment.projectPath), ".apk")
        for (apk in files) {
            val absolutePath = apk.absolutePath

            // Filtering out known bad APKs
            if (!absolutePath.contains("build/outputs") ||
                    absolutePath.endsWith("zipaligned.apk") ||
                    absolutePath.endsWith("signed.apk") ||
                    absolutePath.endsWith("uninstrumented.apk")) {
                continue
            }

            LOGGER.warn("uploading $absolutePath")

            // Upload to HockeyApp
            executeCommand(arrayOf("/usr/bin/curl", "https://rink.hockeyapp.net/api/2/apps/upload", "-H", "X-HockeyAppToken: $hockappToken", "-F", "ipa=@$absolutePath", "-F", "notes=$releaseNotes", "-F", "tags=$releaseTags", "-F", "notes_type=0", // Textual release notes
                    "-F", "status=2", // Make this version available for download
                    "-F", "notify=1", // Notify users who can install the app
                    "-F", "strategy=add", // Add the build if one with the same build number exists
                    "-F", "mandatory=1"                 // Download is mandatory
            ), buildEnvironment.projectPath)
        }
    }
}