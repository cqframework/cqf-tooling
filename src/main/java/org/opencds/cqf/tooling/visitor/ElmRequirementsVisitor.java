package org.opencds.cqf.tooling.visitor;

import org.cqframework.cql.elm.visiting.ElmBaseLibraryVisitor;
import org.hl7.elm.r1.*;

public class ElmRequirementsVisitor extends ElmBaseLibraryVisitor <ElmRequirements, ElmRequirementsContext>{
// in here consider only leaves and collect info
// mark leaf nodes that require todos as abstract
// add visitcoderef to parent

    public ElmRequirementsVisitor() {
        super();
    }

    @Override
    public ElmRequirements visitExpressionDef(ExpressionDef elm, ElmRequirementsContext context) {
        context.getElmRequirements().reportExpressionDef(elm);
        return super.visitExpressionDef(elm, context);
    }

    @Override
    public ElmRequirements visitFunctionDef(FunctionDef elm, ElmRequirementsContext context) {
        context.getElmRequirements().reportFunctionDef(elm);
        return super.visitFunctionDef(elm, context);
    }

    @Override
    public ElmRequirements visitExpressionRef(ExpressionRef elm, ElmRequirementsContext context) {
        context.getElmRequirements().reportExpressionRef(elm);
        return super.visitExpressionRef(elm, context);
    }

    @Override
    public ElmRequirements visitFunctionRef(FunctionRef elm, ElmRequirementsContext context) {
        context.getElmRequirements().reportFunctionRef(elm);
        return super.visitFunctionRef(elm, context);
    }

    @Override
    public ElmRequirements visitParameterDef(ParameterDef elm, ElmRequirementsContext context) {
        context.getElmRequirements().reportParameterDef(elm);
        return super.visitParameterDef(elm, context);
    }

    @Override
    public ElmRequirements visitParameterRef(ParameterRef elm, ElmRequirementsContext context) {
        context.getElmRequirements().reportParameterRef(elm);
        return super.visitParameterRef(elm, context);
    }

    @Override
    public ElmRequirements visitRetrieve(Retrieve elm, ElmRequirementsContext context) {
        context.getElmRequirements().reportRetrieve(elm);
        return super.visitRetrieve(elm, context);
    }

    @Override
    public ElmRequirements visitCodeSystemDef(CodeSystemDef elm, ElmRequirementsContext context) {
        context.getElmRequirements().reportCodeSystemDef(elm);
        return super.visitCodeSystemDef(elm, context);
    }

    @Override
    public ElmRequirements visitValueSetDef(ValueSetDef elm, ElmRequirementsContext context) {
        context.getElmRequirements().reportValueSetDef(elm);
        return super.visitValueSetDef(elm, context);
    }

    @Override
    public ElmRequirements visitCodeSystemRef(CodeSystemRef elm, ElmRequirementsContext context){
        context.getElmRequirements().reportCodeSystemRef(elm);
        return super.visitCodeSystemRef(elm, context);
    }

    @Override
    public ElmRequirements visitValueSetRef(ValueSetRef elm, ElmRequirementsContext context) {
        context.getElmRequirements().reportValueSetRef(elm);
        return context.getElmRequirements();
    }

    @Override
    public ElmRequirements visitLibrary(Library elm, ElmRequirementsContext context) {
        context.getElmRequirements().reportLibraryRef(elm);
        return super.visitLibrary(elm, context);
    }

    @Override
    public ElmRequirements visitIncludeDef(IncludeDef elm, ElmRequirementsContext context) {
        context.getElmRequirements().reportLibraryRef(elm);
        return super.visitIncludeDef(elm, context);
    }

    @Override
    public ElmRequirements visitCodeRef(CodeRef elm, ElmRequirementsContext context){
        context.getElmRequirements().reportCodeRef(elm);
        return super.visitCodeRef(elm, context);
    }

