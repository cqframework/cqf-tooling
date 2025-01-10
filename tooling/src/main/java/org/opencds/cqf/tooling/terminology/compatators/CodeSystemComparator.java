package org.opencds.cqf.tooling.terminology.compatators;

import org.hl7.fhir.r4.model.CodeSystem;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/*
A class that extends Comparator comparing 2 CodeSystems. Its origination is comparing a CodeSystem from a
    terminology server that is being tested with one from the "Source of Truth" IG.
    It COULD be used with any 2 CodeSystems, but the error reporting verbiage would be off.
 */
public class CodeSystemComparator extends Comparator {
    public void compareCodeSystems(CodeSystem terminologyServerCodeSystem, CodeSystem sourceOfTruthCodeSystem, Map<String, Object> csFailureReport) {
        Set<Map<String, String>> fieldsWithErrors = new HashSet<>();
        if (!terminologyServerCodeSystem.getUrl().equals(sourceOfTruthCodeSystem.getUrl())) {
            Map<String, String> urlFailure = new HashMap<>();
            urlFailure.put("URL", "\"" + terminologyServerCodeSystem.getUrl() + "|" + terminologyServerCodeSystem.getVersion() + "\" Does not equal IG URL \"" + sourceOfTruthCodeSystem.getUrl() + "\"" + newLine);
            fieldsWithErrors.add(urlFailure);
        }
        if (!terminologyServerCodeSystem.getVersion().equals(sourceOfTruthCodeSystem.getVersion())) {
            Map<String, String> versionFailure = new HashMap<>();
            versionFailure.put("Version", "\"" + terminologyServerCodeSystem.getVersion() + "\" Does not equal IG Version \"" + sourceOfTruthCodeSystem.getVersion() + "\"" + newLine);
            fieldsWithErrors.add(versionFailure);
        }
        if (!terminologyServerCodeSystem.getStatus().equals(sourceOfTruthCodeSystem.getStatus())) {
            Map<String, String> statusFailure = new HashMap<>();
            statusFailure.put("Status", "\"" + terminologyServerCodeSystem.getStatus() + "\" Does not equal IG Status \"" + sourceOfTruthCodeSystem.getStatus() + "\"" + newLine);
            fieldsWithErrors.add(statusFailure);
        }
        if (!terminologyServerCodeSystem.getExperimental() == sourceOfTruthCodeSystem.getExperimental()) {
            Map<String, String> experimentalFailure = new HashMap<>();
            experimentalFailure.put("Experimental", "\"" + terminologyServerCodeSystem.getExperimental() + "\" Does not equal IG Experimental \"" + sourceOfTruthCodeSystem.getExperimental() + "\"" + newLine);
            fieldsWithErrors.add(experimentalFailure);
        }
        if (!terminologyServerCodeSystem.getName().equals(sourceOfTruthCodeSystem.getName())) {
            Map<String, String> nameFailure = new HashMap<>();
            nameFailure.put("Name", "\"" + terminologyServerCodeSystem.getName() + "\" Does not equal IG Name \"" + sourceOfTruthCodeSystem.getName() + "\"" + newLine);
            fieldsWithErrors.add(nameFailure);
        }
        if (!terminologyServerCodeSystem.getTitle().equals(sourceOfTruthCodeSystem.getTitle())) {
            Map<String, String> titleFailure = new HashMap<>();
            titleFailure.put("Title", "\"" + terminologyServerCodeSystem.getTitle() + "\" Does not match the IG Title \"" + sourceOfTruthCodeSystem.getTitle() + "\"" + newLine);
            fieldsWithErrors.add(titleFailure);
        }
        if (!terminologyServerCodeSystem.getPublisher().equals(sourceOfTruthCodeSystem.getPublisher())) {
            Map<String, String> publisherFailure = new HashMap<>();
            publisherFailure.put("Publisher", "\"" + terminologyServerCodeSystem.getPublisher() + "\" Does not equal IG Publisher \"" + sourceOfTruthCodeSystem.getPublisher() + "\"" + newLine);
            fieldsWithErrors.add(publisherFailure);
        }
        if (!terminologyServerCodeSystem.getContent().equals(sourceOfTruthCodeSystem.getContent())) {
            Map<String, String> contentFailure = new HashMap<>();
            contentFailure.put("Content", "\"" + terminologyServerCodeSystem.getContent() + "\" Does not equal IG Content \"" + sourceOfTruthCodeSystem.getContent() + "\"" + newLine);
            fieldsWithErrors.add(contentFailure);
        }
        if (terminologyServerCodeSystem.getCount() != sourceOfTruthCodeSystem.getCount()) {
            Map<String, String> countFailure = new HashMap<>();
            countFailure.put("Count", "\"" + terminologyServerCodeSystem.getCount() + "\" Does not equal IG Count \"" + sourceOfTruthCodeSystem.getCount() + "\"" + newLine);
            fieldsWithErrors.add(countFailure);
        }
        Map<String, String> conceptErrors = new HashMap<>();
        if (!compareCodeSystemConcepts(terminologyServerCodeSystem.getConcept(), sourceOfTruthCodeSystem.getConcept(), conceptErrors)) {
            fieldsWithErrors.add(conceptErrors);
        }
        compareContacts(fieldsWithErrors, terminologyServerCodeSystem.getContact(), sourceOfTruthCodeSystem.getContact());
        if (!fieldsWithErrors.isEmpty()) {
            csFailureReport.put(terminologyServerCodeSystem.getUrl() + "|" + terminologyServerCodeSystem.getVersion() + " - " + terminologyServerCodeSystem.getName(), fieldsWithErrors);
        }
    }

