package com.jesseyates.manning.m2.stream.canonical;

import com.google.common.collect.ImmutableMap;
import manning.devices.canonical.m2.CanonicalKey;
import manning.devices.canonical.m2.CanonicalValue;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KeyValueMapper;

import java.time.Instant;
import java.util.Map;

/**
 * Maps raw events into CanonicalRecords. Expects the timestamp under the `timestamp` key.
 */
public class CanonicalRecordMapper
  implements KeyValueMapper<String, Map<String, Object>, KeyValue<CanonicalKey, CanonicalValue>> {

  private static final String TIMESTAMP = "timestamp";

  @Override
  public KeyValue<CanonicalKey, CanonicalValue> apply(String uuid, Map<String, Object> event) {
    long timestamp = (long) event.get(TIMESTAMP);
    CanonicalKey key = CanonicalKey.newBuilder()
      .setUuid(uuid)
      .build();
    CanonicalValue.Builder valueBuilder = CanonicalValue.newBuilder()
      .setUuid(uuid)
      .setRegionId((Long) event.get("region"))
      .setArrivalTimeMs(Instant.now().toEpochMilli())
      .setEventTimeMs(timestamp);

    // get the rest of the event fields
    ImmutableMap.Builder<CharSequence, CharSequence> events = ImmutableMap.builder();
    event.keySet()
         .stream().filter(s -> !s.equals(TIMESTAMP))
         .forEach(name -> events.put(name, event.get(name).toString()));
    valueBuilder.setEvents(events.build());

    return new KeyValue<>(key, valueBuilder.build());
  }
}
