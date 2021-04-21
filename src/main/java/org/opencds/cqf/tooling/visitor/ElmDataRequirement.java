package org.opencds.cqf.tooling.visitor;

import org.hl7.elm.r1.Retrieve;
import org.hl7.elm.r1.VersionedIdentifier;

public class ElmDataRequirement extends ElmBaseDataRequirement {

    public ElmDataRequirement(VersionedIdentifier libraryIdentifier, Retrieve retrieve) {
        super(libraryIdentifier, retrieve);
    }

    public Retrieve getRetrieve() {
        return (Retrieve)element;
    }

    public Retrieve getElement() {
        return getRetrieve();
    }
}
