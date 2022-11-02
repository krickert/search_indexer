package com.krickert.search.model;

import com.google.common.collect.Lists;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.avro.util.RandomData;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.platform.commons.util.ReflectionUtils.*;

/**
 * Class made for simple test data to ensure avro serialization and avro tools
 * are working as designed.
 */
public final class SampleAvroData {

    //Link type for tests
    public static Link link1 = Link.newBuilder()
            .setUrl("http://fakeurl.com")
            .setDescription("This is not a real site.").build();

    public static List<Link> listOfLinks = createListOfLinks();

    public static ParsedSiteInfo parsedSiteInfo = ParsedSiteInfo.newBuilder()
            .setBase("sample dummy base")
            .setGenerator("sample dummy generator")
            .setSitename("sample site name")
            .setCharacterCase("sample character case").build();

    public static ParsedWikiArticle parsedWikiArticle = ParsedWikiArticle.newBuilder()
            .setWikiText("fake wiki text markup")
            .setRevisionId("fake revision id")
            .setSiteInfo(parsedSiteInfo)
            .setId("fake id")
            .setText("fake text for document")
            .setTitle("fake title for article")
            .setNamespace("fake namespace")
            .setTimestamp("fake timestamp")
            .setRevisionId("fake revision id")
            .setNamespaceCode(-1)
            .setUrlRefAltText(createListOfLinks())
            .build();

    public static ErrorCheckType sampleErrorCheckType = ErrorCheckType.MD5;

    public static DownloadFileRequest downloadFileRequest = DownloadFileRequest.newBuilder()
            .setURL("https://www.fakeurl.com")
            .setFileName("some_file.tgz")
            .setFileDumpDate("20221002")
            .setErrorcheck("some_md5_sum")
            .setErrorType(sampleErrorCheckType)
            .build();

    public static DownloadedFile downloadedFile = DownloadedFile.newBuilder()
            .setFileName(downloadFileRequest.getFileName())
            .setFileDumpDate(downloadFileRequest.getFileDumpDate())
            .setFullFilePath("/this/path/is/fake/")
            .setErrorcheck(downloadFileRequest.getErrorcheck())
            .setErrorType(downloadFileRequest.getErrorType())
            .build();


    public static PipelineDocument pipelineDocument = PipelineDocument.newBuilder()
            .setBody("fake_body")
            .setId("fake_id")
            .setTitle("fake_title")
            .setDocumentType("fake_document_type")
            .setRevisionId("fake_revision_id")
            .setCreationDate(1585990763)
            .setLastModified(null)
            .setCustom(Map.of(
                    "custom_field1", "custom_value1",
                    "custom_field2", "custom_value2",
                    "custom_field3", "custom_value3",
                    "custom_field4", "custom_value4",
                    "custom_field5", "custom_value5",
                    "custom_field6", "custom_value6",
                    "custom_field7", "custom_value7"))
            .build();
    private static List<Link> createListOfLinks() {
        List<Link> listOfLinks = Lists.newArrayListWithExpectedSize(5);
        Collections.addAll(listOfLinks,
                Link.newBuilder().setDescription("First description link").setUrl("www.example1.com").build(),
                Link.newBuilder().setDescription("Second description link").setUrl("www.example2.com").build(),
                Link.newBuilder().setDescription("Third description link").setUrl("www.example3.com").build(),
                Link.newBuilder().setDescription("Forth description link").setUrl("www.example4.com").build(),
                Link.newBuilder().setDescription("Fifth description link").setUrl("www.example5.com").build());

        return listOfLinks;
    }


    public static <T extends SpecificRecordBase> T specificAvroRecordGenerator(Class<T> avroClassType) {
        try {
            Field field = avroClassType.getDeclaredField("SCHEMA$");
            return specificAvroRecordGenerator((Schema)field.get(null));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends SpecificRecordBase> T specificAvroRecordGenerator(Schema schema) {
            GenericRecord test =
                    (GenericRecord)new RandomData(schema, 1)
                            .iterator().next();
            return (T) SpecificData.get().deepCopy(test.getSchema(), test);
    }
}
