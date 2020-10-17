package com.jesseyates.manning.m2.stream.canonical.metatdata;

import com.jesseyates.manning.common.StreamProcessor;
import com.jesseyates.manning.m2.api.S3;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import manning.devices.canonical.m2.CanonicalErrorValue;
import manning.devices.canonical.m2.CanonicalKey;
import manning.devices.canonical.m2.CanonicalMetadata;
import manning.devices.canonical.m2.CanonicalValue;
import manning.devices.raw.m2.RawRecord;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.RecordContext;
import org.apache.kafka.streams.processor.TopicNameExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static com.jesseyates.manning.m2.stream.canonical.RawRecordUtil.rawRecordSerDe;

public class RawToCanonicalProcessorWithDLQAndSlowLaneWithMetaData
        extends StreamProcessor<CanonicalConfWithMetadata> {

    private static Logger logger = LoggerFactory.getLogger(RawToCanonicalProcessorWithDLQAndSlowLaneWithMetaData.class);

    public RawToCanonicalProcessorWithDLQAndSlowLaneWithMetaData(String name, String description) {
        super(name, description);
    }

    @Override
    protected KafkaStreams buildStream(CanonicalConfWithMetadata conf, Properties props) {
        Duration maxParseTime = Duration.parse(conf.getMaxDuration());

        StreamsBuilder builder = new StreamsBuilder();

        builder.stream(conf.getSource(),
                Consumed.with(Serdes.String(), rawRecordSerDe(conf)))
                .flatMapValues(new MetadataParser(new S3(conf.getS3()), maxParseTime))
                .map(new MetadataParsedRecordReMapper())
                .to(new TopicExtractor(conf), Produced.with(toto(conf), toto2(conf)));

        return new KafkaStreams(builder.build(), props);
    }

    private static Serde<CanonicalKey> toto(CanonicalConfWithMetadata conf) {
        return StreamProcessor.configureAvroSerde(conf, new SpecificAvroSerde<>(), false);
    }

    private static Serde<SpecificRecord> toto2(CanonicalConfWithMetadata conf) {
        final Serde<SpecificRecord> avro = new SpecificAvroSerde<>();
        final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url",
                conf.getKafka().getSchemaRegistry());
        avro.configure(serdeConfig, false);
        return avro;

    }

    private static class TopicExtractor implements TopicNameExtractor<CanonicalKey, SpecificRecord> {

        private final CanonicalConfWithMetadata conf;

        private TopicExtractor(CanonicalConfWithMetadata conf) {
            this.conf = conf;
        }

        @Override
        public String extract(CanonicalKey key, SpecificRecord value, RecordContext recordContext) {
            if (value instanceof CanonicalValue) {
                logger.info("writing to sink = " + conf.getSink());
                return conf.getSink();
            } else if (value instanceof CanonicalErrorValue) {
                logger.info("writing to Dlq = " + conf.getDlq());
                return conf.getDlq();
            } else if (value instanceof RawRecord) {
                logger.info("writing to slow = " + conf.getSlowLane());
                return conf.getSlowLane();
            } else if (value instanceof CanonicalMetadata) {
                logger.info("writing to meta = " + conf.getMetadata());
                return conf.getMetadata();
            }
            throw new RuntimeException("No known mapping for record: " + value);
        }
    }
}
