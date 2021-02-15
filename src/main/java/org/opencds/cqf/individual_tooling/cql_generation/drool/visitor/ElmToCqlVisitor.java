package org.opencds.cqf.individual_tooling.cql_generation.drool.visitor;

import org.cqframework.cql.elm.visiting.ElmVisitor;
import org.hl7.elm.r1.*;
import org.opencds.cqf.individual_tooling.cql_generation.context.CqlContext;


// import org.hl7.elm.r1.AccessModifier;
// import org.hl7.elm.r1.And;
// import org.hl7.elm.r1.BinaryExpression;
// import org.hl7.elm.r1.Expression;
// import org.hl7.elm.r1.ExpressionDef;
// import org.hl7.elm.r1.ExpressionRef;
// import org.hl7.elm.r1.Or;
// import org.hl7.elm.r1.AccessModifier;
// import org.hl7.elm.r1.AliasedQuerySource;
// import org.hl7.elm.r1.As;
// import org.hl7.elm.r1.BinaryExpression;
// import org.hl7.elm.r1.CalculateAgeAt;
// import org.hl7.elm.r1.DateTimePrecision;
// import org.hl7.elm.r1.Equal;
// import org.hl7.elm.r1.Expression;
// import org.hl7.elm.r1.ExpressionRef;
// import org.hl7.elm.r1.FunctionRef;
// import org.hl7.elm.r1.Greater;
// import org.hl7.elm.r1.GreaterOrEqual;
// import org.hl7.elm.r1.InValueSet;
// import org.hl7.elm.r1.Less;
// import org.hl7.elm.r1.LessOrEqual;
// import org.hl7.elm.r1.NamedTypeSpecifier;
// import org.hl7.elm.r1.Not;
// import org.hl7.elm.r1.Property;
// import org.hl7.elm.r1.Quantity;
// import org.hl7.elm.r1.Query;
// import org.hl7.elm.r1.Retrieve;
// import org.hl7.elm.r1.ToQuantity;
// import org.hl7.elm.r1.Today;
// import org.hl7.elm.r1.ValueSetDef;
// import org.hl7.elm.r1.ValueSetRef;

public class ElmToCqlVisitor implements ElmVisitor<Void, CqlContext> {

