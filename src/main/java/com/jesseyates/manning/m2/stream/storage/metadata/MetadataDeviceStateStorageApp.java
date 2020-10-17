package com.jesseyates.manning.m2.stream.storage.metadata;

import com.jesseyates.manning.common.App;
import com.jesseyates.manning.m2.stream.canonical.CanonicalConfiguration;
import io.dropwizard.setup.Bootstrap;
import manning.devices.canonical.m2.CanonicalMetadata;

/**
 * Stream Processor that takes {@link CanonicalMetadata} and writes it directly to the database
 */
public class MetadataDeviceStateStorageApp extends App<CanonicalConfiguration> {

  @Override
  public void initialize(Bootstrap<CanonicalConfiguration> bootstrap) {
    bootstrap.addCommand(new MetadataStorageProcessor("metadata-to-database",
      "Runs a Kafka stream application to write metadata records to a database"));
  }

  public static void main(String[] args) throws Exception {
    new MetadataDeviceStateStorageApp().run(args);
  }
}
