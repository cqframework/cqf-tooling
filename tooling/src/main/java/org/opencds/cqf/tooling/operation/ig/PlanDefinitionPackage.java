package org.opencds.cqf.tooling.operation.ig;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.util.BundleBuilder;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.PlanDefinition;
import org.opencds.cqf.tooling.utilities.ResourceUtils;

import java.util.ArrayList;
import java.util.List;

public class PlanDefinitionPackage {
   private final PlanDefinition r5PlanDefinition; // used for packaging
   private final IBaseResource planDefinition;
   private final FhirContext fhirContext;
   private final LibraryPackage libraryPackage;
   private List<IBaseResource> activityDefinitions;
   private List<IBaseResource> questionnaires;

   // TODO: handle nested PlanDefinitions
   private List<PlanDefinitionPackage> nestedPlanDefinitions;

   public PlanDefinitionPackage(PlanDefinition r5PlanDefinition, IBaseResource planDefinition,
                                FhirContext fhirContext, LibraryPackage libraryPackage) {
      this.r5PlanDefinition = r5PlanDefinition;
      this.planDefinition = planDefinition;
      this.fhirContext = fhirContext;
      this.libraryPackage = libraryPackage;
      this.activityDefinitions = new ArrayList<>();
      this.questionnaires = new ArrayList<>();
   }

   public IBaseBundle bundleResources() {
      BundleBuilder builder = new BundleBuilder(this.fhirContext);
      builder.addTransactionUpdateEntry(planDefinition);
      builder.addTransactionUpdateEntry(libraryPackage.getLibrary());
      libraryPackage.getDependsOnLibraries().forEach(builder::addTransactionUpdateEntry);
      libraryPackage.getDependsOnValueSets().forEach(builder::addTransactionUpdateEntry);
      libraryPackage.getDependsOnCodeSystems().forEach(builder::addTransactionUpdateEntry);
      activityDefinitions.forEach(builder::addTransactionUpdateEntry);
      questionnaires.forEach(builder::addTransactionUpdateEntry);
      return builder.getBundle();
   }

   public PlanDefinition getR5PlanDefinition() {
      return r5PlanDefinition;
   }

   public IBaseResource getPlanDefinition() {
      return planDefinition;
   }

   public FhirContext getFhirContext() {
      return fhirContext;
   }

   public LibraryPackage getLibraryPackage() {
      return libraryPackage;
   }

   public List<IBaseResource> getActivityDefinitions() {
      return activityDefinitions;
   }

   public void addActivityDefinition(IBaseResource activityDefinition) {
      if (activityDefinition != null && this.activityDefinitions.stream().noneMatch(
              dep -> ResourceUtils.compareResourceIdUrlAndVersion(activityDefinition, dep, fhirContext))) {
         this.activityDefinitions.add(activityDefinition);
      }
   }

   public void setActivityDefinitions(List<IBaseResource> activityDefinitions) {
      this.activityDefinitions = activityDefinitions;
   }

   public List<IBaseResource> getQuestionnaires() {
      return questionnaires;
   }

   public void addQuestionnaire(IBaseResource questionnaire) {
      if (questionnaire != null && this.questionnaires.stream().noneMatch(
              dep -> ResourceUtils.compareResourceIdUrlAndVersion(questionnaire, dep, fhirContext))) {
         this.questionnaires.add(questionnaire);
      }
   }

   public void setQuestionnaires(List<IBaseResource> questionnaires) {
      this.questionnaires = questionnaires;
   }

   public List<PlanDefinitionPackage> getNestedPlanDefinitions() {
      return nestedPlanDefinitions;
   }

   public void addNestedPlanDefinition(IBaseResource planDefinition) {
      if (planDefinition != null && this.nestedPlanDefinitions.stream().noneMatch(
              dep -> ResourceUtils.compareResourceIdUrlAndVersion(
                      planDefinition, dep.getPlanDefinition(), fhirContext))) {
         this.questionnaires.add(planDefinition);
      }
   }

   public void setNestedPlanDefinitions(List<PlanDefinitionPackage> nestedPlanDefinitions) {
      this.nestedPlanDefinitions = nestedPlanDefinitions;
   }
}
