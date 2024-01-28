package com.krickert.search.parser.grpc;

import com.krickert.search.parser.tika.DocumentParserGrpc;
import com.krickert.search.parser.tika.ParsedDocumentReply;
import com.krickert.search.parser.tika.RawDocumentRequest;
import io.micronaut.grpc.annotation.GrpcService;

import static com.krickert.search.parser.component.DocumentParser.parseDocument;

@GrpcService
public class DocumentParserServiceImpl extends DocumentParserGrpc.DocumentParserImplBase {

    /**
     * Parses a document request and returns the parsed information in a document reply.
     *
     * @param request          The document request to parse.
     * @param responseObserver The stream observer to send the parsed document reply to.
     */
    @Override
    public void parse(RawDocumentRequest request, io.grpc.stub.StreamObserver<ParsedDocumentReply> responseObserver) {
        try {
            ParsedDocumentReply reply = parseDocument(request.getContent());
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }


}