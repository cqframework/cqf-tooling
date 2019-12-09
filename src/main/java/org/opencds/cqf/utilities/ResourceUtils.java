package org.opencds.cqf.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.hl7.elm.r1.IncludeDef;
import org.hl7.elm.r1.ValueSetDef;
import org.hl7.fhir.Resource;
import org.hl7.fhir.dstu3.model.Library;
import org.hl7.fhir.dstu3.model.RelatedArtifact;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Enumerations.FHIRVersion;
import org.opencds.cqf.library.GenericLibrarySourceProvider;

import ca.uhn.fhir.context.FhirContext;

public class ResourceUtils 
{
    public enum FhirVersion 
    { 
        DSTU3("dstu3"), R4("r4"); 

        private String string; 
    
        public String toString() 
        { 
            return this.string; 
        } 
    
        private FhirVersion(String string) 
        { 
            this.string = string; 
        }

        public static FhirVersion parse(String value) {
            switch (value) {
                case "dstu3": 
                    return DSTU3;
                case "r4":
                    return R4;
                default: 
                    throw new RuntimeException("Unable to parse FHIR version value:" + value);
            }
        }
    }
    
    public static void setIgId(String baseId, IAnyResource resource, Boolean includeVersion)
    {
      String igId = "";
      String resourceName = resource.getClass().getSimpleName().toLowerCase();
      String versionId = includeVersion ? "-" + resource.getMeta().getVersionId() : "";
      
      if (resource instanceof org.hl7.fhir.dstu3.model.Bundle || resource instanceof org.hl7.fhir.r4.model.Bundle) {
        igId = baseId + versionId + "-" + resourceName;        
      }
      else {
        igId = resourceName + "-" + baseId + versionId;

      }
      igId = igId.replace("_", "-");
      resource.setId(igId);
    }

    public static FhirContext getFhirContext(FhirVersion fhirVersion) {
      switch (fhirVersion) {
        case DSTU3:
          return FhirContext.forDstu3();
        case R4:
          return FhirContext.forR4();
        default:
          throw new IllegalArgumentException("Unknown FHIR version: " + fhirVersion);
      }
    }

    public static List<RelatedArtifact> getSTU3RelatedArtifacts(String pathToLibrary) {
      FhirContext fhirContext = FhirContext.forDstu3();
      try {
          Library mainLibrary = (Library) IOUtils.readResource(pathToLibrary, fhirContext);
          return mainLibrary.getRelatedArtifact();
          
      } catch (Exception e) {
          throw new IllegalArgumentException("pathToLibrary must be a path to a  Library Resource");
        }
    }

  public static List<org.hl7.fhir.r4.model.RelatedArtifact> getR4RelatedArtifacts(String pathToLibrary) {
      FhirContext fhirContext = FhirContext.forR4();
      try {
          org.hl7.fhir.r4.model.Library mainLibrary = (org.hl7.fhir.r4.model.Library) IOUtils.readResource(pathToLibrary, fhirContext);
          return mainLibrary.getRelatedArtifact();
          
      } catch (Exception e) {
          throw new IllegalArgumentException("pathToLibrary must be a path to a  Library Resource");
        }
    }    

    public static Map<String, Library> getSTU3DependencyLibraries(String pathToLibrary) {
      Map<String, Library> DependencyLibraries = new HashMap<String, Library>();
      FhirContext fhirContext = FhirContext.forDstu3();
      String pathToLibraryDirectory = pathToLibrary.substring(0, pathToLibrary.lastIndexOf("/"));
      List<RelatedArtifact> relatedArtifacts = getSTU3RelatedArtifacts(pathToLibrary);
      for (RelatedArtifact relatedArtifact : relatedArtifacts) {
          String libraryName = relatedArtifact.getResource().getReference().split("Library/")[1];
          String libraryPath = pathToLibraryDirectory + "/" + libraryName;
          try {
              DependencyLibraries.put(libraryPath, (Library) IOUtils.readResource(libraryPath, fhirContext));
          } catch (Exception e) {
              System.out.println("could not find a library resource for " + libraryName + "in" + pathToLibraryDirectory);
              System.out.println(e.getMessage());
          }
        }
        return DependencyLibraries;
    }

