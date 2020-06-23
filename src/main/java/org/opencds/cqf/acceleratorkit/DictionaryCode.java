package org.opencds.cqf.acceleratorkit;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

/**
 * Created by Bryn on 8/18/2019.
 */
public class DictionaryCode {

    private String label;
    public String getLabel() {
        return this.label;
    }
    public void setLabel(String label) {
        if (label == null) {
            this.label = null;
        }
        this.label = label.replace((char)160, (char)32).trim();
    }

    private String display;
    public String getDisplay() {
        return this.display;
    }
    public void setDisplay(String display) {
        if (display == null) {
            this.display = null;
        }
        this.display = display.replace((char)160, (char)32).trim();
    }

    private String parent;
    public String getParent() {
        return this.parent;
    }
    public void setParent(String parent) {
        this.parent = parent;
    }

    private String system;
    public String getSystem() {
        return this.system;
    }
    public void setSystem(String system) {
        this.system = system;
    }

    private String code;
    public String getCode() {
        return this.code;
    }
    public void setCode(String code) {
        if (code == null) {
            this.code = null;
        }
        this.code = code.replace((char)160, (char)32).trim();
    }

    private List<CodeableConcept> terminologies;
    public List<CodeableConcept> getTerminologies() {
        if (this.terminologies == null) {
            this.terminologies = new ArrayList<>();
        }
        return this.terminologies;
    }


    public CodeableConcept toCodeableConcept() {
        CodeableConcept cc = new CodeableConcept();
        //cc.setText(this.label);
        Coding coding = new Coding();
        coding.setCode(this.code);
        coding.setDisplay(this.display);
        // TODO: Support different systems here
        coding.setSystem(this.system);
        cc.addCoding(coding);
        return cc;
    }
}