    @Override
    public ElmRequirements visitCodeDef(CodeDef elm, ElmRequirementsContext context){
        context.getElmRequirements().reportCodeDef(elm);
        return super.visitCodeDef(elm, context);
    }

    @Override
    public ElmRequirements visitConceptRef(ConceptRef elm, ElmRequirementsContext context) {
        context.getElmRequirements().reportConceptRef(elm);
        return super.visitConceptRef(elm, context);
    }

    @Override
    public ElmRequirements visitConceptDef(ConceptDef elm, ElmRequirementsContext context) {
        context.getElmRequirements().reportConceptDef(elm);
        return super.visitConceptDef(elm, context);
    }

    @Override
    public ElmRequirements visitExpression(Expression elm, ElmRequirementsContext context) {
        return super.visitExpression(elm, context);
    }

    @Override
    public ElmRequirements visitUnaryExpression(UnaryExpression elm, ElmRequirementsContext context) {
        return super.visitUnaryExpression(elm, context);
    }

    @Override
    public ElmRequirements visitBinaryExpression(BinaryExpression elm, ElmRequirementsContext context) {
        return super.visitBinaryExpression(elm, context);
    }

    @Override
    public ElmRequirements visitTernaryExpression(TernaryExpression elm, ElmRequirementsContext context) {
        return super.visitTernaryExpression(elm, context);
    }

    @Override
    public ElmRequirements visitNaryExpression(NaryExpression elm, ElmRequirementsContext context) {
        return super.visitNaryExpression(elm, context);
    }

    @Override
    public ElmRequirements visitOperandDef(OperandDef elm, ElmRequirementsContext context) {
        return super.visitOperandDef(elm, context);
    }

    @Override
    public ElmRequirements visitOperandRef(OperandRef elm, ElmRequirementsContext context) {
        return super.visitOperandRef(elm, context);
    }

    @Override
    public ElmRequirements visitIdentifierRef(IdentifierRef elm, ElmRequirementsContext context) {
        return super.visitIdentifierRef(elm, context);
    }

    @Override
    public ElmRequirements visitLiteral(Literal elm, ElmRequirementsContext context) {
        return super.visitLiteral(elm, context);
    }

    @Override
    public ElmRequirements visitTupleElement(TupleElement elm, ElmRequirementsContext context) {
        return super.visitTupleElement(elm, context);
    }

    @Override
    public ElmRequirements visitTuple(Tuple elm, ElmRequirementsContext context) {
        return super.visitTuple(elm, context);
    }

    @Override
    public ElmRequirements visitInstanceElement(InstanceElement elm, ElmRequirementsContext context) {
        return super.visitInstanceElement(elm, context);
    }

    @Override
    public ElmRequirements visitInstance(Instance elm, ElmRequirementsContext context) {
        return super.visitInstance(elm, context);
    }

    @Override
    public ElmRequirements visitInterval(Interval elm, ElmRequirementsContext context) {
        return super.visitInterval(elm, context);
    }

    @Override
    public ElmRequirements visitList(List elm, ElmRequirementsContext context) {
        return super.visitList(elm, context);
    }

    @Override
    public ElmRequirements visitAnd(And elm, ElmRequirementsContext context) {
        return super.visitAnd(elm, context);
    }

    @Override
    public ElmRequirements visitOr(Or elm, ElmRequirementsContext context) {
        return super.visitOr(elm, context);
    }

    @Override
    public ElmRequirements visitXor(Xor elm, ElmRequirementsContext context) {
        return super.visitXor(elm, context);
    }

    @Override
    public ElmRequirements visitNot(Not elm, ElmRequirementsContext context) {
        return super.visitNot(elm, context);
    }

    @Override
    public ElmRequirements visitIf(If elm, ElmRequirementsContext context) {
        return super.visitIf(elm, context);
    }

    @Override
    public ElmRequirements visitCaseItem(CaseItem elm, ElmRequirementsContext context) {
        return super.visitCaseItem(elm, context);
    }

