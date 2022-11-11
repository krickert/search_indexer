package com.krickert.search.model.util;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

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

    public static <T extends Message> void  saveProtobufToDisk(String dst,T item) throws IOException {
        item.writeTo(new FileOutputStream(new File(dst)));
    }

    public static <T extends Message> void saveProtocoBufsToDisk(String dstPrefix, Collection<T> items) {
        AtomicInteger i = new AtomicInteger();
        items.forEach((item) -> {
            try {
                saveProtobufToDisk(dstPrefix + i.getAndIncrement() + ".bin", item);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
