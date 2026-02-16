package org.opencds.cqf.tooling.acceleratorkit;

public class StructureDefinitionElementObject extends StructureDefinitionBaseObject{
    protected String elementType;         //ed.getType().get(0).getCode()
    protected String elementDescription;  //ed.getShort()
    protected String constraint;          //ed.getConstraint().forEach(constraint->{constraint.getHuman();});

    public String getElementType(){return elementType;}
    public void setElementType(String newElementType){elementType = newElementType;};
    public String getElementDescription() {return elementDescription;}
    public void setElementDescription(String elementDescription) {this.elementDescription = elementDescription;}
    public String getConstraint(){return constraint;}
    public void setConstraint(String newConstraint){constraint = newConstraint;};

}
