package com.krickert.search.download.request;


import com.krickert.search.model.DownloadFileRequest;
import io.micronaut.configuration.picocli.PicocliRunner;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import jakarta.inject.Inject;

import java.util.Collection;
import java.util.UUID;

@Slf4j
@Command(name = "populate-file-requests",
        description = "checks the wikipedia page for downloads and adds new files to the queue for download.",
        mixinStandardHelpOptions = true, version = "POS")
public class SendFileRequestsCommand implements Runnable {

    final DownloadRequestProducer producer;
    final DownloadMd5WikiFileService md5FileCheckService;
    @Option(names = {"-v", "--verbose"}, description = "Shows some project details")
    boolean verbose;
    @Option(names = {"-f", "--flie-list"}, description = "sends the requests to the configured broker")
    String fileList;

    @Inject
    public SendFileRequestsCommand(
            final DownloadRequestProducer producer,
            final DownloadMd5WikiFileService md5FileCheckService) {
        this.producer = producer;
        this.md5FileCheckService = md5FileCheckService;
    }

    public static void main(String[] args) {
        int exitCode = PicocliRunner.execute(SendFileRequestsCommand.class, args);
        System.exit(exitCode);
    }

    public void run() {
        String m = md5FileCheckService.downloadWikiMd5AsString(fileList);
        Collection<String[]> fileList = md5FileCheckService.parseFileList(m);
        if (verbose) {
            log.info(m);
            log.info(fileList.size() + " files found");
        }
        Collection<DownloadFileRequest> sendMe = md5FileCheckService.createDownloadRequests(fileList);
        if (verbose) {
            log.debug("here: " + sendMe);
        }
        for(DownloadFileRequest request : sendMe) {
            producer.sendDownloadRequest(UUID.randomUUID(), request);
        }
        System.out.println("Application exiting.");

    }




}