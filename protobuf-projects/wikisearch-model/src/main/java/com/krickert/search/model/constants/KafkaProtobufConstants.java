package com.krickert.search.model.constants;


/**
 * This class provides constants related to Kafka Protobuf integration.
 */
public class KafkaProtobufConstants {
    /**
     * This constant represents the property key for a specific protobuf value type.
     * The value associated with this key is used in various operations related to
     * protobuf serialization and deserialization.
     */
    public static final String SPECIFIC_CLASS_PROPERTY = "specific.protobuf.value.type";
    /**
     * The fully qualified class name for the DownloadFileRequest class in the Wiki module.
     */
    public static final String DOWNLOAD_FILE_REQUEST_CLASS = "com.krickert.search.model.wiki.DownloadFileRequest";
    /**
     * The fully qualified class name for the downloaded file object in the wiki package.
     * It is used as a constant in the KafkaProtobufConstants class.
     */
    public static final String DOWNLOADED_FILE_CLASS = "com.krickert.search.model.wiki.DownloadedFile";
    /**
     * The constant WIKIARTICLE_CLASS represents the fully qualified class name of the WikiArticle model class.
     */
    public static final String WIKIARTICLE_CLASS = "com.krickert.search.model.wiki.WikiArticle";
    /**
     * The fully qualified class name representing the PipeDocument class in the com.krickert.search.model.pipe package.
     * It is used as a constant.
     */
    public static final String PIPE_DOCUMENT_CLASS = "com.krickert.search.model.pipe.PipeDocument";

    public static final String RAW_DOCUMENT_REQUEST = "com.krickert.search.parser.tika.RawDocumentRequest";
}
