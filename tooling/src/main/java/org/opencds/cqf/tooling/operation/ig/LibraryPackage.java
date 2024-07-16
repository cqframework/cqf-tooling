package org.opencds.cqf.tooling.operation.ig;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.processor.CqlProcessor;
import org.opencds.cqf.tooling.utilities.ResourceUtils;

import java.util.ArrayList;
import java.util.List;

public class LibraryPackage {
   private final IBaseResource library;
   private final FhirContext fhirContext;
   private CqlProcessor.CqlSourceFileInformation cqlFileInfo;
   private List<IBaseResource> dependsOnLibraries;
   private List<IBaseResource> dependsOnValueSets;
   private List<IBaseResource> dependsOnCodeSystems;

   public LibraryPackage(IBaseResource library, FhirContext fhirContext, CqlProcessor.CqlSourceFileInformation cqlFileInfo) {
      this.library = library;
      this.fhirContext = fhirContext;
      this.cqlFileInfo = cqlFileInfo;
      this.dependsOnLibraries = new ArrayList<>();
      this.dependsOnValueSets = new ArrayList<>();
      this.dependsOnCodeSystems = new ArrayList<>();
   }

   public IBaseResource getLibrary() {
      return library;
   }

   public List<IBaseResource> getDependsOnLibraries() {
      return dependsOnLibraries;
   }

   public void addDependsOnLibrary(IBaseResource library) {
      if (library != null && this.dependsOnLibraries.stream().noneMatch(
              dep -> ResourceUtils.compareResourceIdUrlAndVersion(library, dep, fhirContext))) {
         this.dependsOnLibraries.add(library);
      }
   }

   public void setDependsOnLibraries(List<IBaseResource> dependsOnLibraries) {
      this.dependsOnLibraries = dependsOnLibraries;
   }

   public List<IBaseResource> getDependsOnValueSets() {
      return dependsOnValueSets;
   }

   public void addDependsOnValueSet(IBaseResource valueSet) {
      if (valueSet != null && this.dependsOnValueSets.stream().noneMatch(
              dep -> ResourceUtils.compareResourceIdUrlAndVersion(valueSet, dep, fhirContext))) {
         this.dependsOnValueSets.add(valueSet);
      }
   }

   public void setDependsOnValueSets(List<IBaseResource> dependsOnValueSets) {
      this.dependsOnValueSets = dependsOnValueSets;
   }

   public List<IBaseResource> getDependsOnCodeSystems() {
      return dependsOnCodeSystems;
   }

   public void addDependsOnCodeSystem(IBaseResource codeSystem) {
      // TODO: CodeSystems are extensible... Possible for multiple with the same ID - currently just including the first
      if (codeSystem != null && this.dependsOnCodeSystems.stream().noneMatch(
              dep -> ResourceUtils.compareResourcePrimitiveElements(codeSystem, dep, fhirContext, "id"))) {
         this.dependsOnCodeSystems.add(codeSystem);
      }
   }

   public void setDependsOnCodeSystems(List<IBaseResource> dependsOnCodeSystems) {
      this.dependsOnCodeSystems = dependsOnCodeSystems;
   }

   public CqlProcessor.CqlSourceFileInformation getCqlFileInfo() {
      return cqlFileInfo;
   }

   public void setCqlFileInfo(CqlProcessor.CqlSourceFileInformation cqlFileInfo) {
      this.cqlFileInfo = cqlFileInfo;
   }

   public FhirContext getFhirContext() {
      return fhirContext;
   }
}
