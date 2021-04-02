package org.opencds.cqf.tooling.utilities;

import org.cqframework.cql.cql2elm.*;
import org.cqframework.cql.cql2elm.model.TranslatedLibrary;
import org.hl7.fhir.r5.model.Library;
import org.hl7.fhir.r5.model.Measure;
import org.opencds.cqf.tooling.processor.DataRequirementsProcessor;

import java.util.HashSet;
import java.util.Set;

public class ECQMUtils {

    public static Library getModuleDefinitionLibrary(Measure measureToUse, LibraryManager libraryManager, TranslatedLibrary translatedLibrary, CqlTranslatorOptions options){

        Set<String> expressionList = getExpressions(measureToUse);
        DataRequirementsProcessor dqReqTrans = new DataRequirementsProcessor();
        return dqReqTrans.gatherDataRequirements(libraryManager, translatedLibrary, options, expressionList, true);
    }

    private static Set<String> getExpressions(Measure measureToUse) {
        Set<String> expressionSet = new HashSet<>();
        measureToUse.getSupplementalData().forEach(supData->{
            expressionSet.add(supData.getCriteria().getExpression());
        });
        measureToUse.getGroup().forEach(groupMember->{
            groupMember.getPopulation().forEach(population->{
                expressionSet.add(population.getCriteria().getExpression());
            });
            groupMember.getStratifier().forEach(stratifier->{
                expressionSet.add(stratifier.getCriteria().getExpression());
            });
        });
        return expressionSet;
    }
}