    @Override
    public ElmRequirements visitCase(Case elm, ElmRequirementsContext context) {
        return super.visitCase(elm, context);
    }

    @Override
    public ElmRequirements visitNull(Null elm, ElmRequirementsContext context) {
        return super.visitNull(elm, context);
    }

    @Override
    public ElmRequirements visitIsNull(IsNull elm, ElmRequirementsContext context) {
        return super.visitIsNull(elm, context);
    }

    @Override
    public ElmRequirements visitIsTrue(IsTrue elm, ElmRequirementsContext context) {
        return super.visitIsTrue(elm, context);
    }

    @Override
    public ElmRequirements visitIsFalse(IsFalse elm, ElmRequirementsContext context) {
        return super.visitIsFalse(elm, context);
    }

    @Override
    public ElmRequirements visitCoalesce(Coalesce elm, ElmRequirementsContext context) {
        return super.visitCoalesce(elm, context);
    }

    @Override
    public ElmRequirements visitIs(Is elm, ElmRequirementsContext context) {
        return super.visitIs(elm, context);
    }

    @Override
    public ElmRequirements visitAs(As elm, ElmRequirementsContext context) {
        return super.visitAs(elm, context);
    }

    @Override
    public ElmRequirements visitConvert(Convert elm, ElmRequirementsContext context) {
        return super.visitConvert(elm, context);
    }

    @Override
    public ElmRequirements visitToBoolean(ToBoolean elm, ElmRequirementsContext context) {
        return super.visitToBoolean(elm, context);
    }

    @Override
    public ElmRequirements visitToConcept(ToConcept elm, ElmRequirementsContext context) {
        return super.visitToConcept(elm, context);
    }

    @Override
    public ElmRequirements visitToDateTime(ToDateTime elm, ElmRequirementsContext context) {
        return super.visitToDateTime(elm, context);
    }

    @Override
    public ElmRequirements visitToDecimal(ToDecimal elm, ElmRequirementsContext context) {
        return super.visitToDecimal(elm, context);
    }

    @Override
    public ElmRequirements visitToInteger(ToInteger elm, ElmRequirementsContext context) {
        return super.visitToInteger(elm, context);
    }

    @Override
    public ElmRequirements visitToQuantity(ToQuantity elm, ElmRequirementsContext context) {
        return super.visitToQuantity(elm, context);
    }

    @Override
    public ElmRequirements visitToString(ToString elm, ElmRequirementsContext context) {
        return super.visitToString(elm, context);
    }

    @Override
    public ElmRequirements visitToTime(ToTime elm, ElmRequirementsContext context) {
        return super.visitToTime(elm, context);
    }

    @Override
    public ElmRequirements visitEqual(Equal elm, ElmRequirementsContext context) {
        return super.visitEqual(elm, context);
    }

    @Override
    public ElmRequirements visitEquivalent(Equivalent elm, ElmRequirementsContext context) {
        return super.visitEquivalent(elm, context);
    }

    @Override
    public ElmRequirements visitNotEqual(NotEqual elm, ElmRequirementsContext context) {
        return super.visitNotEqual(elm, context);
    }

    @Override
    public ElmRequirements visitLess(Less elm, ElmRequirementsContext context) {
        return super.visitLess(elm, context);
    }

    @Override
    public ElmRequirements visitGreater(Greater elm, ElmRequirementsContext context) {
        return super.visitGreater(elm, context);
    }

    @Override
    public ElmRequirements visitLessOrEqual(LessOrEqual elm, ElmRequirementsContext context) {
        return super.visitLessOrEqual(elm, context);
    }

    @Override
    public ElmRequirements visitGreaterOrEqual(GreaterOrEqual elm, ElmRequirementsContext context) {
        return super.visitGreaterOrEqual(elm, context);
    }

    @Override
    public ElmRequirements visitAdd(Add elm, ElmRequirementsContext context) {
        return super.visitAdd(elm, context);
    }

