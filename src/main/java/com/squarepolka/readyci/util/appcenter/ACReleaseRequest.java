package com.squarepolka.readyci.util.appcenter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ACReleaseRequest {

    @JsonProperty("destination_name")
    String destinationName;

    @JsonProperty("release_notes")
    String releaseNotes;

    @JsonProperty("mandatory_update")
    Boolean mandatoryUpdate = true;

    @JsonProperty("notify_testers")
    Boolean notifyTesters = true;

    public ACReleaseRequest(String destinationName, String releaseNotes) {
        this.destinationName = destinationName;
        this.releaseNotes = releaseNotes;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }

    public Boolean getMandatoryUpdate() {
        return mandatoryUpdate;
    }

    public void setMandatoryUpdate(Boolean mandatoryUpdate) {
        this.mandatoryUpdate = mandatoryUpdate;
    }

    public Boolean getNotifyTesters() {
        return notifyTesters;
    }

    public void setNotifyTesters(Boolean notifyTesters) {
        this.notifyTesters = notifyTesters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ACReleaseRequest request = (ACReleaseRequest) o;
        return Objects.equals(destinationName, request.destinationName) &&
                Objects.equals(releaseNotes, request.releaseNotes) &&
                Objects.equals(mandatoryUpdate, request.mandatoryUpdate) &&
                Objects.equals(notifyTesters, request.notifyTesters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(destinationName, releaseNotes, mandatoryUpdate, notifyTesters);
    }
}
