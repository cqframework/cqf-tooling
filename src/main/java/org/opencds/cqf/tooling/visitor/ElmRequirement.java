package org.opencds.cqf.tooling.visitor;

import org.hl7.elm.r1.Element;
import org.hl7.elm.r1.VersionedIdentifier;

public class ElmRequirement {

    private VersionedIdentifier libraryIdentifier;
    public VersionedIdentifier getLibraryIdentifier() {
        return this.libraryIdentifier;
    }

    private Element element;
    public Element getElement() {
        return this.element;
    }

    public ElmRequirement(VersionedIdentifier libraryIdentifier, Element element) {
        if (libraryIdentifier == null) {
            throw new IllegalArgumentException("libraryIdentifier is required");
        }

        if (element == null) {
            throw new IllegalArgumentException("element is required");
        }

        this.libraryIdentifier = libraryIdentifier;
        this.element = element;
    }
}
