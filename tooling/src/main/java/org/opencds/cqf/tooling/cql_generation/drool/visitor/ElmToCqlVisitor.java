package org.opencds.cqf.tooling.cql_generation.drool.visitor;

import java.util.Arrays;
import java.util.Stack;
import java.util.stream.Collectors;

import org.cqframework.cql.elm.visiting.BaseElmLibraryVisitor;
import org.hl7.elm.r1.*;
import org.hl7.elm.r1.Library.Statements;
import org.opencds.cqf.tooling.cql_generation.context.ElmContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * Visits every node in a Library elm tree and builds the cql string.
 * 
 * @author Joshua Reynolds
 * @since 2021-04-05
 */
public class ElmToCqlVisitor extends BaseElmLibraryVisitor<Void, ElmContext> {
    private static final Logger logger = LoggerFactory.getLogger(ElmToCqlVisitor.class);
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
    @SuppressWarnings("unused")
    private boolean onNewLine;
    // private boolean needsWhitespace;
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

    @SuppressWarnings("unused")
    private int typeSpecifierLevel = 0;

    private void enterTypeSpecifier() {
        typeSpecifierLevel++;
    }

    private void exitTypeSpecifier() {
        typeSpecifierLevel--;
    }

    // private boolean inTypeSpecifier() {
    //     return typeSpecifierLevel > 0;
    // }

    // private int functionDefinitionLevel = 0;

    // private void enterFunctionDefinition() {
    //     functionDefinitionLevel++;
    // }

    // private void exitFunctionDefinition() {
    //     functionDefinitionLevel--;
    // }

    // private boolean inFunctionDefinition() {
    //     return functionDefinitionLevel > 0;
    // }

    // private int functionInvocationLevel = 0;

    // private void enterFunctionInvocation() {
    //     functionInvocationLevel++;
    // }

    // private void exitFunctionInvocation() {
    //     functionInvocationLevel--;
    // }

    // private boolean inFunctionInvocation() {
    //     return functionInvocationLevel > 0;
    // }

    @SuppressWarnings("unused")
    private int retrieveLevel = 0;

    private void enterRetrieve() {
        retrieveLevel++;
    }

    private void exitRetrieve() {
        retrieveLevel--;
    }

    // private boolean inRetrieve() {
    //     return retrieveLevel > 0;
    // }

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

    // private boolean needsWhitespaceBefore(String terminal) {
    //     if (terminal.trim().isEmpty() || terminal.startsWith("//") || terminal.startsWith("/*")) {
    //         return false;
    //     }
    //     switch (terminal) {
    //         case ":":
    //             return false;
    //         case ".":
    //             return false;
    //         case ",":
    //             return false;
    //         case "<":
    //             return !inTypeSpecifier();
    //         case ">":
    //             return !inTypeSpecifier();
    //         case "(":
    //             return !inFunctionDefinition() && !inFunctionInvocation();
    //         case ")":
    //             return !inFunctionDefinition() && !inFunctionInvocation();
    //         case "[":
    //             return inRetrieve();
    //         case "]":
    //             return false;
    //         default:
    //             return true;
    //     }
    // }

    // private boolean needsWhitespaceAfter(String terminal) {
    //     switch (terminal) {
    //         case ".":
    //             return false;
    //         case "<":
    //             return !inTypeSpecifier();
    //         case ">":
    //             return !inTypeSpecifier();
    //         case "(":
    //             return !inFunctionDefinition() && !inFunctionInvocation();
    //         case ")":
    //             return !inFunctionDefinition() && !inFunctionInvocation();
    //         case "[":
    //             return false;
    //         case "]":
    //             return inRetrieve();
    //         default:
    //             return true;
    //     }
    // }

