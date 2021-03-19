package org.opencds.cqf.tooling.visitor;

import org.hl7.elm.r1.*;

import java.util.Set;

public class ElmRequirements {

    private Set <CodeRef> codeRefs;
    private Set <ConceptRef> conceptRefs;
    private Set <ParameterRef> parameterRefs;
    private Set <ValueSetRef> valueSetRefs;
    private Set <ExpressionRef> expressionRefs;// remove????
    private Set <FunctionRef> functionRefs;//remove??  A FunctionRef IS an ExpressionRef which IS an Expression
    private Set <String> libraryRefs;

    //collect them

    public void reportCodeRefs(CodeRef codeRef){
        codeRefs.add(codeRef);
    }

    public void reportConceptRefs(ConceptRef conceptRef){
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



    public Set<CodeRef> getCodeRefs() {return codeRefs;}
    public void setCodeRefs(Set<CodeRef> codeRefs) {this.codeRefs = codeRefs;}
    public Set<ConceptRef> getConceptRefs() {return conceptRefs;}
    public void setConceptRefs(Set<ConceptRef> conceptRefs) {this.conceptRefs = conceptRefs;}
    public Set<ParameterRef> getParameterRefs() {return parameterRefs;}
    public void setParameterRefs(Set<ParameterRef> parameterRefs) {this.parameterRefs = parameterRefs;}
    public Set<ValueSetRef> getValueSetRefs() {return valueSetRefs;}
    public void setValueSetRefs(Set<ValueSetRef> valueSetRefs) {this.valueSetRefs = valueSetRefs;}
    public Set<ExpressionRef> getExpressionRefs() {return expressionRefs;}
    public void setExpressionRefs(Set<ExpressionRef> expressionRefs) {this.expressionRefs = expressionRefs;}
    public Set<FunctionRef> getFunctionRefs() {return functionRefs;}
    public void setFunctionRefs(Set<FunctionRef> functionRefs) {this.functionRefs = functionRefs;}
    public Set<String> getLibraryRefs() {return libraryRefs;}
    public void setLibraryRefs(Set<String> libraries) {this.libraryRefs = libraries;}
}
