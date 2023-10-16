package org.opencds.cqf.tooling.operations.valueset.generate.config;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.CodeType;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.ContactDetail;
import org.hl7.fhir.r5.model.ContactPoint;
import org.hl7.fhir.r5.model.Enumerations;
import org.hl7.fhir.r5.model.ImplementationGuide;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.model.ValueSet;
import org.opencds.cqf.tooling.constants.Terminology;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.operations.codesystem.loinc.HierarchyProcessor;
import org.opencds.cqf.tooling.operations.codesystem.rxnorm.RxMixWorkflowProcessor;
import org.opencds.cqf.tooling.operations.valueset.expansion.FhirTxExpansion;
import org.opencds.cqf.tooling.utilities.FhirContextCache;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.converters.ResourceAndTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Operation(name = "ValueSetsFromConfig")
public class ConfigValueSetGenerator implements ExecutableOperation {
   private final Logger logger = LoggerFactory.getLogger(ConfigValueSetGenerator.class);

   @OperationParam(alias = { "pathtoconfig", "ptc" }, setter = "setPathToConfig", required = true,
           description = "The path to the JSON configuration file.")
   private String pathToConfig;
   @OperationParam(alias = { "e", "encoding" }, setter = "setEncoding", defaultValue = "json",
           description = "The file format to be used for representing the resulting FHIR ValueSets { json, xml } (default json)")
   private String encoding;
   @OperationParam(alias = { "v", "version" }, setter = "setVersion", defaultValue = "r4",
           description = "FHIR version { stu3, r4, r5 } (default r4)")
   private String version;
   @OperationParam(alias = { "op", "outputpath" }, setter = "setOutputPath",
           defaultValue = "src/main/resources/org/opencds/cqf/tooling/terminology/output",
           description = "The directory path to which the generated FHIR ValueSet resources should be written (default src/main/resources/org/opencds/cqf/tooling/terminology/output)")
   private String outputPath;

   private FhirContext fhirContext;

   private final HierarchyProcessor hierarchyProcessor = new HierarchyProcessor();
   private final RxMixWorkflowProcessor rxMixWorkflowProcessor = new RxMixWorkflowProcessor();
   private final FhirTxExpansion fhirTxExpansion = new FhirTxExpansion();

   @Override
   public void execute() {
      ObjectMapper mapper = new ObjectMapper();
      Config config;
      try {
         config = mapper.readValue(new File(pathToConfig), Config.class);
      } catch (IOException e) {
         String message = "Error reading configuration: " + e.getMessage();
         logger.error(message);
         throw new IllegalArgumentException(message);
      }

      fhirContext = FhirContextCache.getContext(version);
      generateValueSets(config).forEach(
              vs -> IOUtils.writeResource(vs, outputPath, IOUtils.Encoding.parse(encoding), fhirContext)
      );
   }

   public List<IBaseResource> generateValueSets(Config config) {
      CommonMetaData commonMetaData = null;
      if (config.getPathToIgResource() != null) {
         IBaseResource igResource = IOUtils.readResource(config.pathToIgResource, fhirContext);
         ImplementationGuide ig = (ImplementationGuide) ResourceAndTypeConverter.convertToR5Resource(fhirContext, igResource);
         commonMetaData = new CommonMetaData(ig, config.getAuthor());
      }

      List<IBaseResource> valueSets = new ArrayList<>();
      for (var valueSet : config.getValueSets()) {
         ValueSet vs;
         if (valueSet.getHierarchy() != null) {
            prepareHierarchyProcessor(valueSet.getHierarchy());
            vs = (ValueSet) ResourceAndTypeConverter.convertToR5Resource(fhirContext, hierarchyProcessor.getValueSet());
         } else if (valueSet.getRulesText() != null) {
            prepareRxMixWorkflowProcessor(valueSet.getRulesText());
            vs = (ValueSet) ResourceAndTypeConverter.convertToR5Resource(fhirContext, rxMixWorkflowProcessor.getValueSet());
         } else if (valueSet.getExpand() != null) {
            prepareFhirTxExpansion(valueSet.getExpand());
            vs = (ValueSet) ResourceAndTypeConverter.convertToR5Resource(fhirContext, fhirTxExpansion.expandValueSet(
                    IOUtils.readResource(valueSet.getExpand().getPathToValueSet(), fhirContext)));
         } else {
            logger.warn("Unable to determine operation for {}, skipping...", valueSet.getId());
            continue;
         }
         valueSets.add(ResourceAndTypeConverter.convertFromR5Resource(
                 fhirContext, updateValueSet(vs, config, valueSet, commonMetaData)));
      }

      return valueSets;
   }

