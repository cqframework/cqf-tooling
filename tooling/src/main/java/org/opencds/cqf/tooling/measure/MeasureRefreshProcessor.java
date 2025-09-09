package org.opencds.cqf.tooling.measure;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collections;

import org.cqframework.cql.cql2elm.CqlCompilerOptions;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.model.CompiledLibrary;
import org.cqframework.cql.elm.requirements.fhir.DataRequirementsProcessor;
import org.cqframework.cql.elm.requirements.fhir.utilities.SpecificationLevel;
import org.hl7.fhir.r5.model.CanonicalType;
import org.hl7.fhir.r5.model.Extension;
import org.hl7.fhir.r5.model.Library;
import org.hl7.fhir.r5.model.Measure;
import org.hl7.fhir.r5.model.Meta;
import org.hl7.fhir.r5.model.Reference;
import org.hl7.fhir.r5.model.RelatedArtifact;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.r5.model.StringType;
import org.opencds.cqf.tooling.utilities.constants.CrmiConstants;

public class MeasureRefreshProcessor {

    public Boolean includePopulationDataRequirements = false;

    public Measure refreshMeasure(Measure measureToUse, LibraryManager libraryManager, CompiledLibrary compiledLibrary, CqlCompilerOptions options) {

        // Computable measure http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/computable-measure-cqfm
        clearMeasureExtensions(measureToUse, "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter");
        clearMeasureExtensions(measureToUse, "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement");
        clearMeasureExtensions(measureToUse, "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-directReferenceCode");
        clearMeasureExtensions(measureToUse, "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition");
        clearMeasureExtensions(measureToUse, "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-effectiveDataRequirements");
        clearMeasureExtensions(measureToUse, CrmiConstants.EFFECTIVE_DATA_REQUIREMENTS_EXT_URL);
        clearRelatedArtifacts(measureToUse);

    	Library moduleDefinitionLibrary = getModuleDefinitionLibrary(measureToUse, libraryManager, compiledLibrary, options);
        removeModelInfoDependencies(moduleDefinitionLibrary);
        measureToUse.setDate(new Date());
        // http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/measure-cqfm
        setMeta(measureToUse, moduleDefinitionLibrary);
        moduleDefinitionLibrary.setId("effective-data-requirements");
        setEffectiveDataRequirements(measureToUse, moduleDefinitionLibrary);
        setEffectiveDataRequirementsReference(measureToUse);
        if (Boolean.TRUE.equals(includePopulationDataRequirements)) {
            setPopulationDataRequirements(measureToUse, libraryManager, compiledLibrary, options);
        }

        return measureToUse;
    }

    private void removeModelInfoDependencies(Library moduleDefinitionLibrary) {
        // NOTE: see similar logic in CqlProcessor.translateFile
        if (moduleDefinitionLibrary.hasRelatedArtifact()) {
            for (int i = moduleDefinitionLibrary.getRelatedArtifact().size() - 1; i >= 0; i--) {
                RelatedArtifact relatedArtifact = moduleDefinitionLibrary.getRelatedArtifact().get(i);
                if (relatedArtifact != null && relatedArtifact.hasResource() && (
                    relatedArtifact.getResource().startsWith("http://hl7.org/fhir/Library/QICore-ModelInfo")
                            || relatedArtifact.getResource().startsWith("http://fhir.org/guides/cqf/common/Library/FHIR-ModelInfo")
                            || relatedArtifact.getResource().startsWith("http://hl7.org/fhir/Library/USCore-ModelInfo")
                )) {
                    // Do not report dependencies on model info loaded from the translator, or
                    // from CQF Common (because these should be loaded from Using CQL now)
                    moduleDefinitionLibrary.getRelatedArtifact().remove(i);
                }
            }
        }
    }

    private Library getModuleDefinitionLibrary(Measure measureToUse, LibraryManager libraryManager, CompiledLibrary compiledLibrary, CqlCompilerOptions options){
        Set<String> expressionList = getExpressions(measureToUse);
        DataRequirementsProcessor dqReqTrans = new DataRequirementsProcessor();
        dqReqTrans.setSpecificationLevel(SpecificationLevel.QM_STU_1);
        return dqReqTrans.gatherDataRequirements(libraryManager, compiledLibrary, options, expressionList, true);
    }

