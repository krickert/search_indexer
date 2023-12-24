package com.krickert.search.download.request.component;


/**
 * Represents a service used to download a MD5 file from Wikipedia.
 */
public interface DownloadMd5WikiFileService {
    /**
     * Downloads the MD5 file from Wikipedia as a string.
     *
     * @param fileList the name of the file to be downloaded
     * @return the contents of the downloaded file as a string
     */
    String downloadWikiMd5AsString(String fileList);
}
