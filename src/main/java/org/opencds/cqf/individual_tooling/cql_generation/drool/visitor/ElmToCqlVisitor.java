package org.opencds.cqf.individual_tooling.cql_generation.drool.visitor;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.cqframework.cql.elm.visiting.ElmBaseLibraryVisitor;
import org.cqframework.cql.gen.cqlBaseVisitor;
import org.cqframework.cql.gen.cqlLexer;
import org.cqframework.cql.gen.cqlParser;
import org.hl7.elm.r1.*;
import org.hl7.elm.r1.Library.Statements;
import org.opencds.cqf.individual_tooling.cql_generation.context.ElmContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import com.google.common.base.Strings;

public class ElmToCqlVisitor extends ElmBaseLibraryVisitor<Void, ElmContext> {
    private boolean useSpaces = true;

    public boolean getUseSpaces() {
        return useSpaces;
    }

    private int indentSize = 2;

    public int getIndentSize() {
        return indentSize;
    }

    private StringBuilder output;

    private final char space = '\u0020';
    private final char tab = '\t';
    private final String newLine = "\r\n";

    private int currentLine = 0;
    private boolean onNewLine;
    private boolean needsWhitespace;
    private int indentLevel = 0;
    private int previousIndentLevel = 0;

    private boolean isFirstTupleElement = false;

    private String currentSection;
    private int sectionCount = 0;

    private void newSection(String section) {
        if (hasSectionContent()) {
            resetIndentLevel();
            newLine();
        }
        sectionCount = 0;
        currentSection = section;
    }

    private boolean needsSectionSeparator(String section) {
        switch (section) {
            case "statement":
                return true;
            default:
                return false;
        }
    }

    private void ensureSectionSeparator() {
        if (needsSectionSeparator(currentSection) && hasSectionContent()) {
            resetIndentLevel();
            newLine();
        }
    }

    private void addToSection(String section) {
        if (!section.equals(currentSection)) {
            newSection(section);
        }

        ensureSectionSeparator();

        sectionCount++;
    }

    private boolean hasSectionContent() {
        return sectionCount > 0;
    }

    private int typeSpecifierLevel = 0;

    private void enterTypeSpecifier() {
        typeSpecifierLevel++;
    }

    private void exitTypeSpecifier() {
        typeSpecifierLevel--;
    }

    private boolean inTypeSpecifier() {
        return typeSpecifierLevel > 0;
    }

    private int functionDefinitionLevel = 0;

    private void enterFunctionDefinition() {
        functionDefinitionLevel++;
    }

    private void exitFunctionDefinition() {
        functionDefinitionLevel--;
    }

    private boolean inFunctionDefinition() {
        return functionDefinitionLevel > 0;
    }

    private int functionInvocationLevel = 0;

    private void enterFunctionInvocation() {
        functionInvocationLevel++;
    }

    private void exitFunctionInvocation() {
        functionInvocationLevel--;
    }

    private boolean inFunctionInvocation() {
        return functionInvocationLevel > 0;
    }

    private int retrieveLevel = 0;

    private void enterRetrieve() {
        retrieveLevel++;
    }

    private void exitRetrieve() {
        retrieveLevel--;
    }

    private boolean inRetrieve() {
        return retrieveLevel > 0;
    }

    private void enterClause() {
        increaseIndentLevel();
        newLine();
    }

    private void exitClause() {
        decreaseIndentLevel();
    }

    private Stack<Integer> groups;

    private void enterGroup() {
        increaseIndentLevel();
        groups.push(currentLine);
    }

    private void exitGroup() {
        Integer groupStartLine = groups.pop();
        decreaseIndentLevel();
        if (currentLine != groupStartLine) {
            newLine();
        }
    }

    private boolean needsWhitespaceBefore(String terminal) {
        if (terminal.trim().isEmpty() || terminal.startsWith("//") || terminal.startsWith("/*")) {
            return false;
        }

        switch (terminal) {
            case ":":
                return false;
            case ".":
                return false;
            case ",":
                return false;
            case "<":
                return !inTypeSpecifier();
            case ">":
                return !inTypeSpecifier();
            case "(":
                return !inFunctionDefinition() && !inFunctionInvocation();
            case ")":
                return !inFunctionDefinition() && !inFunctionInvocation();
            case "[":
                return inRetrieve();
            case "]":
                return false;
            default:
                return true;
        }
    }

    private boolean needsWhitespaceAfter(String terminal) {
        switch (terminal) {
            case ".":
                return false;
            case "<":
                return !inTypeSpecifier();
            case ">":
                return !inTypeSpecifier();
            case "(":
                return !inFunctionDefinition() && !inFunctionInvocation();
            case ")":
                return !inFunctionDefinition() && !inFunctionInvocation();
            case "[":
                return false;
            case "]":
                return inRetrieve();
            default:
                return true;
        }
    }

    private void appendTerminal(String terminal) {
        if (needsWhitespaceBefore(terminal)) {
            ensureWhitespace();
        }
        if (terminal.equals("else")) {
            increaseIndentLevel();
            newLine();
            decreaseIndentLevel();
        }
        if (terminal.equals("end")) {
            newLine();
        }
        output.append(terminal);
        onNewLine = false;
        needsWhitespace = needsWhitespaceAfter(terminal);
    }

