package org.opencds.cqf.tooling.acceleratorkit;

import org.hl7.fhir.r4.model.Enumerations;

import java.util.List;

/**
 * Created by Bryn on 8/18/2019.
 *
 * Represents a WHO Accelerator Kit Data Dictionary Element
 */
public class DictionaryElement {
    public DictionaryElement(String id, String name) {
        if (id == null || id.equals("")) {
            throw new IllegalArgumentException("id required");
        }
        this.id = id;
        if (name == null || name.equals("")) {
            throw new IllegalArgumentException("name required");
        }
        this.name = name;
    }

    private String page;
    public String getPage() {
        return this.page;
    }
    public void setPage(String page) {
        this.page = page;
    }

    private String group;
    public String getGroup() {
        return this.group;
    }
    public void setGroup(String group) {
        this.group = group;
    }

    private String activity;
    public String getActivity() {
        return this.activity;
    }
    public void setActivity(String activity) {
        this.activity = activity;
    }

    private String label;
    public String getLabel() {
        return this.label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    private String id;
    public String getId() {
        return this.id;
    }

    private String name;
    public String getName() {
        return this.name;
    }

    private String masterDataType;
    public String getMasterDataType() {
        return this.masterDataType;
    }
    public void setMasterDataType(String masterDataType) {
        this.masterDataType = masterDataType;
    }

    private String infoIcon;
    public String getInfoIcon() {
        return this.infoIcon;
    }
    public void setInfoIcon(String infoIcon) {
        this.infoIcon = infoIcon;
    }

    private String due;
    public String getDue() {
        return this.due;
    }
    public void setDue(String due) {
        this.due = due;
    }

    private String relevance;
    public String getRelevance() {
        return this.relevance;
    }
    public void setRelevance(String relevance) {
        this.relevance = relevance;
    }

    private String description;
    public String getDescription() {
        return this.description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    private String dataElementLabel;
    public String getDataElementLabel() {
        return this.dataElementLabel;
    }
    public void setDataElementLabel(String dataElementLabel) {
        this.dataElementLabel = dataElementLabel;
    }

    private String dataElementName;
    public String getDataElementName() {
        return this.dataElementName;
    }
    public void setDataElementName(String dataElementName) {
        this.dataElementName = dataElementName;
    }

    private String notes;
    public String getNotes() {
        return this.notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }

    private String type; // Image, Note, QR Code, Text, Date, Checkbox, Calculation, Integer, MC (select one), MC (select multiple), Toaster message
    public String getType() {
        return this.type;
    }
    public void setType(String type) {
        this.type = type;
    }

    private String calculation;
    public String getCalculation() {
        return this.calculation;
    }
    public void setCalculation(String calculation) {
        this.calculation = calculation;
    }

    private String constraint;
    public String getConstraint() {
        return this.constraint;
    }
    public void setConstraint(String constraint) {
        this.constraint = constraint;
    }

    private String required;
    public String getRequired() {
        return this.required;
    }
    public void setRequired(String required) {
        this.required = required;
    }

    private String editable;
    public String getEditable() {
        return this.editable;
    }
    public void setEditable(String editable) {
        this.editable = editable;
    }

    private String scope;
    public String getScope() {
        return this.scope;
    }
    public void setScope(String scope) {
        this.scope = scope;
    }

    private String context;
    public String getContext() {
        return this.context;
    }
    public void setContext(String context) {
        this.context = context;
    }

    private String selector;
    public String getSelector() {
        return this.selector;
    }
    public void setSelector(String selector) {
        this.selector = selector;
    }

    private CodeCollection primaryCodes;
    public CodeCollection getPrimaryCodes() {
        return this.primaryCodes;
    }
    public void setPrimaryCodes(List<DictionaryCode> primaryCodes) {
        this.primaryCodes = new CodeCollection(primaryCodes);
    }

    private String masterDataElementPath;
    public String getMasterDataElementPath() {
        return this.masterDataElementPath;
    }
    public void setMasterDataElementPath(String masterDataElementPath) { this.masterDataElementPath = masterDataElementPath; }

    private DictionaryFhirElementPath fhirElementPath;
    public DictionaryFhirElementPath getFhirElementPath() {
        return this.fhirElementPath;
    }
    public void setFhirElementPath(DictionaryFhirElementPath fhirElementPath) {
        this.fhirElementPath = fhirElementPath;
    }

    private MultipleChoiceElementChoices choices;
    public MultipleChoiceElementChoices getChoices() {
        if (this.choices == null) {
            this.choices = new MultipleChoiceElementChoices();
        }
        return this.choices;
    }

    private String baseProfile;
    public String getBaseProfile() {
        //TODO: Naive check for a URL may need to be improved.
        if (this.baseProfile != null && !this.baseProfile.isEmpty() && !this.baseProfile.toLowerCase().equals("fhir")) {
            return this.baseProfile;
        }
        return String.format("http://hl7.org/fhir/StructureDefinition/%s", this.getFhirElementPath().getResourceType());
    }
    public void setBaseProfile(String baseProfile) {
        this.baseProfile = baseProfile;
    }

    private String customProfileId;
    public String getCustomProfileId() { return this.customProfileId; }
    public void setCustomProfileId(String customProfileId) { this.customProfileId = customProfileId; }

    private String additionalFHIRMappingDetails;
    public String getAdditionalFHIRMappingDetails() { return this.additionalFHIRMappingDetails; }
    public void setAdditionalFHIRMappingDetails(String additionalFHIRMappingDetails) { this.additionalFHIRMappingDetails = additionalFHIRMappingDetails; }

    private String customValueSetName;
    public String getCustomValueSetName() { return this.customValueSetName; }
    public void setCustomValueSetName(String customValueSetName) { this.customValueSetName = customValueSetName; }

    private String bindingStrength;
    public Enumerations.BindingStrength getBindingStrength() {
        if (this.bindingStrength == null || this.bindingStrength.isEmpty()) {
            return Enumerations.BindingStrength.REQUIRED;
        }

        switch (this.bindingStrength.toLowerCase()) {
            case "example":
                return Enumerations.BindingStrength.EXAMPLE;
            case "extensible":
                return Enumerations.BindingStrength.EXTENSIBLE;
            default:
                return Enumerations.BindingStrength.REQUIRED;
        }
    }
    public void setBindingStrength(String bindingStrength) { this.bindingStrength = bindingStrength; }

    private String unitOfMeasure;
    public String getUnitOfMeasure() { return this.unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }

    private String extensionNeeded;
    public String getExtensionNeeded() { return this.extensionNeeded; }
    public void setExtensionNeeded(String extensionNeeded) { this.extensionNeeded = extensionNeeded; }

    private String terminologyIdentifier;
    public String getTerminologyIdentifier() { return this.terminologyIdentifier; }
    public void setTerminologyIdentifier(String terminologyIdentifier) { this.terminologyIdentifier = terminologyIdentifier; }

    private String version;
    public String getVersion() {
        if (this.version != null && !this.version.isEmpty()) {
            return this.version;
        }
        return "4.0.1";
    }
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DictionaryElement && ((DictionaryElement)obj).name.equals(name);
    }
}
