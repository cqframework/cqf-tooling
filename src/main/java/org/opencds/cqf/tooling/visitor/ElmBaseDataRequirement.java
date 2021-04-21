package org.opencds.cqf.tooling.visitor;

import org.hl7.elm.r1.*;

import java.util.HashSet;
import java.util.Set;

public class ElmBaseDataRequirement extends ElmRequirement {
    public ElmBaseDataRequirement(VersionedIdentifier libraryIdentifier, Element element) {
        super(libraryIdentifier, element);
    }

    private Set<Property> propertySet;

    public void reportProperty(ElmPropertyRequirement propertyRequirement) {
        if (propertySet == null) {
            propertySet = new HashSet<Property>();
        }
        propertySet.add(propertyRequirement.getProperty());
    }

    private ElmConjunctiveRequirement conjunctiveRequirement;
    public ElmConjunctiveRequirement getConjunctiveRequirement() {
        ensureConjunctiveRequirement();
        return conjunctiveRequirement;
    }

    private void ensureConjunctiveRequirement() {
        if (conjunctiveRequirement == null) {
            conjunctiveRequirement = new ElmConjunctiveRequirement(libraryIdentifier, new Null());
        }
    }

    public void addConditionRequirement(ElmConditionRequirement conditionRequirement) {
        ensureConjunctiveRequirement();
        conjunctiveRequirement.combine(conditionRequirement);
    }
}
