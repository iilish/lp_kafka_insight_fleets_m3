package com.jesseyates.manning.m2.stream.canonical.metatdata;

import java.time.Instant;

public class ParsedMessageMetadata {
    final long arrivalTime;
    final long startTime;
    long endTime;
    boolean withError;
    boolean slow;
    int numberOfEvents;


    public ParsedMessageMetadata(long arrivalTime, long startTime) {
        this.arrivalTime = arrivalTime;
        this.startTime = startTime;
    }

    public ParsedMessageMetadata endTime() {
        this.endTime = Instant.now().toEpochMilli();
        return this;
    }

    public ParsedMessageMetadata withError() {
        this.withError = true;
        return this;
    }

    public ParsedMessageMetadata setSlow() {
        this.slow = true;
        return this;
    }

    public ParsedMessageMetadata addEventCount() {
        numberOfEvents++;
        return this;
    }
}