    @Override
    public Void visitElement(Element elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitTypeSpecifier(TypeSpecifier elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitNamedTypeSpecifier(NamedTypeSpecifier elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitIntervalTypeSpecifier(IntervalTypeSpecifier elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitListTypeSpecifier(ListTypeSpecifier elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitTupleElementDefinition(TupleElementDefinition elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitTupleTypeSpecifier(TupleTypeSpecifier elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    
// import org.hl7.elm.r1.ExpressionDef;
// import org.hl7.elm.r1.InValueSet;
// import org.hl7.elm.r1.Property;
// import org.hl7.elm.r1.Quantity;
// import org.hl7.elm.r1.Query;
// import org.hl7.elm.r1.Retrieve;
// import org.hl7.elm.r1.Today;
// import org.hl7.elm.r1.BinaryExpression;
// import org.hl7.elm.r1.UnaryExpression;
// import org.hl7.elm.r1.OperatorExpression;
private StringBuilder sb = new StringBuilder();
    @Override
    public Void visitExpression(Expression elm, CqlContext context) {
        sb.append("something");
        if (elm instanceof ExpressionRef) {
            visitExpressionRef((ExpressionRef)elm, context);
        } else if (elm instanceof BinaryExpression) {
            visitBinaryExpression((BinaryExpression)elm, context);
        }
        else if (elm instanceof UnaryExpression) {
            visitUnaryExpression((UnaryExpression)elm, context);
        } else if (elm instanceof Today) {
            visitToday((Today)elm, context);
        } else if (elm instanceof Query) {
            visitQuery((Query)elm, context);
        } else if (elm instanceof Property) {
            visitProperty((Property)elm, context);
        }
        // else if (elm instanceof Quantity) {
        //     visitQuantity((Quantity)elm, context);
        // }
        // else if (elm instanceof Retrieve) {
        //     visitRetrieve((Retrieve)elm, context);
        // }
        // } else if (elm instanceof OperatorExpression) {
        //     visitInValueSet((OperatorExpression)elm, context)
        // }
        return null;
    }

// import org.hl7.elm.r1.Not;
// import org.hl7.elm.r1.As;
// import org.hl7.elm.r1.ToQuantity;
    @Override
    public Void visitUnaryExpression(UnaryExpression elm, CqlContext context) {
        if (elm instanceof Not) {
            visitNot((Not)elm, context);
        } else if (elm instanceof As) {
            visitAs((As)elm, context);
        } else if (elm instanceof ToQuantity) {
            visitToQuantity((ToQuantity)elm, context);
        }
        return null;
    }

    
// import org.hl7.elm.r1.And;
// import org.hl7.elm.r1.Or;
// import org.hl7.elm.r1.Equal;
// import org.hl7.elm.r1.Greater;
// import org.hl7.elm.r1.GreaterOrEqual;
// import org.hl7.elm.r1.Less;
// import org.hl7.elm.r1.LessOrEqual;
// import org.hl7.elm.r1.CalculateAgeAt;
    @Override
    public Void visitBinaryExpression(BinaryExpression elm, CqlContext context) {
        //left operator and right
        if (elm instanceof And) {
            visitAnd((And)elm, context);
        } else if (elm instanceof Or) {
            visitOr((Or)elm, context);
        } else if (elm instanceof Equal) {
            visitEqual((Equal)elm, context);
        } else if (elm instanceof Greater) {
            visitGreater((Greater)elm, context);
        } else if (elm instanceof GreaterOrEqual) {
            visitGreaterOrEqual((GreaterOrEqual)elm, context);
        } else if (elm instanceof Less) {
            visitLess((Less)elm, context);
        } else if (elm instanceof LessOrEqual) {
            visitLessOrEqual((LessOrEqual)elm, context);
        }
        // } else if (elm instanceof CalculateAgeAt) {
        //     visitCalculateAgeAt((CalculateAgeAt)elm, context);
        // }
        return null;
    }

    @Override
    public Void visitTernaryExpression(TernaryExpression elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitNaryExpression(NaryExpression elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitExpressionDef(ExpressionDef elm, CqlContext context) {
        if (elm.getAccessLevel() != null && elm.getAccessLevel().equals(AccessModifier.PUBLIC)) {
            visitExpression(elm.getExpression(), context);
            context.expressionStringStack.push("define " + elm.getName() + ": \n     ");
        }
        return null;
    }

    @Override
    public Void visitFunctionDef(FunctionDef elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    
// import org.hl7.elm.r1.FunctionRef;
    @Override
    public Void visitExpressionRef(ExpressionRef elm, CqlContext context) {
        if (elm instanceof FunctionRef) {
            visitFunctionRef((FunctionRef)elm, context);
        } else {
            context.expressionStringStack.push(elm.getName());
        }
        return null;
    }

    @Override
    public Void visitFunctionRef(FunctionRef elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitParameterDef(ParameterDef elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitParameterRef(ParameterRef elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitOperandDef(OperandDef elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitOperandRef(OperandRef elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitIdentifierRef(IdentifierRef elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitLiteral(Literal elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitTupleElement(TupleElement elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitTuple(Tuple elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitInstanceElement(InstanceElement elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitInstance(Instance elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitInterval(Interval elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitList(List elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitAnd(And elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitOr(Or elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitXor(Xor elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitNot(Not elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitIf(If elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitCaseItem(CaseItem elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitCase(Case elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitNull(Null elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitIsNull(IsNull elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitIsTrue(IsTrue elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitIsFalse(IsFalse elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitCoalesce(Coalesce elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitIs(Is elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitAs(As elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitConvert(Convert elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitToBoolean(ToBoolean elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitToConcept(ToConcept elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitToDateTime(ToDateTime elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitToDecimal(ToDecimal elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitToInteger(ToInteger elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitToQuantity(ToQuantity elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitToString(ToString elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitToTime(ToTime elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitEqual(Equal elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitEquivalent(Equivalent elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitNotEqual(NotEqual elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitLess(Less elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitGreater(Greater elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitLessOrEqual(LessOrEqual elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitGreaterOrEqual(GreaterOrEqual elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitAdd(Add elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitSubtract(Subtract elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitMultiply(Multiply elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitDivide(Divide elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitTruncatedDivide(TruncatedDivide elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitModulo(Modulo elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitCeiling(Ceiling elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitFloor(Floor elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitTruncate(Truncate elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitAbs(Abs elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitNegate(Negate elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitRound(Round elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitLn(Ln elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitExp(Exp elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitLog(Log elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitPower(Power elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitSuccessor(Successor elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitPredecessor(Predecessor elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitMinValue(MinValue elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitMaxValue(MaxValue elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitConcatenate(Concatenate elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitCombine(Combine elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitSplit(Split elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitLength(Length elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitUpper(Upper elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitLower(Lower elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitIndexer(Indexer elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitPositionOf(PositionOf elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitSubstring(Substring elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitDurationBetween(DurationBetween elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitDifferenceBetween(DifferenceBetween elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitDateFrom(DateFrom elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitTimeFrom(TimeFrom elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitTimezoneOffsetFrom(TimezoneOffsetFrom elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitDateTimeComponentFrom(DateTimeComponentFrom elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitTimeOfDay(TimeOfDay elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitToday(Today elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitNow(Now elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitDateTime(DateTime elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitTime(Time elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitSameAs(SameAs elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitSameOrBefore(SameOrBefore elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitSameOrAfter(SameOrAfter elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitWidth(Width elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitStart(Start elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitEnd(End elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitContains(Contains elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitProperContains(ProperContains elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitIn(In elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitProperIn(ProperIn elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitIncludes(Includes elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitIncludedIn(IncludedIn elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitProperIncludes(ProperIncludes elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitProperIncludedIn(ProperIncludedIn elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitBefore(Before elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitAfter(After elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitMeets(Meets elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitMeetsBefore(MeetsBefore elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitMeetsAfter(MeetsAfter elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitOverlaps(Overlaps elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitOverlapsBefore(OverlapsBefore elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitOverlapsAfter(OverlapsAfter elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitStarts(Starts elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitEnds(Ends elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitCollapse(Collapse elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitUnion(Union elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitIntersect(Intersect elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitExcept(Except elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitExists(Exists elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitTimes(Times elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitFilter(Filter elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitFirst(First elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitLast(Last elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitIndexOf(IndexOf elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitFlatten(Flatten elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitSort(Sort elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitForEach(ForEach elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitDistinct(Distinct elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitCurrent(Current elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitSingletonFrom(SingletonFrom elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitAggregateExpression(AggregateExpression elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitCount(Count elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitSum(Sum elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitMin(Min elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitMax(Max elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitAvg(Avg elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitMedian(Median elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitMode(Mode elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitVariance(Variance elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitPopulationVariance(PopulationVariance elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitStdDev(StdDev elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitPopulationStdDev(PopulationStdDev elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitAllTrue(AllTrue elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitAnyTrue(AnyTrue elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitProperty(Property elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitAliasedQuerySource(AliasedQuerySource elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitLetClause(LetClause elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitRelationshipClause(RelationshipClause elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitWith(With elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitWithout(Without elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitSortByItem(SortByItem elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitByDirection(ByDirection elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitByColumn(ByColumn elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitByExpression(ByExpression elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitSortClause(SortClause elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitReturnClause(ReturnClause elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitQuery(Query elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitAliasRef(AliasRef elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void visitQueryLetRef(QueryLetRef elm, CqlContext context) {
        // TODO Auto-generated method stub
        return null;
    }
}
