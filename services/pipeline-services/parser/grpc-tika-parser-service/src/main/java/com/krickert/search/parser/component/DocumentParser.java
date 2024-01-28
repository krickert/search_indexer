package com.krickert.search.parser.component;

import com.google.protobuf.ByteString;
import com.krickert.search.parser.tika.ParsedDocument;
import com.krickert.search.parser.tika.ParsedDocumentReply;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.krickert.search.parser.component.TextUtils.cleanUpText;

public class DocumentParser {
    /**
     * Parses a document and returns the parsed information in a document reply.
     *
     * @param content The content of the document to parse.
     * @return A DocumentReply object containing the parsed title and body of the document.
     * @throws IOException if an I/O error occurs while parsing the document.
     * @throws SAXException if a SAX error occurs while parsing the document.
     * @throws TikaException if a Tika error occurs while parsing the document.
     */
    public static ParsedDocumentReply parseDocument(ByteString content) throws IOException, SAXException, TikaException {
        InputStream stream = new ByteArrayInputStream(content.toByteArray());
        TikaConfig config = new TikaConfig();
        AutoDetectParser parser = new AutoDetectParser(config);
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        parser.parse(stream, handler, metadata);
        String title = cleanUpText(metadata.get("dc:title"));
        String body = cleanUpText(handler.toString());
        return ParsedDocumentReply.newBuilder().setDoc(ParsedDocument.newBuilder().setBody(body).setTitle(title).build()).build();
    }
}
