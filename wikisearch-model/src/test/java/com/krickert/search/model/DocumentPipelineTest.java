package com.krickert.search.model;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
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
import java.nio.channels.Pipe;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
public class DocumentPipelineTest {

    @Test
    void testRandomDataCreationSpecificData() throws FileNotFoundException {
        Iterator<Object> it = new RandomData(PipelineDocument.getClassSchema(), 1).iterator();
        GenericRecord test = (GenericRecord) it.next();
        System.out.println(test);
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
    @Test
    void testSerializeToDisk() throws IOException {
        DatumWriter<DownloadFileRequest> userDatumWriter = new SpecificDatumWriter<>(DownloadFileRequest.class);
        DataFileWriter<DownloadFileRequest> dataFileWriter = new DataFileWriter<>(userDatumWriter);
        DownloadFileRequest request = DownloadFileRequest.newBuilder().setErrorcheck("error-check").setErrorType(ErrorCheckType.SHA1).setFileName("sample file name").setURL("https://dummy-file.dummy-file.com/").setFileDumpDate(System.currentTimeMillis() + "").build();

        File file = new File("datafilewriter.avro");

        dataFileWriter.create(request.getSchema(),file);
        dataFileWriter.append(request);
        dataFileWriter.close();

        // Deserialize Users from disk
        DatumReader<DownloadFileRequest> userDatumReader = new SpecificDatumReader<>(DownloadFileRequest.class);
        DataFileReader<DownloadFileRequest> dataFileReader = new DataFileReader<>(file, userDatumReader);
        assertEquals(request, dataFileReader.next());

        dataFileReader.close();
        FileUtils.forceDelete(file);
    }



}
