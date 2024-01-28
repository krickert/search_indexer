package com.krickert.search.model.util;

import com.google.protobuf.ListValue;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import com.google.protobuf.Value;
import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.model.wiki.DownloadFileRequest;
import com.krickert.search.model.wiki.DownloadedFile;
import com.krickert.search.model.wiki.WikiArticle;
import com.krickert.search.parser.tika.ParsedDocument;
import org.apache.commons.lang3.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class for working with protobuf messages.
 */
public class ProtobufUtils {

    /**
     * Returns the current timestamp as a Timestamp object.
     *
     * @return the current timestamp
     */
    public static Timestamp now() {
        Instant time = Instant.now();
        return Timestamp.newBuilder().setSeconds(time.getEpochSecond())
                .setNanos(time.getNano()).build();
    }

    /**
     * Creates a Timestamp object from the given epoch seconds.
     *
     * @param epochSeconds the number of seconds since January 1, 1970
     * @return a Timestamp object representing the given epoch seconds
     */
    public static Timestamp stamp(long epochSeconds) {
        return Timestamp.newBuilder().setSeconds(epochSeconds)
                .setNanos(0).build();
    }

    /**
     * Saves a Protobuf message to disk.
     *
     * @param dst  The destination file path.
     * @param item The Protobuf message to be saved.
     * @throws IOException If an I/O error occurs while writing to the file.
     */
    public static <T extends Message> void saveProtobufToDisk(String dst, T item) throws IOException {
        item.writeTo(new FileOutputStream(dst));
    }


    /**
     * Saves a collection of Protocol Buffer messages to disk.
     *
     * @param dstPrefix The prefix of the destination file path.
     * @param items     The collection of Protocol Buffer messages to be saved.
     * @param <T>       The type of Protocol Buffer message.
     * @throws RuntimeException If an I/O error occurs while saving the messages.
     */
    public static <T extends Message> void saveProtocoBufsToDisk(String dstPrefix, Collection<T> items) {
        int leftPad = (String.valueOf(items.size())).length();
        saveProtocoBufsToDisk(dstPrefix, items, leftPad);
    }

    /**
     * Saves a collection of Protocol Buffer messages to disk.
     *
     * @param dstPrefix The prefix of the destination file path.
     * @param items     The collection of Protocol Buffer messages to be saved.
     * @param leftPad   The number of digits used for left padding the index of each saved message in the file name.
     * @param <T>       The type of Protocol Buffer message.
     * @throws RuntimeException If an I/O error occurs while saving the messages.
     */
    public static <T extends Message> void saveProtocoBufsToDisk(String dstPrefix, Collection<T> items, int leftPad) {
        AtomicInteger i = new AtomicInteger();
        items.forEach((item) -> {
            try {
                saveProtobufToDisk(dstPrefix + StringUtils.leftPad(String.valueOf(i.getAndIncrement()), leftPad, "0") + ".bin", item);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Creates a UUID key from a given string identifier.
     *
     * @param id The string identifier.
     * @return The UUID key.
     */
    public static UUID createKey(String id) {
        return UUID.nameUUIDFromBytes(id.getBytes());
    }

    /**
     * Generates a UUID key based on the provided DownloadedFile object.
     *
     * @param downloadedFile The DownloadedFile object to generate the key from.

     * @return The generated UUID key.

     */
    public static UUID createKey(DownloadedFile downloadedFile) {
        return createKey(downloadedFile.getErrorCheck().getErrorCheck());
    }

    /**
     * Generates a UUID key based on the provided DownloadFileRequest object.
     *
     * @param downloadFileRequest The DownloadFileRequest object to generate the key from.
     * @return The generated UUID key.
     */
    public static UUID createKey(DownloadFileRequest downloadFileRequest) {
        return UUID.nameUUIDFromBytes(downloadFileRequest.getErrorCheck().getErrorCheck().getBytes());
    }

    /**
     * Creates a UUID key based on the provided WikiArticle object.
     *
     * @param wikiArticle The WikiArticle object to generate the key from.
     * @return The generated UUID key.
     */
    public static UUID createKey(WikiArticle wikiArticle) {
        return createKey(wikiArticle.getId());
    }

    /**
     * Creates a UUID key from a given PipeDocument object.
     *
     * @param pipeDocument The PipeDocument object to generate the key from.
     * @return The generated UUID key.
     */
    public static UUID createKey(PipeDocument pipeDocument) {
        return createKey(pipeDocument.getId());
    }

    /**
     * Creates a UUID key from a given ParsedDocument object.
     *
     * @param parsedDocument The ParsedDocument object to generate the key from.
     * @return The generated UUID key.
     */
    public static UUID createKey(ParsedDocument parsedDocument) { return  createKey(parsedDocument.getId());}

    /**
     * Creates a ListValue object from a collection of strings.
     *
     * @param collectionToConvert The collection of strings to be converted.
     * @return A ListValue object containing the converted strings.
     */
    public static ListValue createListValueFromCollection(Collection<String> collectionToConvert) {
        ListValue.Builder builder = ListValue.newBuilder();
        collectionToConvert.forEach((obj) -> builder.addValues(Value.newBuilder().setStringValue(obj).build()));
        return builder.build();
    }
}
