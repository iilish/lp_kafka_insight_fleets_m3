package com.jesseyates.manning.m2.stream.canonical.slow.lane;

import com.jesseyates.manning.m2.stream.canonical.metatdata.ParsedMessageMetadata;
import manning.devices.raw.m2.RawRecord;

import java.util.Map;

/**
 * Tuple helper for state of parsing
 */
public class ParsedValue {
  public final RawRecord source;
  public Exception failed;
  public Map<String, Object> parsed;
  public boolean slow = false;

  public ParsedValue(RawRecord source) {
    this.source = source;
  }

  public ParsedValue fail(Exception cause){
    this.failed = cause;
    return this;
  }

  public ParsedValue parsed(Map<String, Object> events){
    this.parsed = events;
    return this;
  }

  public ParsedValue slow(){
    this.slow = true;
    return this;
  }

}
