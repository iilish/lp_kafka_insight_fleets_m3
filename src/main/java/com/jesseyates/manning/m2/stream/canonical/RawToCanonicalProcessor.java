package com.jesseyates.manning.m2.stream.canonical;

import com.jesseyates.manning.m2.api.S3;
import com.jesseyates.manning.common.StreamProcessor;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;

import java.util.Properties;

import static com.jesseyates.manning.m2.stream.canonical.RawRecordUtil.rawRecordSerDe;

/**
 * Map raw records to canonical events and then writes them, one-by-one, to the 'sink' topic.
 */
public class RawToCanonicalProcessor extends StreamProcessor<CanonicalConfiguration> {

  protected RawToCanonicalProcessor(String name, String description) {
    super(name, description);
  }

  @Override
  protected KafkaStreams buildStream(CanonicalConfiguration configuration, Properties props) {
    StreamsBuilder builder = new StreamsBuilder();
    builder.stream(configuration.getSource(),
      // key: UUID of the record, extracted here just for tracing
      // value: RawRecord, with either the body or a reference to an external store
      Consumed.with(Serdes.String(), rawRecordSerDe(configuration)))
           .flatMapValues(new JsonParser(new S3(configuration.getS3())))
           .map(new CanonicalRecordMapper())
           .to(configuration.getSink());

    return new KafkaStreams(builder.build(), props);
  }
}
