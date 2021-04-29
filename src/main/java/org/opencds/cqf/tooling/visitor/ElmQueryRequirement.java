package org.opencds.cqf.tooling.visitor;

import org.hl7.elm.r1.*;

import java.util.HashSet;
import java.util.Set;

public class ElmQueryRequirement extends ElmExpressionRequirement {
    public ElmQueryRequirement(VersionedIdentifier libraryIdentifier, Query query) {
        super(libraryIdentifier, query);
    }

    public Query getQuery() {
        return (Query)element;
    }

    public Query getElement() {
        return getQuery();
    }

    private Set<ElmDataRequirement> dataRequirements = new HashSet<ElmDataRequirement>();
    public Iterable<ElmDataRequirement> getDataRequirements() {
        return dataRequirements;
    }

    public void addDataRequirements(ElmDataRequirement dataRequirement) {
        if (dataRequirement == null) {
            throw new IllegalArgumentException("dataRequirement must be provided");
        }
        if (dataRequirement.getQuerySource() == null) {
            throw new IllegalArgumentException("Data requirement must be associated with an alias to be added to a query requirements");
        }
        dataRequirements.add(dataRequirement);
    }

    private ElmDataRequirement getDataRequirement(Element querySource) {
        if (querySource instanceof AliasedQuerySource) {
            return getDataRequirement(((AliasedQuerySource)querySource).getAlias());
        }

        return null;
    }

    private ElmDataRequirement getDataRequirement(String alias) {
        for (ElmDataRequirement dataRequirement : dataRequirements) {
            if (dataRequirement.getQuerySource().getAlias().equals(alias)) {
                return dataRequirement;
            }
        }

        return null;
    }

    @Override
    public boolean hasRequirement(ElmRequirement requirement) {
        boolean superHasRequirement = super.hasRequirement(requirement);
        if (!superHasRequirement) {
            for (ElmDataRequirement dataRequirement : dataRequirements) {
                if (dataRequirement.hasRequirement(requirement)) {
                    return true;
                }
            }
        }
        return superHasRequirement;
    }

    private void distributeConditionRequirement(ElmConditionRequirement requirement) {
        ElmDataRequirement dataRequirement = getDataRequirement(requirement.getProperty().getSource());
        if (dataRequirement != null) {
            dataRequirement.addConditionRequirement(requirement);
        }
    }

    private void distributeNestedConditionRequirement(ElmDataRequirement dataRequirement, String path, ElmConditionRequirement requirement) {
        Property qualifiedProperty = new Property();
        Property nestedProperty = requirement.getProperty().getProperty();
        qualifiedProperty.setPath(String.format("%s.%s", path, nestedProperty.getPath()));
        qualifiedProperty.setScope(dataRequirement.getQuerySource().getAlias());
        qualifiedProperty.setResultType(nestedProperty.getResultType());
        qualifiedProperty.setResultTypeName(nestedProperty.getResultTypeName());
        qualifiedProperty.setResultTypeSpecifier(nestedProperty.getResultTypeSpecifier());
        ElmPropertyRequirement qualifiedPropertyRequirement = new ElmPropertyRequirement(libraryIdentifier, qualifiedProperty, dataRequirement.getQuerySource(), true);
        // TODO: Validate that the comparand is context literal and scope stable
        ElmConditionRequirement qualifiedCondition = new ElmConditionRequirement(libraryIdentifier, requirement.getExpression(), qualifiedPropertyRequirement, requirement.getComparand());
        dataRequirement.addConditionRequirement(qualifiedCondition);
    }

    private void distributeQueryRequirement(ElmQueryRequirement requirement) {
        // If the query is single source and the source is a property reference to an alias in the current query
        // distribute the conjunctive requirements of the nested query as conjunctive requirements against the
        // qualified property
        if (requirement.dataRequirements.size() == 1) {
            for (ElmDataRequirement nestedAlias : requirement.dataRequirements) {
                if (nestedAlias.getQuerySource().getExpression() instanceof Property) {
                    Property sourceProperty = (Property)nestedAlias.getQuerySource().getExpression();
                    if (sourceProperty.getScope() != null) {
                        ElmDataRequirement aliasDataRequirement = getDataRequirement(sourceProperty.getScope());
                        if (aliasDataRequirement != null && nestedAlias.getConjunctiveRequirement() != null) {
                            for (ElmExpressionRequirement nestedRequirement : nestedAlias.getConjunctiveRequirement().getArguments()) {
                                // A conjunctive requirement against a nested query that is based on a property of the current query
                                // can be inferred as a conjunctive requirement against the qualified property in the current query
                                if (nestedRequirement instanceof ElmConditionRequirement) {
                                    distributeNestedConditionRequirement(aliasDataRequirement, sourceProperty.getPath(), (ElmConditionRequirement)nestedRequirement);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void addChildRequirements(ElmRequirement childRequirements) {
        // TODO: Placeholder to support processing child requirements gathered during the query context processing
        // The property requirements have already been reported and processed, so this is currently unnecessary
    }

    public void distributeExpressionRequirement(ElmExpressionRequirement requirement) {
        if (requirement instanceof ElmConjunctiveRequirement) {
            for (ElmExpressionRequirement expressionRequirement : ((ElmConjunctiveRequirement)requirement).getArguments()) {
                distributeExpressionRequirement(expressionRequirement);
            }
        }
        else if (requirement instanceof ElmDisjunctiveRequirement) {
            // TODO: Distribute disjunctive requirements (requires union rewrite)
        }
        else if (requirement instanceof ElmConditionRequirement) {
            distributeConditionRequirement((ElmConditionRequirement)requirement);
        }
        else if (requirement instanceof ElmQueryRequirement) {
            distributeQueryRequirement((ElmQueryRequirement)requirement);
        }
    }

    public void analyzeDataRequirements(ElmRequirementsContext context) {

        // apply query requirements to retrieves
        for (ElmDataRequirement dataRequirement : dataRequirements) {
            dataRequirement.applyDataRequirements(context);
        }
    }
}