    private void increaseIndentLevel() {
        previousIndentLevel = indentLevel;
        indentLevel = previousIndentLevel + 1;
    }

    private void decreaseIndentLevel() {
        previousIndentLevel = indentLevel;
        indentLevel = previousIndentLevel - 1;
    }

    private void resetIndentLevel() {
        indentLevel = 0;
        previousIndentLevel = 0;
    }

    private void indent() {
        int indent = indentLevel * (useSpaces ? indentSize : 1);
        for (int i = 0; i < indent; i++) {
            output.append(useSpaces ? space : tab);
        }
    }

    private void newLine() {
        output.append(newLine);
        currentLine++;
        indent();
        onNewLine = true;
    }

    private void newConstruct(String section) {
        resetIndentLevel();
        newLine();
        addToSection(section);
    }

    private void ensureWhitespace() {
        if (!onNewLine && needsWhitespace) {
            output.append(space);
        }
    }

    private void reset() {
        resetIndentLevel();
        currentLine = 1;
        onNewLine = true;
        output = new StringBuilder();
        groups = new Stack<>();
    }

    //TODO: Once every visit has a basic implementation figure out how to connect through space
    // public Object visitChildren(Expression expression, ElmContext context) {
    //     Object result = defaultResult();
    //     int n = node.getChildCount();
    //     for (int i = 0; i < n; i++) {
    //         if (!shouldVisitNextChild(node, result)) {
    //             break;
    //         }

    //         ParseTree c = node.getChild(i);

    //         if ((node instanceof cqlParser.TupleSelectorContext || node instanceof cqlParser.TupleTypeSpecifierContext)
    //                 && c instanceof TerminalNodeImpl) {
    //             if (((TerminalNodeImpl) c).getSymbol().getText().equals("}")) {
    //                 decreaseIndentLevel();
    //                 newLine();
    //             }
    //         }

    //         Object childResult = c.accept(this);
    //         result = aggregateResult(result, childResult);
    //     }

    //     return result;
    // }

    @Override
    public Void visitLibrary(Library library, ElmContext context) {
        reset();
        resetIndentLevel();
        addToSection("library");
        String id = library.getIdentifier().getId();
        String version = library.getIdentifier().getVersion();
        output.append(currentSection + " " + id + " version " + "\'" + version + "\'");
        super.visitLibrary(library, context);
        if(library.getUsings() != null && library.getUsings().getDef() != null && !library.getUsings().getDef().isEmpty()) {
            library.getUsings().getDef().stream().forEach(using -> visitUsingDef(using, context));
        }
        if (library.getIncludes() != null && library.getIncludes().getDef() != null && !library.getIncludes().getDef().isEmpty()) {
            library.getIncludes().getDef().stream().forEach(include -> visitIncludeDef(include, context));
        }
        if (library.getCodeSystems() != null && library.getCodeSystems().getDef() != null && !library.getCodeSystems().getDef().isEmpty()) {
            library.getCodeSystems().getDef().stream().forEach(codeSystem -> visitCodeSystemDef(codeSystem, context));
        }
        if (library.getValueSets() != null && library.getValueSets().getDef() != null && !library.getValueSets().getDef().isEmpty()) {
            library.getValueSets().getDef().stream().forEach(valueset -> visitValueSetDef(valueset, context));
        }
        if (library.getCodes() != null && library.getCodes().getDef() != null && !library.getCodes().getDef().isEmpty()) {
            library.getCodes().getDef().stream().forEach(code -> visitCodeDef(code, context));
        }
        if (library.getConcepts() != null && library.getConcepts().getDef() != null && !library.getConcepts().getDef().isEmpty()) {
            library.getConcepts().getDef().stream().forEach(concept -> visitConceptDef(concept, context));
        }
        if (library.getParameters() != null && library.getParameters().getDef() != null && !library.getParameters().getDef().isEmpty()) {
            library.getParameters().getDef().stream().forEach(param -> visitParameterDef(param, context));
        }
        if (library.getContexts() != null && library.getContexts().getDef() != null && !library.getContexts().getDef().isEmpty()) {
            library.getContexts().getDef().stream().forEach(contextDef -> visitContextDef(contextDef, context));
        }
        if (library.getStatements() != null && library.getStatements().getDef() != null && !library.getStatements().getDef().isEmpty()) {
            visitStatement(library.getStatements(), context);
        }
        // newLine();
        return null;
    }

    @Override
    public Void visitUsingDef(UsingDef using, ElmContext context) {
        if (!using.getLocalIdentifier().equals("System")) {
            newConstruct("using");
            output.append(String.format("%s %s version \'%s\'", currentSection, using.getLocalIdentifier(), using.getVersion()));
        }
        return super.visitUsingDef(using, context);
    }

    @Override
    public Void visitIncludeDef(IncludeDef include, ElmContext context) {
        newConstruct("include");
        output.append(String.format("%s %s", currentSection, include.getPath()));
        if (!Strings.isNullOrEmpty(include.getVersion())) {
            output.append(String.format(" version \'%s\'", include.getVersion()));
        }
        if (!Strings.isNullOrEmpty(include.getLocalIdentifier())) {
            output.append(String.format(" called %s", include.getLocalIdentifier()));
        }
        return super.visitIncludeDef(include, context);
    }

