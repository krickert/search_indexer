package com.krickert.search.wiki.dump.file.component;

import com.krickert.search.model.wiki.DownloadFileRequest;
import com.krickert.search.model.wiki.DownloadedFile;
import com.krickert.search.wiki.dump.file.messaging.DownloadedFileProcessingProducer;
import info.bliki.wiki.dump.WikiXMLParser;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.krickert.search.model.util.ProtobufUtils.createKey;

@Singleton
public class WikiDumpFileProcessor {
    private static final Logger log = LoggerFactory.getLogger(WikiDumpFileProcessor.class);
    private final WikiArticleFilter wikiArticleFilter;

    private final WikiDumpFileCounter wikiDumpFileCounter;
    private final ExecutorService executorService;
    DownloadedFileProcessingProducer errorResend;



    @Inject
    public WikiDumpFileProcessor(WikiArticleFilter wikiArticleFilter, WikiDumpFileCounter wikiDumpFileCounter, @Value("${wikipedia.max-file-concurrency}") Integer maxFileConcurrency) {
        this.wikiArticleFilter = wikiArticleFilter;
        this.wikiDumpFileCounter = wikiDumpFileCounter;
        this.executorService = new ThreadPoolExecutor(maxFileConcurrency, maxFileConcurrency, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(1000));;

    }

    public Runnable processRunner(DownloadedFile downloadedFile) {
        return () -> process(downloadedFile);
    }

    public void processJob(DownloadedFile downloadedFile) {
        executorService.submit(processRunner(downloadedFile));
    }
    
    public void process(DownloadedFile request) {
        final WikiXMLParser parser;
        try {
            log.info("****** STARTING DOCUMENT {}", request.getFullFilePath());
            wikiDumpFileCounter.recordFileBegin(request.getFullFilePath());
            parser = new WikiXMLParser(new File(request.getFullFilePath()), wikiArticleFilter);
            parser.parse();
            wikiDumpFileCounter.recordFileEnd(request.getFullFilePath());
            log.info("****** COMPLETED {}th document: [{}] in {} ms.", wikiDumpFileCounter.incrementAndGetNumberOfFilesCompleted(), request.getFullFilePath(), wikiDumpFileCounter.getElapsedTime(request.getFullFilePath()));

        } catch (IOException | SAXException e) {
            log.error("Problem with parsing file {}.  Error returned: {}", request, ExceptionUtils.getStackTrace(e));
            errorResend.sendFileProcessingRequestError(createKey(request), request);
        }
    }
}
