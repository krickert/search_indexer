package com.krickert.search.indexer.enhancers;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.krickert.search.indexer.FileLoader;
import jakarta.inject.Inject;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;


//@MicronautTest
public class ProtobufToSolrSchemaTest {
    static ProtobufToSolrSchema protobufToSolrSchema = new ProtobufToSolrSchema();

    @Inject
    FileLoader fileLoader;

    //@Test
    public void testStuff() {
        try {
            generateProtoFiles();
            // Load .proto file
           String protoFileContent = fileLoader.loadResource("file_descriptor_set.pb");
            DynamicSchema dynamicSchema = DynamicSchema.parseFrom(protoFileContent.getBytes());

            // Parse into FileDescriptorSet
            DescriptorProtos.FileDescriptorSet descriptorSet = dynamicSchema.getFileDescriptorSet();

            // In a .proto file, you can have multiple message types, we assume you want the first.
            DescriptorProtos.FileDescriptorProto descriptorProto = descriptorSet.getFile(0);

            // Get the descriptor for the specific message type "Person"
            // Replace "Person" with your actual message type
            Descriptors.Descriptor descriptor = findMessageTypeByName(descriptorProto, "Person");

            // Now you can pass this to the function to generate the Solr schema
            // Assuming you have an instance of ProtobufToSolrSchema called protobufToSolrSchema
            ProtobufToSolrSchema protobufToSolrSchema = new ProtobufToSolrSchema();

            Document solrSchemaDocument = convertProtobufToSchemaDefinition(descriptor);

            // Print Solr schema document to console
            printDocument(solrSchemaDocument);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void generateProtoFiles() {
        String outputFilePath = "/Users/krickert/IdeaProjects/search_indexer/services/solr-semantic-importer/src/test/resources/file_descriptor_set.pb";
        String protoFilePath = "/Users/krickert/IdeaProjects/search_indexer/services/solr-semantic-importer/src/test/resources/person.proto";

        ProcessBuilder pb = new ProcessBuilder(
                "/opt/homebrew/bin/protoc",
                "--descriptor_set_out=" + outputFilePath,
                protoFilePath
        );

        // Set directory if proto files are in a specific directory
        //pb.directory(new File("/path/to/proto/files"));

        Process process = null;
        try {
            process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Error occurred while executing protoc. Exit code: " + exitCode);
                // You might want to throw an exception here, handle error,
                // or add more sophisticated error logging if needed.
            } else {
                System.out.println("protoc executed successfully");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

    public static Document convertProtobufToSchemaDefinition(Descriptors.Descriptor descriptor) throws InvalidProtocolBufferException {
        DynamicMessage dynamicMessage = DynamicMessage.getDefaultInstance(descriptor);
        return protobufToSolrSchema.convertProtobufToSchemaDefinition(dynamicMessage);
    }


    private static Descriptors.Descriptor findMessageTypeByName(DescriptorProtos.FileDescriptorProto fileProto, String name) {
        // Create a descriptor from the file descriptor
        Descriptors.FileDescriptor fileDescriptor;
        try {
            fileDescriptor = Descriptors.FileDescriptor.buildFrom(fileProto, new Descriptors.FileDescriptor[0]);
        } catch (Descriptors.DescriptorValidationException e) {
            throw new IllegalArgumentException("Could not create file descriptor.", e);
        }

        // Get the descriptor for the given message type
        return fileDescriptor.findMessageTypeByName(name);
    }

    public static void printDocument(Document doc) {
        TransformerFactory tf = TransformerFactory.newInstance();
        try {
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            System.out.println(writer.toString());
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

}
