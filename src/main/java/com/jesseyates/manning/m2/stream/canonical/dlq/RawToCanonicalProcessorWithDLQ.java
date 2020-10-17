package com.jesseyates.manning.m2.stream.canonical.dlq;

import com.jesseyates.manning.m2.api.S3;
import com.jesseyates.manning.common.StreamProcessor;
import com.jesseyates.manning.m2.stream.canonical.RawToCanonicalProcessor;
import manning.devices.canonical.m2.CanonicalKey;
import manning.devices.canonical.m2.CanonicalValue;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.processor.RecordContext;
import org.apache.kafka.streams.processor.TopicNameExtractor;

import java.util.Properties;

import static com.jesseyates.manning.m2.stream.canonical.RawRecordUtil.rawRecordSerDe;

/**
 * Like the {@link RawToCanonicalProcessor} but allows the use of a dead-letter-queue within the
 * stream. Usually, we can just handle errors at the consumer side (for a bad deserialization) or
 * at the producer side, where we only can support bad producing. However, the middle part -
 * where we handle the record - is where things get interesting.
 * <p>
 * In our processor we make the choice that we want to parse as much of the record as possible
 * before failing. So records that are half-parsed send what they can downstream, while we store
 * the bad record in the DLQ.
 */
public class RawToCanonicalProcessorWithDLQ extends StreamProcessor<CanonicalConfigurationWithDLQ> {

  protected RawToCanonicalProcessorWithDLQ(String name, String description) {
    super(name, description);
  }

  @Override
  protected KafkaStreams buildStream(CanonicalConfigurationWithDLQ conf, Properties props) {

    StreamsBuilder builder = new StreamsBuilder();
    builder.stream(conf.getSource(),
      Consumed.with(Serdes.String(), rawRecordSerDe(conf)))
           .flatMapValues(new DlqJsonParser(new S3(conf.getS3())))
           .map(new ExceptionOrRecordMapper())
           .to(new TopicExtractor(conf));

    return new KafkaStreams(builder.build(), props);
  }

  private static class TopicExtractor implements TopicNameExtractor<CanonicalKey, SpecificRecord> {

    private final CanonicalConfigurationWithDLQ conf;

    private TopicExtractor(CanonicalConfigurationWithDLQ conf) {
      this.conf = conf;
    }

    @Override
    public String extract(CanonicalKey key, SpecificRecord value, RecordContext recordContext) {
      if(value instanceof CanonicalValue){
        return conf.getSink();
      }else{
        return conf.getDlq();
      }
    }
  }
}