    private boolean compareCodeSystemConcepts(List<CodeSystem.ConceptDefinitionComponent> terminologyConcepts, List<CodeSystem.ConceptDefinitionComponent> truthConcepts, Map<String, String> conceptErrors) {
        AtomicBoolean conceptsMatch = new AtomicBoolean(true);
        if ((terminologyConcepts != null && truthConcepts != null)/* && (terminologyConcepts.size() == truthConcepts.size())*/) {
            Map<String, CodeSystem.ConceptDefinitionComponent> terminologyConceptsMap = createConceptMap(terminologyConcepts);
            Map<String, CodeSystem.ConceptDefinitionComponent> truthConceptsMap = createConceptMap(truthConcepts);
            conceptErrors.put("Concept", "");
            if (terminologyConcepts.size() != truthConcepts.size()) {
                conceptErrors.put("Size", "The terminology concept (" + terminologyConcepts.size() + ") and the IG concept (" + truthConcepts.size() + ") sizes do not match ." + newLine);
            }
            terminologyConceptsMap.forEach((conceptCode, termConcept) -> {
                boolean falseFound = false;
                if (truthConceptsMap.containsKey(conceptCode)){// && conceptsMatch.get()) {
                    CodeSystem.ConceptDefinitionComponent truthConcept = truthConceptsMap.get(conceptCode);
                    if (termConcept != null && truthConcept != null) {
                        if (!compareStrings(termConcept.getCode().trim(), truthConcept.getCode().trim())) {
                            falseFound = true;
                            conceptErrors.put("Code:", "\t \"" + termConcept.getCode() + "\" does not match the IG code \"" + truthConcept.getCode() + "\"" + newLine);
                        }
                        if (!compareStrings(termConcept.getDisplay().trim(), truthConcept.getDisplay().trim())) {
                            falseFound = true;
                            conceptErrors.put("Display:", "\"" + termConcept.getDisplay() + "\" does not match the IG display \"" + truthConcept.getDisplay() + "\"" + newLine);
                        }
                        if (!compareStrings(termConcept.getDefinition().trim(), truthConcept.getDefinition().trim())) {
                            falseFound = true;
                            conceptErrors.put("Definition", "\"" + termConcept.getDefinition() + "\" does not match the IG definition \"" + truthConcept.getDefinition() + "\"" + newLine);
                        }
                        if (falseFound) {
                            conceptsMatch.set(false);
                        }
                    } else {
                        conceptsMatch.set(false);
                        if (termConcept == null) {
                            conceptErrors.put("Concepts Null", " concept is null and IG concept is not null." + newLine);
                        } else {
                            conceptErrors.put("Concepts Null", " concept is not null and IG concept is null." + newLine);
                        }

                    }
                    int termConceptSize = termConcept.getConcept().size();
                    int truthConceptSize = truthConcept.getConcept().size();
                    if ( termConceptSize> 0 &&  truthConceptSize > 0 ) {
                        conceptsMatch.set(compareCodeSystemConcepts(termConcept.getConcept(), truthConcept.getConcept(), conceptErrors));
                    }else if(termConceptSize > 0){
                        conceptsMatch.set(false);
                        conceptErrors.put("Concept", "The concept with code \"" + conceptCode + "\" from the terminology server has "  + termConceptSize + " additional concept(s), but the IG concept has 0." + newLine);
                    }else if(truthConceptSize > 0){
                        conceptsMatch.set(false);
                        conceptErrors.put("Concept", "The concept with code \"" + conceptCode + "\" from the IG has \"  + truthConceptSize + \" additional concept(s), but the terminology concept has 0." + newLine);
                    }
                } else {
                    conceptsMatch.set(false);
                    conceptErrors.put("Code", "The concept code \"" + conceptCode + "\" from the terminology server does not match the concept code \"" + truthConceptsMap.get(conceptCode) + "\" from the IG." + newLine);
                }
            });
        } else {
            conceptsMatch.set(false);
            if (terminologyConcepts == null) {
                conceptErrors.put("Concepts", "The terminology concept is not present, but the IG contains one." + newLine);
            }
            if (truthConcepts == null) {
                conceptErrors.put("Concepts", "The terminology concept is present, but the IG does not contains one." + newLine);
            }
        }
        return conceptsMatch.get();
    }

    private Map<String, CodeSystem.ConceptDefinitionComponent> createConceptMap(List<CodeSystem.ConceptDefinitionComponent> concepts) {
        Map<String, CodeSystem.ConceptDefinitionComponent> conceptMap = new HashMap<>();
        concepts.forEach(concept -> {
            conceptMap.put(concept.getCode(), concept);
        });
        return conceptMap;
    }

    private boolean compareStrings(String terminologyString, String truthString) {
        if ((terminologyString != null && truthString != null) || (terminologyString == null && truthString == null)) {
            if (terminologyString == null && truthString == null) {
                return true;
            }
            if ((terminologyString != null) && (terminologyString.equals(truthString))) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

}