   public ValueSet updateValueSet(ValueSet vsToUpdate, Config config, Config.ValueSets configMetaData, CommonMetaData commonMetaData) {
      ValueSet updatedValueSet = new ValueSet();

      // metadata
      updatedValueSet.setId(configMetaData.getId());
      updatedValueSet.setUrl(configMetaData.getCanonical());
      updatedValueSet.setVersion(configMetaData.getVersion() == null && commonMetaData != null ? commonMetaData.getVersion() : configMetaData.getVersion());
      updatedValueSet.setName(configMetaData.getName());
      updatedValueSet.setTitle(configMetaData.getTitle());
      updatedValueSet.setStatus(configMetaData.getStatus() == null ? null : Enumerations.PublicationStatus.fromCode(configMetaData.getStatus()));
      updatedValueSet.setExperimental(configMetaData.getExperimental());
      updatedValueSet.setDate(configMetaData.getDate());
      updatedValueSet.setPublisher(configMetaData.getPublisher() == null && commonMetaData != null ? commonMetaData.getPublisher() : configMetaData.getPublisher());
      updatedValueSet.setDescription(configMetaData.getDescription());
      updatedValueSet.setJurisdiction(configMetaData.getJurisdiction() == null && commonMetaData != null ? commonMetaData.getJurisdiction() : resolveJurisdiction(configMetaData.getJurisdiction()));
      updatedValueSet.setPurpose(configMetaData.getPurpose());
      updatedValueSet.setCopyright(configMetaData.getCopyright() == null && commonMetaData != null ? commonMetaData.getCopyright() : configMetaData.getCopyright());
      if (configMetaData.getProfiles() != null) {
         configMetaData.getProfiles().forEach(profile -> updatedValueSet.getMeta().addProfile(profile));
      } else if (vsToUpdate.getMeta().hasProfile()) {
         updatedValueSet.getMeta().setProfile(vsToUpdate.getMeta().getProfile());
      }

      // extensions
      updatedValueSet.setExtension(vsToUpdate.getExtension());
      if (config != null && config.getAuthor() != null) {
         updatedValueSet.addExtension(Terminology.VS_AUTHOR_EXT_URL, new ContactDetail()
                 .setName(config.getAuthor().getName()).setTelecom(Collections.singletonList(
                         new ContactPoint().setSystem(ContactPoint.ContactPointSystem.fromCode(
                                 config.getAuthor().getContactType())).setValue(config.getAuthor().contactValue))));
      } else if (commonMetaData != null && commonMetaData.getAuthor() != null) {
         updatedValueSet.addExtension(Terminology.VS_AUTHOR_EXT_URL, commonMetaData.getAuthor());
      } else if (commonMetaData != null && commonMetaData.getContact() != null) {
         for (var contact : commonMetaData.getContact()) {
            updatedValueSet.addExtension(Terminology.VS_AUTHOR_EXT_URL, contact);
         }
      }
      if (configMetaData.getClinicalFocus() != null) {
         updatedValueSet.addExtension(Terminology.CLINICAL_FOCUS_EXT_URL, new StringType(configMetaData.getClinicalFocus()));
      }
      if (configMetaData.getDataElementScope() != null) {
         updatedValueSet.addExtension(Terminology.DATA_ELEMENT_SCOPE_EXT_URL, new StringType(configMetaData.getDataElementScope()));
      }
      if (configMetaData.getInclusionCriteria() != null) {
         updatedValueSet.addExtension(Terminology.VS_INCLUSION_CRITERIA_EXT_URL, new StringType(configMetaData.getInclusionCriteria()));
      }
      if (configMetaData.getExclusionCriteria() != null) {
         updatedValueSet.addExtension(Terminology.VS_EXCLUSION_CRITERIA_EXT_URL, new StringType(configMetaData.getExclusionCriteria()));
      }
      if (configMetaData.getUsageWarning() != null) {
         updatedValueSet.addExtension(Terminology.USAGE_WARNING_EXT_URL, new StringType(configMetaData.getUsageWarning()));
      }
      if (configMetaData.getKnowledgeCapability() != null) {
         for (String kc : configMetaData.getKnowledgeCapability()) {
            updatedValueSet.addExtension(Terminology.KNOWLEDGE_CAPABILITY_EXT_URL, new CodeType(kc));
         }
      }
      if (configMetaData.getKnowledgeRepresentationLevel() != null) {
         for (String krl : configMetaData.getKnowledgeRepresentationLevel()) {
            updatedValueSet.addExtension(Terminology.KNOWLEDGE_REPRESENTATION_LEVEL_EXT_URL, new CodeType(krl));
         }
      }

      // expansion
      if (vsToUpdate.hasExpansion()) {
         updatedValueSet.setExpansion(vsToUpdate.getExpansion());
      } else {
         ValueSet.ValueSetExpansionComponent expansion = new ValueSet.ValueSetExpansionComponent();
         expansion.setTimestamp(new Date());
         List<Coding> codes = getCodesFromCompose(vsToUpdate);
         expansion.setTotal(codes.size());
         for (var code : codes) {
            expansion.addContains().setCode(code.getCode()).setSystem(code.getSystem()).setDisplay(code.getDisplay()).setVersion(code.getVersion());
         }
         updatedValueSet.setExpansion(expansion);
      }

      return updatedValueSet;
   }

