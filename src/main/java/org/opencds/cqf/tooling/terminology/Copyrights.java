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
import org.apache.commons.io.FilenameUtils;
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
        This class imports a file in a data folder at input/data/copyrights for codesystem copyright information in this example format:
        {
            "name": "codesystemCopyrights",
            "title": "Codesystem Copyrights",
            "description": "Copyright information for codesystems utilized by resources",
            "useContext": "How the codesystems are to be used",
            "codesystems": [
                {
                    "name": "snomed",
                    "title": "SNOMED International",
                    "systemUrls": [ "http://snomed.info/sct" ],
                    "copyrightText": "UMLS Metathesaurus® Source Vocabularies and SNOMED CT®"
                }
                ...
            ]
        }
     */

    public Copyrights(){
        try {
            String path = IOUtils.getCopyrightsPath();
            if (path.equals("")){
                throw new IOException("No copyrights file");
            }
            String copyrightJsonString = FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8);

            JsonObject copyrightObj = JsonParser.parseString(copyrightJsonString).getAsJsonObject();
            this.name = copyrightObj.get("name").getAsString();
            this.title = copyrightObj.get("title").getAsString();
            this.description = copyrightObj.get("description").getAsString();
            this.useContext = copyrightObj.get("useContext").getAsString();

            JsonArray codesystemsArray = copyrightObj.getAsJsonArray("codesystems");
            ArrayList<CodesystemInfo> codesystemsList = new ArrayList<>();
            for (JsonElement codesystem : codesystemsArray) {
                ArrayList<String> systemUrls = new ArrayList<>();
                JsonObject codesystemObj = codesystem.getAsJsonObject();
                String name = codesystemObj.get("name").getAsString();
                String title = codesystemObj.get("title").getAsString();
                JsonArray jsonSystemUrls = codesystemObj.getAsJsonArray("systemUrls");
                for (JsonElement url : jsonSystemUrls) {
                    systemUrls.add(url.getAsString());
                }
                String copyrightText = codesystemObj.get("copyrightText").getAsString();
                codesystemsList.add(new CodesystemInfo(name, title, systemUrls, copyrightText));
            }
            this.codesystems = codesystemsList;
        } catch (IOException e){
            if (!e.getMessage().equals("No copyrights file")) {
                System.out.println(e.getMessage());
            }
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

    public String getCopyrightsText(ValueSet valueSet){
        ArrayList<String> copyrightsTextList = new ArrayList<>();

        valueSet.setCopyright("");

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
                .filter(c -> c.getSystemUrls().contains(url))
                .findFirst()
                .orElse(null);
            if (codesystem != null) {
                if (!copyrightsTextList.contains(codesystem.getCopyrightText())) {
                    copyrightsTextList.add(codesystem.getCopyrightText());
                }
            } else {
                LogUtils.info("No copyright info for system: " + url + " from ValueSet: " + valueSet.getId());
            }
        }
        Collections.sort(copyrightsTextList);

        if (IOUtils.sourceIg.getCopyright() != null){
            copyrightsTextList.add(0, IOUtils.sourceIg.getCopyright());
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
            String id = FilenameUtils.getName(artifact.getResource()) + ".json";
            for (String path : filePaths){
                String fileName = FilenameUtils.getName(path);
                fileName = fileName.replace("valueset-", "");
                if (id.equals(fileName)){
                    processor.loadValueSet(fileMap, valueSets, new File(path));
                    break;
                }
            }
        }

        List<String> copyrightsTextList = new ArrayList<>();

        for (ValueSet valueSet : valueSets){
            if (valueSet.hasCopyright()) {
                String[] splitCopyrightText = valueSet.getCopyright().split(", ");
                for (String copyrightText : splitCopyrightText) {
                    if (!copyrightsTextList.contains(copyrightText)) {
                        copyrightsTextList.add(copyrightText);
                    }
                }
            }
        }
        Collections.sort(copyrightsTextList);

        if (IOUtils.sourceIg.getCopyright() != null) {
            copyrightsTextList.remove(IOUtils.sourceIg.getCopyright());
            copyrightsTextList.add(0, IOUtils.sourceIg.getCopyright());
        }

        return String.join(", ", copyrightsTextList);
    }

    public static class CodesystemInfo {
        private final String name;
        private final String title;
        private final List<String> systemUrl;
        private final String copyrightText;

        public CodesystemInfo(String name, String title, List<String> systemUrl, String copyrightText){
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

        public List<String> getSystemUrls() { return this.systemUrl; }

        public String getCopyrightText(){
            return this.copyrightText;
        }
    }
}