    @Override
    public ElmRequirements visitSubtract(Subtract elm, ElmRequirementsContext context) {
        return super.visitSubtract(elm, context);
    }

    @Override
    public ElmRequirements visitMultiply(Multiply elm, ElmRequirementsContext context) {
        return super.visitMultiply(elm, context);
    }

    @Override
    public ElmRequirements visitDivide(Divide elm, ElmRequirementsContext context) {
        return super.visitDivide(elm, context);
    }

    @Override
    public ElmRequirements visitTruncatedDivide(TruncatedDivide elm, ElmRequirementsContext context) {
        return super.visitTruncatedDivide(elm, context);
    }

    @Override
    public ElmRequirements visitModulo(Modulo elm, ElmRequirementsContext context) {
        return super.visitModulo(elm, context);
    }

    @Override
    public ElmRequirements visitCeiling(Ceiling elm, ElmRequirementsContext context) {
        return super.visitCeiling(elm, context);
    }

    @Override
    public ElmRequirements visitFloor(Floor elm, ElmRequirementsContext context) {
        return super.visitFloor(elm, context);
    }

    @Override
    public ElmRequirements visitTruncate(Truncate elm, ElmRequirementsContext context) {
        return super.visitTruncate(elm, context);
    }

    @Override
    public ElmRequirements visitAbs(Abs elm, ElmRequirementsContext context) {
        return super.visitAbs(elm, context);
    }

    @Override
    public ElmRequirements visitNegate(Negate elm, ElmRequirementsContext context) {
        return super.visitNegate(elm, context);
    }

    @Override
    public ElmRequirements visitRound(Round elm, ElmRequirementsContext context) {
        return super.visitRound(elm, context);
    }

    @Override
    public ElmRequirements visitLn(Ln elm, ElmRequirementsContext context) {
        return super.visitLn(elm, context);
    }

    @Override
    public ElmRequirements visitExp(Exp elm, ElmRequirementsContext context) {
        return super.visitExp(elm, context);
    }

    @Override
    public ElmRequirements visitLog(Log elm, ElmRequirementsContext context) {
        return super.visitLog(elm, context);
    }

    @Override
    public ElmRequirements visitPower(Power elm, ElmRequirementsContext context) {
        return super.visitPower(elm, context);
    }

    @Override
    public ElmRequirements visitSuccessor(Successor elm, ElmRequirementsContext context) {
        return super.visitSuccessor(elm, context);
    }

    @Override
    public ElmRequirements visitPredecessor(Predecessor elm, ElmRequirementsContext context) {
        return super.visitPredecessor(elm, context);
    }

    @Override
    public ElmRequirements visitMinValue(MinValue elm, ElmRequirementsContext context) {
        return super.visitMinValue(elm, context);
    }

    @Override
    public ElmRequirements visitMaxValue(MaxValue elm, ElmRequirementsContext context) {
        return super.visitMaxValue(elm, context);
    }

    @Override
    public ElmRequirements visitConcatenate(Concatenate elm, ElmRequirementsContext context) {
        return super.visitConcatenate(elm, context);
    }

    @Override
    public ElmRequirements visitCombine(Combine elm, ElmRequirementsContext context) {
        return super.visitCombine(elm, context);
    }

    @Override
    public ElmRequirements visitSplit(Split elm, ElmRequirementsContext context) {
        return super.visitSplit(elm, context);
    }

    @Override
    public ElmRequirements visitLength(Length elm, ElmRequirementsContext context) {
        return super.visitLength(elm, context);
    }

    @Override
    public ElmRequirements visitUpper(Upper elm, ElmRequirementsContext context) {
        return super.visitUpper(elm, context);
    }

    @Override
    public ElmRequirements visitLower(Lower elm, ElmRequirementsContext context) {
        return super.visitLower(elm, context);
    }

    @Override
    public ElmRequirements visitIndexer(Indexer elm, ElmRequirementsContext context) {
        return super.visitIndexer(elm, context);
    }