    public static Map<String, org.hl7.fhir.r4.model.Library> getR4DependencyLibraries(String pathToLibrary) {
      Map<String, org.hl7.fhir.r4.model.Library> DependencyLibraries = new HashMap<String, org.hl7.fhir.r4.model.Library>();
      FhirContext fhirContext = FhirContext.forR4();
      String pathToLibraryDirectory = pathToLibrary.substring(0, pathToLibrary.lastIndexOf("/"));
      List<org.hl7.fhir.r4.model.RelatedArtifact> relatedArtifacts = getR4RelatedArtifacts(pathToLibrary);
      for (org.hl7.fhir.r4.model.RelatedArtifact relatedArtifact : relatedArtifacts) {
          String libraryName = relatedArtifact.getResource().split("Library/")[1];
          String libraryPath = pathToLibraryDirectory + "/" + libraryName;
          try {
              DependencyLibraries.put(libraryPath, (org.hl7.fhir.r4.model.Library) IOUtils.readResource(libraryPath, fhirContext));
          } catch (Exception e) {
              System.out.println("could not find a library resource for " + libraryName + "in" + pathToLibraryDirectory);
              System.out.println(e.getMessage());
          }
        }
        return DependencyLibraries;
    }

    public static ArrayList<String> getIncludedLibraryNames(String cqlContentPath) {
      ArrayList<String> includedLibraryNames = new ArrayList<String>();
      ArrayList<IncludeDef> includedDefs = getIncludedDefs(cqlContentPath);
      for (IncludeDef def : includedDefs) {
        includedLibraryNames.add(def.getPath());
      }
      return includedLibraryNames;
    }

    public static ArrayList<String> getDependencyValueSetNames(String cqlContentPath) {
      ArrayList<String> includedLibraryNames = new ArrayList<String>();
      ArrayList<ValueSetDef> valueSetDefs = getValueSetDefs(cqlContentPath);
      for (ValueSetDef def : valueSetDefs) {
        includedLibraryNames.add(def.getName());
      }
      return includedLibraryNames;
    }

    public static ArrayList<IncludeDef> getIncludedDefs(String cqlContentPath) {
      ArrayList<IncludeDef> includedDefs = new ArrayList<IncludeDef>();
      org.hl7.elm.r1.Library elm = getElmFromCql(cqlContentPath);
      if (elm.getIncludes() != null && !elm.getIncludes().getDef().isEmpty()) {
        for (IncludeDef def : elm.getIncludes().getDef()) {
          includedDefs.add(def);
        }
      }
      return includedDefs;
    }

    public static ArrayList<ValueSetDef> getValueSetDefs(String cqlContentPath) {
      ArrayList<ValueSetDef> valueSetDefs = new ArrayList<ValueSetDef>();
      org.hl7.elm.r1.Library elm = getElmFromCql(cqlContentPath);
      if (elm.getValueSets() != null && !elm.getValueSets().getDef().isEmpty()) {
        for (ValueSetDef def : elm.getValueSets().getDef()) {
          valueSetDefs.add(def);
        }
      }
      return valueSetDefs;
    }

    public static org.hl7.elm.r1.Library getElmFromCql(String cqlContentPath) {
      String cqlDirPath = IOUtils.getParentDirectoryPath(cqlContentPath);
      ModelManager modelManager = new ModelManager();
      GenericLibrarySourceProvider sourceProvider = new GenericLibrarySourceProvider(cqlDirPath);
      LibraryManager libraryManager = new LibraryManager(modelManager);
      libraryManager.getLibrarySourceLoader().registerProvider(sourceProvider);

      CqlTranslator translator = IOUtils.translate(cqlContentPath, modelManager, libraryManager);
      
      return translator.toELM();  
    }  
}
