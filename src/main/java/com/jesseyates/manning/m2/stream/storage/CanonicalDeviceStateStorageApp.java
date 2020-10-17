package com.jesseyates.manning.m2.stream.storage;

import com.jesseyates.manning.common.App;
import com.jesseyates.manning.m2.stream.canonical.CanonicalConfiguration;
import io.dropwizard.setup.Bootstrap;
import manning.devices.canonical.m2.CanonicalValue;

/**
 * Stream Processor that takes {@link CanonicalValue} and writes it directly to the database
 */
public class CanonicalDeviceStateStorageApp extends App<CanonicalConfiguration> {

  @Override
  public void initialize(Bootstrap<CanonicalConfiguration> bootstrap) {
    bootstrap.addCommand(new CanonicalStorageProcessor("canonical-to-database",
      "Runs a Kafka stream application to write canonical records to a database"));
  }

  public static void main(String[] args) throws Exception {
    new CanonicalDeviceStateStorageApp().run(args);
  }
}
