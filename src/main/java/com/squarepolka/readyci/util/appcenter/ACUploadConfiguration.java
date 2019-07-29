package com.squarepolka.readyci.util.appcenter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ACUploadConfiguration {

    @JsonProperty("upload_id")
    private String uploadId;

    @JsonProperty("upload_url")
    private String uploadUrl;

    @JsonProperty("asset_id")
    private String assetId;

    @JsonProperty("asset_domain")
    private String assetDomain;

    @JsonProperty("asset_token")
    private String assetToken;

    public ACUploadConfiguration() { }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getAssetDomain() {
        return assetDomain;
    }

    public void setAssetDomain(String assetDomain) {
        this.assetDomain = assetDomain;
    }

    public String getAssetToken() {
        return assetToken;
    }

    public void setAssetToken(String assetToken) {
        this.assetToken = assetToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ACUploadConfiguration that = (ACUploadConfiguration) o;
        return Objects.equals(uploadId, that.uploadId) &&
                Objects.equals(uploadUrl, that.uploadUrl) &&
                Objects.equals(assetId, that.assetId) &&
                Objects.equals(assetDomain, that.assetDomain) &&
                Objects.equals(assetToken, that.assetToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uploadId, uploadUrl, assetId, assetDomain, assetToken);
    }
}
