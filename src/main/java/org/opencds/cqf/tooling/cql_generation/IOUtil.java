package org.opencds.cqf.tooling.cql_generation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import java.io.BufferedReader;
import java.io.BufferedWriter;

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
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error reading Content file: " + file.getName());
        }
        return content.toString();
    }

    public static void writeToFile(File file, String content) {
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
            writer.flush();
            writer.append(content);
            writer.close();
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
