package org.opencds.cqf.tooling.processor;

import org.hl7.elm.r1.*;
import org.hl7.elm.r1.Expression;
import org.hl7.fhir.r5.model.*;
import org.hl7.fhir.r5.model.Library;
import org.opencds.cqf.tooling.visitor.ElmRequirementsContext;
import org.opencds.cqf.tooling.visitor.ElmRequirementsVisitor;

import java.util.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DataRequirementsProcessor{
    ElmRequirementsVisitor elmRequirementsVisitor;
    ElmRequirementsContext elmRequirementsContext;
    String processingLibraryName;

    public Library gatherDataRequirements(org.hl7.elm.r1.Library elmLibraryToProcess, Set<String> expressions){
        if(null != elmLibraryToProcess) {
            processingLibraryName = elmLibraryToProcess.getIdentifier().getId();
            elmRequirementsVisitor = new ElmRequirementsVisitor();
            elmRequirementsContext = new ElmRequirementsContext();
            if (null != elmLibraryToProcess) {
                if (expressions == null || expressions.isEmpty()) {
                    extractReqsFromElmLibrary(elmLibraryToProcess);
                } else {
                    expressions.forEach(expression -> {
                        extractReqsForExpression(elmLibraryToProcess, expression);
                    });
                }
                Library moduleLibrary = createLibrary();
                return moduleLibrary;
            }
        }
        return null;
    }

    public void extractReqsForExpression(org.hl7.elm.r1.Library library, String expression){
        ExpressionDef exDef = getExpressionDef(library.getStatements().getDef(), expression);
        if(null != exDef){
            extractRequirementsFromExpressionDef(exDef);
        }
    }

    public void extractReqsFromElmLibrary(org.hl7.elm.r1.Library elmLibrary){

        elmRequirementsVisitor.visitLibrary(elmLibrary, elmRequirementsContext);
/*
        org.hl7.elm.r1.Library.Statements statements = elmLibrary.getStatements();
        if(null != elmLibrary.getStatements()
                && null != elmLibrary.getStatements().getDef()
                && ! elmLibrary.getStatements().getDef().isEmpty()) {
            elmLibrary.getStatements().getDef().forEach(def -> {
                extractRequirementsFromExpressionDef(def);
            });
        }
*/
    }

    public void extractRequirementsFromExpressionDef(ExpressionDef expDef) {
        if(null != expDef){
            elmRequirementsVisitor.visitExpressionDef(expDef, elmRequirementsContext);
        }
    }

    public List<RelatedArtifact> createArtifactsFromContext(){

        // visit cqlprocessor result.parameters.addAll(extractParameters(translator.toELM(), paramMessages));   ETC
        List<RelatedArtifact> relatedArtifacts= new ArrayList<>();
//        relatedArtifacts.addAll(getRefsFromContext(elmRequirementsContext.getElmRequirements().getCodeRefs()));
//        relatedArtifacts.addAll(getRefsFromContext(elmRequirementsContext.getElmRequirements().getCodeSystemRefs()));
//        relatedArtifacts.addAll(getRefsFromContext(elmRequirementsContext.getElmRequirements().getConceptRefs()));
//        relatedArtifacts.addAll(getRefsFromContext(elmRequirementsContext.getElmRequirements().getParameterRefs()));
        relatedArtifacts.addAll(getValueSetRefsFromContext());
//        relatedArtifacts.addAll(getRefsFromContext(elmRequirementsContext.getElmRequirements().getExpressionRefs()));
//        relatedArtifacts.addAll(getFunctionRefsFromContext(elmRequirementsContext.getElmRequirements().getFunctionRefs()));
        relatedArtifacts.addAll(getLibraryRefsFromContext(elmRequirementsContext.getElmRequirements().getLibraryRefs()));
        relatedArtifacts.addAll(getRetrievesFromContext());

        return relatedArtifacts;
    }

    private Collection<? extends RelatedArtifact> getRetrievesFromContext() {
        List<RelatedArtifact> refArtifacts= new ArrayList<>();
        List<Retrieve> retrieves = elmRequirementsContext.getElmRequirements().getRetrieves();
        if(null != retrieves && !retrieves.isEmpty()){
            retrieves.forEach(retrieve -> {
                RelatedArtifact refArtifact = new RelatedArtifact();
                refArtifact.setType(RelatedArtifact.RelatedArtifactType.DEPENDSON);
                refArtifact.setDisplay("Retrieve");
                refArtifact.setResource(retrieve.getTemplateId());
                boolean artifactAddedAlready = false;
                for(RelatedArtifact refAdded : refArtifacts){
                    if(refAdded.getResource().equalsIgnoreCase(retrieve.getTemplateId())){
                        artifactAddedAlready = true;
                        break;
                    }
                }
                if(!artifactAddedAlready){
                    refArtifacts.add(refArtifact);
                }

            });
        }
        return refArtifacts;
    }

    private Collection<? extends RelatedArtifact> getValueSetRefsFromContext() {
        List<RelatedArtifact> refArtifacts= new ArrayList<>();
        List<ValueSetRef> valueSetRefs = new ArrayList<>(elmRequirementsContext.getElmRequirements().getValueSetRefs());
        List<ValueSetDef> valueSetDefs = new ArrayList<>(elmRequirementsContext.getElmRequirements().getValueSetDefs());
        if(null != valueSetRefs && !valueSetRefs.isEmpty()
                && null != valueSetDefs && !valueSetDefs.isEmpty()) {
            valueSetRefs.forEach(valueSetRef -> {
                ValueSetDef valueSetDef = findValueSetDef(valueSetRef.getName(), valueSetDefs);
                if(null != valueSetDef && null != valueSetDef.getId() && !valueSetDef.getId().isEmpty()){
                    RelatedArtifact refArtifact = new RelatedArtifact();
                    refArtifact.setType(RelatedArtifact.RelatedArtifactType.DEPENDSON);
                    refArtifact.setDisplay(valueSetDef.getName());
                    refArtifact.setResource(valueSetDef.getId());
                    boolean artifactAddedAlready = false;
                    for(RelatedArtifact refAdded : refArtifacts){
                        if(refAdded.getResource().equalsIgnoreCase(valueSetDef.getId())){
                            artifactAddedAlready = true;
                            break;
                        }
                    }
                    if(!artifactAddedAlready){
                        refArtifacts.add(refArtifact);
                    }
                }
            });
        }
        return refArtifacts;
    }

    private ValueSetDef findValueSetDef(String name, List<ValueSetDef> valueSetDefs) {
        return valueSetDefs.stream().filter(valueSetDef -> valueSetDef
                .getName()
                .equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private List<RelatedArtifact> getFunctionRefsFromContext(List<FunctionRef> refs) {
        List<RelatedArtifact> refArtifacts= new ArrayList<>();
        if(null != refs && !refs.isEmpty()) {
            refs.forEach(ref -> {
                String libraryName = (ref).getLibraryName();
                String functionName = (ref).getName();
                String referenceToFunction;
                if(null != libraryName && !libraryName.isEmpty()){
                    referenceToFunction = libraryName + "." + functionName;
                }else{
                    referenceToFunction = processingLibraryName + "." + functionName;
                }
                RelatedArtifact refArtifact = new RelatedArtifact();
                refArtifact.setType(RelatedArtifact.RelatedArtifactType.DEPENDSON);
                refArtifact.setDisplay("FunctionRef");
                refArtifact.setResource(referenceToFunction);
                boolean artifactAddedAlready = false;
                for(RelatedArtifact refAdded : refArtifacts){
                    if(refAdded.getResource().equalsIgnoreCase(referenceToFunction)){
                        artifactAddedAlready = true;
                        break;
                    }
                }
                if(!artifactAddedAlready){
                    refArtifacts.add(refArtifact);
                }
            });
        }
        return refArtifacts;
    }



    private List<RelatedArtifact> getLibraryRefsFromContext(List<String> libraryRefs) {
        List<RelatedArtifact> libraryArtifacts= new ArrayList<>();
        if(null != libraryRefs && !libraryRefs.isEmpty()) {
            libraryRefs.forEach(libraryRef -> {
                RelatedArtifact libraryArtifact = new RelatedArtifact();
                libraryArtifact.setType(RelatedArtifact.RelatedArtifactType.DEPENDSON);
                libraryArtifact.setResource(libraryRef);
                libraryArtifacts.add(libraryArtifact);
            });
        }

        return libraryArtifacts;
    }

    private List<? extends RelatedArtifact> getRefsFromContext(List<? extends Expression> refs) {
        List<RelatedArtifact> refArtifacts= new ArrayList<>();
        if(null != refs && !refs.isEmpty()) {
            refs.forEach(ref -> {
                RelatedArtifact refArtifact = new RelatedArtifact();
                refArtifact.setType(RelatedArtifact.RelatedArtifactType.DEPENDSON);
                //            refArtifact.setResource(codeRef.);
                refArtifacts.add(refArtifact);
            });
        }
        return refArtifacts;
    }

    private ExpressionDef getExpressionDef(List<ExpressionDef> defList, String expName){
        for(ExpressionDef def: defList){
            if(def.getName().equalsIgnoreCase(expName)){
                return def;
            }
        }
        return null;
    }

    private Library createLibrary() {
        Library returnLibrary = new Library();
        returnLibrary.setStatus(Enumerations.PublicationStatus.ACTIVE);
        CodeableConcept libraryType = new CodeableConcept();
        Coding typeCoding = new Coding().setCode("module-definition");
        typeCoding.setSystem("http://terminology.hl7.org/CodeSystem/library-type");
        libraryType.addCoding(typeCoding);
        returnLibrary.setType(libraryType);
        returnLibrary.setDate(new Date());
        returnLibrary.setStatus(Enumerations.PublicationStatus.ACTIVE);
//        returnLibrary.addRelatedArtifact()
        returnLibrary.setRelatedArtifact(createArtifactsFromContext());

        return returnLibrary;
    }
}
