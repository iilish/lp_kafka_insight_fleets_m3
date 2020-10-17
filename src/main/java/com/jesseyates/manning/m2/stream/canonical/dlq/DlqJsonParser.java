package com.jesseyates.manning.m2.stream.canonical.dlq;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.AbstractIterator;
import com.jesseyates.manning.m2.api.S3;
import manning.devices.raw.m2.RawRecord;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.streams.kstream.ValueMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

import static com.jesseyates.manning.m2.stream.canonical.RawRecordUtil.message;

/**
 * Map the record into its event parts, or catch the exception and the source record so it can be
 * handled by the downstream dead-letter-queue (DLQ). Bad records are those that are not valid
 * JSON, rather than records that are not valid in our Avro schemas (similar techniques can be
 * used for bad JSON).
 */
public class DlqJsonParser
  implements ValueMapper<RawRecord,
  Iterable<Pair<Pair<Exception, RawRecord>, Map<String, Object>>>> {

  private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE =
    new TypeReference<Map<String, Object>>() {
    };
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private final S3 s3;

  public DlqJsonParser(S3 s3) {
    this.s3 = s3;
  }

  @Override
  public Iterable<Pair<Pair<Exception, RawRecord>, Map<String, Object>>> apply(RawRecord value) {
    return () -> {
      BufferedReader reader = new BufferedReader(new InputStreamReader(message(s3, value)));
      return new AbstractIterator<Pair<Pair<Exception, RawRecord>, Map<String, Object>>>() {
        private boolean done = false;

        @Override
        protected Pair<Pair<Exception, RawRecord>, Map<String, Object>> computeNext() {
          if (done) {
            return endOfData();
          }
          try {
            String line = reader.readLine();
            if (line == null) {
              return endOfData();
            }
            return new ImmutablePair<>(null, MAPPER.readValue(line, MAP_TYPE_REFERENCE));
          } catch (Exception e) {
            done = true;
            return new ImmutablePair<>(new ImmutablePair<>(e, value), null);
          }
        }
      };
    };
  }
}
