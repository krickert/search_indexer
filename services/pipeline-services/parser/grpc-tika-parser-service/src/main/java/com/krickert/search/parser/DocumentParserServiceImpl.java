package com.krickert.search.parser;

import com.google.protobuf.ByteString;
import com.krickert.search.parser.tika.DocumentParserGrpc;
import com.krickert.search.parser.tika.DocumentReply;
import com.krickert.search.parser.tika.DocumentRequest;
import io.micronaut.grpc.annotation.GrpcService;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.krickert.search.parser.TextUtils.cleanUpText;

@GrpcService
public class DocumentParserServiceImpl extends DocumentParserGrpc.DocumentParserImplBase {

    /**
     * Parses a document request and returns the parsed information in a document reply.
     *
     * @param request          The document request to parse.
     * @param responseObserver The stream observer to send the parsed document reply to.
     */
    @Override
    public void parse(DocumentRequest request, io.grpc.stub.StreamObserver<DocumentReply> responseObserver) {
        try {
            DocumentReply reply = parseDocument(request.getContent());
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    static DocumentReply parseDocument(ByteString content) throws IOException, SAXException, TikaException {
        InputStream stream = new ByteArrayInputStream(content.toByteArray());
        TikaConfig config = new TikaConfig();
        AutoDetectParser parser = new AutoDetectParser(config);
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        parser.parse(stream, handler, metadata);
        String title = cleanUpText(metadata.get("dc:title"));
        String body = cleanUpText(handler.toString());
        return DocumentReply.newBuilder()
        .setTitle(title)
        .setBody(body)
        .build();
    }
}