package com.krickert.search.model.util;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.model.wiki.DownloadFileRequest;
import com.krickert.search.model.wiki.DownloadedFile;
import com.krickert.search.model.wiki.WikiArticle;

import java.io.File;
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

    public static UUID createKey(String id) {
        return UUID.nameUUIDFromBytes(id.getBytes());
    }

    public static UUID createKey(DownloadedFile downloadedFile) {
        return createKey(downloadedFile.getFileName());
    }
    public static UUID createKey(DownloadFileRequest downloadFileRequest) {
        return createKey(downloadFileRequest.getFileName());
    }
    public static UUID createKey(WikiArticle wikiArticle) {
        return createKey(wikiArticle.getId());
    }
    public static UUID createKey(PipeDocument pipeDocument) {
        return createKey(pipeDocument.getId());
    }
}