    @Override
    public Void visitCodeSystemDef(CodeSystemDef codeSystem, ElmContext context) {
        newConstruct("codesystem");
        if (codeSystem.getAccessLevel() != null) {
            visitAccessModifier(codeSystem.getAccessLevel(), context);
        }
        output.append(String.format("%s \"%s\" : \'%s\'", currentSection, codeSystem.getName(), codeSystem.getId()));
        if (!Strings.isNullOrEmpty(codeSystem.getVersion())) {
            output.append(String.format("version %s", codeSystem.getVersion()));
        }
        return super.visitCodeSystemDef(codeSystem, context);
    }

    @Override
    public Void visitValueSetDef(ValueSetDef valueset, ElmContext context) {
        newConstruct("valueset");
        if (valueset.getAccessLevel() != null) {
            visitAccessModifier(valueset.getAccessLevel(), context);
        }
        output.append(String.format("%s \"%s\" : \'%s\'", currentSection, valueset.getName(), valueset.getId()));
        if (!Strings.isNullOrEmpty(valueset.getVersion())) {
            output.append(String.format(" version %s", valueset.getVersion()));
        }
        if (valueset.getCodeSystem() != null && !valueset.getCodeSystem().isEmpty()) {
            output.append("codesystems { ");
            int index = 0;
            for (CodeSystemRef codeSystem : valueset.getCodeSystem()) {
                if (index != 0) {
                    output.append(", ");
                }
                visitCodeSystemRef(codeSystem, context);
            }
            output.append(" }");
        }
        return super.visitValueSetDef(valueset, context);
    }

    @Override
    public Void visitCodeSystemRef(CodeSystemRef codeSystem, ElmContext context) {
        output.append(" ");
        if (!Strings.isNullOrEmpty(codeSystem.getLibraryName())) {
            output.append(String.format("\"%s\".", codeSystem.getLibraryName()));
        }
        output.append(String.format("\"%s\"",codeSystem.getName()));
        return null;
    }

    public Void visitCodeDef(CodeDef code, ElmContext context) {
        newConstruct("code");
        if (code.getAccessLevel() != null) {
            visitAccessModifier(code.getAccessLevel(), context);
        }
        output.append(String.format("%s \"%s\" : \'%s\' from", currentSection, code.getName(), code.getId()));
        visitCodeSystemRef(code.getCodeSystem(), context);
        if (!Strings.isNullOrEmpty(code.getDisplay())) {
            output.append(String.format(" display \'%s\'", code.getDisplay()));
        }
        return null;
    }

    public Void visitConceptDef(ConceptDef concept, ElmContext context) {
        newConstruct("concept");
        return null;
    }

    @Override
    public Void visitParameterDef(ParameterDef parameter, ElmContext context) {
        newConstruct("parameter");
        return super.visitParameterDef(parameter, context);
    }

    @Override
    public Void visitIdentifierRef(IdentifierRef identifier, ElmContext context) {
        return super.visitIdentifierRef(identifier, context);
    }

    public Void visitAccessModifier(AccessModifier access, ElmContext context) {
        if (access.equals(AccessModifier.PRIVATE)) {
            output.append("private ");
        } else {
            // default to nothing for now.
        }
        return null;
    }

    @Override
    public Void visitTypeSpecifier(TypeSpecifier typeSpecifier, ElmContext context) {
        enterTypeSpecifier();
        try {
            return super.visitTypeSpecifier(typeSpecifier, context);
        } finally {
            exitTypeSpecifier();
        }
    }

    @Override
    public Void visitNamedTypeSpecifier(NamedTypeSpecifier namedTypeSpecifier, ElmContext context) {
        output.append(" " + namedTypeSpecifier.getResultType().toLabel());
        return super.visitNamedTypeSpecifier(namedTypeSpecifier, context);
    }

    @Override
    public Void visitListTypeSpecifier(ListTypeSpecifier typeSpecifier, ElmContext context) {
        return super.visitListTypeSpecifier(typeSpecifier, context);
    }

    @Override
    public Void visitIntervalTypeSpecifier(IntervalTypeSpecifier intervalTypeSpecifier, ElmContext context) {
        return super.visitIntervalTypeSpecifier(intervalTypeSpecifier, context);
    }

    @Override
    public Void visitTupleTypeSpecifier(TupleTypeSpecifier typeSpecifier, ElmContext context) {
        isFirstTupleElement = true;
        return super.visitTupleTypeSpecifier(typeSpecifier, context);
    }

    @Override
    public Void visitTupleElementDefinition(TupleElementDefinition tuple, ElmContext context) {
        if (isFirstTupleElement) {
            increaseIndentLevel();
            isFirstTupleElement = false;
        }
        newLine();
        return super.visitTupleElementDefinition(tuple, context);
    }

    public Void visitChoiceTypeSpecifier(ChoiceTypeSpecifier choice, ElmContext context) {
        return null;
    }

