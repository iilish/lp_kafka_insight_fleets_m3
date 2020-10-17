package com.jesseyates.manning.m2.stream.canonical.dlq;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jesseyates.manning.m2.stream.canonical.CanonicalConfiguration;

import javax.validation.constraints.NotNull;

public class CanonicalConfigurationWithDLQ extends CanonicalConfiguration {
  @NotNull
  private String dlq;

  @JsonProperty("dlq")
  public String getDlq() {
    return dlq;
  }

  @JsonProperty("dlq")
  public void setDlq(String Dlq) {
    dlq = Dlq;
  }
}
