package org.opencds.cqf.tooling.terminology.compatators;

import org.hl7.fhir.r4.model.ValueSet;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/*
A class that extends Comparator comparing 2 ValueSets. Its origination is comparing a ValueSet from a
    terminology server that is being tested with one from the "Source of Truth" IG.
    It COULD be used with any 2 CodeSystems, but the error reporting verbiage would be off.
 */
public class ValuesetComparator extends Comparator {
    public void compareValueSets(ValueSet terminologyServerValueSet, ValueSet sourceOfTruthValueSet, Map<String, Object> vsFailureReport) {
        Set<Map<String, String>> fieldsWithErrors = new HashSet<>();
        if (!terminologyServerValueSet.getUrl()
                .equals(sourceOfTruthValueSet.getUrl())) {
            Map<String, String> urlFailure = new HashMap<>();
            urlFailure.put("URL", terminologyServerValueSet.getUrl() + "|" + terminologyServerValueSet.getVersion() + " Does not equal IG URL " + sourceOfTruthValueSet.getUrl() + newLine);
            fieldsWithErrors.add(urlFailure);
        }
        if (!terminologyServerValueSet.getVersion().equals(sourceOfTruthValueSet.getVersion())) {
            Map<String, String> versionFailure = new HashMap<>();
            versionFailure.put("Version", "\"" + terminologyServerValueSet.getVersion() + "\" Does not equal IG Version \"" + sourceOfTruthValueSet.getVersion() + "\"" + newLine);
            fieldsWithErrors.add(versionFailure);
        }
        if (!terminologyServerValueSet.getStatus().equals(sourceOfTruthValueSet.getStatus())) {
            Map<String, String> statusFailure = new HashMap<>();
            statusFailure.put("Status", "\"" + terminologyServerValueSet.getStatus() + "\" Does not equal IG Status \"" + sourceOfTruthValueSet.getStatus() + "\"" + newLine);
            fieldsWithErrors.add(statusFailure);
        }
        if (!terminologyServerValueSet.getExperimental() == sourceOfTruthValueSet.getExperimental()) {
            Map<String, String> experimentalFailure = new HashMap<>();
            experimentalFailure.put("Experimental", "\"" + terminologyServerValueSet.getExperimental() + "\" Does not equal IG Experimental \"" + sourceOfTruthValueSet.getExperimental() + "\"" + newLine);
            fieldsWithErrors.add(experimentalFailure);
        }
        if (!terminologyServerValueSet.getName().equals(sourceOfTruthValueSet.getName())) {
            Map<String, String> nameFailure = new HashMap<>();
            nameFailure.put("Name", "\"" + terminologyServerValueSet.getName() + "\" Does not equal IG Experimental \"" + sourceOfTruthValueSet.getName() + "\"" + newLine);
            fieldsWithErrors.add(nameFailure);
        }
        if (!terminologyServerValueSet.getTitle().equals(sourceOfTruthValueSet.getTitle())) {
            Map<String, String> titleFailure = new HashMap<>();
            titleFailure.put("Status", "\"" + terminologyServerValueSet.getTitle() + "\" Does not equal IG Experimental \"" + sourceOfTruthValueSet.getTitle() + "\"" + newLine);
            fieldsWithErrors.add(titleFailure);
        }
        if (!terminologyServerValueSet.getPublisher().equals(sourceOfTruthValueSet.getPublisher())) {
            Map<String, String> publisherFailure = new HashMap<>();
            publisherFailure.put("Publisher", "\"" + terminologyServerValueSet.getPublisher() + "\" Does not equal IG Experimental \"" + sourceOfTruthValueSet.getPublisher() + "\"" + newLine);
            fieldsWithErrors.add(publisherFailure);
        }
        compareContacts(fieldsWithErrors, terminologyServerValueSet.getContact(), sourceOfTruthValueSet.getContact());
        if (!compareComposes(terminologyServerValueSet.getCompose(), sourceOfTruthValueSet.getCompose())) {
        }
        if (!fieldsWithErrors.isEmpty()) {
            vsFailureReport.put(terminologyServerValueSet.getUrl() + "|" +  terminologyServerValueSet.getVersion()  + " - " + terminologyServerValueSet.getName(), fieldsWithErrors);
        }
    }

    private boolean compareComposes(ValueSet.ValueSetComposeComponent terminologyServerComposeComponent, ValueSet.ValueSetComposeComponent sourceOfTruthComposeComponent) {
        AtomicBoolean composesMatch = new AtomicBoolean(true);
        List<ValueSet.ConceptSetComponent> terminologyServerIncludes = terminologyServerComposeComponent.getInclude();
        Map<String, Object> terminologyServerIncludesMap = createIncludesMap(terminologyServerIncludes);
        List<ValueSet.ConceptSetComponent> sourceOfTruthIncludes = sourceOfTruthComposeComponent.getInclude();
        Map<String, Object> sourceOfTruthIncludesMap = createIncludesMap(sourceOfTruthIncludes);
        if (!terminologyServerIncludesMap.isEmpty() && !sourceOfTruthIncludesMap.isEmpty()) {
            if (terminologyServerIncludesMap.size() == sourceOfTruthIncludesMap.size()) {
                terminologyServerIncludesMap.forEach((terminologyIncludeKey, terminologyIncludeValue) -> {
                    if (sourceOfTruthIncludesMap.containsKey(terminologyIncludeKey)) {
                        terminologyServerIncludesMap.forEach((terminologyIncludesKey, terminologyIncludesValue) -> {
                            Map<?, ?> terminologyConceptsMap = (HashMap) (terminologyServerIncludesMap.get(terminologyIncludeKey));
                            Map<?, ?> truthConceptsMap = (HashMap) (sourceOfTruthIncludesMap.get(terminologyIncludeKey));
                            if (!terminologyConceptsMap.isEmpty() && !truthConceptsMap.isEmpty() &&
                                    terminologyConceptsMap.size() == truthConceptsMap.size()) {
                                terminologyConceptsMap.forEach((terminologyConceptsKey, terminologyConceptsValue) -> {
                                    if (truthConceptsMap.containsKey(terminologyConceptsKey)) {
                                        String truthConceptsValue = (String) (truthConceptsMap.get(terminologyConceptsKey));
                                        if (!truthConceptsValue.equalsIgnoreCase((String) terminologyConceptsValue)) {
                                            composesMatch.set(false);
                                        }

                                    }
                                });
                            }

                        });
                    }
                });
            }
        }
        return composesMatch.get();
    }

    private Map<String, Object> createIncludesMap(List<ValueSet.ConceptSetComponent> includes) {
        HashMap<String, Object> includesMap = new HashMap<>();
        includes.forEach(include -> {
            Map<String, String> conceptMap = new HashMap<>();
            List<ValueSet.ConceptReferenceComponent> concepts = include.getConcept();
            concepts.forEach(concept -> {
                conceptMap.put(concept.getCode(), concept.getDisplay());
            });
            includesMap.put(include.getSystem(), conceptMap);
        });
        return includesMap;
    }
}