    @Override
    public ElmRequirements visitPositionOf(PositionOf elm, ElmRequirementsContext context) {
        return super.visitPositionOf(elm, context);
    }

    @Override
    public ElmRequirements visitSubstring(Substring elm, ElmRequirementsContext context) {
        return super.visitSubstring(elm, context);
    }

    @Override
    public ElmRequirements visitDurationBetween(DurationBetween elm, ElmRequirementsContext context) {
        return super.visitDurationBetween(elm, context);
    }

    @Override
    public ElmRequirements visitDifferenceBetween(DifferenceBetween elm, ElmRequirementsContext context) {
        return super.visitDifferenceBetween(elm, context);
    }

    @Override
    public ElmRequirements visitDateFrom(DateFrom elm, ElmRequirementsContext context) {
        return super.visitDateFrom(elm, context);
    }

    @Override
    public ElmRequirements visitTimeFrom(TimeFrom elm, ElmRequirementsContext context) {
        return super.visitTimeFrom(elm, context);
    }

    @Override
    public ElmRequirements visitTimezoneOffsetFrom(TimezoneOffsetFrom elm, ElmRequirementsContext context) {
        return super.visitTimezoneOffsetFrom(elm, context);
    }

    @Override
    public ElmRequirements visitDateTimeComponentFrom(DateTimeComponentFrom elm, ElmRequirementsContext context) {
        return super.visitDateTimeComponentFrom(elm, context);
    }

    @Override
    public ElmRequirements visitTimeOfDay(TimeOfDay elm, ElmRequirementsContext context) {
        return super.visitTimeOfDay(elm, context);
    }

    @Override
    public ElmRequirements visitToday(Today elm, ElmRequirementsContext context) {
        return super.visitToday(elm, context);
    }

    @Override
    public ElmRequirements visitNow(Now elm, ElmRequirementsContext context) {
        return super.visitNow(elm, context);
    }

    @Override
    public ElmRequirements visitDateTime(DateTime elm, ElmRequirementsContext context) {
        return super.visitDateTime(elm, context);
    }

    @Override
    public ElmRequirements visitTime(Time elm, ElmRequirementsContext context) {
        return super.visitTime(elm, context);
    }

    @Override
    public ElmRequirements visitSameAs(SameAs elm, ElmRequirementsContext context) {
        return super.visitSameAs(elm, context);
    }

    @Override
    public ElmRequirements visitSameOrBefore(SameOrBefore elm, ElmRequirementsContext context) {
        return super.visitSameOrBefore(elm, context);
    }

    @Override
    public ElmRequirements visitSameOrAfter(SameOrAfter elm, ElmRequirementsContext context) {
        return super.visitSameOrAfter(elm, context);
    }

    @Override
    public ElmRequirements visitWidth(Width elm, ElmRequirementsContext context) {
        return super.visitWidth(elm, context);
    }

    @Override
    public ElmRequirements visitStart(Start elm, ElmRequirementsContext context) {
        return super.visitStart(elm, context);
    }

    @Override
    public ElmRequirements visitEnd(End elm, ElmRequirementsContext context) {
        return super.visitEnd(elm, context);
    }

    @Override
    public ElmRequirements visitContains(Contains elm, ElmRequirementsContext context) {
        return super.visitContains(elm, context);
    }

    @Override
    public ElmRequirements visitProperContains(ProperContains elm, ElmRequirementsContext context) {
        return super.visitProperContains(elm, context);
    }

    @Override
    public ElmRequirements visitIn(In elm, ElmRequirementsContext context) {
        return super.visitIn(elm, context);
    }

    @Override
    public ElmRequirements visitProperIn(ProperIn elm, ElmRequirementsContext context) {
        return super.visitProperIn(elm, context);
    }

    @Override
    public ElmRequirements visitIncludes(Includes elm, ElmRequirementsContext context) {
        return super.visitIncludes(elm, context);
    }

