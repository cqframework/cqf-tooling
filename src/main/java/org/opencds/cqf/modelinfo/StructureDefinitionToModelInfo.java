package org.opencds.cqf.modelinfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.opencds.cqf.Operation;

import com.google.common.io.Files;
import com.google.gson.*;


public class StructureDefinitionToModelInfo extends Operation {

    @Override
    public void execute(String[] args) {
        if (args.length > 2) {
            setOutputPath(args[2]);
        }
        else {
            setOutputPath("src/main/resources/org/opencds/cqf/modelinfo/output");
        }

        String inputPath = null;
        if (args.length > 1) {
            inputPath = args[1];
        }
        else {
            inputPath = "../FHIR-Spec";
        }

        List<JsonObject> resources = new ArrayList<JsonObject> ();


        System.out.println("Reading 3.0.1 StructureDefinitions");
        resources.addAll(readJsonObjectFromFolder(inputPath + "/3.0.1"));

        System.out.println("Reading US-Core 1.0.1 StructureDefinitions");
        resources.addAll(readJsonObjectFromFolder(inputPath + "/US-Core/1.0.1"));

        System.out.println("Reading QI-Core 2.0.0 StructureDefinitions");
        resources.addAll(readJsonObjectFromFolder(inputPath + "/QI-Core/2.0.0"));

        System.out.println("Reading QI-Core 3.1.0 StructureDefinitions");
        resources.addAll(readJsonObjectFromFolder(inputPath + "/QI-Core/3.1.0"));

        try {
            writeOutput("bubba.txt", "test");
        } catch (IOException e) {
            System.err.println("Encountered the following exception while creating file " + "bubba" + e.getMessage());
            e.printStackTrace();
            return;
        }

    }

    private void writeOutput(String fileName, String content) throws IOException {
        try (FileOutputStream writer = new FileOutputStream(getOutputPath() + "/" + fileName)) {
            writer.write(content.getBytes());
            writer.flush();
        }
    }

    private List<JsonObject> readJsonObjectFromFolder(String path) {
        Collection<File> files = getFiles(path);

        JsonParser parser = new JsonParser();

        List<JsonObject> objects = new ArrayList<JsonObject>();

        for (File f : files) {
            
            try {
                String content = Files.asCharSource(f, Charset.forName("UTF-8")).read();
                JsonElement element = parser.parse(content);

                if (element.isJsonObject()) {
                    objects.addAll(unrollBundles(element.getAsJsonObject()));
                }
            }
            catch(IOException e) {
                
            }
        }

        return objects;
    }

    private Collection<File> getFiles(String path) {
        File folder = new File(path);
        return FileUtils.listFiles(folder, new WildcardFileFilter("*.json"), null);
    }

    private List<JsonObject> unrollBundles(JsonObject object) {
        List<JsonObject> objects = new ArrayList<JsonObject>();
        if (object.get("resourceType").getAsString() == "Bundle") {
            JsonArray arr = object.get("entry").getAsJsonArray();
            for (int i = 0; i < arr.size(); i++) {
                objects.add(arr.get(i).getAsJsonObject().get("resource").getAsJsonObject());
            }

            return objects;
        }
        else
        {
            return Collections.singletonList(object);
        }

    }

}
