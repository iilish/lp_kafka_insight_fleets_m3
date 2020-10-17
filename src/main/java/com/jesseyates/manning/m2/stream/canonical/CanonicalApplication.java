package com.jesseyates.manning.m2.stream.canonical;

import com.jesseyates.manning.common.App;
import com.jesseyates.manning.m2.stream.canonical.metatdata.CanonicalConfWithMetadata;
import com.jesseyates.manning.m2.stream.canonical.metatdata.RawToCanonicalProcessorWithDLQAndSlowLaneWithMetaData;
import io.dropwizard.setup.Bootstrap;

public class CanonicalApplication extends App<CanonicalConfWithMetadata> {

  @Override
  public void initialize(Bootstrap<CanonicalConfWithMetadata> bootstrap) {
    bootstrap.addCommand(new RawToCanonicalProcessorWithDLQAndSlowLaneWithMetaData("raw-to-canonical",
      "Runs a Kafka stream application to map raw records into canonical records"));
  }

  public static void main(String[] args) throws Exception {
    new CanonicalApplication().run(args);
  }
}
