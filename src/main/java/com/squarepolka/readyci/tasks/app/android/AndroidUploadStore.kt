package com.squarepolka.readyci.tasks.app.android

import com.google.api.client.http.FileContent
import com.google.api.services.androidpublisher.model.*
import com.squarepolka.readyci.configuration.ReadyCIConfiguration
import com.squarepolka.readyci.taskrunner.BuildEnvironment
import com.squarepolka.readyci.taskrunner.TaskFailedException
import com.squarepolka.readyci.tasks.Task
import com.squarepolka.readyci.util.android.AndroidPublisherHelper
import com.squarepolka.readyci.util.Util
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.ApkSignStatus
import java.io.File
import java.util.ArrayList
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.xml.sax.InputSource
import com.squarepolka.readyci.configuration.AndroidPropConstants.*

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

            val filteredApks = Util.findAllByExtension(File(buildEnvironment.projectPath), ".apk")
                    .filter {
                        val absolutePath = it.absolutePath

                        absolutePath.contains("build/outputs") &&
                                !absolutePath.endsWith("aligned.apk") &&
                                !absolutePath.endsWith("signed.apk") &&
                                !absolutePath.endsWith("uninstrumented.apk")
                    }
                    .filter {
                        val apk = ApkFile(it)
                        val signStatus = apk.verifyApk()
                        signStatus == ApkSignStatus.signed
                    }
                    .filter {
                        val apk = ApkFile(it)

                        val inputSource = InputSource(StringReader(apk.manifestXml))
                        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSource)
                        val debuggable = (doc.getElementsByTagName("application").item(0) as Element).getAttribute("android:debuggable")

                        debuggable == null || !debuggable.toBoolean()
                    }

            if (filteredApks.size != 1) {
                throw RuntimeException("Either there's no valid APK, or too many valid APKs")
            }

            val rawFile = filteredApks[0]
            val apkMetadata = ApkFile(rawFile).apkMeta

            // Create the API service.
            val service = AndroidPublisherHelper.init(apkMetadata.packageName, playStoreEmail, playStoreCertLocation)
            val edits = service.edits()

            // Create a new edit to make changes to your listing.
            val editRequest = edits.insert(apkMetadata.packageName, null)
            val edit = editRequest.execute()
            val editId = edit.id
            LOGGER.info("AndroidUploadStore: Created edit with id: {}", editId)
            val apkFile = FileContent(AndroidPublisherHelper.MIME_TYPE_APK, rawFile)
            val uploadRequest = edits
                    .apks()
                    .upload(apkMetadata.packageName, editId, apkFile)
            val apk = uploadRequest.execute()
            LOGGER.info("AndroidUploadStore: Version code {} has been uploaded", apk.versionCode)

            // Assign apk to alpha track.
            val apkVersionCodes = ArrayList<Long>()
            apkVersionCodes.add(apk.versionCode.toLong())
            val updateTrackRequest = edits
                    .tracks()
                    .update(apkMetadata.packageName,
                            editId,
                            deployTrack,
                            Track().setReleases(
                                    listOf(TrackRelease()
                                            .setName(apkMetadata.versionName)
                                            .setVersionCodes(apkVersionCodes)
                                            .setStatus("completed"))))
            val updatedTrack = updateTrackRequest.execute()
            LOGGER.info("AndroidUploadStore: Track {} has been updated.", updatedTrack.track)

            // Commit changes for edit.
            val commitRequest = edits.commit(apkMetadata.packageName, editId)
            val appEdit = commitRequest.execute()
            LOGGER.info("AndroidUploadStore: App edit with id {} has been committed", appEdit.id)
        } catch (ex: Exception) {
            LOGGER.error("AndroidUploadStore: Exception was thrown while uploading apk to alpha track", ex)
        }
    }
}