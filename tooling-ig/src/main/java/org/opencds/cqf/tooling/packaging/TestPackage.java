package org.opencds.cqf.tooling.packaging;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.Set;

public class TestPackage<G extends IBaseResource, B extends IBaseBundle> {
    G group;
    Set<B> tests;

    public G getGroup() {
        return group;
    }

    public void setGroup(G group) {
        this.group = group;
    }

    public Set<B> getTests() {
        return tests;
    }

    public void setTests(Set<B> tests) {
        this.tests = tests;
    }
}
