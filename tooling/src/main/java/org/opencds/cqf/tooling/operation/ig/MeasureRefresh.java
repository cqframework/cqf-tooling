package org.opencds.cqf.tooling.operation.ig;

import ca.uhn.fhir.util.BundleUtil;
import org.cqframework.cql.elm.requirements.fhir.DataRequirementsProcessor;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Library;
import org.hl7.fhir.r5.model.Measure;
import org.opencds.cqf.tooling.processor.CqlProcessor;
import org.opencds.cqf.tooling.utilities.BundleUtils;
import org.opencds.cqf.tooling.utilities.constants.CqfmConstants;
import org.opencds.cqf.tooling.utilities.converters.ResourceAndTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MeasureRefresh extends Refresh {
   private static final Logger logger = LoggerFactory.getLogger(MeasureRefresh.class);
   private final CqlProcessor cqlProcessor;
   private final List<LibraryPackage> libraryPackages;
   private final List<MeasurePackage> measurePackages;

   public MeasureRefresh(IGInfo igInfo, CqlProcessor cqlProcessor, List<LibraryPackage> libraryPackages) {
      super(igInfo);
      this.cqlProcessor = cqlProcessor;
      this.libraryPackages = libraryPackages;
      this.measurePackages = new ArrayList<>();
   }

   @Override
   public List<IBaseResource> refresh() {
      List<IBaseResource> refreshedMeasures = new ArrayList<>();

      if (getIgInfo().isRefreshMeasures()) {
         logger.info("Refreshing Measures...");

         if (cqlProcessor.getFileMap() == null) {
            cqlProcessor.execute();
         }

         DataRequirementsProcessor dataRecProc = new DataRequirementsProcessor();
         Class<? extends IBaseResource> clazz = getFhirContext().getResourceDefinition(
                 "Measure").newInstance().getClass();
         IBaseBundle bundle = BundleUtils.getBundleOfResourceTypeFromDirectory(
                 getIgInfo().getMeasureResourcePath(), getFhirContext(), clazz);

         for (var resource : BundleUtil.toListOfResources(getFhirContext(), bundle)) {
            Measure measure = (Measure) ResourceAndTypeConverter.convertToR5Resource(getFhirContext(), resource);

            logger.info("Refreshing {}", measure.getId());

            validatePrimaryLibraryReference(measure);
            String libraryUrl = measure.getLibrary().get(0).getValueAsString();
            LibraryPackage libraryPackage = libraryPackages.stream().filter(
                            pkg -> libraryUrl.endsWith(pkg.getCqlFileInfo().getIdentifier().getId()))
                    .findFirst().orElse(null);
            for (CqlProcessor.CqlSourceFileInformation info : cqlProcessor.getAllFileInformation()) {
               if (libraryUrl.endsWith(info.getIdentifier().getId())) {
                  // TODO: should likely verify or resolve/refresh the following elements:
                  //  cqfm-artifactComment, cqfm-allocation, cqfm-softwaresystem, url, identifier, version,
                  //  name, title, status, experimental, type, publisher, contact, description, useContext,
                  //  jurisdiction, and profile(s) (http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/measure-cqfm)
                  measure.setDate(new Date());
                  addProfiles(measure, CqfmConstants.COMPUTABLE_MEASURE_PROFILE_URL);
                  Library moduleDefinitionLibrary = getModuleDefinitionLibrary(
                          measure, dataRecProc, info);
                  refreshCqfmExtensions(measure, moduleDefinitionLibrary);
                  attachModuleDefinitionLibrary(measure, moduleDefinitionLibrary);
                  IBaseResource refreshedMeasure = ResourceAndTypeConverter.convertFromR5Resource(getFhirContext(), measure);
                  refreshedMeasures.add(refreshedMeasure);
                  measurePackages.add(new MeasurePackage(measure, refreshedMeasure, getFhirContext(), libraryPackage));
               }
            }

            logger.info("Success!");
         }
      }
      return refreshedMeasures;
   }

   private Library getModuleDefinitionLibrary(Measure measure, DataRequirementsProcessor dataRecProc,
                                              CqlProcessor.CqlSourceFileInformation info) {
      Set<String> expressions = getExpressions(measure);
      return dataRecProc.gatherDataRequirements(
              cqlProcessor.getLibraryManager(),
              cqlProcessor.getLibraryManager().resolveLibrary(
                      info.getIdentifier(), new ArrayList<>()),
              info.getOptions().getCqlCompilerOptions(), expressions, true);
   }

   private Set<String> getExpressions(Measure measure) {
      Set<String> expressionSet = new HashSet<>();
      // TODO: check if expression is a cql expression
      measure.getSupplementalData().forEach(supData -> {
         if (supData.hasCriteria() && isExpressionIdentifier(supData.getCriteria())) {
            expressionSet.add(supData.getCriteria().getExpression());
         }
      });
      measure.getGroup().forEach(groupMember -> {
         groupMember.getPopulation().forEach(population -> {
            if (population.hasCriteria() && isExpressionIdentifier(population.getCriteria())) {
               expressionSet.add(population.getCriteria().getExpression());
            }
         });
         groupMember.getStratifier().forEach(stratifier -> {
            if (stratifier.hasCriteria() && isExpressionIdentifier(stratifier.getCriteria())) {
               expressionSet.add(stratifier.getCriteria().getExpression());
            }
         });
      });
      return expressionSet;
   }

   public List<MeasurePackage> getMeasurePackages() {
      return measurePackages;
   }
}
