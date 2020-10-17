package com.jesseyates.manning.m2.stream.canonical.metatdata;

import com.jesseyates.manning.m2.stream.canonical.slow.lane.ParsedValue;
import manning.devices.raw.m2.RawRecord;

import java.util.Map;

/**
 * Tuple helper for state of parsing
 */
public class MetadataParsedValue extends ParsedValue {
    ParsedMessageMetadata metadata;

    public MetadataParsedValue(RawRecord source) {
        super(source);
    }

    public MetadataParsedValue metadata(ParsedMessageMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    @Override
    public MetadataParsedValue fail(Exception cause) {
        this.failed = cause;
        return this;
    }

    @Override
    public MetadataParsedValue parsed(Map<String, Object> events) {
        this.parsed = events;
        return this;
    }

    @Override
    public MetadataParsedValue slow() {
        this.slow = true;
        return this;
    }
}