    public Void visitStatement(Statements statements, ElmContext context) {
        newConstruct("statement");
        Object result = "TODO";//defaultResult();
        int n = statements.getDef().size();
        for (int i=0; i<n; i++) {
            // if (!shouldVisitNextChild(statements.get(i), result)) {
            //     break;
            // }

            Element c = statements.getDef().get(i);
            if (c instanceof ExpressionDef) {
                enterClause();
                try {
                    Object childResult = visitExpressionDef((ExpressionDef)c, context);
                    result = childResult;
                    // result = aggregateResult(result, childResult);
                }
                finally {
                    exitClause();
                }
            } else if (c instanceof ContextDef) {
                Object childResult = visitContextDef((ContextDef)c, context);
                result = childResult;
            } else if (c instanceof FunctionDef) {
                Object childResult = visitFunctionDef((FunctionDef)c, context);
                result = childResult;
            }
        }

        return null;
    }

    public Void visitExpressionDef(ExpressionDef expressionDef, ElmContext context) {
        newConstruct("statement");
        output.append("define");
        if (expressionDef.getAccessLevel() != null) {
            visitAccessModifier(expressionDef.getAccessLevel(), context);
        }
        if (expressionDef.getName().equals("Patient has a discharge disposition of-6ff8beb15ce0add65b41d080669c199b")){
            System.out.println("Units:-81c2ddf471455503cd0e601e");
        }
        output.append(String.format(" \"%s\":", expressionDef.getName()));
        if (expressionDef.getExpression() != null) {
            visitExpression(expressionDef.getExpression(), context);
        }
        return null;
    }

    public Void visitContextDef(ContextDef contextDef, ElmContext context) {
        newConstruct("context");
        output.append(String.format("%s %s", currentSection, contextDef.getName()));
        //System.out.println(output.toString());
        return null;
    }

    @Override
    public Void visitFunctionDef(FunctionDef function, ElmContext context) {
        newConstruct("statement");

        if (function.getAccessLevel() != null) {
            visitAccessModifier(function.getAccessLevel(), context);
        }
        Object result = "TODO"; // defaultResult();
        int n = function.getOperand().size();
        boolean clauseEntered = false;
        try {
            for (int i = 0; i < n; i++) {
                // if (!shouldVisitNextChild(ctx, result)) {
                //     break;
                // }

                OperandDef c = function.getOperand().get(i);
            }
        }
        finally {
            if (clauseEntered) {
                exitClause();
            }
        }
        if (function.getExpression() != null) {
            visitExpression(function.getExpression(), context);
        }
        else if (function.getResultTypeSpecifier() != null) {
            visitTypeSpecifier(function.getResultTypeSpecifier(), context);
        }

        return null;
    }

    @Override
    public Void visitOperandDef(OperandDef operand, ElmContext context) {
        return super.visitOperandDef(operand, context);
    }

    @Override
    public Void visitAliasedQuerySource(AliasedQuerySource source, ElmContext context) {
        try {
            output.append("(");
            if (!(source.getExpression() instanceof Retrieve)) {
                enterClause();
            }
            visitExpression(source.getExpression(), context);
        } finally {
            if (!(source.getExpression() instanceof Retrieve)) {
                exitClause();
                newLine();
            }
            output.append(") ");
        }
        output.append(" " + source.getAlias());
        return super.visitAliasedQuerySource(source, context);
    }

    @Override
    public Void visitAliasRef(AliasRef alias, ElmContext context) {
        output.append(" " + alias.getName());
        return super.visitAliasRef(alias, context);
    }

    @Override
    public Void visitRelationshipClause(RelationshipClause relationship, ElmContext context) {
        Object result = "TODO";// defaultResult();
        boolean clauseEntered = false;     
        try {
            enterClause();
            if (relationship instanceof With) {
                visitWith((With) relationship, context);
            } else if (relationship instanceof Without) {
                visitWithout((Without) relationship, context);
            }
            clauseEntered = true;
        }
        finally {
            if (clauseEntered) {
                exitClause();
            }
        }
        return null;
    }

    @Override
    public Void visitWith(With with, ElmContext context) {
        output.append(String.format("with"));
        return null;
    }

    @Override
    public Void visitWithout(Without without, ElmContext context) {
        output.append(String.format("without"));
        return null;
    }

    @Override
    public Void visitRetrieve(Retrieve retrieve, ElmContext context) {
        enterRetrieve();
        try {
            output.append(String.format("[%s", retrieve.getDataType().getLocalPart()));
            if (retrieve.getCodes() != null) {
                if (!Strings.isNullOrEmpty(retrieve.getCodeComparator())) {
                    if (!Strings.isNullOrEmpty(retrieve.getCodeProperty())) {
                        output.append(String.format(": %s %s", retrieve.getCodeProperty(), retrieve.getCodeComparator()));
                    }
                } else {
                    output.append(" :");
                }
                output.append(" ");
                //TODO: ToList should exist in UnaryExpression super Visitor
                if (retrieve.getCodes() instanceof ToList) {
                    //System.out.println(output.toString());
                    visitToList((ToList)retrieve.getCodes(), context);
                } else {
                    visitExpression(retrieve.getCodes(), context);
                }
            }
            output.append("]");
            return super.visitRetrieve(retrieve, context);
        }
        finally {
            exitRetrieve();
        }
    }

