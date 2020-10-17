package com.jesseyates.manning.m2.stream.canonical.dlq;

import com.jesseyates.manning.m2.stream.canonical.CanonicalRecordMapper;
import manning.devices.canonical.m2.CanonicalErrorValue;
import manning.devices.canonical.m2.CanonicalKey;
import manning.devices.canonical.m2.CanonicalValue;
import manning.devices.raw.m2.RawRecord;
import org.apache.avro.specific.SpecificRecord;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KeyValueMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles mapping a failed record or a valid canonical record into a Avro record
 */
public class ExceptionOrRecordMapper
  implements
  KeyValueMapper<String, Pair<Pair<Exception, RawRecord>,
    Map<String, Object>>, KeyValue<CanonicalKey, ? extends SpecificRecord>> {

  private final CanonicalRecordMapper canonicalMapper;

  public ExceptionOrRecordMapper() {
    this.canonicalMapper = new CanonicalRecordMapper();
  }

  @Override
  public KeyValue<CanonicalKey, SpecificRecord> apply(String uuid,
    Pair<Pair<Exception, RawRecord>, Map<String, Object>> value) {
    if (value.getRight() != null) {
      // have to re-create kv b/c of java generics handling
      KeyValue<CanonicalKey, CanonicalValue> kv = canonicalMapper.apply(uuid, value.getValue());
      return new KeyValue<>(kv.key, kv.value);
    }

    return mapError(value.getLeft());
  }

  public static KeyValue<CanonicalKey, SpecificRecord> mapError(
    Pair<Exception, RawRecord> errorEvent) {
    String uuid = errorEvent.getValue().getUuid().toString();
    CanonicalKey key = CanonicalKey.newBuilder()
                                   .setUuid(uuid)
                                   .build();

    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
        errorEvent.getValue().writeExternal(oos);
      }

      CanonicalErrorValue error =
        CanonicalErrorValue.newBuilder()
                           .setRawRecordBytes(ByteBuffer.wrap(bos.toByteArray()))
                           .setStackTrace(buildTrace(errorEvent.getKey()))
                           .build();
      return new KeyValue<>(key, error);
    } catch (IOException e) {
      throw new RuntimeException("Unexpected failure serializing error", e);
    }
  }

  private static List<CharSequence> buildTrace(Exception key) {
    return Arrays.stream(key.getStackTrace())
                 .map(StackTraceElement::toString)
                 .collect(Collectors.toList());
  }
}
