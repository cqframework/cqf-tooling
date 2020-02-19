package org.opencds.cqf.acceleratorkit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Bryn on 8/18/2019.
 *
 * Represents a WHO Accelerator Kit Data Dictionary Element
 */
public class DictionaryElement {
    public DictionaryElement(String name) {
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

    private String label;
    public String getLabel() {
        return this.label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    private String name;
    public String getName() {
        return this.name;
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

    private List<DictionaryCode> choices;
    public List<DictionaryCode> getChoices() {
        if (this.choices == null) {
            this.choices = new ArrayList<>();
        }
        return this.choices;
    }
    public List<DictionaryCode> getValidChoices() {
        return this.getChoices().stream()
                .filter((c) -> !c.getCode().trim().isEmpty())
                .collect(Collectors.toList());
    }
    public List<DictionaryCode> getChoicesForSystem(String system) {
        if (this.choices == null) {
            this.choices = new ArrayList<>();
        }
        List<DictionaryCode> codes = this.getValidChoices().stream()
                .filter((c) -> c.getSystem() == system)
                .collect(Collectors.toList());
        return codes;
    }

    private ArrayList<String> codeSystemUrls;
    public List<String> getCodeSystemUrls() {
        if (this.codeSystemUrls == null) {
            this.codeSystemUrls = new ArrayList<>();
        }
        List<String> codeSystemUrls = this.getValidChoices().stream()
                .map((c) -> c.getSystem())
                .distinct()
                .collect(Collectors.toList());
        return codeSystemUrls;
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

    private DictionaryCode code;
    public DictionaryCode getCode() {
        return this.code;
    }
    public void setCode(DictionaryCode code) {
        this.code = code;
    }

    private DictionaryFhirElementPath fhirElementPath;
    public DictionaryFhirElementPath getFhirElementPath() {
        return this.fhirElementPath;
    }
    public void setFhirElementPath(DictionaryFhirElementPath fhirElementPath) {
        this.fhirElementPath = fhirElementPath;
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
