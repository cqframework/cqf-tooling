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

    public Library gatherDataRequirements(org.hl7.elm.r1.Library elmLibraryToProcess, Set<String> expressions){
        elmRequirementsVisitor = new ElmRequirementsVisitor();
        elmRequirementsContext = new ElmRequirementsContext();
        if(null != elmLibraryToProcess) {
            if(expressions == null || expressions.isEmpty()){
                extractRefsFromElmLibrary(elmLibraryToProcess);
            }else{
                expressions.forEach(expression->{
                    extractExpressionFromElmLibrary(elmLibraryToProcess, expression);
                });
            }
            Library moduleLibrary = createLibrary();
            return moduleLibrary;
        }
        return null;
    }

    public void extractRefsFromElmLibrary(org.hl7.elm.r1.Library elmLibrary){
        org.hl7.elm.r1.Library.Statements statements = elmLibrary.getStatements();
//        statements
    }

    public void extractExpressionFromElmLibrary(org.hl7.elm.r1.Library library, String expression){
        ExpressionDef exDef = getExpressionDef(library.getStatements().getDef(), expression);
        if(null != exDef){
            elmRequirementsVisitor.visitExpressionDef(exDef, elmRequirementsContext);
//            Expression exFromDef = exDef.getExpression();
            System.out.println();
        }
    }


    public List<RelatedArtifact> createArtifactsFromContext(){
        List<RelatedArtifact> relatedArtifacts= new ArrayList<>();
        relatedArtifacts.addAll(getRefsFromContext(elmRequirementsContext.getElmRequirements().getCodeRefs()));
        relatedArtifacts.addAll(getRefsFromContext(elmRequirementsContext.getElmRequirements().getConceptRefs()));
        relatedArtifacts.addAll(getRefsFromContext(elmRequirementsContext.getElmRequirements().getParameterRefs()));
        relatedArtifacts.addAll(getRefsFromContext(elmRequirementsContext.getElmRequirements().getValueSetRefs()));
        relatedArtifacts.addAll(getRefsFromContext(elmRequirementsContext.getElmRequirements().getExpressionRefs()));
        relatedArtifacts.addAll(getLibraryRefsFromContext(elmRequirementsContext.getElmRequirements().getLibraryRefs()));

        return relatedArtifacts;
    }

    private List<RelatedArtifact> getLibraryRefsFromContext(Set<String> libraryRefs) {
        List<RelatedArtifact> libraryArtifacts= new ArrayList<>();
        libraryRefs.forEach(libraryRef -> {
            RelatedArtifact libraryArtifact = new RelatedArtifact();
            libraryArtifact.setType(RelatedArtifact.RelatedArtifactType.DEPENDSON);
            libraryArtifact.setResource(libraryRef);
            libraryArtifacts.add(libraryArtifact);
        });

        return libraryArtifacts;
    }

    private Collection<? extends RelatedArtifact> getRefsFromContext(Set<? extends Expression> refs) {
        List<RelatedArtifact> refArtifacts= new ArrayList<>();
        refs.forEach(ref -> {
            RelatedArtifact refArtifact = new RelatedArtifact();
            refArtifact.setType(RelatedArtifact.RelatedArtifactType.DEPENDSON);
//            refArtifact.setResource(codeRef.);
            refArtifacts.add(refArtifact);
        });

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
