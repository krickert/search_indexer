package com.krickert.search.wikipedia;

import bt.Bt;
import bt.data.Storage;
import bt.data.file.FileSystemStorage;
import bt.dht.DHTConfig;
import bt.dht.DHTModule;
import bt.metainfo.Torrent;
import bt.metainfo.TorrentFile;
import bt.runtime.BtClient;
import bt.runtime.Config;
import com.google.inject.Module;
import com.krickert.util.download.DownloaderCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class WikipediaArticleBitorrentDownloader {

    final String workspaceLocation;

    final String wikipediaTorrentUrl;
    private final Boolean isDownloadWikiTorrent;
    Logger logger = LoggerFactory.getLogger(WikipediaArticleBitorrentDownloader.class);

    public WikipediaArticleBitorrentDownloader(@Value("${download.wikitorrent}") Boolean isDownloadWikiTorrent,
                                               @Value("${workspace.location}") String workspaceLocation,
                                               @Value("${wikipedia.torrent.url}") String wikipediaTorrentUrl) {
        this.workspaceLocation = workspaceLocation;
        this.wikipediaTorrentUrl = wikipediaTorrentUrl;
        this.isDownloadWikiTorrent = isDownloadWikiTorrent;
    }

    public void downloadWikipediaTorrent() {

        if (!isDownloadWikiTorrent) {
            return;
        }
        //start bittorrent download of wikimedia
        // enable multithreaded verification of torrent data
        Config config = new Config() {
            @Override
            public int getNumOfHashingThreads() {
                return Runtime.getRuntime().availableProcessors() * 2;
            }
        };

        // enable bootstrapping from public routers
        Module dhtModule = new DHTModule(new DHTConfig() {
            @Override
            public boolean shouldUseRouterBootstrap() {
                return true;
            }
        });

        // get download directory
        Path targetDirectory = Paths.get(workspaceLocation);

        // create file system based backend for torrent data
        Storage storage = new FileSystemStorage(targetDirectory);

        DownloaderCallback callback = new DownloaderCallback() {
            final AtomicBoolean completed = new AtomicBoolean(false);

            @Override
            public void fileDownloadCompleted(Torrent torrent, TorrentFile torrentFile, Storage storage) {
                logger.info("The torrent is completed!!!  here is a hook we can do something");
                completed.set(true);
            }

            @Override
            public boolean completed() {
                return this.completed.get();
            }
        };
        // create client with a private runtime
        BtClient client = Bt.client()
                .config(config)
                .storage(storage)
                .magnet(wikipediaTorrentUrl)
                .autoLoadModules()
                .module(dhtModule)
                .stopWhenDownloaded()
                .afterFileDownloaded(callback)
                .build();

        // launch
        logger.info("bittorrent starting");
        client.startAsync();
        int i = 0;
        while (!callback.completed()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            logger.info("Are we there yet? " + ++i);
        }
        logger.info("we are done!!");
        client.stop();
    }

}