   // helper functions/classes
   private List<Coding> getCodesFromCompose(ValueSet vs) {
      List<Coding> codes = new ArrayList<>();
      for (var compose : vs.getCompose().getInclude()) {
         for (var concept : compose.getConcept()) {
            codes.add(new Coding().setSystem(compose.getSystem()).setCode(concept.getCode()).setDisplay(concept.getDisplay()).setVersion(compose.getVersion()));
         }
      }
      return codes;
   }

   private List<CodeableConcept> resolveJurisdiction(Config.ValueSets.Jurisdiction jurisdiction) {
      if (jurisdiction == null) {
         return null;
      }
      String code = jurisdiction.getCoding().getCode();
      String system = jurisdiction.getCoding().getSystem();
      String display = jurisdiction.getCoding().getDisplay();
      return Collections.singletonList(new CodeableConcept().addCoding(new Coding().setCode(code).setSystem(system).setDisplay(display)));
   }

   private void prepareHierarchyProcessor(Config.ValueSets.Hierarchy hierarchy) {
      hierarchyProcessor.setFhirContext(fhirContext);
      hierarchyProcessor.setVersion(version);
      hierarchyProcessor.setEncoding(encoding);
      hierarchyProcessor.setQuery(hierarchy.getQuery());
      hierarchyProcessor.setNarrative(hierarchy.getNarrative());
      // TODO: remove after demo, uncomment block below and make Config.java auth required
      hierarchyProcessor.setUsername("cschuler");
      hierarchyProcessor.setPassword("knight");
//      if (hierarchyProcessor.getUsername() == null) {
//         hierarchyProcessor.setUsername(hierarchy.getAuth().getUser());
//      }
//      if (hierarchyProcessor.getPassword() == null) {
//         hierarchyProcessor.setPassword(hierarchy.getAuth().getPassword());
//      }
   }

   private void prepareRxMixWorkflowProcessor(Config.ValueSets.RulesText rulesText) {
      rxMixWorkflowProcessor.setFhirContext(fhirContext);
      rxMixWorkflowProcessor.setVersion(version);
      rxMixWorkflowProcessor.setEncoding(encoding);
      rxMixWorkflowProcessor.setRulesText(rulesText.getNarrative());
      rxMixWorkflowProcessor.setWorkflow(rulesText.getWorkflowXml());
      rxMixWorkflowProcessor.setInputs(rulesText.getInput());
      rxMixWorkflowProcessor.setIncludeFilters(rulesText.getIncludeFilter());
      rxMixWorkflowProcessor.setExcludeFilters(rulesText.getExcludeFilter());
   }

   private void prepareFhirTxExpansion(Config.ValueSets.Expand expand) {
      fhirTxExpansion.setFhirContext(fhirContext);
      fhirTxExpansion.setVersion(version);
      fhirTxExpansion.setEncoding(encoding);
      fhirTxExpansion.setFhirServer(expand.getTxServer().getBaseUrl());
   }

   public Logger getLogger() {
      return logger;
   }

   public String getPathToConfig() {
      return pathToConfig;
   }

   public void setPathToConfig(String pathToConfig) {
      this.pathToConfig = pathToConfig;
   }

   public String getEncoding() {
      return encoding;
   }

   public void setEncoding(String encoding) {
      this.encoding = encoding;
   }

   public String getVersion() {
      return version;
   }

   public void setVersion(String version) {
      this.version = version;
   }

   public String getOutputPath() {
      return outputPath;
   }

   public void setOutputPath(String outputPath) {
      this.outputPath = outputPath;
   }

   public void setFhirContext(FhirContext fhirContext) {
      this.fhirContext = fhirContext;
   }

   static class CommonMetaData {
      private String version;
      private String publisher;
      private String copyright;
      private ContactDetail author;
      private List<ContactDetail> contact;
      private List<CodeableConcept> jurisdiction;

      public CommonMetaData() {
         version = null;
         publisher = null;
         copyright = null;
         contact = null;
         jurisdiction = null;
      }

      public CommonMetaData(ImplementationGuide ig, Config.Author author) {
         if (ig.hasVersion()) {
            this.version = ig.getVersion();
         }
         if (ig.hasPublisher()) {
            this.publisher = ig.getPublisher();
         }
         if (ig.hasCopyright()) {
            this.copyright = ig.getCopyright();
         }
         if (author != null) {
            this.author = new ContactDetail().setName(author.getName()).setTelecom(Collections.singletonList(
                    new ContactPoint().setSystem(ContactPoint.ContactPointSystem.fromCode(author.getContactType()))
                            .setValue(author.contactValue)));
         }
         if (ig.hasContact()) {
            this.contact = ig.getContact();
         }
         if (ig.hasJurisdiction()) {
            this.jurisdiction = ig.getJurisdiction();
         }
      }

      public String getVersion() {
         return version;
      }

      public String getPublisher() {
         return publisher;
      }

      public String getCopyright() {
         return copyright;
      }

      public ContactDetail getAuthor() {
         return author;
      }

      public List<ContactDetail> getContact() {
         return contact;
      }

      public List<CodeableConcept> getJurisdiction() {
         return jurisdiction;
      }
   }
}
