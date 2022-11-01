package com.krickert.search.download.dump.messaging;

import com.krickert.search.download.dump.component.FileDownloader;
import com.krickert.search.model.DownloadFileRequest;
import com.krickert.search.model.DownloadedFile;
import com.krickert.search.model.ErrorCheckType;
import io.micronaut.configuration.kafka.annotation.*;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

@Slf4j
@KafkaListener(threads = 3,
        groupId = "download-request-listener")
@Prototype
public class DownloadDumpFileListener {
    final String downloadLocation;
    final FileDownloader fileDownloader;
    final DownloadedFileProcessingProducer producer;

    @Inject
    public DownloadDumpFileListener(
            @Value("${wikipedia.download-location}")
            final String downloadLocation,
            final FileDownloader fileDownloader,
            final DownloadedFileProcessingProducer producer) {
        this.downloadLocation = downloadLocation;
        this.fileDownloader = fileDownloader;
        this.producer = producer;
    }

    @Topic("download-request")
    public void receive(@KafkaKey UUID uuid,
                        DownloadFileRequest request,
                        long offset,
                        int partition,
                        String topic,
                        long timestamp) {
        log.debug("Got the request {} with UUID {}", request, uuid.toString());
        log.info("this {} was sent {} ago from partition {} from the {} topic at {}",
                request.getURL(), offset, partition, topic, timestamp);

        //now we're going to download the file
        downloadFileToDestination(request);
    }

    private void downloadFileToDestination(DownloadFileRequest request) {
        try {
            String fullFilePath = this.downloadLocation + "/" + request.getFileName();
            fileDownloader.download(
                    new URL(request.getURL()),
                    new File(fullFilePath),
                    request.getErrorcheck());

            log.info("******SENDING TO PROCESS: " + request.getFileName());
            producer.sendFileProcessingRequest(UUID.randomUUID(),
                    DownloadedFile.newBuilder()
                            .setFileName(request.getFileName())
                            .setFullFilePath(fullFilePath)
                            .setErrorType(ErrorCheckType.MD5)
                            .setErrorcheck(request.getErrorcheck())
                            .setFileDumpDate(request.getFileDumpDate())
                            .setServerName("localhost")
                            .build());
        } catch (MalformedURLException e) {
            log.error("message {} was sent ok but did not have a valid URL", request);
        }
    }

}