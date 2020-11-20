package org.opencds.cqf.individual_tooling.cql_generation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.io.BufferedWriter;

public class IOUtil {
    public static void writeToFile(File file, String content) {
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
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
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
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
}
