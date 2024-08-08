package com.krickert.search.download.dump.messaging;

import com.google.protobuf.Timestamp;
import com.krickert.search.download.dump.component.FileDownloader;
import com.krickert.search.model.constants.KafkaProtobufConstants;
import com.krickert.search.model.wiki.DownloadFileRequest;
import com.krickert.search.model.wiki.DownloadedFile;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.UUID;

import static com.krickert.search.model.util.ProtobufUtils.createKey;
import static com.krickert.search.model.util.ProtobufUtils.now;

@KafkaListener(threads = 3,
        groupId = "download-request-listener",
        properties = @Property(name = KafkaProtobufConstants.SPECIFIC_CLASS_PROPERTY,
                value = KafkaProtobufConstants.DOWNLOAD_FILE_REQUEST_CLASS))
@Singleton
public class DownloadDumpFileListener {
    private static final Logger log = LoggerFactory.getLogger(DownloadDumpFileListener.class);

    final String downloadLocation;
    final FileDownloader fileDownloader;
    final DownloadedFileProcessingProducer producer;
    final String hostname;

    @Inject
    public DownloadDumpFileListener(
            @Value("${download.location}") final String downloadLocation,
            final FileDownloader fileDownloader,
            final DownloadedFileProcessingProducer producer)
            throws UnknownHostException {
        this.downloadLocation = downloadLocation;
        this.fileDownloader = fileDownloader;
        this.producer = producer;
        this.hostname = InetAddress.getLocalHost().getHostName();
    }

    @Topic("download-request")
    public void receive(@KafkaKey UUID key,
                        DownloadFileRequest request,
                        long offset,
                        int partition,
                        String topic,
                        long timestamp) {
        log.debug("Got the request {} with key {}", request, key);
        log.info("this {} was sent {} ago from partition {} from the {} topic at {}",
                request.getUrl(), offset, partition, topic, timestamp);

        //now we're going to download the file
        downloadFileToDestination(request);
    }

    private void downloadFileToDestination(DownloadFileRequest request) {
        try {
            String fullFilePath = this.downloadLocation + "/" + request.getFileName();
            Timestamp downloadedStart = now();
            fileDownloader.download(
                    new URL(request.getUrl()),
                    new File(fullFilePath),
                    request.getErrorCheck().getErrorCheck());
            Timestamp downloadedEnd = now();
            log.info("******SENDING TO PROCESS: " + request.getFileName());
            producer.sendFileProcessingRequest(
                    createKey(request),
                    DownloadedFile.newBuilder()
                            .setFileName(request.getFileName())
                            .setFullFilePath(fullFilePath)
                            .setErrorCheck(request.getErrorCheck())
                            .setFileDumpDate(request.getFileDumpDate())
                            .setServerName(hostname)
                            .setDownloadStart(downloadedStart)
                            .setDownloadEnd(downloadedEnd)
                            .build());
        } catch (MalformedURLException e) {
            log.error("message {} was sent ok but did not have a valid URL", request);
        }
    }

}