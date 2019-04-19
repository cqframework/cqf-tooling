package org.opencds.cqf.modelinfo;

import java.io.FileOutputStream;
import java.io.IOException;

import org.opencds.cqf.Operation;

public class StructureDefinitionToModelInfo extends Operation {

    @Override
    public void execute(String[] args) {
        if (args.length > 1) {
            setOutputPath(args[1]);
        }
        else {
            setOutputPath("src/main/resources/org/opencds/cqf/modelinfo/output");
        }



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

}
