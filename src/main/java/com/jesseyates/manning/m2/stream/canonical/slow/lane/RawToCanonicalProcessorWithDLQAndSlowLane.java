package com.jesseyates.manning.m2.stream.canonical.slow.lane;

import com.jesseyates.manning.m2.api.S3;
import com.jesseyates.manning.common.StreamProcessor;
import manning.devices.canonical.m2.CanonicalErrorValue;
import manning.devices.canonical.m2.CanonicalKey;
import manning.devices.canonical.m2.CanonicalValue;
import manning.devices.raw.m2.RawRecord;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.processor.RecordContext;
import org.apache.kafka.streams.processor.TopicNameExtractor;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

import java.time.Duration;
import java.util.Properties;

import static com.jesseyates.manning.m2.stream.canonical.RawRecordUtil.rawRecordSerDe;

public class RawToCanonicalProcessorWithDLQAndSlowLane
  extends StreamProcessor<CanonicalConfWithSlowLane> {

  protected RawToCanonicalProcessorWithDLQAndSlowLane(String name, String description) {
    super(name, description);
  }

  @Override
  protected KafkaStreams buildStream(CanonicalConfWithSlowLane conf, Properties props) {
    Duration maxParseTime = Duration.parse(conf.getMaxDuration());

    StreamsBuilder builder = new StreamsBuilder();
    // create store
    StoreBuilder<KeyValueStore<String,ParsedValue>> keyValueStoreBuilder =
            Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore("metaDataStore"),
                    Serdes.String(),
                    Serdes.serdeFrom(ParsedValue.class));
    // register store
    builder.addStateStore(keyValueStoreBuilder);

    builder.stream(conf.getSource(),
      Consumed.with(Serdes.String(), rawRecordSerDe(conf)))
           .flatMapValues(new FastLaneParser(new S3(conf.getS3()), maxParseTime))
           .map(new ParsedRecordReMapper())
           .to(new TopicExtractor(conf));

    return new KafkaStreams(builder.build(), props);
  }

  private static class TopicExtractor implements TopicNameExtractor<CanonicalKey, SpecificRecord> {

    private final CanonicalConfWithSlowLane conf;

    private TopicExtractor(CanonicalConfWithSlowLane conf) {
      this.conf = conf;
    }

    @Override
    public String extract(CanonicalKey key, SpecificRecord value, RecordContext recordContext) {
      if (value instanceof CanonicalValue) {
        return conf.getSink();
      } else if (value instanceof CanonicalErrorValue) {
        return conf.getDlq();
      } else if (value instanceof RawRecord) {
        return conf.getSlowLane();
      }
      throw new RuntimeException("No known mapping for record: " + value);
    }
  }
}
