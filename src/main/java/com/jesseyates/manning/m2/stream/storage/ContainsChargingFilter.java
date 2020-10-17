package com.jesseyates.manning.m2.stream.storage;

  import manning.devices.canonical.m2.CanonicalValue;
  import org.apache.kafka.streams.kstream.Predicate;

/**
 * Filters out events that dont have charging information, e.g. other diagnostic events
 */
public class ContainsChargingFilter<T> implements Predicate<T, CanonicalValue> {

  @Override
  public boolean test(T key, CanonicalValue value) {
    return value.getEvents().containsKey("charging");
  }
}