    @Override
    public ElmRequirements visitIncludedIn(IncludedIn elm, ElmRequirementsContext context) {
        return super.visitIncludedIn(elm, context);
    }

    @Override
    public ElmRequirements visitProperIncludes(ProperIncludes elm, ElmRequirementsContext context) {
        return super.visitProperIncludes(elm, context);
    }

    @Override
    public ElmRequirements visitProperIncludedIn(ProperIncludedIn elm, ElmRequirementsContext context) {
        return super.visitProperIncludedIn(elm, context);
    }

    @Override
    public ElmRequirements visitBefore(Before elm, ElmRequirementsContext context) {
        return super.visitBefore(elm, context);
    }

    @Override
    public ElmRequirements visitAfter(After elm, ElmRequirementsContext context) {
        return super.visitAfter(elm, context);
    }

    @Override
    public ElmRequirements visitMeets(Meets elm, ElmRequirementsContext context) {
        return super.visitMeets(elm, context);
    }

    @Override
    public ElmRequirements visitMeetsBefore(MeetsBefore elm, ElmRequirementsContext context) {
        return super.visitMeetsBefore(elm, context);
    }

    @Override
    public ElmRequirements visitMeetsAfter(MeetsAfter elm, ElmRequirementsContext context) {
        return super.visitMeetsAfter(elm, context);
    }

    @Override
    public ElmRequirements visitOverlaps(Overlaps elm, ElmRequirementsContext context) {
        return super.visitOverlaps(elm, context);
    }

    @Override
    public ElmRequirements visitOverlapsBefore(OverlapsBefore elm, ElmRequirementsContext context) {
        return super.visitOverlapsBefore(elm, context);
    }

    @Override
    public ElmRequirements visitOverlapsAfter(OverlapsAfter elm, ElmRequirementsContext context) {
        return super.visitOverlapsAfter(elm, context);
    }

    @Override
    public ElmRequirements visitStarts(Starts elm, ElmRequirementsContext context) {
        return super.visitStarts(elm, context);
    }

    @Override
    public ElmRequirements visitEnds(Ends elm, ElmRequirementsContext context) {
        return super.visitEnds(elm, context);
    }

    @Override
    public ElmRequirements visitCollapse(Collapse elm, ElmRequirementsContext context) {
        return super.visitCollapse(elm, context);
    }

    @Override
    public ElmRequirements visitUnion(Union elm, ElmRequirementsContext context) {
        return super.visitUnion(elm, context);
    }

    @Override
    public ElmRequirements visitIntersect(Intersect elm, ElmRequirementsContext context) {
        return super.visitIntersect(elm, context);
    }

    @Override
    public ElmRequirements visitExcept(Except elm, ElmRequirementsContext context) {
        return super.visitExcept(elm, context);
    }

    @Override
    public ElmRequirements visitExists(Exists elm, ElmRequirementsContext context) {
        return super.visitExists(elm, context);
    }

    @Override
    public ElmRequirements visitTimes(Times elm, ElmRequirementsContext context) {
        return super.visitTimes(elm, context);
    }

    @Override
    public ElmRequirements visitFilter(Filter elm, ElmRequirementsContext context) {
        return super.visitFilter(elm, context);
    }

    @Override
    public ElmRequirements visitFirst(First elm, ElmRequirementsContext context) {
        return super.visitFirst(elm, context);
    }

    @Override
    public ElmRequirements visitLast(Last elm, ElmRequirementsContext context) {
        return super.visitLast(elm, context);
    }

    @Override
    public ElmRequirements visitIndexOf(IndexOf elm, ElmRequirementsContext context) {
        return super.visitIndexOf(elm, context);
    }

    @Override
    public ElmRequirements visitFlatten(Flatten elm, ElmRequirementsContext context) {
        return super.visitFlatten(elm, context);
    }

    @Override
    public ElmRequirements visitSort(Sort elm, ElmRequirementsContext context) {
        return super.visitSort(elm, context);
    }

