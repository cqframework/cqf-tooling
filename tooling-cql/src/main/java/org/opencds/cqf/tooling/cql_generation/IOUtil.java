package org.opencds.cqf.tooling.cql_generation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

// This should be replaced by a FileDal
/**
 * @author Joshua Reynolds
 * @since 2021-02-24
 */
public class IOUtil {

    public static String readFile(String path) {
        File file = new File(path);
        return readFile(file);
    }

    public static String readFile(File file) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading Content file: " + file.getName(), e);
        }
        return content.toString();
    }

    public static void writeToFile(File file, String content) {
        try {
            if (file.exists()) {
                file.delete();
            }
            try (Writer writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writer.write(content);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing file: " + file.getName(), e);
        }
    }

    public static void writeToFile(String filePath, String content) {
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        writeToFile(file, content);
    }
}
