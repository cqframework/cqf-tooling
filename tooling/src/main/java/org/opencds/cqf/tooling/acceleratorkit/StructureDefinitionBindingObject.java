package org.opencds.cqf.tooling.acceleratorkit;

public class StructureDefinitionBindingObject extends StructureDefinitionBaseObject{

    String bindingStrength;
    String bindingValueSetURL;
    String bindingValueSetVersion;
    String bindingValueSetName;
    String bindingObjectExtension;
    int cardinalityMin;

    public String getBindingStrength() {return bindingStrength;}
    public void setBindingStrength(String bindingStrength) {this.bindingStrength = bindingStrength;}
    public String getBindingValueSetURL() {return bindingValueSetURL;}
    public void setBindingValueSetURL(String bindingValueSetURL) {this.bindingValueSetURL = bindingValueSetURL;}
    public String getBindingValueSetVersion() {return bindingValueSetVersion;}
    public void setBindingValueSetVersion(String bindingValueSetVersion) {this.bindingValueSetVersion = bindingValueSetVersion;}
    public String getBindingValueSetName() {return bindingValueSetName;}
    public void setBindingValueSetName(String bindingValueSetName) {this.bindingValueSetName = bindingValueSetName;}
    public String getCodeSystemsURLs() {return codeSystemsURLs;}
    public void setCodeSystemsURLs(String codeSystemsURLs) {this.codeSystemsURLs = codeSystemsURLs;}
    public String getBindingObjectExtension() {return bindingObjectExtension;}
    public void setBindingObjectExtension(String bindingObjectExtension) {this.bindingObjectExtension = bindingObjectExtension;}
    public int getCardinalityMin(){return cardinalityMin;};
    public void setCardinalityMin(int cardinalityMin){this.cardinalityMin = cardinalityMin;}
}