    @Override
    public Void visitQuery(Query query, ElmContext context) {
        boolean internalValidation = false;
        if (query.getSource() != null && !query.getSource().isEmpty()) {
            for(AliasedQuerySource source : query.getSource()) {
                if (source.getAlias().equals("$this")) {
                    internalValidation = true;
                    visitExpression(source.getExpression(), context);
                } else {
                    visitAliasedQuerySource(source, context);
                }
            }
        }
        if (internalValidation) {
            visitExpression(query.getReturn().getExpression(), context);
        } else {
            if (query.getLet() != null && !query.getLet().isEmpty()) {
                enterClause();
                output.append(String.format("let"));
                query.getLet().stream().forEach(let -> visitLetClause(let, context));
                exitClause();
            }
            //System.out.println(output.toString());
            if (query.getRelationship() != null && !query.getRelationship().isEmpty()) {
                query.getRelationship().stream().forEach(relationship -> visitRelationshipClause(relationship, context));
            }
            if (query.getWhere() != null) {
                visitWhereClause(query.getWhere(), context);
            }
            if (query.getReturn() != null) {
                visitReturnClause(query.getReturn(), context);
            }
            if (query.getSort() != null) {
                visitSortClause(query.getSort(), context);
            }
        }
        return super.visitQuery(query, context);
    }

    public Void visitToList(ToList toList, ElmContext context) {
        output.append("{ ");
        //TODO: ToList should exist in UnaryExpression super Visitor
        if (toList.getOperand() instanceof ToList) {
            visitToList((ToList)toList.getOperand(), context);
        } else {
            if (toList.getOperand() instanceof CodeRef) {
                visitCodeRef((CodeRef)toList.getOperand(), context);
            } else {
                visitExpression(toList.getOperand(), context);
            }
        }
        output.append(" }");
        return null;
    }

    @Override
    public Void visitUnion(Union union, ElmContext context) {
        int operandCount = 0;
        for (Expression expression : union.getOperand()) {
            if (operandCount > 0) {
                newLine();
                output.append("union ");
            }
            output.append("( ");
            enterClause();
            visitExpression(expression, context);
            exitClause();
            newLine();
            output.append(") ");
            operandCount++;
        }
        return null;
    }

    @Override
    public Void visitIntersect(Intersect intersect, ElmContext context) {
        return null;
    }

    @Override
    public Void visitExcept(Except except, ElmContext context) {
        return null;
    }

    @Override
    public Void visitLetClause(LetClause let, ElmContext context) {
        output.append(String.format(" %s: ", let.getIdentifier()));
        // System.out.println(output.toString());
        visitExpression(let.getExpression(), context);
        newLine();
        return null;
    }

    // @Override
    // public Object visitLetClauseItem(cqlParser.LetClauseItemContext ctx) {
    //     return super.visitLetClauseItem(ctx);
    // }

    public Void visitWhereClause(Expression where, ElmContext context) {
        try {
            enterClause();
            output.append("where");
            visitExpression(where, context);
            return null;
        }
        finally {
            exitClause();
        }
    }

    @Override
    public Void visitReturnClause(ReturnClause returnClause, ElmContext context) {
        enterClause();
        output.append("return");
        visitExpression(returnClause.getExpression(), context);
        try {
            return super.visitReturnClause(returnClause, context);
        }
        finally {
            exitClause();
        }
    }

    @Override
    public Void visitSortClause(SortClause sortClause, ElmContext context) {
        enterClause();
        try {
            return super.visitSortClause(sortClause, context);
        }
        finally {
            exitClause();
        }
    }

    public Void visitSortDirection(SortDirection sortDirection, ElmContext context) {
        return null;
    }

    @Override
    public Void visitSortByItem(SortByItem sortByItem, ElmContext context) {
        return super.visitSortByItem(sortByItem, context);
    }

    @Override
    public Void visitDurationBetween(DurationBetween durationBetween, ElmContext context) {
        return super.visitDurationBetween(durationBetween, context);
    }

    @Override
    public Void visitNull(Null nullElement, ElmContext context) {
        output.append("null");
        return super.visitNull(nullElement, context);
    }

    @Override
    public Void visitNot(Not not, ElmContext context) {
        output.append(" not ");
        if (not.getOperand() instanceof AnyInValueSet) {
            visitAnyInValueSet((AnyInValueSet) not.getOperand(), context);
        } else {
            visitExpression(not.getOperand(), context);
        }
        return super.visitNot(not, context);
    }

    @Override
    public Void visitEqual(Equal equal, ElmContext context) {
        output.append(" Equal(");
        visitExpression(equal.getOperand().get(0), context);
        output.append(", ");
        visitExpression(equal.getOperand().get(1), context);
        output.append(")");
        //System.out.println(output.toString());
        return super.visitEqual(equal, context);
    }

    @Override
    public Void visitOr(Or or, ElmContext context) {
        visitExpression(or.getOperand().get(0), context);
        output.append(" or (");
        enterClause();
        visitExpression(or.getOperand().get(1), context);
        exitClause();
        newLine();
        output.append(")");
        return super.visitOr(or, context);
    }

