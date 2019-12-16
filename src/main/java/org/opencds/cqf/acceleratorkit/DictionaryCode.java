package org.opencds.cqf.acceleratorkit;

import org.hl7.fhir.r4.model.CodeableConcept;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bryn on 8/18/2019.
 */
public class DictionaryCode {
    private String label;
    public String getLabel() {
        return this.label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    private String openMRSEntityParent;
    public String getOpenMRSEntityParent() {
        return this.openMRSEntityParent;
    }
    public void setOpenMRSEntityParent(String openMRSEntityParent) {
        this.openMRSEntityParent = openMRSEntityParent;
    }

    private String openMRSEntity;
    public String getOpenMRSEntity() {
        return this.openMRSEntity;
    }
    public void setOpenMRSEntity(String openMRSEntity) {
        this.openMRSEntity = openMRSEntity;
    }

    private String openMRSEntityId;
    public String getOpenMRSEntityId() {
        return this.openMRSEntityId;
    }
    public void setOpenMRSEntityId(String openMRSEntityId) {
        this.openMRSEntityId = openMRSEntityId;
    }

    private List<CodeableConcept> terminologies;
    public List<CodeableConcept> getTerminologies() {
        if (this.terminologies == null) {
            this.terminologies = new ArrayList<CodeableConcept>();
        }
        return this.terminologies;
    }
}
