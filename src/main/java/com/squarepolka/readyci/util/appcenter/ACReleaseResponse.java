package com.squarepolka.readyci.util.appcenter;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class ACReleaseResponse {

    @JsonProperty("release_notes")
    String releaseNotes;

    public ACReleaseResponse() { }

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ACReleaseResponse that = (ACReleaseResponse) o;
        return Objects.equals(releaseNotes, that.releaseNotes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(releaseNotes);
    }
}
