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
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

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
        output.append("library " + id + "\'" + version + "\'");
        super.visitLibrary(library, context);
        // newLine();
        return null;
    }

    @Override
    public Void visitUsingDef(UsingDef using, ElmContext context) {
        newConstruct("using");
        return super.visitUsingDef(using, context);
    }

    @Override
    public Void visitIncludeDef(IncludeDef include, ElmContext context) {
        newConstruct("include");
        return super.visitIncludeDef(include, context);
    }

    @Override
    public Void visitCodeSystemDef(CodeSystemDef codeSystem, ElmContext context) {
        newConstruct("codesystem");
        return super.visitCodeSystemDef(codeSystem, context);
    }

    @Override
    public Void visitValueSetDef(ValueSetDef valueset, ElmContext context) {
        newConstruct("valueset");
        return super.visitValueSetDef(valueset, context);
    }

    public Void visitCodeDef(CodeDef code, ElmContext context) {
        newConstruct("code");
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

            ExpressionDef c = statements.getDef().get(i);
            enterClause();
            try {
                Object childResult = visitExpressionDef(c, context);
                result = childResult;
                // result = aggregateResult(result, childResult);
            }
            finally {
                exitClause();
            }
        }

        return null;
    }

    public Void visitExpressionDef(ExpressionDef expressionDef, ElmContext context) {
        newConstruct("statement");
        return null;
    }

    public Void visitContextDef(ContextDef contextDef, ElmContext context) {
        newConstruct("statement");
        return null;
    }

    @Override
    public Void visitFunctionDef(FunctionDef function, ElmContext context) {
        newConstruct("statement");

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

        return null;
    }

    @Override
    public Void visitOperandDef(OperandDef operand, ElmContext context) {
        return super.visitOperandDef(operand, context);
    }

    @Override
    public Void visitAliasedQuerySource(AliasedQuerySource source, ElmContext context) {
        return super.visitAliasedQuerySource(source, context);
    }

    @Override
    public Void visitAliasRef(AliasRef alias, ElmContext context) {
        return super.visitAliasRef(alias, context);
    }

    private Void visitWithOrWithoutClause(RelationshipClause withOrWithout, ElmContext context) {
        Object result = "TODO";// defaultResult();
        boolean clauseEntered = false;     
        try {
            Expression suchThat = withOrWithout.getSuchThat();
            enterClause();
            // need to resolve some kind of space   
            visitExpression(suchThat, context);
            clauseEntered = true;
            Object childResult = visitExpression(withOrWithout.getExpression(), context);
            result = childResult; // aggregateResult(result, childResult);
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
        return visitWithOrWithoutClause(with, context);
    }

    @Override
    public Void visitWithout(Without without, ElmContext context) {
        return visitWithOrWithoutClause(without, context);
    }

    @Override
    public Void visitRetrieve(Retrieve retrieve, ElmContext context) {
        enterRetrieve();
        try {
            return super.visitRetrieve(retrieve, context);
        }
        finally {
            exitRetrieve();
        }
    }

    @Override
    public Void visitQuery(Query query, ElmContext context) {
        return super.visitQuery(query, context);
    }

    @Override
    public Void visitUnion(Union union, ElmContext context) {
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
        enterClause();
        try {
            // Object result = defaultResult();
            // int n = ctx.getChildCount();
            // for (int i = 0; i < n; i++) {
            //     // if (!shouldVisitNextChild(ctx, result)) {
            //     //     break;
            //     // }

            //     ParseTree c = ctx.getChild(i);

            //     if (i > 1 && !c.getText().equals(",")) {
            //         newLine();
            //     }

            //     Object childResult = c.accept(this);
            //     result = aggregateResult(result, childResult);
            // }
            // return result;
            return null;
        }
        finally {
            exitClause();
        }
    }

    // @Override
    // public Object visitLetClauseItem(cqlParser.LetClauseItemContext ctx) {
    //     return super.visitLetClauseItem(ctx);
    // }

    public Void visitWhereClause(Expression where, ElmContext context) {
        enterClause();
        try {
            return null;
        }
        finally {
            exitClause();
        }
    }

    @Override
    public Void visitReturnClause(ReturnClause returnClause, ElmContext context) {
        enterClause();
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
    public Void visitNot(Not not, ElmContext context) {
        return super.visitNot(not, context);
    }

    public Void visitBoolean(Literal booleanLiteral, ElmContext context) {
        return null;
    }

    @Override
    public Void visitOr(Or or, ElmContext context) {
        return visitBinaryExpression(or, context);
    }

    @Override
    public Void visitBinaryExpression(BinaryExpression binary, ElmContext context) {
        // Object result = defaultResult();
        // int n = ctx.getChildCount();
        // boolean clauseEntered = false;
        try {
            // for (int i = 0; i < n; i++) {
            //     if (!shouldVisitNextChild(ctx, result)) {
            //         break;
            //     }

            //     ParseTree c = ctx.getChild(i);

            //     if (i == 1) {
            //         enterClause();
            //         clauseEntered = true;
            //     }

            //     Object childResult = c.accept(this);
            //     result = aggregateResult(result, childResult);
            // }
            // return result;
            return null;
        }
        finally {
            //if (clauseEntered) {
                exitClause();
            //}
        }
    }

    @Override
    public Void visitAnd(And and, ElmContext context) {
        return visitBinaryExpression(and, context);
    }

    @Override
    public Void visitDifferenceBetween(DifferenceBetween differenceBetween, ElmContext context) {
        return super.visitDifferenceBetween(differenceBetween, context);
    }

    @Override
    public Void visitExists(Exists exists, ElmContext context) {
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
        return null;
    }

    @Override
    public Void visitDistinct(Distinct distinct, ElmContext context) {
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
        return null;
    }

    @Override
    public Void visitGreater(Greater greater, ElmContext context) {
        return null;
    }

    @Override
    public Void visitGreaterOrEqual(GreaterOrEqual greaterOrElement, ElmContext context) {
        return null;
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
        return super.visitLiteral(literal, context);
    }

    @Override
    public Void visitInterval(Interval interval, ElmContext context) {
        return super.visitInterval(interval, context);
    }

    @Override
    public Void visitFunctionRef(FunctionRef function, ElmContext context) {
        // Object result = defaultResult();
        // int n = ctx.getChildCount();
        // for (int i = 0; i < n; i++) {
        //     if (!shouldVisitNextChild(ctx, result)) {
        //         break;
        //     }

        //     ParseTree c = ctx.getChild(i);

        //     if (c == ctx.paramList()) {
        //         enterGroup();
        //     }
        //     try {
        //         Object childResult = c.accept(this);
        //         result = aggregateResult(result, childResult);
        //     }
        //     finally {
        //         if (c == ctx.paramList()) {
        //             exitGroup();
        //         }
        //     }
        // }
        return null; //result;
    }

    public Object visitParamList(cqlParser.ParamListContext ctx) {
        return null;
    }

    @Override
    public Void visitQuantity(Quantity quantity, ElmContext context) {
        return super.visitQuantity(quantity, context);
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