    private void setPopulationDataRequirements(Measure measureToUse, LibraryManager libraryManager, CompiledLibrary compiledLibrary, CqlCompilerOptions options) {
        DataRequirementsProcessor dqReqTrans = new DataRequirementsProcessor();
        dqReqTrans.setSpecificationLevel(SpecificationLevel.QM_STU_1);
        measureToUse.getGroup().forEach(groupMember -> groupMember.getPopulation().forEach(population -> {
            if (population.hasId()) { // Requirement for computable measures
                var popMDL = dqReqTrans.gatherDataRequirements(libraryManager, compiledLibrary, options, Collections.singleton(population.getCriteria().getExpression()), false);
                var mdlID = population.getId() + "-effectiveDataRequirements";
                popMDL.setId(mdlID);
                setEffectiveDataRequirements(measureToUse, popMDL);
                population.getExtension().removeAll(population.getExtensionsByUrl(CrmiConstants.EFFECTIVE_DATA_REQUIREMENTS_EXT_URL));
                population.addExtension(CrmiConstants.EFFECTIVE_DATA_REQUIREMENTS_EXT_URL, new CanonicalType("#" + mdlID));
            }
        }));
    }

    private Set<String> getExpressions(Measure measureToUse) {
        Set<String> expressionSet = new HashSet<>();
        measureToUse.getSupplementalData().forEach(supData-> expressionSet.add(supData.getCriteria().getExpression()));
        measureToUse.getGroup().forEach(groupMember->{
            groupMember.getPopulation().forEach(population-> expressionSet.add(population.getCriteria().getExpression()));
            groupMember.getStratifier().forEach(stratifier-> expressionSet.add(stratifier.getCriteria().getExpression()));
        });
        return expressionSet;
    }

    private void clearMeasureExtensions(Measure measure, String extensionUrl) {
        List<Extension> extensionsToRemove = measure.getExtensionsByUrl(extensionUrl);
        measure.getExtension().removeAll(extensionsToRemove);
    }

    private void clearRelatedArtifacts(Measure measure) {
        measure.getRelatedArtifact().removeIf(r -> r.getType() == RelatedArtifact.RelatedArtifactType.DEPENDSON);
    }

    private void setEffectiveDataRequirements(Measure measureToUse, Library moduleDefinitionLibrary) {
    	int delIndex = -1;
    	for (Resource res : measureToUse.getContained()) {
    		if (res instanceof Library && res.getId().equalsIgnoreCase(moduleDefinitionLibrary.getId())) {
    			delIndex = measureToUse.getContained().indexOf(res);
    			break;
    		}
    	}

    	if (delIndex >= 0) {
    		measureToUse.getContained().remove(delIndex);
    	}

    	measureToUse.getContained().add(moduleDefinitionLibrary);
    }

    private void setEffectiveDataRequirementsReference(Measure measureToUse) {
        Extension effDataReqExtension = new Extension();
        effDataReqExtension.setUrl(CrmiConstants.EFFECTIVE_DATA_REQUIREMENTS_EXT_URL);
        effDataReqExtension.setValue(new CanonicalType("#" + CrmiConstants.EFFECTIVE_DATA_REQUIREMENTS_IDENTIFIER));
        measureToUse.addExtension(effDataReqExtension);
    }

    private void setParameters(Measure measureToUse, Library moduleDefinitionLibrary) {
        Set<String> parameterName = new HashSet<>();
        moduleDefinitionLibrary.getParameter().forEach(parameter->{
            if(!parameterName.contains(parameter.getName())) {
                Extension parameterExtension = new Extension();
                parameterExtension.setUrl("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter");
                parameterExtension.setValue(parameter);
                measureToUse.addExtension(parameterExtension);
                parameterName.add(parameter.getName());
            }
        });
    }

    private void setMeta(Measure measureToUse, Library moduleDefinitionLibrary){
        if (measureToUse.getMeta() == null) {
            measureToUse.setMeta(new Meta());
        }
        boolean hasProfileMarker = false;
        for (CanonicalType canonical : measureToUse.getMeta().getProfile()) {
            if ("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/computable-measure-cqfm".equals(canonical.getValue())) {
                hasProfileMarker = true;
            }
        }
        if (!hasProfileMarker) {
            measureToUse.getMeta().addProfile("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/computable-measure-cqfm");
        }
    }


}
