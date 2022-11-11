package com.krickert.search.model.util;

import com.google.protobuf.Timestamp;

import java.time.Instant;

public class ProtobufUtils {
    public static Timestamp now() {
        Instant time = Instant.now();
        return Timestamp.newBuilder().setSeconds(time.getEpochSecond())
                .setNanos(time.getNano()).build();
    }
    public static Timestamp stamp(long epochSeconds) {
        return Timestamp.newBuilder().setSeconds(epochSeconds)
                .setNanos(0).build();
    }
}
