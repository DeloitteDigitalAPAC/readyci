package com.squarepolka.readyci.tasks.app.android;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.FileContent;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.model.*;
import com.squarepolka.readyci.configuration.ReadyCIConfiguration;
import com.squarepolka.readyci.taskrunner.BuildEnvironment;
import com.squarepolka.readyci.taskrunner.TaskFailedException;
import com.squarepolka.readyci.tasks.Task;
import com.squarepolka.readyci.util.android.AndroidPublisherHelper;
import com.squarepolka.readyci.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;
import net.dongliu.apk.parser.bean.ApkSignStatus;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.List;

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import static com.squarepolka.readyci.configuration.AndroidPropConstants.*;

@Component
public class AndroidUploadStore extends Task {

    public static final String TASK_UPLOAD_STORE = "android_upload_play_store";

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadyCIConfiguration.class);

    @Override
    public String taskIdentifier() {
        return TASK_UPLOAD_STORE;
    }

    @Override
    public void performTask(BuildEnvironment buildEnvironment) throws TaskFailedException {

        try {

            String deployTrack = buildEnvironment.getProperty(BUILD_PROP_DEPLOY_TRACK, "");
            String playStoreEmail = buildEnvironment.getProperty(BUILD_PROP_SERVICE_ACCOUNT_EMAIL, "");
            String playStoreCert = buildEnvironment.getProperty(BUILD_PROP_SERVICE_ACCOUNT_FILE, "");

            if (deployTrack.isEmpty() ||
                    playStoreEmail.isEmpty() ||
                    playStoreCert.isEmpty()) {

                StringBuilder sb = new StringBuilder();

                sb.append("AndroidUploadStore: Missing vital details for play store deployment:");
                if(deployTrack.isEmpty())
                    sb.append("\n- deployTrack is required");
                if(playStoreEmail.isEmpty())
                    sb.append("\n- playStoreEmail is required");
                if(playStoreCert.isEmpty())
                    sb.append("\n- playStoreCert is required");

                throw new Exception(sb.toString());
            }

            String playStoreCertLocation = String.format("%s/%s", buildEnvironment.getCredentialsPath(), playStoreCert);

            Collection<File> unfilteredApks = Util.findAllByExtension(new File(buildEnvironment.getProjectPath()), ".apk");
            List<File> filteredApks = new ArrayList<File>();
            for(File file : unfilteredApks) {
                String absolutePath = file.getAbsolutePath();
                // filter based on known-bad path
                if(!absolutePath.contains("build/outputs") || 
                    absolutePath.endsWith("aligned.apk") || 
                    absolutePath.endsWith("signed.apk") || 
                    absolutePath.endsWith("uninstrumented.apk")) {

                    LOGGER.info("Filtering out apk with known-bad file path: " + absolutePath);

                    continue;
                }

                ApkFile apk = new ApkFile(file);

                // filter out unsigned apks
                ApkSignStatus signStatus = apk.verifyApk();
                if(signStatus != ApkSignStatus.signed) {
                    LOGGER.info("Filtering out unsigned apk: " + absolutePath);
                    continue;
                }

                // filter out debuggable apks
                InputSource is = new InputSource(new StringReader(apk.getManifestXml()));
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
                String debuggable = ((Element)doc.getElementsByTagName("application").item(0)).getAttribute("android:debuggable");
                if(debuggable != null && Boolean.valueOf(debuggable)) {
                    LOGGER.info("Filtering out debuggable apk: " + absolutePath);
                    continue;
                }

                filteredApks.add(file);
            }

            if(filteredApks.size() != 1) {
                throw new RuntimeException("Either there's no valid APK, or too many valid APKs");
            }

            File rawFile = filteredApks.get(0);
            ApkMeta apkMetadata = new ApkFile(rawFile).getApkMeta();


            // Create the API service.
            AndroidPublisher service = AndroidPublisherHelper.init(apkMetadata.getPackageName(), playStoreEmail, playStoreCertLocation);
            final AndroidPublisher.Edits edits = service.edits();

            // Create a new edit to make changes to your listing.
            AndroidPublisher.Edits.Insert editRequest = edits.insert(apkMetadata.getPackageName(), null);
            AppEdit edit = editRequest.execute();
            final String editId = edit.getId();
            LOGGER.info("AndroidUploadStore: Created edit with id: {}", editId);

            final AbstractInputStreamContent apkFile = new FileContent(AndroidPublisherHelper.MIME_TYPE_APK, rawFile);
            AndroidPublisher.Edits.Apks.Upload uploadRequest = edits
                    .apks()
                    .upload(apkMetadata.getPackageName(), editId, apkFile);

            Apk apk = uploadRequest.execute();
            LOGGER.info("AndroidUploadStore: Version code {} has been uploaded", apk.getVersionCode());

            // Assign apk to alpha track.
            List<Long> apkVersionCodes = new ArrayList<Long>();
            apkVersionCodes.add(Long.valueOf(apk.getVersionCode()));
            AndroidPublisher.Edits.Tracks.Update updateTrackRequest = edits
                    .tracks()
                    .update(apkMetadata.getPackageName(),
                            editId,
                            deployTrack,
                            new Track().setReleases(
                                    Collections.singletonList(
                                            new TrackRelease()
                                                    .setName(apkMetadata.getVersionName())
                                                    .setVersionCodes(apkVersionCodes)
                                                    .setStatus("completed"))));
            Track updatedTrack = updateTrackRequest.execute();
            LOGGER.info("AndroidUploadStore: Track {} has been updated.", updatedTrack.getTrack());


            // Commit changes for edit.
            AndroidPublisher.Edits.Commit commitRequest = edits.commit(apkMetadata.getPackageName(), editId);
            AppEdit appEdit = commitRequest.execute();
            LOGGER.info("AndroidUploadStore: App edit with id {} has been committed", appEdit.getId());

        } catch (Exception ex) {
            LOGGER.error("AndroidUploadStore: Exception was thrown while uploading apk to alpha track", ex);
        }
    }

}
