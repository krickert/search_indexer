package com.krickert.search.download.request;


import com.krickert.search.download.request.component.DownloadMd5WikiFileService;
import com.krickert.search.download.request.component.WikipediaErrorFileParser;
import com.krickert.search.download.request.messaging.DownloadRequestProducer;
import com.krickert.search.model.wiki.DownloadFileRequest;
import io.micronaut.configuration.picocli.PicocliRunner;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Collection;

import static com.krickert.search.model.util.ProtobufUtils.createKey;

/**
 * This class represents a command that checks the Wikipedia page for downloads
 * and adds new files to the queue for download.
 */
@Command(name = "populate-file-requests",
        description = "checks the wikipedia page for downloads and adds new files to the queue for download.",
        mixinStandardHelpOptions = true, version = "POS")
public class SendFileRequestsCommand implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(SendFileRequestsCommand.class);

    /**
     * The producer variable represents a Kafka producer that is responsible for sending download requests to the topic "download-request".
     */
    final DownloadRequestProducer producer;
    /**
     * The md5FileCheckService variable is of type DownloadMd5WikiFileService and is used to download MD5 checksums for a given file list.
     * It provides the following functionality:
     * 1. Downloads MD5 checksums for a list of files.
     * 2. Parses the file list to extract the filenames and file types.
     * 3. Determines the type of each file and filters out the files of unsupported types.
     * 4. Creates a collection of DownloadFileRequest objects for each valid file in the file list.
     */
    final DownloadMd5WikiFileService md5FileCheckService;
    /**
     * The WikipediaErrorFileParser class is responsible for parsing a file list and extracting the valid files to be downloaded.
     */
    final WikipediaErrorFileParser wikipediaErrorFileParser;

    @Option(names = {"-v", "--verbose"}, description = "Shows some project details")
    boolean verbose;
    @Option(names = {"-f", "--flie-list"}, description = "name of a preconfigured file that's ready-to-use and already downloaded.")
    String fileList;

    /**
     * Sends file requests by checking the Wikipedia page for downloads and adding new files to the download queue.
     *
     * <p>This class is responsible for checking the Wikipedia page for downloads and adding new files to the download queue.
     * It takes a {@link DownloadRequestProducer}, {@link DownloadMd5WikiFileService}, and {@link WikipediaErrorFileParser}
     * as dependencies, which are injected through the constructor using the {@link Inject} annotation.
     * The {@link SendFileRequestsCommand} class implements the {@link Runnable} interface and provides a {@code run} method
     * to execute the functionality.
     */
    @Inject
    public SendFileRequestsCommand(
            final DownloadRequestProducer producer,
            final DownloadMd5WikiFileService md5FileCheckService,
            final WikipediaErrorFileParser wikipediaErrorFileParser) {
        this.producer = producer;
        this.md5FileCheckService = md5FileCheckService;
        this.wikipediaErrorFileParser = wikipediaErrorFileParser;
    }

    /**
     * The main method of the program.
     *
     * @param args The command-line arguments passed to the program.
     */
    public static void main(String[] args) {
        int exitCode = PicocliRunner.execute(SendFileRequestsCommand.class, args);
        System.exit(exitCode);
    }

    /**
     * This method is called to execute the functionality of checking the Wikipedia page for downloads
     * and adding new files to the queue for download.
     */
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