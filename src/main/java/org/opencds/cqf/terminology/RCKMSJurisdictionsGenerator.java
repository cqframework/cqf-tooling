package org.opencds.cqf.terminology;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.apache.poi.ss.usermodel.*;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.CodeSystem;
import org.hl7.fhir.dstu3.model.CodeType;
import org.opencds.cqf.Operation;
import org.apache.commons.text.WordUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class RCKMSJurisdictionsGenerator extends Operation {

    private String pathToSpreadsheet; // -pathtospreadsheet (-pts)
    private String encoding = "json"; // -encoding (-e)
    private String phaState;

    // Code sheet defaults
    private int codeSheetNum = 0; // -codesheetnum (-csn)
    private int codeListRow = 1; // -codelistrow (-clr)
    private int idCol = 0; // -idcol (-ic)
    private int nameCol = 1; // -namecol (-nc)
    private int typeCol = 2; // -typecol (-tc)
    private int stateCol = 3; // -statecol (-sc)
    private int postalcodeCol = 4; // -postalcodecol (-pc)

    @Override
    public void execute(String[] args) {
        setOutputPath("src/main/resources/org/opencds/cqf/terminology/output"); // default

        for (String arg : args) {
            if (arg.equals("-JurisdictionsXlsxToCodeSystem")) continue;
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "outputpath": case "op": setOutputPath(value); break; // -outputpath (-op)
                case "pathtospreadsheet": case "pts": pathToSpreadsheet = value; break;
                case "encoding": case "e": encoding = value.toLowerCase(); break;
                case "codesheetnum": case "csn": codeSheetNum = Integer.valueOf(value); break;
                case "codelistrow": case "clr": codeListRow = Integer.valueOf(value); break;
                case "idcol": case "ic": idCol = Integer.valueOf(value); break;
                case "namecol": case "nc": nameCol = Integer.valueOf(value); break;
                case "typecol": case "tc": typeCol = Integer.valueOf(value); break;
                case "statecol": case "sc": stateCol = Integer.valueOf(value); break;
                case "postalcodeCol": case "pc": postalcodeCol = Integer.valueOf(value); break;
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }

        if (pathToSpreadsheet == null) {
            throw new IllegalArgumentException("The path to the spreadsheet is required");
        }

        Workbook workbook = SpreadsheetHelper.getWorkbook(pathToSpreadsheet);
        CodeSystem cs = new CodeSystem();
        writeCodeSystem(cs, workbook);
        writeCodeSystemToFile(cs, encoding);
    }


    private void writeCodeSystemToFile(CodeSystem cs, String encoding) {
        String fileName = ("CodeSystem-ersd-jurisdictions").concat("." + encoding);
        IParser parser =
                encoding == null
                        ? FhirContext.forDstu3().newJsonParser()
                        : encoding.toLowerCase().startsWith("j")
                                ? FhirContext.forDstu3().newJsonParser()
                                : FhirContext.forDstu3().newXmlParser();
        try (FileOutputStream writer = new FileOutputStream(getOutputPath() + "/" + fileName)) {
            writer.write(parser.setPrettyPrint(true).encodeResourceToString(cs).getBytes());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error writing ValueSet to file: " + e.getMessage());
        }
    }

    public void writeCodeSystem(CodeSystem cs, Workbook workbook) {
        cs.setId("ersd-jurisdictions");
        cs.setUrl("http://hl7.org/fhir/us/ecr/CodeSystem/ersd-jurisdictions");
        cs.setVersion("0.1.0");
        cs.setName("ERSDJurisdictions");
        cs.setTitle("eRSD Jurisdictions");
        cs.setStatus(Enumerations.PublicationStatus.ACTIVE);
        cs.setDescription("This code system describes jurisdictions that require public health reporting.");
        cs.setContent(CodeSystem.CodeSystemContentMode.COMPLETE);
        CodeSystem.PropertyComponent propertyType = new CodeSystem.PropertyComponent();
            propertyType.setCode("type");
            propertyType.setUri("http://hl7.org/fhir/us/ecr/CodeSystem/ersd-jurisdiction-types");
            propertyType.setDescription("Type of public health jurisdiction.");
            propertyType.setType(CodeSystem.PropertyType.CODE);
            cs.addProperty(propertyType);
        CodeSystem.PropertyComponent propertyState = new CodeSystem.PropertyComponent();    
            propertyState.setCode("state");
            propertyState.setUri("http://hl7.org/fhir/us/ecr/CodeSystem/ersd-jurisdiction-states");
            propertyState.setDescription("Public health jurisdiction state or territory.");
            propertyState.setType(CodeSystem.PropertyType.CODE);
            cs.addProperty(propertyState);
        CodeSystem.PropertyComponent propertyPost = new CodeSystem.PropertyComponent();     
            propertyPost.setCode("postalcode");
            propertyPost.setUri("http://hl7.org/fhir/us/ecr/CodeSystem/ersd-jurisdiction-postalcodes");
            propertyPost.setDescription("Postal code within the public health jurisdiction.");
            propertyPost.setType(CodeSystem.PropertyType.CODE);
            cs.addProperty(propertyPost);
        CodeSystem.ConceptDefinitionComponent concept = new CodeSystem.ConceptDefinitionComponent(); 
        CodeSystem.ConceptPropertyComponent conceptPropType;
        CodeSystem.ConceptPropertyComponent conceptPropState;
        CodeSystem.ConceptPropertyComponent conceptProp = new CodeSystem.ConceptPropertyComponent();

        ArrayList<String> phaCodes = new ArrayList<String>();
        Iterator<Row> i = workbook.getSheetAt(codeSheetNum).rowIterator();
        while (i.hasNext()) { 
            Row row = i.next();
            String phaId = SpreadsheetHelper.getCellAsString(row.getCell(idCol));
            String pha_State = SpreadsheetHelper.getCellAsString(row.getCell(stateCol));
            String phaStateArr[] = pha_State.split("_");
            phaState = String.join(" ", phaStateArr);
            phaState = WordUtils.capitalizeFully(phaState);
            String phaZip = SpreadsheetHelper.getCellAsString(row.getCell(postalcodeCol));

            if (row.getRowNum() < codeListRow) {
                continue;
            }

            convertStateToCode();

            if(!phaCodes.contains(phaId)) {
                concept = new CodeSystem.ConceptDefinitionComponent();      
                    concept.setCode(phaId.toUpperCase());
                    concept.setDisplay(SpreadsheetHelper.getCellAsString(row.getCell(stateCol)));
                    concept.setDefinition(SpreadsheetHelper.getCellAsString(row.getCell(nameCol)));
                    cs.addConcept(concept);
               conceptPropType = new CodeSystem.ConceptPropertyComponent();
                    conceptPropType.setCode("type");
                    conceptPropType.setValue(new CodeType(SpreadsheetHelper.getCellAsString(row.getCell(typeCol))));
                    concept.addProperty(conceptPropType);
                conceptPropState = new CodeSystem.ConceptPropertyComponent();
                    conceptPropState.setCode("state");
                    conceptPropState.setValue(new CodeType(phaState));
                    concept.addProperty(conceptPropState);
                if (!phaZip.equals("(null)")){
                conceptProp = new CodeSystem.ConceptPropertyComponent();
                    conceptProp.setCode("postalcode");
                    conceptProp.setValue(new CodeType(phaZip));
                    concept.addProperty(conceptProp);      
                }
                phaCodes.add(phaId);   
            }  else {
                conceptProp = new CodeSystem.ConceptPropertyComponent();
                    conceptProp.setCode("postalcode");
                    conceptProp.setValue(new CodeType(phaZip));
                    concept.addProperty(conceptProp);      
            }                             
        }      
    }

    public void convertStateToCode() {
        switch (phaState) {
            case "Alaska": phaState = "AK"; break;
            case "Alabama": phaState = "AL"; break;
            case "Arkansas": phaState = "AR"; break;
            case "American Samoa": phaState = "AS"; break;
            case "Arizona": phaState = "AZ"; break;
            case "California": phaState = "CA"; break;
            case "Colorado": phaState = "CO"; break;
            case "Connecticut": phaState = "CT"; break;
            case "District Of Columbia": phaState = "DC"; break;
            case "Delaware": phaState = "DE"; break;
            case "Florida": phaState = "FL"; break;
            case "Federated States Of Micronesia": phaState = "FM"; break;
            case "Georgia": phaState = "GA"; break;
            case "Guam": phaState = "GU"; break;
            case "Hawaii": phaState = "HI"; break;
            case "Iowa": phaState = "IA"; break;
            case "Idaho": phaState = "ID"; break;
            case "Illinois": phaState = "IL"; break;
            case "Indiana": phaState = "IN"; break;
            case "Kansas": phaState = "KS"; break;
            case "Kentucky": phaState = "KY"; break;
            case "Louisiana": phaState = "LA"; break;
            case "Massachusetts": phaState = "MA"; break;
            case "Maryland": phaState = "MD"; break;
            case "Maine": phaState = "ME"; break;
            case "Marshall Islands": phaState = "MH"; break;
            case "Michigan": phaState = "MI"; break;
            case "Minnesota": phaState = "MN"; break;
            case "Missouri": phaState = "MO"; break;
            case "Northern Mariana Islands": phaState = "MP"; break;
            case "Mississippi": phaState = "MS"; break;
            case "Montana": phaState = "MT"; break;
            case "North Carolina": phaState = "NC"; break;
            case "North Dakota": phaState = "ND"; break;
            case "Nebraska": phaState = "NE"; break;
            case "New Hampshire": phaState = "NH"; break;
            case "New Jersey": phaState = "NJ"; break;
            case "New Mexico": phaState = "NM"; break;
            case "Nevada": phaState = "NV"; break;
            case "New York": phaState = "NY"; break;
            case "Ohio": phaState = "OH"; break;
            case "Oklahoma": phaState = "OK"; break;
            case "Oregon": phaState = "OR"; break;
            case "Pennsylvania": phaState = "PA"; break;
            case "Puerto Rico": phaState = "PR"; break;
            case "Palau": phaState = "PW"; break;
            case "Rhode Island": phaState = "RI"; break;
            case "South Carolina": phaState = "SC"; break;
            case "South Dakota": phaState = "SD"; break;
            case "Tennessee": phaState = "TN"; break;
            case "Texas": phaState = "TX"; break;
            case "U.S. Minor Outlying Islands": phaState = "UM"; break;
            case "Utah": phaState = "UT"; break;
            case "Virginia": phaState = "VA"; break;
            case "Virgin Islands of the U.S.": phaState = "VI"; break;
            case "Virgin Islands": phaState = "VI"; break;
            case "Vermont": phaState = "VT"; break;
            case "Washington": phaState = "WA"; break;
            case "Wisconsin": phaState = "WI"; break;
            case "West Virginia": phaState = "WV"; break;
            case "Wyoming": phaState = "WY"; break;
            default: throw new IllegalArgumentException("Unknown State: " + phaState);
        }
    }
}