    @Override
    public ElmRequirements visitForEach(ForEach elm, ElmRequirementsContext context) {
        return super.visitForEach(elm, context);
    }

    @Override
    public ElmRequirements visitDistinct(Distinct elm, ElmRequirementsContext context) {
        return super.visitDistinct(elm, context);
    }

    @Override
    public ElmRequirements visitCurrent(Current elm, ElmRequirementsContext context) {
        return super.visitCurrent(elm, context);
    }

    @Override
    public ElmRequirements visitSingletonFrom(SingletonFrom elm, ElmRequirementsContext context) {
        return super.visitSingletonFrom(elm, context);
    }

    @Override
    public ElmRequirements visitAggregateExpression(AggregateExpression elm, ElmRequirementsContext context) {
        return super.visitAggregateExpression(elm, context);
    }

    @Override
    public ElmRequirements visitCount(Count elm, ElmRequirementsContext context) {
        return super.visitCount(elm, context);
    }

    @Override
    public ElmRequirements visitSum(Sum elm, ElmRequirementsContext context) {
        return super.visitSum(elm, context);
    }

    @Override
    public ElmRequirements visitMin(Min elm, ElmRequirementsContext context) {
        return super.visitMin(elm, context);
    }

    @Override
    public ElmRequirements visitMax(Max elm, ElmRequirementsContext context) {
        return super.visitMax(elm, context);
    }

    @Override
    public ElmRequirements visitAvg(Avg elm, ElmRequirementsContext context) {
        return super.visitAvg(elm, context);
    }

    @Override
    public ElmRequirements visitMedian(Median elm, ElmRequirementsContext context) {
        return super.visitMedian(elm, context);
    }

    @Override
    public ElmRequirements visitMode(Mode elm, ElmRequirementsContext context) {
        return super.visitMode(elm, context);
    }

    @Override
    public ElmRequirements visitVariance(Variance elm, ElmRequirementsContext context) {
        return super.visitVariance(elm, context);
    }

    @Override
    public ElmRequirements visitPopulationVariance(PopulationVariance elm, ElmRequirementsContext context) {
        return super.visitPopulationVariance(elm, context);
    }

    @Override
    public ElmRequirements visitStdDev(StdDev elm, ElmRequirementsContext context) {
        return super.visitStdDev(elm, context);
    }

    @Override
    public ElmRequirements visitPopulationStdDev(PopulationStdDev elm, ElmRequirementsContext context) {
        return super.visitPopulationStdDev(elm, context);
    }

    @Override
    public ElmRequirements visitAllTrue(AllTrue elm, ElmRequirementsContext context) {
        return super.visitAllTrue(elm, context);
    }

    @Override
    public ElmRequirements visitAnyTrue(AnyTrue elm, ElmRequirementsContext context) {
        return super.visitAnyTrue(elm, context);
    }

    @Override
    public ElmRequirements visitProperty(Property elm, ElmRequirementsContext context) {
        return super.visitProperty(elm, context);
    }

    @Override
    public ElmRequirements visitAliasedQuerySource(AliasedQuerySource elm, ElmRequirementsContext context) {
        return super.visitAliasedQuerySource(elm, context);
    }

    @Override
    public ElmRequirements visitLetClause(LetClause elm, ElmRequirementsContext context) {
        return super.visitLetClause(elm, context);
    }

    @Override
    public ElmRequirements visitRelationshipClause(RelationshipClause elm, ElmRequirementsContext context) {
        return super.visitRelationshipClause(elm, context);
    }

    @Override
    public ElmRequirements visitWith(With elm, ElmRequirementsContext context) {
        return super.visitWith(elm, context);
    }

    @Override
    public ElmRequirements visitWithout(Without elm, ElmRequirementsContext context) {
        return super.visitWithout(elm, context);
    }

    @Override
    public ElmRequirements visitSortByItem(SortByItem elm, ElmRequirementsContext context) {
        return super.visitSortByItem(elm, context);
    }

