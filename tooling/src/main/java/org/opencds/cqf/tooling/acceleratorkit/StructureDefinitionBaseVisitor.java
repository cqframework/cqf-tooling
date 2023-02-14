package org.opencds.cqf.tooling.acceleratorkit;

import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.StructureDefinition;

import java.util.List;

public abstract class StructureDefinitionBaseVisitor {

    public void visitContact(StructureDefinition sd) {
    }

    public void visitUseContext(StructureDefinition sd) {
    }

    public void visitJurisdiction(StructureDefinition sd) {
    }

    public void visitMapping(StructureDefinition sd) {
    }

    public void visitContext(StructureDefinition sd) {
    }

    public List<ElementDefinition> visitSnapshot(StructureDefinition sd) {
        StructureDefinition.StructureDefinitionSnapshotComponent snapshot = sd.getSnapshot();
        if (snapshot != null && snapshot.hasElement()) {
            return snapshot.getElement();
        }
        return null;
    }

    public List<ElementDefinition> visitDifferential(StructureDefinition sd) {
        StructureDefinition.StructureDefinitionDifferentialComponent sddc = sd.getDifferential();
        if (sddc != null && sddc.hasElement()) {
            return sddc.getElement();
        }
        return null;
    }
}
