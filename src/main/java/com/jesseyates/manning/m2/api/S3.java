package com.jesseyates.manning.m2.api;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
 * Light wrapper around S3 access to make life a bit simpler.
 */
public class S3 {

  // private DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private DateTimeFormatter format =
          DateTimeFormatter.ofPattern("yyyy-MM-dd") //.ofLocalizedDateTime( FormatStyle.SHORT )
                  .withLocale( Locale.FRANCE )
                  .withZone( ZoneId.systemDefault() );
  private final AmazonS3 s3;
  private final String bucket;

  public S3(AmazonS3 s3, String bucket) {
    this.s3 = s3;
    this.bucket = bucket;
  }

  public S3(S3Conf conf) {
    this.s3 = new AmazonS3Client();
    // s3.setEndpoint(conf.getEndpoint());
    this.bucket = conf.getBucket();
  }

  /**
   * Write the device information to a unique file on S3.
   * @param deviceUUID UUID of the device, to generate a 'safe' path for the file
   * @param fileName name of the file
   * @param stream data to write
   * @param size size in bytes of the stream to write
   * @return the path to the file in the configured bucket
   */
  public String put(String deviceUUID, String fileName, InputStream stream, long size) {
    Instant now = Instant.now();
    // generate a day-based groups of the events, for easier debugging later
    String formated = format.format(now);
    String key = String.format("%s/%s/%s", formated, deviceUUID, fileName);
    ObjectMetadata meta = new ObjectMetadata();
    meta.setContentLength(size);
    PutObjectRequest request = new PutObjectRequest(bucket, key, stream, meta);
    s3.putObject(request);
    return key;
  }

  public InputStream read(String key) {
    return s3.getObject(bucket, key).getObjectContent();
  }
}
