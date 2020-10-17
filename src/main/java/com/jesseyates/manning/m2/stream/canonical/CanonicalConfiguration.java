package com.jesseyates.manning.m2.stream.canonical;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jesseyates.manning.m2.api.S3Conf;
import com.jesseyates.manning.common.StreamConfiguration;

import javax.validation.constraints.NotNull;

public class CanonicalConfiguration extends StreamConfiguration {

  private S3Conf s3 = new S3Conf();
  @NotNull
  private String sink;

  @JsonProperty("s3")
  public S3Conf getS3() {
    return s3;
  }

  @JsonProperty("s3")
  public void setS3(S3Conf S3) {
    s3 = S3;
  }

  @JsonProperty("sink")
  public String getSink() {
    return sink;
  }

  @JsonProperty("sink")
  public void setSink(String Sink) {
    sink = Sink;
  }
}
