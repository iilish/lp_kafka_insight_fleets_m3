package com.jesseyates.manning.m2.stream.canonical.slow.lane;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.AbstractIterator;
import com.jesseyates.manning.m2.api.S3;
import manning.devices.raw.m2.RawRecord;
import org.apache.kafka.streams.kstream.ValueMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static com.jesseyates.manning.m2.stream.canonical.RawRecordUtil.message;

/**
 * A 'fast-lane' parser that will re-direct events that are too slow to parse into a slow-lane
 * topic.
 */
public class FastLaneParser implements ValueMapper<RawRecord, Iterable<ParsedValue>> {

  private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE =
    new TypeReference<Map<String, Object>>() {
    };
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private final S3 s3;
  private final Duration max;

  public FastLaneParser(S3 s3, Duration maxTime) {
    this.s3 = s3;
    this.max = maxTime;
  }

  @Override
  public Iterable<ParsedValue> apply(RawRecord value) {
    return () -> {
      final Instant start = Instant.now();
      BufferedReader reader = new BufferedReader(new InputStreamReader(message(s3, value)));
      return new AbstractIterator<ParsedValue>() {
        private boolean done = false;

        @Override
        protected ParsedValue computeNext() {
          if (done) {
            return endOfData();
          }
          Instant incrementalTime = Instant.now();
          Duration remainingParsingTime = Duration.between(start, incrementalTime).minus(max);
          if (remainingParsingTime.isZero() || remainingParsingTime.isNegative()) {

            return new ParsedValue(value).slow();
          }
          try {
            String line = reader.readLine();
            if (line == null) {
              return endOfData();
            }
            return new ParsedValue(value).parsed(MAPPER.readValue(line, MAP_TYPE_REFERENCE));
          } catch (Exception e) {
            done = true;
            return new ParsedValue(value).fail(e);
          }
        }
      };
    };
  }
}
