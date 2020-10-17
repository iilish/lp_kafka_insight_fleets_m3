package com.jesseyates.manning.m2.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class ApiConfiguration extends com.jesseyates.manning.m1.api.ApiConfiguration {

  // max body size, in bytes
  private int maxBodySize = 1024 * 1024; // 1MB

  @JsonProperty("maxBodySize")
  public int getMaxBodySize() {
    return maxBodySize;
  }

  @JsonProperty("maxBodySize")
  public void setMaxBodySize(int MaxBodySize) {
    maxBodySize = MaxBodySize;
  }

  @NotNull
  private S3Conf s3;

  @JsonProperty("s3")
  public S3Conf getS3() {
    return s3;
  }

  @JsonProperty("s3")
  public void setS3(S3Conf S3) {
    s3 = S3;
  }

}
