package com.krickert.search.model;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.avro.util.RandomData;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
public class AvroModelTests {

    @Test
    void testRandomDataCreationSpecificData() throws FileNotFoundException {
        Iterator<Object> it = new RandomData(PipelineDocument.getClassSchema(), 1).iterator();
        GenericRecord test = (GenericRecord) it.next();
        assertInstanceOf(PipelineDocument.class, SpecificData.get().deepCopy(test.getSchema(), test));
    }

    @Test
    void testGenericAvroGenerator() {
        assertInstanceOf(PipelineDocument.class, SampleAvroData.specificAvroRecordGenerator(PipelineDocument.class));
        assertInstanceOf(ParsedWikiArticle.class, SampleAvroData.specificAvroRecordGenerator(ParsedWikiArticle.class));
    }

    @Test
    void testGenericAvroGeneratorBySchema() {
        assertInstanceOf(PipelineDocument.class, SampleAvroData.specificAvroRecordGenerator(PipelineDocument.getClassSchema()));
        assertInstanceOf(ParsedWikiArticle.class, SampleAvroData.specificAvroRecordGenerator(ParsedWikiArticle.getClassSchema()));
    }

    <T extends SpecificRecordBase> void  testSerializeToDiskHelper(T specificRecordToTest) throws IOException {
        DatumWriter<T> userDatumWriter = new SpecificDatumWriter<>(specificRecordToTest.getSchema());
        DataFileWriter<T> dataFileWriter = new DataFileWriter<>(userDatumWriter);

        File file = new File(specificRecordToTest.getClass().getName());

        dataFileWriter.create(specificRecordToTest.getSchema(),file);
        dataFileWriter.append(specificRecordToTest);
        dataFileWriter.close();

        // Deserialize Users from disk
        DatumReader<T> userDatumReader = new SpecificDatumReader<>(specificRecordToTest.getSchema());
        DataFileReader<T> dataFileReader = new DataFileReader<>(file, userDatumReader);
        assertEquals(specificRecordToTest, dataFileReader.next());

        dataFileReader.close();
        FileUtils.forceDelete(file);
    }

    @Test
    void testSerializeDownloadFileToDisk() throws IOException {
        testSerializeToDiskHelper(SampleAvroData.downloadFileRequest);
    }
    @Test
    void testSerializeDownloadFileRequestToDisk() throws IOException {
        testSerializeToDiskHelper(SampleAvroData.downloadFileRequest);
    }
    @Test
    void testSerializeParsedWikiArticleToDisk() throws IOException {
        testSerializeToDiskHelper(SampleAvroData.parsedWikiArticle);
    }
    @Test
    void testSerializeParsedSiteInfoToDisk() throws IOException {
        testSerializeToDiskHelper(SampleAvroData.parsedSiteInfo);
    }
    @Test
    void testSerializePipelineDocumentToDisk() throws IOException {
        testSerializeToDiskHelper(SampleAvroData.pipelineDocument);
    }
    @Test
    void testSerializeLinkToDisk() throws IOException {
        testSerializeToDiskHelper(SampleAvroData.link1);
    }


}
