package org.opencds.cqf.tooling.visitor;

import org.hl7.elm.r1.*;

import java.util.ArrayList;
import java.util.List;

public class ElmRequirements {

    private List <CodeRef> codeRefs;
    private List <CodeSystemRef> codeSystemRefs;
    private List <ConceptRef> conceptRefs;
    private List <ParameterRef> parameterRefs;
    private List <ValueSetRef> valueSetRefs;
    private List <ExpressionRef> expressionRefs;//    remember contexts     not in datareqs
    private List <FunctionRef> functionRefs;//  A FunctionRef IS an ExpressionRef which IS an Expression   not in datareqs
    private List <String> libraryRefs;
    private List <Retrieve> retrieves; //  pass these to extractDataRequirements(translator  see cqlprocessor

    private List <CodeDef> codeDefs;
    private List <CodeSystemDef> codeSystemDefs;
    private List <ConceptDef> conceptDefs;
    private List <ParameterDef> parameterDefs;
    private List <ValueSetDef> valueSetDefs;
    private List <ExpressionDef> expressionDefs;//    remember contexts     not in datareqs
    private List <FunctionDef> functionDefs;//  A FunctionRef IS an ExpressionRef which IS an Expression   not in datareqs



    public ElmRequirements(){
        codeRefs = new ArrayList<>();
        codeSystemRefs = new ArrayList<>();
        conceptRefs = new ArrayList<>();
        parameterRefs = new ArrayList<>();
        valueSetRefs = new ArrayList<>();
        expressionRefs = new ArrayList<>();
        functionRefs = new ArrayList<>();
        libraryRefs = new ArrayList<>();
        retrieves = new ArrayList<>();

        codeDefs = new ArrayList<>();
        codeSystemDefs = new ArrayList<>();
        conceptDefs = new ArrayList<>();
        parameterDefs = new ArrayList<>();
        valueSetDefs = new ArrayList<>();
        expressionDefs = new ArrayList<>();
        functionDefs = new ArrayList<>();
    }

    public void reportCodeRef(CodeRef codeRef){
        codeRefs.add(codeRef);
    }
    public void reportCodeSystemRef(CodeSystemRef codeSystemRef){
        codeSystemRefs.add(codeSystemRef);
    }
    public void reportConceptRef(ConceptRef conceptRef){
        conceptRefs.add(conceptRef);
    }
    public void reportParameterRef(ParameterRef parameterRef){
        parameterRefs.add(parameterRef);
    }
    public void reportValueSetRef(ValueSetRef valueSetRef){
        valueSetRefs.add(valueSetRef);
    }
    public void reportExpressionRef(ExpressionRef expressionRef){expressionRefs.add(expressionRef);}
    public void reportFunctionRef(FunctionRef functionRef){
        functionRefs.add(functionRef);
    }

    public void reportLibraryRef(Library library){
        libraryRefs.add("Library/" + library.getIdentifier().getId() + "|" + library.getIdentifier().getVersion());
    }

    public void reportRetrieve(Retrieve retrieve){retrieves.add(retrieve);}

    public void reportCodeDef(CodeDef codeDef){
        codeDefs.add(codeDef);
    }
    public void reportCodeSystemDef(CodeSystemDef codeSystemDef){
        codeSystemDefs.add(codeSystemDef);
    }
    public void reportConceptDef(ConceptDef conceptDef){
        conceptDefs.add(conceptDef);
    }
    public void reportParameterDef(ParameterDef parameterDef){
        parameterDefs.add(parameterDef);
    }
    public void reportValueSetDef(ValueSetDef valueSetDef){
        valueSetDefs.add(valueSetDef);
    }
    public void reportExpressionDef(ExpressionDef expressionDef){expressionDefs.add(expressionDef);}
    public void reportFunctionDef(FunctionDef functionDef){
        functionDefs.add(functionDef);
    }

    public List<ValueSetDef> getValueSetDefs() {return valueSetDefs;}
    public List<CodeDef> getCodeDefs(){return codeDefs;}
    public List<CodeSystemDef> getCodeSystemDefs(){return codeSystemDefs;}
    public List<ConceptDef> getConceptDefs(){return conceptDefs;}
    public List<ParameterDef>  getParameterDefs(){return parameterDefs;}
    public List<ExpressionDef> getExpressionDefs(){return expressionDefs;}
    public List<FunctionDef> getFunctionDefs(){return functionDefs;}

    public List<CodeRef> getCodeRefs() {return codeRefs;}
    public void setCodeRefs(List<CodeRef> codeRefs) {this.codeRefs = codeRefs;}
    public List<CodeSystemRef> getCodeSystemRefs() {return codeSystemRefs;}
    public void setCodeSystemRefs(List<CodeSystemRef> codeSystemRefs) {this.codeSystemRefs = codeSystemRefs;}
    public List<ConceptRef> getConceptRefs() {return conceptRefs;}
    public void setConceptRefs(List<ConceptRef> conceptRefs) {this.conceptRefs = conceptRefs;}
    public List<ParameterRef> getParameterRefs() {return parameterRefs;}
    public void setParameterRefs(List<ParameterRef> parameterRefs) {this.parameterRefs = parameterRefs;}
    public List<ValueSetRef> getValueSetRefs() {return valueSetRefs;}
    public void setValueListRefs(List<ValueSetRef> valueSetRefs) {this.valueSetRefs = valueSetRefs;}
    public List<ExpressionRef> getExpressionRefs() {return expressionRefs;}
    public void setExpressionRefs(List<ExpressionRef> expressionRefs) {this.expressionRefs = expressionRefs;}
    public List<FunctionRef> getFunctionRefs() {return functionRefs;}
    public void setFunctionRefs(List<FunctionRef> functionRefs) {this.functionRefs = functionRefs;}
    public List<String> getLibraryRefs() {return libraryRefs;}
    public void setLibraryRefs(List<String> libraries) {this.libraryRefs = libraries;}
    public List<Retrieve> getRetrieves() {return retrieves;}
    public void setRetrieves(List<Retrieve> retrieves) {this.retrieves = retrieves;}
}
