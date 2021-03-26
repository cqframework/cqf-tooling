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
        List<RelatedArtifact> relatedArtifacts= new ArrayList<>();
        relatedArtifacts.addAll(getCodeDefsFromContext());
//        relatedArtifacts.addAll(getRefsFromContext(elmRequirementsContext.getElmRequirements().getCodeRefs()));
        relatedArtifacts.addAll(getCodeSystemRefsFromContext());
// Need an example:
//        relatedArtifacts.addAll(getRefsFromContext(elmRequirementsContext.getElmRequirements().getConceptRefs()));
        relatedArtifacts.addAll(getParameterRefsFromContext());
        relatedArtifacts.addAll(getValueSetRefsFromContext());
        relatedArtifacts.addAll(getExpressionRefsFromContext());
//        relatedArtifacts.addAll(getFunctionRefsFromContext(elmRequirementsContext.getElmRequirements().getFunctionRefs()));
//      No reason to do this (??) FunctionRefs are handled in ExpressionRefs
        relatedArtifacts.addAll(getLibraryRefsFromContext(elmRequirementsContext.getElmRequirements().getLibraryRefs()));
        return relatedArtifacts;
    }

    private Collection<? extends RelatedArtifact> getCodeDefsFromContext() {
        List<RelatedArtifact> refArtifacts= new ArrayList<>();
        List<CodeDef> codeDefs = new ArrayList<>(elmRequirementsContext.getElmRequirements().getCodeDefs());
        codeDefs.forEach(codeDef -> {
            RelatedArtifact refArtifact = new RelatedArtifact();
            refArtifact.setType(RelatedArtifact.RelatedArtifactType.DEPENDSON);
            refArtifact.setDisplay("CodeDef");
            refArtifact.setResource(codeDef.getCodeSystem().getName() + "." + codeDef.getName());
            if(!checkIfArtifactAdded(refArtifacts, refArtifact.getResource())){
                refArtifacts.add(refArtifact);
            }
        });
        return refArtifacts;
    }

    private Collection<? extends RelatedArtifact> getExpressionRefsFromContext() {
        List<RelatedArtifact> refArtifacts= new ArrayList<>();
        List<ExpressionRef> expressionRefs = new ArrayList<>(elmRequirementsContext.getElmRequirements().getExpressionRefs());
        expressionRefs.forEach(expressionRef -> {
            RelatedArtifact refArtifact = new RelatedArtifact();
            refArtifact.setType(RelatedArtifact.RelatedArtifactType.DEPENDSON);
            refArtifact.setDisplay("ExpressionRef");
            if(expressionRef instanceof FunctionRef){
                refArtifact.setDisplay("FunctionRef");
            }
            String libraryName = (expressionRef).getLibraryName();
            String expressionName = (expressionRef).getName();
            String referenceToExpression = libraryName + "." + expressionName;;
            if(null == libraryName || libraryName.isEmpty()){
                referenceToExpression = processingLibraryName + "." + expressionName;
            }
            refArtifact.setResource(referenceToExpression);
            if(!checkIfArtifactAdded(refArtifacts, referenceToExpression)){
                refArtifacts.add(refArtifact);
            }
        });
        return refArtifacts;
    }

    private Collection<? extends RelatedArtifact> getParameterRefsFromContext() {
        List<RelatedArtifact> refArtifacts= new ArrayList<>();
        List<ParameterRef> parameterRefs = new ArrayList<>(elmRequirementsContext.getElmRequirements().getParameterRefs());
        parameterRefs.forEach(parameterRef -> {
            RelatedArtifact refArtifact = new RelatedArtifact();
            refArtifact.setType(RelatedArtifact.RelatedArtifactType.DEPENDSON);
            refArtifact.setDisplay("ParameterRef");
            refArtifact.setResource(parameterRef.getName());
            if(!checkIfArtifactAdded(refArtifacts, parameterRef.getName())){
                refArtifacts.add(refArtifact);
            }
        });
        return refArtifacts;
    }

    private boolean checkIfArtifactAdded(List<RelatedArtifact> refArtifacts, String toCompare){
        boolean wasAdded = false;
        for(RelatedArtifact refAdded : refArtifacts){
            if(refAdded.getResource().equalsIgnoreCase(toCompare)){
                wasAdded = true;
                break;
            }
        }
        return wasAdded;
    }

    private CodeSystemDef findCodeSystemDef(String name, List<CodeSystemDef> codeSystemDefs) {
        return codeSystemDefs.stream().filter(codeSystemDef -> codeSystemDef
                .getName()
                .equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private List<RelatedArtifact> getCodeSystemRefsFromContext() {
        List<RelatedArtifact> refArtifacts= new ArrayList<>();
        List<CodeSystemRef> codeSystemRefs = new ArrayList<>(elmRequirementsContext.getElmRequirements().getCodeSystemRefs());
        List<CodeSystemDef> codeSystemDefs = new ArrayList<>(elmRequirementsContext.getElmRequirements().getCodeSystemDefs());
        if(null != codeSystemRefs && !codeSystemRefs.isEmpty()
                && null != codeSystemDefs && !codeSystemDefs.isEmpty()) {
            codeSystemRefs.forEach(codeSystemRef -> {
                CodeSystemDef codeSystemDef = findCodeSystemDef(codeSystemRef.getName(), codeSystemDefs);
                if(null != codeSystemDef && null != codeSystemDef.getId() && !codeSystemDef.getId().isEmpty()){
                    RelatedArtifact refArtifact = new RelatedArtifact();
                    refArtifact.setType(RelatedArtifact.RelatedArtifactType.DEPENDSON);
                    refArtifact.setDisplay(codeSystemDef.getName());
                    refArtifact.setResource(codeSystemDef.getId());
                    if(!checkIfArtifactAdded(refArtifacts, codeSystemDef.getId())){
                        refArtifacts.add(refArtifact);
                    }
                }
            });
        }
        return refArtifacts;
    }

    private List<DataRequirement> getRetrievesFromContext() {
        List<DataRequirement> dataRequirementList = new ArrayList<>();

        List<Retrieve> retrieves = elmRequirementsContext.getElmRequirements().getRetrieves();
        if(null != retrieves && !retrieves.isEmpty()){
            retrieves.forEach(retrieve -> {
                DataRequirement dataRequirement = new DataRequirement();
                dataRequirement.setType(Enumerations.FHIRAllTypes.valueOf(retrieve.getDataType().getLocalPart().toUpperCase()));
                boolean retrieveAddedAlready = false;
                for(DataRequirement dataRequirementAdded : dataRequirementList){
                    if(dataRequirementAdded.getType() == dataRequirement.getType()){
                        retrieveAddedAlready = true;
                        break;
                    }
                }
                if(!retrieveAddedAlready){
                    dataRequirementList.add(dataRequirement);
                }
            });
        }
        return dataRequirementList;
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
                    if(!checkIfArtifactAdded(refArtifacts, valueSetDef.getId())){
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
                if(!checkIfArtifactAdded(refArtifacts, referenceToFunction)){
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
        returnLibrary.setRelatedArtifact(createArtifactsFromContext());
        List<DataRequirement> dataRequirements = getRetrievesFromContext();
        dataRequirements.forEach(dataRequirement -> {
            returnLibrary.addDataRequirement(dataRequirement);
        });

        return returnLibrary;
    }
}
