package org.opencds.cqf.tooling.terminology;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.opencds.cqf.tooling.Operation;
import java.io.File;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ReportingConditionTranslator extends Operation {

    private static String pathToSource = "C:/Users/marks/Repos/DCG/cqf-tooling/src/main/java/org/opencds/cqf/tooling/terminology/demo-Utah.json"; // -pathtosource (-pts)

    @Override
    public void execute(final String[] args) {
        setOutputPath("src/main/resources/org/opencds/cqf/tooling/terminology/output"); // default

        for (final String arg : args) {
            if (arg.equals("-ReportingConditionTranslator")) continue;
            final String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            final String flag = flagAndValue[0];
            final String value = flagAndValue[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "outputpath": case "op": setOutputPath(value); break; // -outputpath (-op)
                case "pathtosource": case "pts": pathToSource = value; break;
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }

        if (pathToSource == null) {
            throw new IllegalArgumentException("The path to the file is required");
        } 
    } 

    public static void main(String[] args) throws IOException {
        byte[] jsonData = Files.readAllBytes(Paths.get(pathToSource));
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonData);
        JsonNode code = rootNode.at("/cdsCodeDTO/codeId");
        JsonNode condition = rootNode.at("/cdsCodeDTO/displayName");
        objectMapper.writeValue(new File("updatedCondition.json"), code + " = " + condition);
    }
}