    @Override
    public Void visitBinaryExpression(BinaryExpression binary, ElmContext context) {
        try {
            enterClause();
            return super.visitBinaryExpression(binary, context);
        }
        finally {
            exitClause();
        }
    }

    @Override
    public Void visitEquivalent(Equivalent equivalent, ElmContext context) {
        visitExpression(equivalent.getOperand().get(0), context);
        output.append(" ~ ");
        visitExpression(equivalent.getOperand().get(1), context);
        return super.visitEquivalent(equivalent, context);
    }

    @Override
    public Void visitIn(In in, ElmContext context) {
        visitExpression(in.getOperand().get(0), context);
        output.append(" in ");
        visitExpression(in.getOperand().get(1), context);
        return super.visitIn(in, context);
    }

    @Override
    public Void visitAnd(And and, ElmContext context) {
        visitExpression(and.getOperand().get(0), context);
        output.append(" and (");
        enterClause();
        visitExpression(and.getOperand().get(1), context);
        exitClause();
        newLine();
        output.append(")");
        return super.visitAnd(and, context);
    }

    @Override
    public Void visitDifferenceBetween(DifferenceBetween differenceBetween, ElmContext context) {
        return super.visitDifferenceBetween(differenceBetween, context);
    }

    @Override
    public Void visitExists(Exists exists, ElmContext context) {
        enterClause();
        output.append("exists (");
        enterClause();
        visitExpression(exists.getOperand(), context);
        exitClause();
        newLine();
        output.append(")");
        exitClause();
        //System.out.println(output.toString());
        return super.visitExists(exists, context);
    }

    public Void visitImplies(Implies implies, ElmContext context) {
        return null;
    }

    public Void visitDateTimePrecision(DateTimePrecision dateTimePrecision, ElmContext context) {
        return null;
    }

    @Override
    public Void visitDateTimeComponentFrom(DateTimeComponentFrom dateTime, ElmContext context) {
        return super.visitDateTimeComponentFrom(dateTime, context);
    }

    @Override
    public Void visitToday(Today today, ElmContext context) {
        output.append(" Today()");
        //System.out.println(output.toString());
        return super.visitToday(today, context);
    }

    @Override
    public Void visitAdd(Add add, ElmContext context) {
        return super.visitAdd(add, context);
    }

    @Override
    public Void visitWidth(Width width, ElmContext context) {
        return super.visitWidth(width, context);
    }

    @Override
    public Void visitTime(Time time, ElmContext context) {
        return super.visitTime(time, context);
    }

    @Override
    public Void visitIf(If ifExpression, ElmContext context) {
        return super.visitIf(ifExpression, context);
    }

    public Void visitExpand(Expand expand, ElmContext context) {
        return null;
    }

    @Override
    public Void visitFlatten(Flatten flatten, ElmContext context) {
        output.append("flatten (");
        enterClause();
        visitExpression(flatten.getOperand(), context);
        exitClause();
        newLine();
        output.append(")");
        return null;
    }

    @Override
    public Void visitDistinct(Distinct distinct, ElmContext context) {
        // could be a keyword in returnClause but adding parentheses to be safe
        output.append("distinct (");
        enterClause();
        visitExpression(distinct.getOperand(), context);
        exitClause();
        newLine();
        output.append(")");
        //System.out.println(output.toString());
        return super.visitDistinct(distinct, context);
    }

    @Override
    public Void visitFirst(First first, ElmContext context) {
        output.append(" First (");
        enterClause();
        visitExpression(first.getSource(), context);
        exitClause();
        newLine();
        output.append(")");
        return super.visitFirst(first, context);
    }

    @Override
    public Void visitLast(Last last, ElmContext context) {
        output.append(" Last (");
        enterClause();
        visitExpression(last.getSource(), context);
        exitClause();
        newLine();
        output.append(")");
        return super.visitLast(last, context);
    }

    @Override
    public Void visitSplit(Split split, ElmContext context) {
        output.append("Split (");
        visitExpression(split.getStringToSplit(), context);
        output.append(", ");
        visitExpression(split.getSeparator(), context);
        output.append(")");
        //System.out.println(output.toString());
        return super.visitSplit(split, context);
    }

    public Void visitAnyInValueSet(AnyInValueSet anyInValueSet, ElmContext context) {
        output.append("AnyInValueSet(");
        visitExpression(anyInValueSet.getCodes(), context);
        output.append(", ");
        visitExpression(anyInValueSet.getValueset(), context);
        output.append(")");
        //System.out.println(output.toString());
        return null;
    }

    @Override
    public Void visitCollapse(Collapse collapse, ElmContext context) {
        return null;
    }

    @Override
    public Void visitPredecessor(Predecessor predecessor, ElmContext context) {
        return super.visitPredecessor(predecessor, context);
    }

    @Override
    public Void visitMultiply(Multiply multiply, ElmContext context) {
        return super.visitMultiply(multiply, context);
    }

