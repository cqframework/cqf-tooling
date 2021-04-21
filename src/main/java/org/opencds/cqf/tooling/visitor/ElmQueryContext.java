package org.opencds.cqf.tooling.visitor;

import org.hl7.elm.r1.*;

import java.util.*;
import java.util.List;

public class ElmQueryContext {
    public ElmQueryContext(VersionedIdentifier libraryIdentifier, Query query) {
        if (libraryIdentifier == null) {
            throw new IllegalArgumentException("libraryIdentifier is required");
        }
        if (query == null) {
            throw new IllegalArgumentException("query is required");
        }
        this.libraryIdentifier = libraryIdentifier;
        this.query = query;
        this.queryRequirements = new ElmConjunctiveRequirement(libraryIdentifier, query);
    }

    private VersionedIdentifier libraryIdentifier;
    private Query query;
    private ElmExpressionRequirement queryRequirements;
    private ElmQueryAliasContext definitionContext;
    private List<ElmQueryAliasContext> aliasContexts = new ArrayList<ElmQueryAliasContext>();
    private Set<ElmAliasDataRequirement> aliasDataRequirements = new HashSet<ElmAliasDataRequirement>();

    public void enterAliasDefinitionContext(AliasedQuerySource querySource) {
        if (definitionContext != null) {
            throw new IllegalArgumentException("Alias definition already in progress");
        }
        definitionContext = new ElmQueryAliasContext(libraryIdentifier, querySource);
    }

    public void exitAliasDefinitionContext() {
        if (definitionContext == null) {
            throw new IllegalArgumentException("Alias definition not in progress");
        }
        aliasContexts.add(definitionContext);
        definitionContext = null;
    }

    public ElmQueryAliasContext resolveAlias(String aliasName) {
        for (ElmQueryAliasContext aliasContext : aliasContexts) {
            if (aliasContext.getAlias().equals(aliasName)) {
                return aliasContext;
            }
        }

        return null;
    }

    private ElmQueryAliasContext getAliasContext(AliasedQuerySource querySource) {
        for (ElmQueryAliasContext aliasContext : aliasContexts) {
            if (aliasContext.getAlias().equals(querySource.getAlias())) {
                return aliasContext;
            }
        }

        return null;
    }

    private void addAliasDataRequirements(ElmAliasDataRequirement aliasDataRequirement) {
        aliasDataRequirements.add(aliasDataRequirement);
    }

    public void descopeAlias(AliasedQuerySource querySource) {
        ElmQueryAliasContext aliasContext = getAliasContext(querySource);
        if (aliasContext != null) {
            aliasContexts.remove(aliasContext);
            addAliasDataRequirements(aliasContext.getRequirements());
        }
        aliasContexts.removeIf(x -> x.getAlias().equals(querySource.getAlias()));
    }

    public void reportQueryRequirements(ElmRequirement requirements) {
        if (requirements instanceof ElmExpressionRequirement) {
            queryRequirements = queryRequirements.combine((ElmExpressionRequirement)requirements);
        }
    }

    private ElmAliasDataRequirement getAliasDataRequirement(Element querySource) {
        if (querySource instanceof AliasedQuerySource) {
            for (ElmAliasDataRequirement aliasDataRequirement : aliasDataRequirements) {
                if (aliasDataRequirement.getQuerySource().getAlias().equals(((AliasedQuerySource)querySource).getAlias())) {
                    return aliasDataRequirement;
                }
            }
        }

        return null;
    }

    private void distributeConditionRequirement(ElmConditionRequirement requirement) {
        ElmAliasDataRequirement aliasDataRequirement = getAliasDataRequirement(requirement.getProperty().getSource());
        if (aliasDataRequirement != null) {
            aliasDataRequirement.addConditionRequirement(requirement);
        }
    }

    private void distributeExpressionRequirement(ElmExpressionRequirement requirement) {
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
    }

    public void analyzeDataRequirements() {
        // Gather requirements from any sources still in scope in the query
        for (ElmQueryAliasContext aliasContext : aliasContexts) {
            addAliasDataRequirements(aliasContext.getRequirements());
        }

        // distribute query requirements to each alias
        distributeExpressionRequirement(queryRequirements);
    }
}
