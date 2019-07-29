package com.squarepolka.readyci.util.appcenter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ACUploadConfirmationResponse {

    @JsonProperty("release_id")
    private String releaseID;

    @JsonProperty("release_url")
    private String releaseUrl;

    public ACUploadConfirmationResponse() { }


    public String getReleaseID() {
        return releaseID;
    }

    public void setReleaseID(String releaseID) {
        this.releaseID = releaseID;
    }

    public String getReleaseUrl() {
        return releaseUrl;
    }

    public void setReleaseUrl(String releaseUrl) {
        this.releaseUrl = releaseUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ACUploadConfirmationResponse that = (ACUploadConfirmationResponse) o;
        return Objects.equals(releaseID, that.releaseID) &&
                Objects.equals(releaseUrl, that.releaseUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(releaseID, releaseUrl);
    }
}
