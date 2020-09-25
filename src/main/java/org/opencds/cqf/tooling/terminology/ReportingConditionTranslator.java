package org.opencds.cqf.tooling.terminology;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.opencds.cqf.tooling.Operation;
import java.io.File;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ReportingConditionTranslator extends Operation {

    //Set this for quick testing, need to adjust this path to be used with command line comand from Main.java. Don't forget to personalize path if you're trying to test this.
    private static String pathToSource = "C:/../DCG/cqf-tooling/src/main/java/org/opencds/cqf/tooling/terminology/demo-Utah.json"; // -pathtosource (-pts)

    //Specifying command line commands
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

    //Load the demo-UT JSON, pull negative criteria for Chlamydia (incomplete), and make concise JSON with only data we need for CQL.
    public static void main(String[] args) throws IOException {
        byte[] jsonData = Files.readAllBytes(Paths.get(pathToSource));
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonData);
        JsonNode chlamydia = rootNode.get(1); 
        JsonNode neg005 = chlamydia.at("/conditionCriteriaRels").get(20);
        JsonNode neg005Label = neg005.at("/label");
        JsonNode neg005Left = neg005.at("/predicates").get(0).at("/predicateParts").get(0).at("/partAlias");
        JsonNode neg005Right = neg005.at("/predicates").get(0).at("/predicateParts").get(2).at("/predicatePartConcepts").get(0).at("/openCdsConceptDTO/displayName");
        JsonNode neg005LeftType = neg005.at("/predicates").get(1).at("/predicateParts").get(0).at("/partAlias");
        JsonNode neg005RightResult = neg005.at("/predicates").get(1).at("/predicateParts").get(2).at("/predicatePartConcepts").get(0).at("/openCdsConceptDTO/displayName"); 
        JsonNode neg006 = chlamydia.at("/conditionCriteriaRels").get(23);
        JsonNode neg006Label = neg006.at("/label");
        JsonNode neg006Left = neg006.at("/predicates").get(0).at("/predicateParts").get(0).at("/partAlias");
        JsonNode neg006Right = neg006.at("/predicates").get(0).at("/predicateParts").get(2).at("/predicatePartConcepts").get(0).at("/openCdsConceptDTO/displayName");
        JsonNode neg006LeftType = neg006.at("/predicates").get(1).at("/predicateParts").get(0).at("/partAlias");
        JsonNode neg006RightResult = neg006.at("/predicates").get(1).at("/predicateParts").get(2).at("/predicatePartConcepts").get(0).at("/openCdsConceptDTO/displayName"); 
        JsonNode lab = chlamydia.at("/logicSets").get(1).at("/name");
        JsonNode labReporting = chlamydia.at("/logicSets").get(2).at("/name");
        JsonNode jurisdictionCode = chlamydia.at("/responsibleAgencies").get(0).at("/jurisdictionIdentifier");
        String negString = "{\"negativeCriteria\": [{\"label\": " + neg005Label + ", \"predicates\": [{\"case\": " + neg005Left + ", \"operator\": \"=\", \"condition\": " + neg005Right + "}, {\"conjunction\": \"and\"}, {\"case\": " + neg005LeftType + ", \"operator\": \"=\", \"condition\": " + neg005RightResult + "}], \"reporting\": [{\"facility\": [{\"type\": " + lab + "}, {\"type\": " + labReporting + "}]}, {\"jurisdiction\": " + jurisdictionCode + "}]}, {\"label\": " + neg006Label + ", \"predicates\": [{\"case\": " + neg006Left + ", \"operator\": \"=\", \"condition\": " + neg006Right + "}, {\"conjunction\": \"and\"}, {\"case\": " + neg006LeftType + ", \"operator\": \"=\", \"condition\": " + neg006RightResult + "}], \"reporting\": [{\"facility\": [{\"type\": " + lab + "}, {\"type\": " + labReporting + "}]}, {\"jurisdiction\": " + jurisdictionCode + "}]}]}";
        JsonNode negUT = objectMapper.readTree(negString);
        objectMapper.writeValue(new File("updatedCondition.json"), negUT);
    }
}
