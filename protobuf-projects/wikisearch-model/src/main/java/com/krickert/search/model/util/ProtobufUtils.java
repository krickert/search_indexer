package com.krickert.search.model.util;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.model.wiki.DownloadFileRequest;
import com.krickert.search.model.wiki.DownloadedFile;
import com.krickert.search.model.wiki.WikiArticle;
import org.apache.commons.lang3.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
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
        item.writeTo(new FileOutputStream(dst));
    }

    public static <T extends Message> void saveProtocoBufsToDisk(String dstPrefix, Collection<T> items) {
        int leftPad = ("" + items.size()).length();
        saveProtocoBufsToDisk(dstPrefix, items, leftPad);
    }
    public static <T extends Message> void saveProtocoBufsToDisk(String dstPrefix, Collection<T> items, int leftPad) {
        AtomicInteger i = new AtomicInteger();
        items.forEach((item) -> {
            try {
                saveProtobufToDisk(dstPrefix + StringUtils.leftPad("" + i.getAndIncrement(), leftPad, "0") + ".bin", item);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static UUID createKey(String id) {
        return UUID.nameUUIDFromBytes(id.getBytes());
    }

    public static UUID createKey(DownloadedFile downloadedFile) {
        return createKey(downloadedFile.getErrorCheck().getErrorCheck());
    }
    public static UUID createKey(DownloadFileRequest downloadFileRequest) {
        return UUID.randomUUID();
    }
    public static UUID createKey(WikiArticle wikiArticle) {
        return createKey(wikiArticle.getId());
    }
    public static UUID createKey(PipeDocument pipeDocument) {
        return createKey(pipeDocument.getId());
    }
}