    @Override
    public Void visitAggregateExpression(AggregateExpression expression, ElmContext context) {
        if (expression instanceof Count) {
            output.append("Count(");
            visitExpression(expression.getSource(), context);
            output.append(")");
        }
        return super.visitAggregateExpression(expression, context);
    }

    @Override
    public Void visitCase(Case caseExpression, ElmContext context) {
            newLine();
            if (previousIndentLevel == indentLevel) {
                increaseIndentLevel();
            }

        return super.visitCase(caseExpression, context);
    }

    @Override
    public Void visitPower(Power power, ElmContext context) {
        return super.visitPower(power, context);
    }

    @Override
    public Void visitSuccessor(Successor successor, ElmContext context) {
        return super.visitSuccessor(successor, context);
    }

    @Override
    public Void visitCaseItem(CaseItem caseItem, ElmContext context) {
        try {
            enterClause();
            return super.visitCaseItem(caseItem, context);
        } finally {
            exitClause();
        }
    }

    @Override
    public Void visitLess(Less less, ElmContext context) {
        int operandCount = 0;
        for (Expression expression : less.getOperand()) {
            if (operandCount > 0) {
                output.append(" < ");
            }
            visitExpression(expression, context);
            operandCount++;
        }
        return super.visitLess(less, context);
    }

    @Override
    public Void visitLessOrEqual(LessOrEqual lessOrElement, ElmContext context) {
        int operandCount = 0;
        for (Expression expression : lessOrElement.getOperand()) {
            if (operandCount > 0) {
                output.append(" <= ");
            }
            visitExpression(expression, context);
            operandCount++;
        }
        return super.visitLessOrEqual(lessOrElement, context);
    }

    @Override
    public Void visitGreater(Greater greater, ElmContext context) {
        int operandCount = 0;
        for (Expression expression : greater.getOperand()) {
            if (operandCount > 0) {
                output.append(" > ");
            }
            visitExpression(expression, context);
            operandCount++;
        }
        return super.visitGreater(greater, context);
    }

    @Override
    public Void visitGreaterOrEqual(GreaterOrEqual greaterOrElement, ElmContext context) {
        int operandCount = 0;
        for (Expression expression : greaterOrElement.getOperand()) {
            if (operandCount > 0) {
                output.append(" >= ");
            }
            visitExpression(expression, context);
            operandCount++;
        }
        return super.visitGreaterOrEqual(greaterOrElement, context);
    }

    @Override
    public Void visitIncludes(Includes includes, ElmContext context) {
        return super.visitIncludes(includes, context);
    }

    @Override
    public Void visitIncludedIn(IncludedIn includedIn, ElmContext context) {
        return super.visitIncludedIn(includedIn, context);
    }

    public Object visitBeforeOrAfter(BinaryExpression beforeOrAfter, ElmContext context) {
        return null;
    }

    @Override
    public Void visitBefore(Before before, ElmContext context) {
        return super.visitBefore(before, context);
    }

    @Override
    public Void visitAfter(After after, ElmContext context) {
        return super.visitAfter(after, context);
    }

    @Override
    public Void visitMeets(Meets meet, ElmContext context) {
        return super.visitMeets(meet, context);
    }

    @Override
    public Void visitOverlaps(Overlaps overlaps, ElmContext context) {
        return super.visitOverlaps(overlaps, context);
    }

    @Override
    public Void visitStarts(Starts starts, ElmContext context) {
        return super.visitStarts(starts, context);
    }

    @Override
    public Void visitEnds(Ends ends, ElmContext context) {
        return super.visitEnds(ends, context);
    }

    @Override
    public Void visitLiteral(Literal literal, ElmContext context) {
        if (literal.getResultType().toLabel().equals("System.String")) {
            output.append(String.format("\'%s\'", literal.getValue()));
        } else {
            output.append(literal.getValue());
        }
        return super.visitLiteral(literal, context);
    }

    @Override
    public Void visitInterval(Interval interval, ElmContext context) {
        return super.visitInterval(interval, context);
    }

    @Override
    public Void visitFunctionRef(FunctionRef function, ElmContext context) {
        if (function.getLibraryName() != null && function.getLibraryName().equals("FHIRHelpers")) {
            function.getOperand().stream().forEach(operand -> visitExpression(operand, context));
        } else if (function.getLibraryName() != null) {
            if (function.getLibraryName().equals("System")) {
                //log system function
                output.append(String.format(" %s(", function.getName()));
                commaDeliminatedOperandVisitation(function, context);
                output.append(")");
            } else {
                output.append(String.format(" %s.%s", function.getLibraryName(), function.getName()));
                output.append("(");
                commaDeliminatedOperandVisitation(function, context);
                output.append(")");
            }
        } else {
            output.append(String.format(" %s(", function.getName()));
            commaDeliminatedOperandVisitation(function, context);
            output.append(")");
        }
        return null; //result;
    }

    private void commaDeliminatedOperandVisitation(FunctionRef function, ElmContext context) {
        int operandCount = 0;
        for (Expression operand : function.getOperand()) {
            if (operandCount > 0) {
                output.append(", ");
            }
            visitExpression(operand, context);
            operandCount++;
        }
    }

    public Object visitParamList(cqlParser.ParamListContext ctx) {
        return null;
    }

