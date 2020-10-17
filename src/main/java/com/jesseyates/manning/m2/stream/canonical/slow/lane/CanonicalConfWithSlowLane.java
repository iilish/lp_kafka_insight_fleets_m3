package com.jesseyates.manning.m2.stream.canonical.slow.lane;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jesseyates.manning.m2.stream.canonical.CanonicalConfiguration;
import com.jesseyates.manning.m2.stream.canonical.dlq.CanonicalConfigurationWithDLQ;

import javax.validation.constraints.NotNull;

/**
 *
 */
public class CanonicalConfWithSlowLane extends CanonicalConfigurationWithDLQ {

  private String maxDuration = "PT1M";
  @NotNull
  private String slowLane;

  @JsonProperty("slowLane")
  public String getSlowLane() {
    return slowLane;
  }

  @JsonProperty("slowLane")
  public void setSlowLane(String SlowLane) {
    slowLane = SlowLane;
  }

  @JsonProperty("maxDuration")
  public String getMaxDuration() {
    return maxDuration;
  }

  @JsonProperty("maxDuration")
  public void setMaxDuration(String MaxDuration) {
    maxDuration = MaxDuration;
  }
}
