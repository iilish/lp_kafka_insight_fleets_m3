package com.jesseyates.manning.m2.stream.canonical;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jesseyates.manning.m2.api.S3;
import manning.devices.raw.m2.RawRecord;
import org.apache.kafka.streams.kstream.ValueMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import static com.jesseyates.manning.m2.stream.canonical.RawRecordUtil.message;

/**
 * Flat maps a blob into an iterable of JSON events, one per-line.
 */
public class JsonParser implements ValueMapper<RawRecord, Iterable<Map<String, Object>>> {

  private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE =
    new TypeReference<Map<String, Object>>() {
    };
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private final S3 s3;

  public JsonParser(S3 s3) {
    this.s3 = s3;
  }

  @Override
  public Iterable<Map<String, Object>> apply(RawRecord value) {
    return () -> {
      BufferedReader reader = new BufferedReader(new InputStreamReader(message(s3, value)));
      return reader.lines()
                   .map(line -> {
                     try {
                       return (Map<String, Object>) MAPPER.readValue(line, MAP_TYPE_REFERENCE);
                     } catch (IOException e) {
                       throw new RuntimeException(e);
                     }
                   })
                   .iterator();
    };
  }
}
