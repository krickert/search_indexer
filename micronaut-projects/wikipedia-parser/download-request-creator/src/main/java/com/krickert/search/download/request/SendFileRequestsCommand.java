package com.krickert.search.download.request;


import com.krickert.search.model.wiki.DownloadFileRequest;
import io.micronaut.configuration.picocli.PicocliRunner;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Collection;

import static com.krickert.search.model.util.ProtobufUtils.createKey;

@Command(name = "populate-file-requests",
        description = "checks the wikipedia page for downloads and adds new files to the queue for download.",
        mixinStandardHelpOptions = true, version = "POS")
public class SendFileRequestsCommand implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(SendFileRequestsCommand.class);

    final DownloadRequestProducer producer;
    final DownloadMd5WikiFileService md5FileCheckService;
    final WikipediaErrorFileParser wikipediaErrorFileParser;

    @Option(names = {"-v", "--verbose"}, description = "Shows some project details")
    boolean verbose;
    @Option(names = {"-f", "--flie-list"}, description = "name of a preconfigured file that's ready-to-use and already downloaded.")
    String fileList;

    @Inject
    public SendFileRequestsCommand(
            final DownloadRequestProducer producer,
            final DownloadMd5WikiFileService md5FileCheckService,
            final WikipediaErrorFileParser wikipediaErrorFileParser) {
        this.producer = producer;
        this.md5FileCheckService = md5FileCheckService;
        this.wikipediaErrorFileParser = wikipediaErrorFileParser;
    }

    public static void main(String[] args) {
        int exitCode = PicocliRunner.execute(SendFileRequestsCommand.class, args);
        System.exit(exitCode);
    }

    public void run() {
        String m = md5FileCheckService.downloadWikiMd5AsString(fileList);
        Collection<String[]> fileList = wikipediaErrorFileParser.parseFileList(m);
        if (verbose) {
            log.info(m);
            log.info(fileList.size() + " files found");
        }
        Collection<DownloadFileRequest> sendMe = wikipediaErrorFileParser.createDownloadRequests(fileList);
        if (verbose) {
            log.debug("here: " + sendMe);
        }

        for (DownloadFileRequest request : sendMe) {
            producer.sendDownloadRequest(createKey(request), request);
        }
        System.out.println("Application exiting.");

    }


}