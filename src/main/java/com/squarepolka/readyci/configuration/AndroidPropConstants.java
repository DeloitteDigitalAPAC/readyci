package com.squarepolka.readyci.configuration;

public class AndroidPropConstants {

    private AndroidPropConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String BUILD_PROP_SCHEME = "scheme";
    public static final String BUILD_PROP_DEPLOY_TRACK = "deployTrack";
    public static final String BUILD_PROP_SERVICE_ACCOUNT_FILE = "playStoreAuthCert";
    public static final String BUILD_PROP_SERVICE_ACCOUNT_EMAIL = "playStoreEmail";
    public static final String BUILD_PROP_JAVA_KEYSTORE_PATH = "javaKeystorePath";
    public static final String BUILD_PROP_STOREPASS = "storepass";
    public static final String BUILD_PROP_KEYSTORE_ALIAS = "keystoreAlias";
    public static final String BUILD_PROP_KEYSTORE_PROPERTIES_PATH = "keystorePropertiesPath";
    public static final String BUILD_PROP_SDK_PATH = "androidSdkPath";
    public static final String BUILD_PROP_HOCKEYAPP_TOKEN = "hockappToken";
    public static final String BUILD_PROP_HOCKEYAPP_RELEASE_TAGS = "hockeyappReleaseTags";
    public static final String BUILD_PROP_HOCKEYAPP_RELEASE_NOTES = "hockeyappReleaseNotes";
    public static final String BUILD_PROP_FILENAME_FILTERS = "fileNameFilters";

}
