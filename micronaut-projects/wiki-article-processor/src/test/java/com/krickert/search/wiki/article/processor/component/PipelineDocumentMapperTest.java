package com.krickert.search.wiki.article.processor.component;

import com.google.protobuf.Timestamp;
import com.krickert.search.model.util.ProtobufUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

class PipelineDocumentMapperTest {
    private static final Logger log = LoggerFactory.getLogger(PipelineDocumentMapperTest.class);
    PipelineDocumentMapper mock = new PipelineDocumentMapper();
    @Test
    void testDateConversion() {
        Timestamp now = Timestamp.newBuilder().setSeconds(1668046556).setNanos(204003000).build();
        String date = mock.parseDateParsed(now);
        log.info("Date answer: {} seconds: {} nanos: {}", date, now.getSeconds(), now.getNanos());
        assertEquals("2022-11-10T02:15:56.204003Z", date);
    }

}