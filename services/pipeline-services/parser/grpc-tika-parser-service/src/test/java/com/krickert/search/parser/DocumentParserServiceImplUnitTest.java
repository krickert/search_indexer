package com.krickert.search.parser;

import com.google.protobuf.ByteString;
import com.krickert.search.parser.tika.DocumentReply;
import org.apache.tika.exception.TikaException;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DocumentParserServiceImplUnitTest {

    private final DocumentParserServiceImpl documentParserService = new DocumentParserServiceImpl();

    @Test
    public void testParseDocument() throws IOException, TikaException, SAXException {
        String html = """
                <html>
                <head>
                  <title>HTML Unit Test</title>
                </head>
                <body>

                The content of the document......

                </body>
                </html>""";
        ByteString byteString = ByteString.copyFromUtf8(html);
        DocumentReply expectedResponse = DocumentReply.newBuilder()
                .setTitle("HTML Unit Test")
                .setBody("The content of the document......")
                .build();
        DocumentReply actualResponse = DocumentParserServiceImpl.parseDocument(byteString);
        assertEquals(expectedResponse, actualResponse);
    }

}