    // private void appendTerminal(String terminal) {
    //     if (needsWhitespaceBefore(terminal)) {
    //         ensureWhitespace();
    //     }
    //     if (terminal.equals("else")) {
    //         increaseIndentLevel();
    //         newLine();
    //         decreaseIndentLevel();
    //     }
    //     if (terminal.equals("end")) {
    //         newLine();
    //     }
    //     output.append(terminal);
    //     onNewLine = false;
    //     needsWhitespace = needsWhitespaceAfter(terminal);
    // }

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
        logger.debug("Adding new construct: {}", section);
        resetIndentLevel();
        newLine();
        addToSection(section);
    }

    // private void ensureWhitespace() {
    //     if (!onNewLine && needsWhitespace) {
    //         output.append(space);
    //     }
    // }

    private void reset() {
        resetIndentLevel();
        currentLine = 1;
        onNewLine = true;
        output = new StringBuilder();
        groups = new Stack<>();
    }

    /**
     * Visit a Library. This method will be called for
     * every node in the tree that is a Library.
     *
     * @param library     the Library
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitLibrary(Library library, ElmContext context) {
        reset();
        resetIndentLevel();
        addToSection("library");
        String id = library.getIdentifier().getId();
        String version = library.getIdentifier().getVersion();
        output.append(currentSection + " " + id + " version " + "\'" + version + "\'");
        super.visitLibrary(library, context);
        newLine();
        return null;
    }

    /**
     * Visit a UsingDef. This method will be called for
     * every node in the tree that is a UsingDef.
     *
     * @param using     the UsingDef
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitUsingDef(UsingDef using, ElmContext context) {
        if (!using.getLocalIdentifier().equals("System")) {
            newConstruct("using");
            output.append(String.format("%s %s version \'%s\'", currentSection, using.getLocalIdentifier(), using.getVersion()));
        }
        return super.visitUsingDef(using, context);
    }

    /**
     * Visit a IncludeDef. This method will be called for
     * every node in the tree that is a IncludeDef.
     *
     * @param include     the IncludeDef
     * @param context the context passed to the visitor
     * @return the visitor result
     */
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

    /**
     * Visit a CodeSystemDef. This method will be called for
     * every node in the tree that is a CodeSystemDef.
     *
     * @param codeSystem     the CodeSystemDef
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitCodeSystemDef(CodeSystemDef codeSystem, ElmContext context) {
        newConstruct("codesystem");
        super.visitCodeSystemDef(codeSystem, context);
        output.append(String.format("%s \"%s\" : \'%s\'", currentSection, codeSystem.getName(), codeSystem.getId()));
        if (!Strings.isNullOrEmpty(codeSystem.getVersion())) {
            output.append(String.format("version %s", codeSystem.getVersion()));
        }
        return null;
    }

    /**
     * Visit a ValueSetDef. This method will be called for
     * every node in the tree that is a ValueSetDef.
     *
     * @param valueset     the ValueSetDef
     * @param context the context passed to the visitor
     * @return the visitor result
     */
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
        return null;
    }

    /**
     * Visit a CodeSystemRef. This method will be called for
     * every node in the tree that is a CodeSystemRef.
     *
     * @param codeSystem     the CodeSystemRef
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitCodeSystemRef(CodeSystemRef codeSystem, ElmContext context) {
        output.append(" ");
        if (!Strings.isNullOrEmpty(codeSystem.getLibraryName())) {
            output.append(String.format("\"%s\".", codeSystem.getLibraryName()));
        }
        output.append(String.format("\"%s\"",codeSystem.getName()));
        return null;
    }

    /**
     * Visit a CodeDef. This method will be called for
     * every node in the tree that is a CodeDef.
     *
     * @param code     the CodeDef
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
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

    /**
     * Visit a ConceptDef. This method will be called for
     * every node in the tree that is a ConceptDef.
     *
     * @param concept     the ConceptDef
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    public Void visitConceptDef(ConceptDef concept, ElmContext context) {
        newConstruct("concept");
        return null;
    }

    /**
     * Visit a ParameterDef. This method will be called for
     * every node in the tree that is a ParameterDef.
     *
     * @param parameter     the ParamterDef
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitParameterDef(ParameterDef parameter, ElmContext context) {
        newConstruct("parameter");
        return super.visitParameterDef(parameter, context);
    }

    /**
     * Visit a IdentifierRef. This method will be called for
     * every node in the tree that is a IdentifierRef.
     *
     * @param identifier     the IdentifierRef
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitIdentifierRef(IdentifierRef identifier, ElmContext context) {
        return super.visitIdentifierRef(identifier, context);
    }

    /**
     * Visit AccessModifier. This method will be called for
     * every node in the tree that is a AccessModifier.
     *
     * @param access     the AccessModifier
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    public Void visitAccessModifier(AccessModifier access, ElmContext context) {
        if (access.equals(AccessModifier.PRIVATE)) {
            output.append("private ");
        } else {
            logger.debug("Default cql output to ignore public access modifier");
        }
        return null;
    }

    /**
     * Visit a TypeSpecifier. This method will be called for
     * every node in the tree that is a TypeSpecifier.
     *
     * @param typeSpecifier     the TypeSpecifier
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitTypeSpecifier(TypeSpecifier typeSpecifier, ElmContext context) {
        enterTypeSpecifier();
        try {
            return super.visitTypeSpecifier(typeSpecifier, context);
        } finally {
            exitTypeSpecifier();
        }
    }

    /**
     * Visit a NamedTypeSpecifier. This method will be called for
     * every node in the tree that is a NamedTypeSpecifier.
     *
     * @param namedTypeSpecifier     the NamedTypeSpecifier
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitNamedTypeSpecifier(NamedTypeSpecifier namedTypeSpecifier, ElmContext context) {
        output.append(" " + namedTypeSpecifier.getResultType().toLabel());
        return super.visitNamedTypeSpecifier(namedTypeSpecifier, context);
    }

    /**
     * Visit a ListTypeSpecifier. This method will be called for
     * every node in the tree that is a ListTypeSpecifier.
     *
     * @param typeSpecifier     the ListTypeSpecifier
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitListTypeSpecifier(ListTypeSpecifier typeSpecifier, ElmContext context) {
        return super.visitListTypeSpecifier(typeSpecifier, context);
    }
  
    /**
     * Visit a IntervalTypeSpecifier. This method will be called for
     * every node in the tree that is a IntervalTypeSpecifier.
     *
     * @param intervalTypeSpecifier     the IntervalTypeSpecifier
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitIntervalTypeSpecifier(IntervalTypeSpecifier intervalTypeSpecifier, ElmContext context) {
        return super.visitIntervalTypeSpecifier(intervalTypeSpecifier, context);
    }

    /**
     * Visit a TupleTypeSpecifier. This method will be called for
     * every node in the tree that is a TupleTypeSpecifier.
     *
     * @param typeSpecifier     the TupleTypeSpecifier
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitTupleTypeSpecifier(TupleTypeSpecifier typeSpecifier, ElmContext context) {
        isFirstTupleElement = true;
        return super.visitTupleTypeSpecifier(typeSpecifier, context);
    }

    /**
     * Visit a TupleElementDefinition. This method will be called for
     * every node in the tree that is a TupleElementDefinition.
     *
     * @param tuple     the TupleElementDefinition
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitTupleElementDefinition(TupleElementDefinition tuple, ElmContext context) {
        if (isFirstTupleElement) {
            increaseIndentLevel();
            isFirstTupleElement = false;
        }
        newLine();
        return super.visitTupleElementDefinition(tuple, context);
    }

    /**
     * Visit a ChoiceTypeSpecifier. This method will be called for
     * every node in the tree that is a ChoiceTypeSpecifier.
     *
     * @param choice     the ChoiceTypeSpecifier
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    public Void visitChoiceTypeSpecifier(ChoiceTypeSpecifier choice, ElmContext context) {
        return null;
    }

    /**
     * Visit Statements. This method will be called for
     * every node in the tree that is a Statements.
     *
     * @param statements     the Statements
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    public Void visitStatements(Statements statements, ElmContext context) {
        newConstruct("statement");
        int n = statements.getDef().size();
        for (int i=0; i<n; i++) {
            Element c = statements.getDef().get(i);
            if (c instanceof ExpressionDef) {
                enterClause();
                try {
                    visitExpressionDef((ExpressionDef)c, context);
                }
                finally {
                    exitClause();
                }
            } else if (c instanceof ContextDef) {
                visitContextDef((ContextDef)c, context);
            } else if (c instanceof FunctionDef) {
                visitFunctionDef((FunctionDef)c, context);
            }
        }
        return null;
    }

    /**
     * Visit ExpressionDef. This method will be called for
     * every node in the tree that is a ExpressionDef.
     *
     * @param expressionDef     the ExpressionDef
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitExpressionDef(ExpressionDef expressionDef, ElmContext context) {
        newConstruct("statement");
        output.append("define");
        if (expressionDef.getAccessLevel() != null) {
            visitAccessModifier(expressionDef.getAccessLevel(), context);
        }
        output.append(String.format(" \"%s\":", expressionDef.getName()));
        enterClause();
        if (expressionDef.getExpression() != null) {
            visitElement(expressionDef.getExpression(), context);
        }
        if (expressionDef instanceof FunctionDef) {
            visitFunctionDef((FunctionDef)expressionDef, context);
        }
        exitClause();
        return null;
    }

    /**
     * Visit ContextDef. This method will be called for
     * every node in the tree that is a ContextDef.
     *
     * @param contextDef     the ContextDef
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    public Void visitContextDef(ContextDef contextDef, ElmContext context) {
        newConstruct("context");
        output.append(String.format("%s %s", currentSection, contextDef.getName()));
        return super.visitContextDef(contextDef, context);
    }

    /**
     * Visit FunctionDef. This method will be called for
     * every node in the tree that is a FunctionDef.
     *
     * @param function     the FunctionDef
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitFunctionDef(FunctionDef function, ElmContext context) {
        newConstruct("statement");
        if (function.getAccessLevel() != null) {
            visitAccessModifier(function.getAccessLevel(), context);
        }
        if (function.getExpression() != null) {
            visitExpression(function.getExpression(), context);
        }
        else if (function.getResultTypeSpecifier() != null) {
            visitTypeSpecifier(function.getResultTypeSpecifier(), context);
        }

        return null;
    }

    /**
     * Visit OperandDef. This method will be called for
     * every node in the tree that is a OperandDef.
     *
     * @param operand     the OperandDef
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitOperandDef(OperandDef operand, ElmContext context) {
        return super.visitOperandDef(operand, context);
    }

    /**
     * Visit AliasedQuerySource. This method will be called for
     * every node in the tree that is a AliasedQuerySource.
     *
     * @param source     the AliasedQuerySource
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitAliasedQuerySource(AliasedQuerySource source, ElmContext context) {
        try {
            if (!(source.getExpression() instanceof Retrieve)) {
                enterClause();
                output.append("(");
            }
            super.visitAliasedQuerySource(source, context);
        } finally {
            if (!(source.getExpression() instanceof Retrieve)) {
                exitClause();
                newLine();
                output.append(")");
            }
        }
        output.append(" " + source.getAlias());
        return null;
    }

    /**
     * Visit AliasRef. This method will be called for
     * every node in the tree that is a AliasRef.
     *
     * @param alias     the AliasRef
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitAliasRef(AliasRef alias, ElmContext context) {
        output.append(" " + alias.getName());
        return super.visitAliasRef(alias, context);
    }

    /**
     * Visit RelationshipClause. This method will be called for
     * every node in the tree that is a RelationshipClause.
     *
     * @param relationship     the RelationshipClause
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitRelationshipClause(RelationshipClause relationship, ElmContext context) {
        boolean clauseEntered = false;     
        try {
            enterClause();
            super.visitRelationshipClause(relationship, context);
            clauseEntered = true;
        }
        finally {
            if (clauseEntered) {
                exitClause();
            }
        }
        return null;
    }

    /**
     * Visit With. This method will be called for
     * every node in the tree that is a With.
     *
     * @param with     the With
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitWith(With with, ElmContext context) {
        output.append(String.format("with"));
        return null;
    }

    /**
     * Visit Without. This method will be called for
     * every node in the tree that is a Without.
     *
     * @param without     the Without
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitWithout(Without without, ElmContext context) {
        output.append(String.format("without"));
        return null;
    }

    /**
     * Visit Retrieve. This method will be called for
     * every node in the tree that is a Retrieve.
     *
     * @param retrieve     the Retrieve
     * @param context the context passed to the visitor
     * @return the visitor result
     */
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
                visitElement(retrieve.getCodes(), context);
            }
            output.append("]");
            return null;
        }
        finally {
            exitRetrieve();
        }
    }

    /**
     * Visit Query. This method will be called for
     * every node in the tree that is a Query.
     *
     * @param query     the Query
     * @param context the context passed to the visitor
     * @return the visitor result
     */
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
                output.append("let");
                query.getLet().stream().forEach(let -> visitLetClause(let, context));
                exitClause();
            }
            if (query.getRelationship() != null && !query.getRelationship().isEmpty()) {
                query.getRelationship().stream().forEach(relationship -> visitRelationshipClause(relationship, context));
            }
            if (query.getWhere() != null) {
                visitExpression(query.getWhere(), context);
            }
            if (query.getReturn() != null) {
                visitReturnClause(query.getReturn(), context);
            }
            if (query.getSort() != null) {
                visitSortClause(query.getSort(), context);
            }
        }
        return null;
    }

    /**
     * Visit ToList. This method will be called for
     * every node in the tree that is a ToList.
     *
     * @param toList     the ToList
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitToList(ToList toList, ElmContext context) {
        output.append("{ ");
        super.visitElement(toList.getOperand(), context);
        output.append(" }");
        return null;
    }

    /**
     * Visit Union. This method will be called for
     * every node in the tree that is a Union.
     *
     * @param union     the Union
     * @param context the context passed to the visitor
     * @return the visitor result
     */
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

    /**
     * Visit Intersect. This method will be called for
     * every node in the tree that is a Intersect.
     *
     * @param intersect     the Intersect
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitIntersect(Intersect intersect, ElmContext context) {
        return null;
    }

    /**
     * Visit Except. This method will be called for
     * every node in the tree that is a Except.
     *
     * @param except     the Except
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitExcept(Except except, ElmContext context) {
        return null;
    }

    /**
     * Visit LetClause. This method will be called for
     * every node in the tree that is a LetClause.
     *
     * @param let     the LetClause
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitLetClause(LetClause let, ElmContext context) {
        output.append(String.format(" %s: ", let.getIdentifier()));
        super.visitLetClause(let, context);
        newLine();
        return null;
    }

    /**
     * Visit WhereClause. This method will be called for
     * WhereClause expression nodes.
     *
     * @param where     the Expression
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    public Void visitWhereClause(Expression where, ElmContext context) {
        try {
            enterClause();
            output.append("where");
            visitElement(where, context);
            return null;
        }
        finally {
            exitClause();
        }
    }

    /**
     * Visit ReturnClause. This method will be called for
     * every node in the tree that is a ReturnClause.
     *
     * @param returnClause     the ReturnClause
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitReturnClause(ReturnClause returnClause, ElmContext context) {
        enterClause();
        output.append("return");
        try {
            return super.visitReturnClause(returnClause, context);
        }
        finally {
            exitClause();
        }
    }

    /**
     * Visit SortClause. This method will be called for
     * every node in the tree that is a SortClause.
     *
     * @param sortClause     the SortClause
     * @param context the context passed to the visitor
     * @return the visitor result
     */
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

    /**
     * Visit SortDirection. This method will be called for
     * every node in the tree that is a SortDirection.
     *
     * @param sortDirection     the SortDirection
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    public Void visitSortDirection(SortDirection sortDirection, ElmContext context) {
        return null;
    }

    /**
     * Visit SortByItem. This method will be called for
     * every node in the tree that is a SortByItem.
     *
     * @param sortByItem     the SortByItem
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitSortByItem(SortByItem sortByItem, ElmContext context) {
        return super.visitElement(sortByItem, context);
    }

    /**
     * Visit DurationBetween. This method will be called for
     * every node in the tree that is a DurationBetween.
     *
     * @param durationBetween     the DurationBetween
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitDurationBetween(DurationBetween durationBetween, ElmContext context) {
        return super.visitElement(durationBetween, context);
    }

    /**
     * Visit Null. This method will be called for
     * every node in the tree that is a Null.
     *
     * @param nullElement     the Null
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitNull(Null nullElement, ElmContext context) {
        output.append("null");
        return super.visitNull(nullElement, context);
    }

    /**
     * Visit Not. This method will be called for
     * every node in the tree that is a Not.
     *
     * @param not     the Not
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitNot(Not not, ElmContext context) {
        output.append(" not ");
        return super.visitElement(not.getOperand(), context);
    }

    /**
     * Visit Equal. This method will be called for
     * every node in the tree that is a Equal.
     *
     * @param equal     the Equal
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitEqual(Equal equal, ElmContext context) {
        output.append(" Equal(");
        visitElement(equal.getOperand().get(0), context);
        output.append(", ");
        visitElement(equal.getOperand().get(1), context);
        output.append(")");
        return null;
    }

    /**
     * Visit Or. This method will be called for
     * every node in the tree that is a Or.
     *
     * @param or     the Or
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitOr(Or or, ElmContext context) {
        visitElement(or.getOperand().get(0), context);
        newLine();
        enterGroup();
        output.append(" or (");
        visitElement(or.getOperand().get(1), context);
        exitGroup();
        output.append(")");
        return null;
    }

    /**
     * Visit Equivalent. This method will be called for
     * every node in the tree that is a Equivalent.
     *
     * @param equivalent     the Equivalent
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitEquivalent(Equivalent equivalent, ElmContext context) {
        visitElement(equivalent.getOperand().get(0), context);
        output.append(" ~ ");
        visitElement(equivalent.getOperand().get(1), context);
        return null;
    }

    /**
     * Visit In. This method will be called for
     * every node in the tree that is a In.
     *
     * @param in     the In
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitIn(In in, ElmContext context) {
        visitElement(in.getOperand().get(0), context);
        output.append(" in ");
        visitElement(in.getOperand().get(1), context);
        return null;
    }

    /**
     * Visit And. This method will be called for
     * every node in the tree that is a And.
     *
     * @param and     the And
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitAnd(And and, ElmContext context) {
        visitElement(and.getOperand().get(0), context);
        newLine();
        enterGroup();
        output.append(" and (");
        visitElement(and.getOperand().get(1), context);
        exitGroup();
        output.append(")");
        return null;
    }

    /**
     * Visit DifferenceBetween. This method will be called for
     * every node in the tree that is a DifferenceBetween.
     *
     * @param differenceBetween     the DifferenceBetween
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitDifferenceBetween(DifferenceBetween differenceBetween, ElmContext context) {
        return super.visitDifferenceBetween(differenceBetween, context);
    }

    /**
     * Visit Exists. This method will be called for
     * every node in the tree that is a Exists.
     *
     * @param exists     the Exists
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitExists(Exists exists, ElmContext context) {
        output.append("exists (");
        visitElement(exists.getOperand(), context);
        exitClause();
        newLine();
        output.append(")");
        return null;
    }

    /**
     * Visit Implies. This method will be called for
     * every node in the tree that is a Implies.
     *
     * @param implies     the Implies
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitImplies(Implies implies, ElmContext context) {
        return null;
    }

    /**
     * Visit DateTimePrecision. This method will be called for
     * every node in the tree that is a DateTimePrecision.
     *
     * @param dateTimePrecision     the DateTimePrecision
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    public Void visitDateTimePrecision(DateTimePrecision dateTimePrecision, ElmContext context) {
        return null;
    }

    /**
     * Visit DateTimeComponentFrom. This method will be called for
     * every node in the tree that is a DateTimeComponentFrom.
     *
     * @param dateTime     the DateTimeComponentFrom
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitDateTimeComponentFrom(DateTimeComponentFrom dateTime, ElmContext context) {
        return super.visitDateTimeComponentFrom(dateTime, context);
    }

    /**
     * Visit Today. This method will be called for
     * every node in the tree that is a Today.
     *
     * @param today     the Today
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitToday(Today today, ElmContext context) {
        output.append(" Today()");
        return super.visitToday(today, context);
    }

    /**
     * Visit Add. This method will be called for
     * every node in the tree that is a Add.
     *
     * @param add     the Add
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitAdd(Add add, ElmContext context) {
        return super.visitAdd(add, context);
    }

    /**
     * Visit Width. This method will be called for
     * every node in the tree that is a Width.
     *
     * @param width     the Width
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitWidth(Width width, ElmContext context) {
        return super.visitWidth(width, context);
    }

    /**
     * Visit Time. This method will be called for
     * every node in the tree that is a Time.
     *
     * @param time     the Time
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitTime(Time time, ElmContext context) {
        return super.visitTime(time, context);
    }

    /**
     * Visit If. This method will be called for
     * every node in the tree that is a If.
     *
     * @param ifExpression     the If
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitIf(If ifExpression, ElmContext context) {
        return super.visitIf(ifExpression, context);
    }

    /**
     * Visit Expand. This method will be called for
     * every node in the tree that is a Expand.
     *
     * @param expand     the Expand
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitExpand(Expand expand, ElmContext context) {
        return null;
    }

    /**
     * Visit Flatten. This method will be called for
     * every node in the tree that is a Flatten.
     *
     * @param flatten     the Flatten
     * @param context the context passed to the visitor
     * @return the visitor result
     */
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

    /**
     * Visit Distinct. This method will be called for
     * every node in the tree that is a Distinct.
     *
     * @param distinct     the Distinct
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitDistinct(Distinct distinct, ElmContext context) {
        output.append("distinct (");
        enterClause();
        visitExpression(distinct.getOperand(), context);
        exitClause();
        newLine();
        output.append(")");
        return null;
    }

    /**
     * Visit First. This method will be called for
     * every node in the tree that is a First.
     *
     * @param first     the First
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitFirst(First first, ElmContext context) {
        output.append(" First (");
        enterClause();
        visitExpression(first.getSource(), context);
        exitClause();
        newLine();
        output.append(")");
        return null;
    }

    /**
     * Visit Last. This method will be called for
     * every node in the tree that is a Last.
     *
     * @param last     the Last
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitLast(Last last, ElmContext context) {
        output.append(" Last (");
        enterClause();
        visitExpression(last.getSource(), context);
        exitClause();
        newLine();
        output.append(")");
        return null;
    }

    /**
     * Visit Split. This method will be called for
     * every node in the tree that is a Split.
     *
     * @param split     the Split
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitSplit(Split split, ElmContext context) {
        output.append("Split (");
        visitExpression(split.getStringToSplit(), context);
        output.append(", ");
        visitExpression(split.getSeparator(), context);
        output.append(")");
        return null;
    }

    /**
     * Visit AnyInValueSet. This method will be called for
     * every node in the tree that is a AnyInValueSet.
     *
     * @param anyInValueSet     the AnyInValueSet
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitAnyInValueSet(AnyInValueSet anyInValueSet, ElmContext context) {
        output.append("AnyInValueSet(");
        visitExpression(anyInValueSet.getCodes(), context);
        output.append(", ");
        visitExpression(anyInValueSet.getValueset(), context);
        output.append(")");
        return null;
    }

    /**
     * Visit Collapse. This method will be called for
     * every node in the tree that is a Collapse.
     *
     * @param collapse     the Collapse
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitCollapse(Collapse collapse, ElmContext context) {
        return null;
    }

    /**
     * Visit Predecessor. This method will be called for
     * every node in the tree that is a Predecessor.
     *
     * @param predecessor     the Predecessor
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitPredecessor(Predecessor predecessor, ElmContext context) {
        return super.visitPredecessor(predecessor, context);
    }

    /**
     * Visit Multiply. This method will be called for
     * every node in the tree that is a Multiply.
     *
     * @param multiply     the Multiply
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitMultiply(Multiply multiply, ElmContext context) {
        return super.visitMultiply(multiply, context);
    }

    /**
     * Visit AggregateExpression. This method will be called for
     * every node in the tree that is a AggregateExpression.
     *
     * @param expression     the AggregateExpression
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitAggregateExpression(AggregateExpression expression, ElmContext context) {
        if (expression instanceof Count) {
            output.append("Count(");
            visitExpression(expression.getSource(), context);
            output.append(")");
        }
        return super.visitAggregateExpression(expression, context);
    }

    /**
     * Visit Case. This method will be called for
     * every node in the tree that is a Case.
     *
     * @param caseExpression     the Case
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitCase(Case caseExpression, ElmContext context) {
            newLine();
            if (previousIndentLevel == indentLevel) {
                increaseIndentLevel();
            }

        return super.visitCase(caseExpression, context);
    }

    /**
     * Visit Power. This method will be called for
     * every node in the tree that is a Power.
     *
     * @param power     the Power
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitPower(Power power, ElmContext context) {
        return super.visitPower(power, context);
    }

    /**
     * Visit Successor. This method will be called for
     * every node in the tree that is a Successor.
     *
     * @param successor     the Successor
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitSuccessor(Successor successor, ElmContext context) {
        return super.visitSuccessor(successor, context);
    }

    /**
     * Visit CaseItem. This method will be called for
     * every node in the tree that is a CaseItem.
     *
     * @param caseItem     the CaseItem
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitCaseItem(CaseItem caseItem, ElmContext context) {
        try {
            enterClause();
            return super.visitCaseItem(caseItem, context);
        } finally {
            exitClause();
        }
    }

    /**
     * Visit Less. This method will be called for
     * every node in the tree that is a Less.
     *
     * @param less     the Less
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitLess(Less less, ElmContext context) {
        int operandCount = 0;
        for (Expression expression : less.getOperand()) {
            if (operandCount > 0) {
                output.append(" < ");
            }
            visitElement(expression, context);
            operandCount++;
        }
        return null;
    }

    /**
     * Visit LessOrEqual. This method will be called for
     * every node in the tree that is a LessOrEqual.
     *
     * @param lessOrElement     the LessOrEqual
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitLessOrEqual(LessOrEqual lessOrElement, ElmContext context) {
        int operandCount = 0;
        for (Expression expression : lessOrElement.getOperand()) {
            if (operandCount > 0) {
                output.append(" <= ");
            }
            visitElement(expression, context);
            operandCount++;
        }
        return null;
    }

    /**
     * Visit Greater. This method will be called for
     * every node in the tree that is a Greater.
     *
     * @param greater     the Greater
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitGreater(Greater greater, ElmContext context) {
        int operandCount = 0;
        for (Expression expression : greater.getOperand()) {
            if (operandCount > 0) {
                output.append(" > ");
            }
            visitElement(expression, context);
            operandCount++;
        }
        return null;
    }

    /**
     * Visit GreaterOrEqual. This method will be called for
     * every node in the tree that is a GreaterOrEqual.
     *
     * @param greaterOrElement     the GreaterOrEqual
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitGreaterOrEqual(GreaterOrEqual greaterOrElement, ElmContext context) {
        int operandCount = 0;
        for (Expression expression : greaterOrElement.getOperand()) {
            if (operandCount > 0) {
                output.append(" >= ");
            }
            visitElement(expression, context);
            operandCount++;
        }
        return null;
    }

    /**
     * Visit Includes. This method will be called for
     * every node in the tree that is a Includes.
     *
     * @param includes     the Includes
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitIncludes(Includes includes, ElmContext context) {
        return super.visitIncludes(includes, context);
    }

    /**
     * Visit IncludedIn. This method will be called for
     * every node in the tree that is a IncludedIn.
     *
     * @param includedIn     the IncludedIn
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitIncludedIn(IncludedIn includedIn, ElmContext context) {
        return super.visitIncludedIn(includedIn, context);
    }

    /**
     * Visit BinaryExpression. This method will be called for
     * every node in the tree that is a BinaryExpression.
     *
     * @param beforeOrAfter     the BinaryExpression
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    public Object visitBeforeOrAfter(BinaryExpression beforeOrAfter, ElmContext context) {
        return null;
    }

    /**
     * Visit Before. This method will be called for
     * every node in the tree that is a Before.
     *
     * @param before     the Before
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitBefore(Before before, ElmContext context) {
        return super.visitBefore(before, context);
    }

    /**
     * Visit After. This method will be called for
     * every node in the tree that is a After.
     *
     * @param after     the After
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitAfter(After after, ElmContext context) {
        return super.visitAfter(after, context);
    }

    /**
     * Visit Meets. This method will be called for
     * every node in the tree that is a Meets.
     *
     * @param meet     the Meets
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitMeets(Meets meet, ElmContext context) {
        return super.visitMeets(meet, context);
    }

    /**
     * Visit Overlaps. This method will be called for
     * every node in the tree that is a Overlaps.
     *
     * @param overlaps     the Overlaps
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitOverlaps(Overlaps overlaps, ElmContext context) {
        return super.visitOverlaps(overlaps, context);
    }

    /**
     * Visit Starts. This method will be called for
     * every node in the tree that is a Starts.
     *
     * @param starts     the Starts
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitStarts(Starts starts, ElmContext context) {
        return super.visitStarts(starts, context);
    }

    /**
     * Visit Ends. This method will be called for
     * every node in the tree that is a Ends.
     *
     * @param ends     the Ends
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitEnds(Ends ends, ElmContext context) {
        return super.visitEnds(ends, context);
    }

    /**
     * Visit Literal. This method will be called for
     * every node in the tree that is a Literal.
     *
     * @param literal     the Literal
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitLiteral(Literal literal, ElmContext context) {
        if (literal.getResultType().toLabel().equals("System.String")) {
            output.append(String.format("\'%s\'", literal.getValue()));
        } else {
            output.append(literal.getValue());
        }
        return super.visitLiteral(literal, context);
    }

    /**
     * Visit Interval. This method will be called for
     * every node in the tree that is a Interval.
     *
     * @param interval     the Interval
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitInterval(Interval interval, ElmContext context) {
        return super.visitInterval(interval, context);
    }

    /**
     * Visit FunctionRef. This method will be called for
     * every node in the tree that is a FunctionRef.
     *
     * @param function     the FunctionRef
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitFunctionRef(FunctionRef function, ElmContext context) {
        if (function.getLibraryName() != null && function.getLibraryName().equals("FHIRHelpers")) {
            function.getOperand().stream().forEach(operand -> visitElement(operand, context));
        } else if (function.getLibraryName() != null) {
            if (function.getLibraryName().equals("System")) {
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
        return null;
    }

    private void commaDeliminatedOperandVisitation(FunctionRef function, ElmContext context) {
        int operandCount = 0;
        for (Expression operand : function.getOperand()) {
            if (operandCount > 0) {
                output.append(", ");
            }
            visitElement(operand, context);
            operandCount++;
        }
    }

    /**
     * Visit Quantity. This method will be called for
     * every node in the tree that is a Quantity.
     *
     * @param quantity     the Quantity
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitQuantity(Quantity quantity, ElmContext context) {
        if (quantity.getValue() != null) {
            output.append(quantity.getValue());
        }
        if (quantity.getUnit() != null) {
            output.append(" \'" + quantity.getUnit() + "\'");
        }
        return super.visitQuantity(quantity, context);
    }

    /**
     * Visit Property. This method will be called for
     * every node in the tree that is a Property.
     *
     * @param property     the Property
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitProperty(Property property, ElmContext context) {
        if (property.getSource() != null) {
            if (property.getSource() instanceof As) {
                output.append("(");
                visitElement(property.getSource(), context);
                output.append(")");
            } else {
                visitElement(property.getSource(), context);
            }
        } else if (!Strings.isNullOrEmpty(property.getScope())) {
            if (property.getScope().equals("$this")) {
                logger.debug("Found internal scope, outputting property path only");
            } else {
                output.append(" " + property.getScope());
            }
        }
        String removeResourceType = Arrays.stream(property.getPath().split("\\."))
            .filter(split -> !(split.equals("Observation") || split.equals("Condition") || split.equals("System")))
            .collect(Collectors.joining("."));
        output.append(String.format(".%s", removeResourceType));
        return null;
    }

    /**
     * Visit InValueSet. This method will be called for
     * every node in the tree that is a InValueSet.
     *
     * @param inValueSet     the InValueSet
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override 
    public Void visitInValueSet(InValueSet inValueSet, ElmContext context) {
        visitExpression(inValueSet.getCode(), context);
        output.append(" in");
        visitExpression(inValueSet.getValueset(), context);
        return null;
    }

    /**
     * Visit ExpressionRef. This method will be called for
     * every node in the tree that is a ExpressionRef.
     *
     * @param expressionRef     the ExpressionRef
     * @param context the context passed to the visitor
     * @return the visitor result
     */
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
        return null;
    }

    /**
     * Visit ValueSetRef. This method will be called for
     * every node in the tree that is a ValueSetRef.
     *
     * @param valueSetRef     the ValueSetRef
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitValueSetRef(ValueSetRef valueSetRef, ElmContext context) {
        if (valueSetRef.getLibraryName() != null) {
            output.append(String.format(" \"%s\".\"%s\"", valueSetRef.getLibraryName(), valueSetRef.getName()));
        } else {
            output.append(" \"" + valueSetRef.getName() + "\"");
        }
        return null;
    }

    /**
     * Visit CodeRef. This method will be called for
     * every node in the tree that is a CodeRef.
     *
     * @param codeRef     the CodeRef
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitCodeRef(CodeRef codeRef, ElmContext context) {
        if (codeRef.getLibraryName() != null) {
            output.append(String.format("\"%s\".\"%s\"", codeRef.getLibraryName(), codeRef.getName()));
        } else {
            output.append("\"" + codeRef.getName() + "\"");
        }
        return null;
    }

    /**
     * Visit ToConcept. This method will be called for
     * every node in the tree that is a ToConcept.
     *
     * @param toConcept     the ToConcept
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitToConcept(ToConcept toConcept, ElmContext context) {
        output.append("ToConcept(");
        visitElement(toConcept.getOperand(), context);
        output.append(")");
        return null;
    }

    /**
     * Visit ToQuantity. This method will be called for
     * every node in the tree that is a ToQuantity.
     *
     * @param toQuantity     the ToQuantity
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitToQuantity(ToQuantity toQuantity, ElmContext context) {
        output.append("ToQuantity(");
        super.visitToQuantity(toQuantity, context);
        output.append(")");
        return null;
    }

    /**
     * Visit ToDecimal. This method will be called for
     * every node in the tree that is a ToDecimal.
     *
     * @param toDecimal     the ToDecimal
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitToDecimal(ToDecimal toDecimal, ElmContext context) {
        output.append("ToDecimal(");
        visitElement(toDecimal.getOperand(), context);
        output.append(")");
        return super.visitToDecimal(toDecimal, context);
    }

    /**
     * Visit List. This method will be called for
     * every node in the tree that is a List.
     *
     * @param list     the List
     * @param context the context passed to the visitor
     * @return the visitor result
     */
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

    /**
     * Visit As. This method will be called for
     * every node in the tree that is a As.
     *
     * @param as     the As
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitAs(As as, ElmContext context) {
        if (as.getOperand() != null) {
            visitExpression(as.getOperand(), context);
        }
        output.append(" as");
        if (as.getAsTypeSpecifier() != null) {
            visitElement(as.getAsTypeSpecifier(), context);
        }
        return null;
    }

    /**
     * Visit UnaryExpression. This method will be called for
     * every node in the tree that is a UnaryExpression.
     *
     * @param elm     the UnaryExpression
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitUnaryExpression(UnaryExpression elm, ElmContext context) {
        if (elm instanceof Abs) return visitAbs((Abs)elm, context);
        else if (elm instanceof As) return visitAs((As)elm, context);
        else if (elm instanceof Ceiling) return visitCeiling((Ceiling)elm, context);
        else if (elm instanceof CanConvert) return visitCanConvert((CanConvert)elm, context);
        else if (elm instanceof Convert) return visitConvert((Convert)elm, context);
        else if (elm instanceof ConvertsToBoolean) return visitConvertsToBoolean((ConvertsToBoolean) elm, context);
        else if (elm instanceof ConvertsToDate) return visitConvertsToDate((ConvertsToDate)elm, context);
        else if (elm instanceof ConvertsToDateTime) return visitConvertsToDateTime((ConvertsToDateTime)elm, context);
        else if (elm instanceof ConvertsToDecimal) return visitConvertsToDecimal((ConvertsToDecimal)elm, context);
        else if (elm instanceof ConvertsToInteger) return visitConvertsToInteger((ConvertsToInteger)elm, context);
        else if (elm instanceof ConvertsToLong) return visitConvertsToLong((ConvertsToLong)elm, context);
        else if (elm instanceof ConvertsToQuantity) return visitConvertsToQuantity((ConvertsToQuantity)elm, context);
        else if (elm instanceof ConvertsToRatio) return visitConvertsToRatio((ConvertsToRatio)elm, context);
        else if (elm instanceof ConvertsToString) return visitConvertsToString((ConvertsToString)elm, context);
        else if (elm instanceof ConvertsToTime) return visitConvertsToTime((ConvertsToTime)elm, context);
        else if (elm instanceof DateFrom) return visitDateFrom((DateFrom)elm, context);
        else if (elm instanceof DateTimeComponentFrom) return visitDateTimeComponentFrom((DateTimeComponentFrom)elm, context);
        else if (elm instanceof Distinct) return visitDistinct((Distinct)elm, context);
        else if (elm instanceof End) return visitEnd((End)elm, context);
        else if (elm instanceof Exists) return visitExists((Exists)elm, context);
        else if (elm instanceof Exp) return visitExp((Exp)elm, context);
        else if (elm instanceof Flatten) return visitFlatten((Flatten)elm, context);
        else if (elm instanceof Floor) return visitFloor((Floor)elm, context);
        else if (elm instanceof Is) return visitIs((Is)elm, context);
        else if (elm instanceof IsFalse) return visitIsFalse((IsFalse)elm, context);
        else if (elm instanceof IsNull) return visitIsNull((IsNull)elm, context);
        else if (elm instanceof IsTrue) return visitIsTrue((IsTrue)elm, context);
        else if (elm instanceof Length) return visitLength((Length)elm, context);
        else if (elm instanceof Ln) return visitLn((Ln)elm, context);
        else if (elm instanceof Lower) return visitLower((Lower)elm, context);
        else if (elm instanceof Negate) return visitNegate((Negate)elm, context);
        else if (elm instanceof Not) return visitNot((Not)elm, context);
        else if (elm instanceof PointFrom) return visitPointFrom((PointFrom)elm, context);
        else if (elm instanceof Precision) return visitPrecision((Precision)elm, context);
        else if (elm instanceof Predecessor) return visitPredecessor((Predecessor)elm, context);
        else if (elm instanceof SingletonFrom) return visitSingletonFrom((SingletonFrom)elm, context);
        else if (elm instanceof Size) return visitSize((Size)elm, context);
        else if (elm instanceof Start) return visitStart((Start)elm, context);
        else if (elm instanceof Successor) return visitSuccessor((Successor)elm, context);
        else if (elm instanceof TimeFrom) return visitTimeFrom((TimeFrom)elm, context);
        else if (elm instanceof TimezoneFrom) return visitTimezoneFrom((TimezoneFrom)elm, context);
        else if (elm instanceof TimezoneOffsetFrom) return visitTimezoneOffsetFrom((TimezoneOffsetFrom)elm, context);
        else if (elm instanceof ToBoolean) return visitToBoolean((ToBoolean)elm, context);
        else if (elm instanceof ToConcept) return visitToConcept((ToConcept)elm, context);
        else if (elm instanceof ToChars) return visitToChars((ToChars)elm, context);
        else if (elm instanceof ToDate) return visitToDate((ToDate)elm, context);
        else if (elm instanceof ToDateTime) return visitToDateTime((ToDateTime)elm, context);
        else if (elm instanceof ToDecimal) return visitToDecimal((ToDecimal)elm, context);
        else if (elm instanceof ToInteger) return visitToInteger((ToInteger)elm, context);
        else if (elm instanceof ToLong) return visitToLong((ToLong)elm, context);
        else if (elm instanceof ToList) return visitToList((ToList)elm, context);
        else if (elm instanceof ToQuantity) return visitToQuantity((ToQuantity)elm, context);
        else if (elm instanceof ToRatio) return visitToRatio((ToRatio)elm, context);
        else if (elm instanceof ToString) return visitToString((ToString)elm, context);
        else if (elm instanceof ToTime) return visitToTime((ToTime)elm, context);
        else if (elm instanceof Truncate) return visitTruncate((Truncate)elm, context);
        else if (elm instanceof Upper) return visitUpper((Upper)elm, context);
        else if (elm instanceof Width) return visitWidth((Width)elm, context);
        else return null;
    }

    /**
     * Visit BinaryExpression. This method will be called for
     * every node in the tree that is a BinaryExpression.
     *
     * @param elm     the BinaryExpression
     * @param context the context passed to the visitor
     * @return the visitor result
     */
    @Override
    public Void visitBinaryExpression(BinaryExpression elm, ElmContext context) {
        if (elm instanceof Add) return visitAdd((Add)elm, context);
        else if (elm instanceof After) return visitAfter((After)elm, context);
        else if (elm instanceof And) return visitAnd((And)elm, context);
        else if (elm instanceof Before) return visitBefore((Before)elm, context);
        else if (elm instanceof CanConvertQuantity) return visitCanConvertQuantity((CanConvertQuantity)elm, context);
        else if (elm instanceof Contains) return visitContains((Contains)elm, context);
        else if (elm instanceof ConvertQuantity) return visitConvertQuantity((ConvertQuantity)elm, context);
        else if (elm instanceof Collapse) return visitCollapse((Collapse)elm, context);
        else if (elm instanceof DifferenceBetween) return visitDifferenceBetween((DifferenceBetween)elm, context);
        else if (elm instanceof Divide) return visitDivide((Divide)elm, context);
        else if (elm instanceof DurationBetween) return visitDurationBetween((DurationBetween)elm, context);
        else if (elm instanceof Ends) return visitEnds((Ends)elm, context);
        else if (elm instanceof EndsWith) return visitEndsWith((EndsWith)elm, context);
        else if (elm instanceof Equal) return visitEqual((Equal)elm, context);
        else if (elm instanceof Equivalent) return visitEquivalent((Equivalent)elm, context);
        else if (elm instanceof Expand) return visitExpand((Expand)elm, context);
        else if (elm instanceof Greater) return visitGreater((Greater)elm, context);
        else if (elm instanceof GreaterOrEqual) return visitGreaterOrEqual((GreaterOrEqual)elm, context);
        else if (elm instanceof HighBoundary) return visitHighBoundary((HighBoundary)elm, context);
        else if (elm instanceof Implies) return visitImplies((Implies)elm, context);
        else if (elm instanceof In) return visitIn((In)elm, context);
        else if (elm instanceof IncludedIn) return visitIncludedIn((IncludedIn)elm, context);
        else if (elm instanceof Includes) return visitIncludes((Includes)elm, context);
        else if (elm instanceof Indexer) return visitIndexer((Indexer)elm, context);
        else if (elm instanceof Less) return visitLess((Less)elm, context);
        else if (elm instanceof LessOrEqual) return visitLessOrEqual((LessOrEqual)elm, context);
        else if (elm instanceof Log) return visitLog((Log)elm, context);
        else if (elm instanceof LowBoundary) return visitLowBoundary((LowBoundary)elm, context);
        else if (elm instanceof Matches) return visitMatches((Matches)elm, context);
        else if (elm instanceof Meets) return visitMeets((Meets)elm, context);
        else if (elm instanceof MeetsAfter) return visitMeetsAfter((MeetsAfter)elm, context);
        else if (elm instanceof MeetsBefore) return visitMeetsBefore((MeetsBefore)elm, context);
        else if (elm instanceof Modulo) return visitModulo((Modulo)elm, context);
        else if (elm instanceof Multiply) return visitMultiply((Multiply)elm, context);
        else if (elm instanceof NotEqual) return visitNotEqual((NotEqual)elm, context);
        else if (elm instanceof Or) return visitOr((Or)elm, context);
        else if (elm instanceof Overlaps) return visitOverlaps((Overlaps)elm, context);
        else if (elm instanceof OverlapsAfter) return visitOverlapsAfter((OverlapsAfter)elm, context);
        else if (elm instanceof OverlapsBefore) return visitOverlapsBefore((OverlapsBefore)elm, context);
        else if (elm instanceof Power) return visitPower((Power)elm, context);
        else if (elm instanceof ProperContains) return visitProperContains((ProperContains)elm, context);
        else if (elm instanceof ProperIn) return visitProperIn((ProperIn)elm, context);
        else if (elm instanceof ProperIncludedIn) return visitProperIncludedIn((ProperIncludedIn)elm, context);
        else if (elm instanceof ProperIncludes) return visitProperIncludes((ProperIncludes)elm, context);
        else if (elm instanceof SameAs) return visitSameAs((SameAs)elm, context);
        else if (elm instanceof SameOrAfter) return visitSameOrAfter((SameOrAfter)elm, context);
        else if (elm instanceof SameOrBefore) return visitSameOrBefore((SameOrBefore)elm, context);
        else if (elm instanceof Starts) return visitStarts((Starts)elm, context);
        else if (elm instanceof StartsWith) return visitStartsWith((StartsWith)elm, context);
        else if (elm instanceof Subtract) return visitSubtract((Subtract)elm, context);
        else if (elm instanceof Times) return visitTimes((Times)elm, context);
        else if (elm instanceof TruncatedDivide) return visitTruncatedDivide((TruncatedDivide)elm, context);
        else if (elm instanceof Xor) return visitXor((Xor)elm, context);
        else return null;
    }

    public String getOutput() {
        return output.toString();
    }
}