    @Override
    public ElmRequirements visitByDirection(ByDirection elm, ElmRequirementsContext context) {
        return super.visitByDirection(elm, context);
    }

    @Override
    public ElmRequirements visitByColumn(ByColumn elm, ElmRequirementsContext context) {
        return super.visitByColumn(elm, context);
    }

    @Override
    public ElmRequirements visitByExpression(ByExpression elm, ElmRequirementsContext context) {
        return super.visitByExpression(elm, context);
    }

    @Override
    public ElmRequirements visitSortClause(SortClause elm, ElmRequirementsContext context) {
        return super.visitSortClause(elm, context);
    }

    @Override
    public ElmRequirements visitReturnClause(ReturnClause elm, ElmRequirementsContext context) {
        return super.visitReturnClause(elm, context);
    }

    @Override
    public ElmRequirements visitQuery(Query elm, ElmRequirementsContext context) {
        return super.visitQuery(elm, context);
    }

    @Override
    public ElmRequirements visitAliasRef(AliasRef elm, ElmRequirementsContext context) {
        return super.visitAliasRef(elm, context);
    }

    @Override
    public ElmRequirements visitQueryLetRef(QueryLetRef elm, ElmRequirementsContext context) {
        return super.visitQueryLetRef(elm, context);
    }

    @Override
    public ElmRequirements visitCode(Code elm, ElmRequirementsContext context) {
        return super.visitCode(elm, context);
    }

    @Override
    public ElmRequirements visitConcept(Concept elm, ElmRequirementsContext context) {
        return super.visitConcept(elm, context);
    }

    @Override
    public ElmRequirements visitInCodeSystem(InCodeSystem elm, ElmRequirementsContext context) {
        return super.visitInCodeSystem(elm, context);
    }

    @Override
    public ElmRequirements visitInValueSet(InValueSet elm, ElmRequirementsContext context) {
        return super.visitInValueSet(elm, context);
    }

    @Override
    public ElmRequirements visitQuantity(Quantity elm, ElmRequirementsContext context) {
        return super.visitQuantity(elm, context);
    }

    @Override
    public ElmRequirements visitCalculateAge(CalculateAge elm, ElmRequirementsContext context) {
        return super.visitCalculateAge(elm, context);
    }

    @Override
    public ElmRequirements visitCalculateAgeAt(CalculateAgeAt elm, ElmRequirementsContext context) {
        return super.visitCalculateAgeAt(elm, context);
    }

    @Override
    public ElmRequirements visitElement(Element elm, ElmRequirementsContext context) {
        return super.visitElement(elm, context);
    }

    @Override
    public ElmRequirements visitTypeSpecifier(TypeSpecifier elm, ElmRequirementsContext context) {
        return super.visitTypeSpecifier(elm, context);
    }

    @Override
    public ElmRequirements visitNamedTypeSpecifier(NamedTypeSpecifier elm, ElmRequirementsContext context) {
        return super.visitNamedTypeSpecifier(elm, context);
    }

    @Override
    public ElmRequirements visitIntervalTypeSpecifier(IntervalTypeSpecifier elm, ElmRequirementsContext context) {
        return super.visitIntervalTypeSpecifier(elm, context);
    }

    @Override
    public ElmRequirements visitListTypeSpecifier(ListTypeSpecifier elm, ElmRequirementsContext context) {
        return super.visitListTypeSpecifier(elm, context);
    }

    @Override
    public ElmRequirements visitTupleElementDefinition(TupleElementDefinition elm, ElmRequirementsContext context) {
        return super.visitTupleElementDefinition(elm, context);
    }

    @Override
    public ElmRequirements visitTupleTypeSpecifier(TupleTypeSpecifier elm, ElmRequirementsContext context) {
        return super.visitTupleTypeSpecifier(elm, context);
    }

    @Override
    public ElmRequirements visitUsingDef(UsingDef elm, ElmRequirementsContext context) {
        return super.visitUsingDef(elm, context);
    }
}
