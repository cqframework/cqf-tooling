package org.opencds.cqf.tooling.visitor;

import org.hl7.elm.r1.AliasedQuerySource;
import org.hl7.elm.r1.VersionedIdentifier;

public class ElmAliasDataRequirement extends ElmBaseDataRequirement {
    public ElmAliasDataRequirement(VersionedIdentifier libraryIdentifier, AliasedQuerySource querySource) {
        super(libraryIdentifier, querySource);
    }

    public AliasedQuerySource getQuerySource() {
        return (AliasedQuerySource)element;
    }

    public AliasedQuerySource getElement() {
        return getQuerySource();
    }
}
