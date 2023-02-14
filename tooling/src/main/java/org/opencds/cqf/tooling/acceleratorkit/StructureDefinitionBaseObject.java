package org.opencds.cqf.tooling.acceleratorkit;

public abstract class StructureDefinitionBaseObject {
    protected String sdName;
    protected String sdURL;
    protected String sdVersion;
    protected String elementId;
    protected String mustSupport;
    protected String codeSystemsURLs;
    protected String cardinality;

    public String getSdName() {return sdName;}
    public void setSdName(String sdName) {this.sdName = sdName;}
    public String getSdURL() {return sdURL;}
    public void setSdURL(String sdURL) {this.sdURL = sdURL;}
    public String getSdVersion() {return sdVersion;}
    public void setSdVersion(String sdVersion) {this.sdVersion = sdVersion;}
    public String getElementId() {return elementId;}
    public void setElementId(String elementId) {this.elementId = elementId;}
    public String getMustSupport() {return mustSupport;}
    public void setMustSupport(String mustSupport) {this.mustSupport = mustSupport;}
    public String getCardinality(){
        return cardinality;
    }
    public void setCardinality(String cardinality){
        this.cardinality = cardinality;
    }

}
