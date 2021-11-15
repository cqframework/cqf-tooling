package org.opencds.cqf.tooling.terminology;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import ca.uhn.fhir.context.FhirContext;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.r5.model.RelatedArtifact;
import org.opencds.cqf.tooling.processor.ValueSetsProcessor;
import org.opencds.cqf.tooling.utilities.*;
import org.hl7.fhir.r4.model.ValueSet;

public class Copyrights {
    private String name;
    private String title;
    private String description;
    private String useContext;
    private List<CodesystemInfo> codesystems;

    /*
        This class imports a file for codesystem copyright information in this example format:
        {
            "name": "codesystemCopyrights",
            "title": "Codesystem Copyrights",
            "description": "Copyright information for codesystems utilized by resources",
            "useContext": "This is how the codesystems are used in compliance with their creators",
            "codesystems": [
                {
                    "name": "snomed",
                    "title": "SNOMED International",
                    "systemUrl": "http://snomed.info/sct",
                    "copyrightText": "UMLS Metathesaurus® Source Vocabularies and SNOMED CT®"
                }
                ...
            ]
        }
     */

    public Copyrights(){
        try {
            String path = IOUtils.getCopyrightsPath();
            String copyrightJsonString = FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8);

            JsonObject copyrightObj = JsonParser.parseString(copyrightJsonString).getAsJsonObject();
            this.name = copyrightObj.get("name").getAsString();
            this.title = copyrightObj.get("title").getAsString();
            this.description = copyrightObj.get("description").getAsString();
            this.useContext = copyrightObj.get("useContext").getAsString();

            JsonArray codesystemsArray = copyrightObj.getAsJsonArray("codesystems");
            ArrayList<CodesystemInfo> codesystemsList = new ArrayList<>();
            for (JsonElement codesystem : codesystemsArray) {
                JsonObject codesystemObj = codesystem.getAsJsonObject();
                String name = codesystemObj.get("name").getAsString();
                String title = codesystemObj.get("title").getAsString();
                String systemUrl = codesystemObj.get("systemUrl").getAsString();
                String copyrightText = codesystemObj.get("copyrightText").getAsString();
                codesystemsList.add(new CodesystemInfo(name, title, systemUrl, copyrightText));
            }
            this.codesystems = codesystemsList;
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public String getName(){
        return this.name;
    }

    public String getTitle(){
        return this.title;
    }

    public String getDescription(){
        return this.description;
    }

    public String getUseContext(){
        return this.useContext;
    }

    private String alphoraCopyright(){
        CodesystemInfo alphora = this.codesystems.stream()
                .filter(c -> c.getName().equals("alphora"))
                .findFirst()
                .orElse(null);
        if (alphora != null) {
            return alphora.getCopyrightText();
        } else {
            return null;
        }
    }

    public String getCopyrightsText(ValueSet valueSet){
        ArrayList<String> copyrightsTextList = new ArrayList<>();

        copyrightsTextList.add(alphoraCopyright());

        if (valueSet.hasCopyright()) {
            valueSet.setCopyright("");
        }

        ArrayList<String> valueSetSystemUrls = new ArrayList<>();
        if (valueSet.getExpansion().hasContains()){
            for (ValueSet.ValueSetExpansionContainsComponent concept : valueSet.getExpansion().getContains()){
                if (!valueSetSystemUrls.contains(concept.getSystem())){
                    valueSetSystemUrls.add(concept.getSystem());
                }
            }
        }

        if (valueSet.getCompose().hasInclude()){
            for (ValueSet.ConceptSetComponent concept : valueSet.getCompose().getInclude()){
                if(!valueSetSystemUrls.contains((concept.getSystem()))){
                    valueSetSystemUrls.add(concept.getSystem());
                }
            }
        }

        for (String url : valueSetSystemUrls){
            CodesystemInfo codesystem = this.codesystems.stream()
                    .filter(c -> url.equals(c.getSystemUrl()))
                    .findFirst()
                    .orElse(null);
            if (codesystem != null){
                if (!copyrightsTextList.contains(codesystem.getCopyrightText())) {
                    copyrightsTextList.add(codesystem.getCopyrightText());
                }
            } else {
                LogUtils.info("No copyright info for system: " + url + " from ValueSet: " + valueSet.getId());
            }
        }
        return String.join(", ", copyrightsTextList);
    }

    public String getCopyrightsText(List<RelatedArtifact> relatedArtifacts){
        ValueSetsProcessor processor = new ValueSetsProcessor();
        HashSet<String> filePaths = IOUtils.getTerminologyPaths(FhirContext.forR4());
        Map<String, String> fileMap = new HashMap<>();
        List<ValueSet> valueSets = new ArrayList<>();

        List<RelatedArtifact> valueSetArtifacts = relatedArtifacts.stream()
                .filter(artifact -> artifact.getResource().contains("/ValueSet/"))
                .collect(Collectors.toList());

        for (RelatedArtifact artifact : valueSetArtifacts){
            List<String> splitResource = Arrays.asList(artifact.getResource().split("/"));
            String id = splitResource.get(splitResource.size() -1);
            for (String path : filePaths){
                if (path.contains(id)){
                    processor.loadValueSet(fileMap, valueSets, new File(path));
                }
            }
        }

        List<String> copyrightsTextList = new ArrayList<>();
        for (ValueSet valueSet : valueSets){
            String[] splitCopyrightText = valueSet.getCopyright().split(", ");
            for (String copyrightText : splitCopyrightText){
                if (!copyrightsTextList.contains(copyrightText)){
                    copyrightsTextList.add(copyrightText);
                }
            }
        }

        return String.join(", ", copyrightsTextList);
    }

    public static class CodesystemInfo {
        private final String name;
        private final String title;
        private final String systemUrl;
        private final String copyrightText;

        public CodesystemInfo(String name, String title, String systemUrl, String copyrightText){
            this.name = name;
            this.title = title;
            this.systemUrl = systemUrl;
            this.copyrightText = copyrightText;
        }

        public String getName(){
            return this.name;
        }

        public String getTitle(){
            return this.title;
        }

        public String getSystemUrl(){
            return this.systemUrl;
        }

        public String getCopyrightText(){
            return this.copyrightText;
        }
    }
}
