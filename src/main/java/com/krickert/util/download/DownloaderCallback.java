package com.krickert.util.download;

import bt.torrent.callbacks.FileDownloadCompleteCallback;

public interface DownloaderCallback extends FileDownloadCompleteCallback {
    boolean completed();
}
