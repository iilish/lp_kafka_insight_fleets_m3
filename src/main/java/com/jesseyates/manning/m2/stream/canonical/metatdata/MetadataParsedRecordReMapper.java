package com.jesseyates.manning.m2.stream.canonical.metatdata;

import com.jesseyates.manning.m2.stream.canonical.CanonicalRecordMapper;
import manning.devices.canonical.m2.CanonicalKey;
import manning.devices.canonical.m2.CanonicalMetadata;
import manning.devices.canonical.m2.CanonicalValue;
import org.apache.avro.specific.SpecificRecord;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KeyValueMapper;

import static com.jesseyates.manning.m2.stream.canonical.dlq.ExceptionOrRecordMapper.mapError;

public class MetadataParsedRecordReMapper
        implements KeyValueMapper<String, MetadataParsedValue, KeyValue<CanonicalKey, SpecificRecord>> {
    private final CanonicalRecordMapper canonicalMapper;

    public MetadataParsedRecordReMapper() {
        this.canonicalMapper = new CanonicalRecordMapper();
    }

    @Override
    public KeyValue<CanonicalKey, SpecificRecord> apply(String uuid, MetadataParsedValue value) {
        if (value.metadata != null) {
            return mapMetadata(uuid, value.metadata);
        } else if (value.failed != null) {
            return mapError(new ImmutablePair<>(value.failed, value.source));
        } else if (value.parsed != null) {

            KeyValue<CanonicalKey, CanonicalValue> kv = canonicalMapper.apply(uuid, value.parsed);
            return new KeyValue<>(kv.key, kv.value);
        } else {
            // it must have been slow, so just end the basic record
            CanonicalKey key = CanonicalKey.newBuilder()
                    .setUuid(uuid)
                    .build();
            return new KeyValue<>(key, value.source);
        }
    }

    private KeyValue<CanonicalKey, SpecificRecord> mapMetadata(String uuid, ParsedMessageMetadata metadata) {
        CanonicalKey key = CanonicalKey.newBuilder()
                .setUuid(uuid)
                .build();

        CanonicalMetadata meta = CanonicalMetadata.newBuilder()
                .setUuid(uuid)
                .setArrivalTimeMs(metadata.arrivalTime)
                .setStartTimeMs(metadata.startTime)
                .setEndTimeMs(metadata.endTime)
                .setSlow(metadata.slow)
                .setWithError(metadata.withError)
                .setNumberEvents(metadata.numberOfEvents)
                .build();

        return new KeyValue<>(key, meta);
    }

}
