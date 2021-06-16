package org.opencds.cqf.tooling.visitor;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import org.hl7.elm.r1.CodeDef;
import org.hl7.elm.r1.CodeSystemDef;
import org.hl7.elm.r1.ConceptDef;
import org.hl7.elm.r1.Element;
import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.FunctionDef;
import org.hl7.elm.r1.IncludeDef;
import org.hl7.elm.r1.ParameterDef;
import org.hl7.elm.r1.Retrieve;
import org.hl7.elm.r1.UsingDef;
import org.hl7.elm.r1.ValueSetDef;
import org.hl7.elm.r1.VersionedIdentifier;

/**
 * @author Adam Stevenson
 */
public class ElmRequirements extends ElmRequirement {

    private HashSet<ElmRequirement> requirements = new LinkedHashSet<ElmRequirement>();
    public Iterable<ElmRequirement> getRequirements() {
        return requirements;
    }

    public ElmRequirements(VersionedIdentifier libraryIdentifier, Element element) {
        super(libraryIdentifier, element);
    }

    public void reportRequirement(ElmRequirement requirement) {
        if (requirement instanceof ElmRequirements) {
            for (ElmRequirement r : ((ElmRequirements)requirement).getRequirements()) {
                reportRequirement(r);
            }
        }
        else {
            if (requirement != null) {
                requirements.add(requirement);
            }
        }
    }

    public Iterable<ElmRequirement> getUsingDefs() {
        return requirements.stream().filter(x -> x.getElement() instanceof UsingDef).collect(Collectors.toList());
    }

    public Iterable<ElmRequirement> getIncludeDefs() {
        return requirements.stream().filter(x -> x.getElement() instanceof IncludeDef).collect(Collectors.toList());
    }

    public Iterable<ElmRequirement> getCodeSystemDefs() {
        return requirements.stream().filter(x -> x.getElement() instanceof CodeSystemDef).collect(Collectors.toList());
    }

    public Iterable<ElmRequirement> getValueSetDefs() {
        return requirements.stream().filter(x -> x.getElement() instanceof ValueSetDef).collect(Collectors.toList());
    }

    public Iterable<ElmRequirement> getCodeDefs() {
        return requirements.stream().filter(x -> x.getElement() instanceof CodeDef).collect(Collectors.toList());
    }

    public Iterable<ElmRequirement> getConceptDefs() {
        return requirements.stream().filter(x -> x.getElement() instanceof ConceptDef).collect(Collectors.toList());
    }

    public Iterable<ElmRequirement> getParameterDefs() {
        return requirements.stream().filter(x -> x.getElement() instanceof ParameterDef).collect(Collectors.toList());
    }

    public Iterable<ElmRequirement> getExpressionDefs() {
        return requirements.stream().filter(x -> x.getElement() instanceof ExpressionDef && !(x.getElement() instanceof FunctionDef)).collect(Collectors.toList());
    }

    public Iterable<ElmRequirement> getFunctionDefs() {
        return requirements.stream().filter(x -> x.getElement() instanceof FunctionDef).collect(Collectors.toList());
    }

    public Iterable<ElmRequirement> getRetrieves() {
        return requirements.stream().filter(x -> x.getElement() instanceof Retrieve).collect(Collectors.toList());
    }
}
