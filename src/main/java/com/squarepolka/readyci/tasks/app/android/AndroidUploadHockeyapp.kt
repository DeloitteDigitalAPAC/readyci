package com.squarepolka.readyci.tasks.app.android

import com.squarepolka.readyci.configuration.AndroidPropConstants
import com.squarepolka.readyci.configuration.ReadyCIConfiguration
import com.squarepolka.readyci.taskrunner.BuildEnvironment
import com.squarepolka.readyci.tasks.Task
import com.squarepolka.readyci.tasks.app.android.extensions.isDebuggable
import com.squarepolka.readyci.tasks.app.android.extensions.isSigned
import com.squarepolka.readyci.util.Util
import net.dongliu.apk.parser.ApkFile
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

import java.io.File

@Component
class AndroidUploadHockeyapp : Task() {

    companion object {
        private const val TASK_UPLOAD_HOCKEYAPP = "android_upload_hockeyapp"
    }

    override fun taskIdentifier(): String = TASK_UPLOAD_HOCKEYAPP

    override fun performTask(buildEnvironment: BuildEnvironment) {
        val hockappToken = buildEnvironment.getProperty(AndroidPropConstants.BUILD_PROP_HOCKEYAPP_TOKEN)
        val releaseTags = buildEnvironment.getProperty(AndroidPropConstants.BUILD_PROP_HOCKEYAPP_RELEASE_TAGS, "")
        val releaseNotes = buildEnvironment.getProperty(AndroidPropConstants.BUILD_PROP_HOCKEYAPP_RELEASE_NOTES, "")
        val filenameFilters = buildEnvironment.getProperties(AndroidPropConstants.BUILD_PROP_FILENAME_FILTERS,
                listOf("zipaligned", "unsigned"))

        if(hockappToken == null) {
            throw RuntimeException("AndroidUploadStore: Missing vital details for play store deployment:\n- hockappToken is required")
        }

        val filteredApks = AndroidUtil.findAllApkOutputs(buildEnvironment.projectPath)
                .filter { file ->
                    filenameFilters.none { file.nameWithoutExtension.contains(it) }
                }
                .map { Pair(it, ApkFile(it)) }
                .filter { it.second.isSigned }

        when(filteredApks.size) {
            0 -> throw RuntimeException("Could not find the signed APK")
            1 -> {} // do nothing
            else -> throw RuntimeException("There are too many valid APKs that we can upload, please provide a more specific scheme for this pipeline ")
        }

        val rawFile = filteredApks.first().first

        executeCommand(arrayOf("/usr/bin/curl", "https://rink.hockeyapp.net/api/2/apps/upload",
                "-H", "X-HockeyAppToken: $hockappToken",
                "-F", "ipa=@${rawFile.absolutePath}",
                "-F", "notes=$releaseNotes",
                "-F", "tags=$releaseTags",
                "-F", "notes_type=0", // Textual release notes
                "-F", "status=2", // Make this version available for download
                "-F", "notify=1", // Notify users who can install the app
                "-F", "strategy=add", // Add the build if one with the same build number exists
                "-F", "mandatory=1" // Download is mandatory
        ), buildEnvironment.projectPath)
    }
}