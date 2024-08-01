package com.krickert.search.parser;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.VisitHandler;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.multipart.StreamingFileUpload;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller("/parse")
public class MarkdownControllerGenAI {

    @Post(value = "/markdown-file", consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.APPLICATION_JSON)
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<Map<String, Object>> markdownFile(StreamingFileUpload file) {
        try (InputStream inputStream = file.asInputStream();
             Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {

            scanner.useDelimiter("\\A");
            String content = scanner.hasNext() ? scanner.next() : "";

            Parser parser = Parser.builder().build();
            Document document = parser.parse(content);

            AbstractYamlFrontMatterVisitor frontMatterVisitor = new AbstractYamlFrontMatterVisitor();
            frontMatterVisitor.visit(document);
            Map<String, List<String>> metaData = frontMatterVisitor.getData();

            // Prepare the structure for cleaned text
            List<Section> sections = new ArrayList<>();

            // Visit nodes and extract text
            NodeVisitor nodeVisitor = new NodeVisitor(
                    new VisitHandler<>(Paragraph.class,
                            node -> sections.add(new Section("paragraph", node.getChars().toString(), -1))),
                    new VisitHandler<>(Heading.class,
                            node -> sections.add(new Section("heading", node.getChars().toString(), node.getLevel())))
            );
            nodeVisitor.visit(document);

            // Prepare the response
            Map<String, Object> response = new HashMap<>();
            response.put("metadata", metaData);
            response.put("sections", sections);

            return HttpResponse.ok(response); // Response will be serialized to JSON automatically

        } catch (IOException e) {
            return HttpResponse.serverError();
        }
    }
    @Post(value = "/markdown-raw", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public HttpResponse<String> getMarkdownString(@Body StringBodyRequst markdownString) {
        String markdown = markdownString.markdown;
        String parsedMarkdown = parseMarkdown(markdown);
        return HttpResponse.ok(parsedMarkdown);
    }

    private static @NotNull String parseMarkdown(String markdown) {
        Parser parser = Parser.builder().build();
        Document document = parser.parse(markdown);

        AbstractYamlFrontMatterVisitor visitor = new AbstractYamlFrontMatterVisitor();
        visitor.visit(document);

        Map<String, List<String>> data = visitor.getData();
        // Populate metadata

        String parsedMarkdown = document.getChars() + "\nMetaData: " + data.toString();
        return parsedMarkdown;
    }

    @Serdeable
    public record StringBodyRequst(String markdown) {}
}