    @Override
    public Void visitQuantity(Quantity quantity, ElmContext context) {
        if (quantity.getValue() != null) {
            output.append(quantity.getValue());
        }
        if (quantity.getUnit() != null) {
            output.append(" \'" + quantity.getUnit() + "\'");
        }
        //System.out.println(output.toString());
        return super.visitQuantity(quantity, context);
    }

    @Override
    public Void visitProperty(Property property, ElmContext context) {
        if (property.getSource() != null) {
            if (property.getSource() instanceof As) {
                output.append("(");
                visitExpression(property.getSource(), context);
                output.append(")");
            } else {
                visitExpression(property.getSource(), context);
            }
        } else if (!Strings.isNullOrEmpty(property.getScope())) {
            if (property.getScope().equals("$this")) {
                // log internalValidation
            } else {
                output.append(" " + property.getScope());
            }
        }
        String removeResourceType = Arrays.stream(property.getPath().split("\\."))
            .filter(split -> !(split.equals("Observation") || split.equals("Condition") || split.equals("System")))
            .collect(Collectors.joining("."));
        output.append(String.format(".%s", removeResourceType));
        return super.visitProperty(property, context);
    }

    @Override 
    public Void visitInValueSet(InValueSet inValueSet, ElmContext context) {
        visitExpression(inValueSet.getCode(), context);
        output.append(" in");
        visitExpression(inValueSet.getValueset(), context);
        return super.visitInValueSet(inValueSet, context);
    }

    @Override
    public Void visitExpressionRef(ExpressionRef expressionRef, ElmContext context) {
        if (expressionRef instanceof FunctionRef) {
            visitFunctionRef((FunctionRef) expressionRef, context);
        } else {
            if (expressionRef.getLibraryName() != null) {
                output.append(String.format(" \"%s\".\"%s\"", expressionRef.getLibraryName(), expressionRef.getName()));
            } else {
                output.append(String.format(" \"%s\"", expressionRef.getName()));
            }
        }
        return super.visitExpressionRef(expressionRef, context);
    }

    @Override
    public Void visitValueSetRef(ValueSetRef valueSetRef, ElmContext context) {
        if (valueSetRef.getLibraryName() != null) {
            output.append(String.format(" \"%s\".\"%s\"", valueSetRef.getLibraryName(), valueSetRef.getName()));
        } else {
            output.append(" \"" + valueSetRef.getName() + "\"");
        }
        return super.visitValueSetRef(valueSetRef, context);
    }

    public Void visitCodeRef(CodeRef codeRef, ElmContext context) {
        if (codeRef.getLibraryName() != null) {
            output.append(String.format("\"%s\".\"%s\"", codeRef.getLibraryName(), codeRef.getName()));
        } else {
            output.append("\"" + codeRef.getName() + "\"");
        }
        return null;
    }

    @Override
    public Void visitToConcept(ToConcept toConcept, ElmContext context) {
        output.append("ToConcept(");
        if (toConcept.getOperand() instanceof CodeRef) {
            visitCodeRef((CodeRef)toConcept.getOperand(), context);
        } else {
            visitExpression(toConcept.getOperand(), context);
        }
        output.append(")");
        return super.visitToConcept(toConcept, context);
    }

    @Override
    public Void visitToQuantity(ToQuantity toQuantity, ElmContext context) {
        output.append("ToQuantity(");
        visitExpression(toQuantity.getOperand(), context);
        output.append(")");
        return super.visitToQuantity(toQuantity, context);
    }

    @Override
    public Void visitToDecimal(ToDecimal toDecimal, ElmContext context) {
        output.append("ToDecimal(");
        visitExpression(toDecimal.getOperand(), context);
        output.append(")");
        return super.visitToDecimal(toDecimal, context);
    }

    @Override
    public Void visitElement(Element elm, ElmContext context) {
        if (elm instanceof CodeRef) return visitCodeRef((CodeRef) elm, context);
        else return super.visitElement(elm, context);
    }

    @Override
    public Void visitList(org.hl7.elm.r1.List list, ElmContext context) {
        output.append("{ ");
        if (list.getTypeSpecifier() != null) {
            visitElement(list.getTypeSpecifier(), context);
        }
        int elementPosition = 0;
        for (Expression element : list.getElement()) {
            if (elementPosition > 0) {
                output.append(", ");
            }
            visitElement(element, context);
            elementPosition++;
        }
        output.append(" }");
        return null;
    }

    @Override
    public Void visitAs(As as, ElmContext context) {
        visitExpression(as.getOperand(), context);
        output.append(" as");
        visitTypeSpecifier(as.getAsTypeSpecifier(), context);
        return super.visitAs(as, context);
    }

    private static class SyntaxErrorListener extends BaseErrorListener {

        private List<Exception> errors = new ArrayList<>();

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer,
                                Object offendingSymbol,
                                int line, int charPositionInLine,
                                String msg, RecognitionException e)
        {
            if (!((Token) offendingSymbol).getText().trim().isEmpty()) {
                errors.add(new Exception(String.format("[%d:%d]: %s", line, charPositionInLine, msg)));
            }
        }
    }

    public String getOutput() {
        return output.toString();
    }
}
