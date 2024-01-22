package com.krickert.search.parser;

import com.google.protobuf.ByteString;
import com.krickert.search.parser.tika.DocumentRequest;
import com.krickert.search.parser.tika.DocumentReply;
import io.grpc.stub.StreamObserver;
import org.apache.tika.exception.TikaException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DocumentParserServiceImplTest {
    @Test
    void parseTest() throws IOException, SAXException, TikaException {
        // Arrange
        DocumentParserServiceImpl documentParserService = Mockito.spy(DocumentParserServiceImpl.class);
        DocumentRequest mockDocument = DocumentRequest.newBuilder().setContent(ByteString.EMPTY).build();
        StreamObserver<DocumentReply> mockObserver = Mockito.mock(StreamObserver.class);
        DocumentReply expectedDocumentReply = DocumentReply.newBuilder()
                                                .setTitle("")
                                                .setBody("")
                                                .build();
        // Run the method under test and capture argument
        when(documentParserService.parseDocument(mockDocument.getContent())).thenReturn(expectedDocumentReply);
        doNothing().when(mockObserver).onNext(expectedDocumentReply);
        doNothing().when(mockObserver).onCompleted();

        // Act
        documentParserService.parse(mockDocument, mockObserver);
        DocumentReply actualDocumentReply = documentParserService.parseDocument(mockDocument.getContent());

        // Assert
        assertEquals(expectedDocumentReply, actualDocumentReply);
    }
}