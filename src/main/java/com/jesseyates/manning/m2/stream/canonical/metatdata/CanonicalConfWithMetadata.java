package com.jesseyates.manning.m2.stream.canonical.metatdata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jesseyates.manning.m2.stream.canonical.slow.lane.CanonicalConfWithSlowLane;

import javax.validation.constraints.NotNull;

public class CanonicalConfWithMetadata extends CanonicalConfWithSlowLane {

    @NotNull
    private String metadata;

    @JsonProperty("metadata")
    public String getMetadata() {
        return metadata;
    }

    @JsonProperty("metadata")
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

}
