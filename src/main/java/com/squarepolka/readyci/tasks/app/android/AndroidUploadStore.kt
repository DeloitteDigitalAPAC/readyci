package com.squarepolka.readyci.tasks.app.android

import com.google.api.client.http.FileContent
import com.google.api.services.androidpublisher.model.*
import com.squarepolka.readyci.configuration.AndroidPropConstants
import com.squarepolka.readyci.configuration.ReadyCIConfiguration
import com.squarepolka.readyci.taskrunner.BuildEnvironment
import com.squarepolka.readyci.taskrunner.TaskFailedException
import com.squarepolka.readyci.tasks.Task
import com.squarepolka.readyci.util.android.AndroidPublisherHelper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import net.dongliu.apk.parser.ApkFile
import java.util.ArrayList
import com.squarepolka.readyci.configuration.AndroidPropConstants.*
import com.squarepolka.readyci.tasks.app.android.extensions.isDebuggable
import com.squarepolka.readyci.tasks.app.android.extensions.isSigned

@Component
class AndroidUploadStore : Task() {
    companion object {
        private const val TASK_UPLOAD_STORE = "android_upload_play_store"
        private val LOGGER = LoggerFactory.getLogger(ReadyCIConfiguration::class.java)
    }

    override fun taskIdentifier(): String = TASK_UPLOAD_STORE

    @Throws(TaskFailedException::class)
    override fun performTask(buildEnvironment: BuildEnvironment) {
        try {
            val deployTrack = buildEnvironment.getProperty(BUILD_PROP_DEPLOY_TRACK, "")
            val playStoreEmail = buildEnvironment.getProperty(BUILD_PROP_SERVICE_ACCOUNT_EMAIL, "")
            val playStoreCert = buildEnvironment.getProperty(BUILD_PROP_SERVICE_ACCOUNT_FILE, "")
            val filenameFilters = buildEnvironment.getProperties(AndroidPropConstants.BUILD_PROP_FILENAME_FILTERS,
                    listOf("zipaligned", "unsigned"))

            if ((deployTrack.isEmpty() || playStoreEmail.isEmpty() || playStoreCert.isEmpty())) {
                val sb = StringBuilder()
                sb.append("AndroidUploadStore: Missing vital details for play store deployment:")
                if (deployTrack.isEmpty())
                    sb.append("\n- deployTrack is required")
                if (playStoreEmail.isEmpty())
                    sb.append("\n- playStoreEmail is required")
                if (playStoreCert.isEmpty())
                    sb.append("\n- playStoreCert is required")
                throw Exception(sb.toString())
            }

            val playStoreCertLocation = String.format("%s/%s", buildEnvironment.credentialsPath, playStoreCert)

            val filteredApks = AndroidUtil.findAllApkOutputs(buildEnvironment.projectPath)
                    .filter { file ->
                        filenameFilters.none { file.nameWithoutExtension.contains(it) }
                    }
                    .map { Pair(it, ApkFile(it)) }
                    .filter { it.second.isSigned }
                    .filter { !it.second.isDebuggable }

            when(filteredApks.size) {
                0 -> throw RuntimeException("Could not find the signed APK")
                1 -> {} // do nothing
                else -> throw RuntimeException("There are too many valid APKs that we can upload, please provide a more specific scheme for this pipeline ")
            }

            val rawFile = filteredApks.first().first
            val apkMetadata = filteredApks.first().second.apkMeta

            // Create the API service.
            val service = AndroidPublisherHelper.init(apkMetadata.packageName, playStoreEmail, playStoreCertLocation)
            val edits = service.edits()

            // Create a new edit to make changes to your listing.
            val editRequest = edits.insert(apkMetadata.packageName, null)
            val edit = editRequest.execute()
            LOGGER.info("AndroidUploadStore: Created edit with id: {}", edit.id)

            val apkFile = FileContent(AndroidPublisherHelper.MIME_TYPE_APK, rawFile)
            val uploadRequest = edits
                    .apks()
                    .upload(apkMetadata.packageName, edit.id, apkFile)
            val apk = uploadRequest.execute()
            LOGGER.info("AndroidUploadStore: Version code {} has been uploaded", apk.versionCode)

            // Assign apk to alpha track.
            val apkVersionCodes = ArrayList<Long>()
            apkVersionCodes.add(apk.versionCode.toLong())
            val updateTrackRequest = edits
                    .tracks()
                    .update(apkMetadata.packageName,
                            edit.id,
                            deployTrack,
                            Track().setReleases(
                                    listOf(TrackRelease()
                                            .setName(apkMetadata.versionName)
                                            .setVersionCodes(apkVersionCodes)
                                            .setStatus("completed"))))
            val updatedTrack = updateTrackRequest.execute()
            LOGGER.info("AndroidUploadStore: Track {} has been updated.", updatedTrack.track)

            // Commit changes for edit.
            val commitRequest = edits.commit(apkMetadata.packageName, edit.id)
            val appEdit = commitRequest.execute()
            LOGGER.info("AndroidUploadStore: App edit with id {} has been committed", appEdit.id)
        } catch (ex: Exception) {
            LOGGER.error("AndroidUploadStore: Exception was thrown while uploading apk to alpha track", ex)
        }
    }
}