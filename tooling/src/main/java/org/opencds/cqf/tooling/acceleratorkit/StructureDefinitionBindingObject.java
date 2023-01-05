package org.opencds.cqf.tooling.acceleratorkit;

public class StructureDefinitionBindingObject {

    String sdName;
    String sdURL;
    String sdVersion;
    String elementId;
    String bindingStrength;
    String bindingValueSetURL;
    String bindingValueSetVersion;
    String bindingValueSetName;
    String mustSupport;
    String codeSystemsURLs;
    String cardinality;

    public String getSdName() {return sdName;}
    public void setSdName(String sdName) {this.sdName = sdName;}
    public String getSdURL() {return sdURL;}
    public void setSdURL(String sdURL) {this.sdURL = sdURL;}
    public String getSdVersion() {return sdVersion;}
    public void setSdVersion(String sdVersion) {this.sdVersion = sdVersion;}
    public String getElementId() {return elementId;}
    public void setElementId(String elementId) {this.elementId = elementId;}
    public String getBindingStrength() {return bindingStrength;}
    public void setBindingStrength(String bindingStrength) {this.bindingStrength = bindingStrength;}
    public String getBindingValueSetURL() {return bindingValueSetURL;}
    public void setBindingValueSetURL(String bindingValueSetURL) {this.bindingValueSetURL = bindingValueSetURL;}
    public String getBindingValueSetVersion() {return bindingValueSetVersion;}
    public void setBindingValueSetVersion(String bindingValueSetVersion) {this.bindingValueSetVersion = bindingValueSetVersion;}
    public String getBindingValueSetName() {return bindingValueSetName;}
    public void setBindingValueSetName(String bindingValueSetName) {this.bindingValueSetName = bindingValueSetName;}
    public String getMustSupport() {return mustSupport;}
    public void setMustSupport(String mustSupport) {this.mustSupport = mustSupport;}
    public String getCodeSystemsURLs() {return codeSystemsURLs;}
    public void setCodeSystemsURLs(String codeSystemsURLs) {this.codeSystemsURLs = codeSystemsURLs;}

    public String getCardinality(){
        return cardinality;
    }
    public void setCardinality(String cardinality){
        this.cardinality = cardinality;
    }
}
