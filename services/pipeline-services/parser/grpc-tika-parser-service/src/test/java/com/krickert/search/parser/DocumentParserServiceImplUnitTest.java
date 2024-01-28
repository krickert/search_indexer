package com.krickert.search.parser;

import com.google.protobuf.ByteString;
import com.krickert.search.parser.component.DocumentParser;
import com.krickert.search.parser.grpc.DocumentParserServiceImpl;
import com.krickert.search.parser.tika.ParsedDocument;
import com.krickert.search.parser.tika.ParsedDocumentReply;
import org.apache.tika.exception.TikaException;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DocumentParserServiceImplUnitTest {

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
        ParsedDocumentReply expectedResponse = ParsedDocumentReply.newBuilder().setDoc(
        ParsedDocument.newBuilder()
                .setTitle("HTML Unit Test")
                .setBody("The content of the document......")
                .build()).build();
        ParsedDocumentReply actualResponse = DocumentParser.parseDocument(byteString);
        assertEquals(expectedResponse, actualResponse);
    }

}