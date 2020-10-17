package com.jesseyates.manning.m2.stream.canonical;

import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.jesseyates.manning.common.StreamConfiguration;
import com.jesseyates.manning.common.StreamProcessor;
import com.jesseyates.manning.m2.api.S3;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import manning.devices.raw.m2.RawRecord;
import org.apache.kafka.common.serialization.Serde;

import java.io.InputStream;
import java.nio.ByteBuffer;

public class RawRecordUtil {

  public static InputStream message(S3 s3, RawRecord value){
    InputStream stream;
    ByteBuffer buffer = value.getBody();
    if (buffer != null) {
      stream = new ByteBufferBackedInputStream(buffer);
    } else {
      stream = s3.read(value.getBodyReference().toString());
    }
    return stream;
  }

  /**
   * Small helper to make ensure we don't fat-finger raw consumers, our most common type of
   * consumer.
   */
  public static Serde<RawRecord> rawRecordSerDe(StreamConfiguration conf){
    return StreamProcessor.configureAvroSerde(conf, new SpecificAvroSerde<>(), false);
  }
}
