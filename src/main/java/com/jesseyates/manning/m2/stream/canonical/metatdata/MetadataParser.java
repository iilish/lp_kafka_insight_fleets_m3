package com.jesseyates.manning.m2.stream.canonical.metatdata;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.AbstractIterator;
import com.jesseyates.manning.m2.api.S3;
import manning.devices.raw.m2.RawRecord;
import org.apache.kafka.streams.kstream.ValueMapper;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static com.jesseyates.manning.m2.stream.canonical.RawRecordUtil.message;

/**
 * A 'metadata' parser that will re-direct events to specific topic and create metadata.
 */
public class MetadataParser implements ValueMapper<RawRecord, Iterable<MetadataParsedValue>> {
    private static final Logger logger = LoggerFactory.getLogger(MetadataParser.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE =
            new TypeReference<Map<String, Object>>() {
            };
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final S3 s3;
    private final Duration max;

    public MetadataParser(S3 s3, Duration maxTime) {
        this.s3 = s3;
        this.max = maxTime;
    }


    @Override
    public Iterable<MetadataParsedValue> apply(RawRecord value) {
        return () -> {
            final Instant start = Instant.now();
            ParsedMessageMetadata metadata =
                    new ParsedMessageMetadata(value.getArrivalTimeMs(), start.toEpochMilli());

            BufferedReader reader = new BufferedReader(new InputStreamReader(message(s3, value)));
            return new AbstractIterator<MetadataParsedValue>() {
                private boolean done = false;
                private boolean readyToSendMetadataMessage = false;

                @Override
                protected MetadataParsedValue computeNext() {
                    if (readyToSendMetadataMessage) {
                        done = true;
                        readyToSendMetadataMessage = false;
                        return new MetadataParsedValue(value).metadata(metadata.endTime());
                    }

                    if (done) {
                        return endOfData();
                    }
                    Instant incrementalTime = Instant.now();
                    logger.info("between " + Duration.between(start, incrementalTime) + " max " + max);
                    Duration remainingParsingTime = max.minus(Duration.between(start, incrementalTime));
                    if (remainingParsingTime.isZero() || remainingParsingTime.isNegative()) {
                        readyToSendMetadataMessage = true;
                        metadata.setSlow();
                        return new MetadataParsedValue(value).slow();
                    }
                    try {
                        String line = reader.readLine();
                        if (line == null) {
                            done = true;
                            readyToSendMetadataMessage = false;
                            return new MetadataParsedValue(value).metadata(metadata.endTime());
                        }
                        Map<String, Object> events = MAPPER.readValue(line, MAP_TYPE_REFERENCE);
                        events.put("timestamp", Instant.now().toEpochMilli());
                        metadata.addEventCount();
                        return new MetadataParsedValue(value).parsed(events);
                    } catch (Exception e) {
                        readyToSendMetadataMessage = true;
                        metadata.withError();
                        return new MetadataParsedValue(value).fail(e);
                    }
                }
            };
        };
    }

}