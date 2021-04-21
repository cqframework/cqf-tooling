package org.opencds.cqf.tooling.visitor;

import org.hl7.elm.r1.AliasedQuerySource;
import org.hl7.elm.r1.Property;
import org.hl7.elm.r1.Retrieve;
import org.hl7.elm.r1.VersionedIdentifier;

public class ElmQueryAliasContext {
    public ElmQueryAliasContext(VersionedIdentifier libraryIdentifier, AliasedQuerySource querySource) {
        if (libraryIdentifier == null) {
            throw new IllegalArgumentException("libraryIdentifier is required");
        }
        if (querySource == null) {
            throw new IllegalArgumentException("querySource is required");
        }
        this.libraryIdentifier = libraryIdentifier;
        this.querySource = querySource;
        this.requirements = new ElmAliasDataRequirement(libraryIdentifier, querySource);
    }

    private VersionedIdentifier libraryIdentifier;
    private AliasedQuerySource querySource;
    public AliasedQuerySource getQuerySource() {
        return querySource;
    }
    public String getAlias() {
        return querySource.getAlias();
    }

    private ElmAliasDataRequirement requirements;
    public ElmAliasDataRequirement getRequirements() {
        return requirements;
    }

    public void reportProperty(ElmPropertyRequirement propertyRequirement) {
        requirements.reportProperty(propertyRequirement);
    }
}
