package org.opencds.cqf.tooling.terminology.generate;

import ca.uhn.fhir.context.FhirContext;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactDetail;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.ImplementationGuide;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Helper {
   private static final Logger logger = LoggerFactory.getLogger(Helper.class);

   private Helper() { }

   public static ImplementationGuide resolveIgResource(String igResourcePath, FhirContext fhirContext) {
      IBaseResource ig = IOUtils.readResource(igResourcePath, fhirContext);
      if (ig instanceof ImplementationGuide) {
         return (ImplementationGuide) ig;
      }
      String message = String.format(
              "Expected ImplementationGuide resource from path %s, found %s", igResourcePath, ig.fhirType());
      logger.error(message);
      throw new FHIRException(message);
   }

   public static ValueSet boilerPlateValueSet(Config config, Config.ValueSets cvs, CommonMetaData cmd) {
      ValueSet vs = new ValueSet();

      // Base Resource Elements
      vs.setId(cvs.getId());
      if (cvs.getProfiles() != null && !cvs.getProfiles().isEmpty()) {
         vs.setMeta(new Meta().setProfile(
                 cvs.getProfiles().stream().map(CanonicalType::new).collect(Collectors.toList())));
      }
      else {
         vs.setMeta(new Meta().addProfile(Constants.CPG_COMPUTABLE_VS_PROFILE_URL)
                 .addProfile(Constants.CPG_EXECUTABLE_VS_PROFILE_URL));
      }

      // Extensions
      if (cvs.getClinicalFocus() != null) {
         vs.addExtension(Constants.CLINICAL_FOCUS_EXT_URL, new StringType(cvs.getClinicalFocus()));
      }
      if (cvs.getDataElementScope() != null) {
         vs.addExtension(Constants.DATA_ELEMENT_SCOPE_EXT_URL, new StringType(cvs.getDataElementScope()));
      }
      if (cvs.getInclusionCriteria() != null) {
         vs.addExtension(Constants.VS_INCLUSION_CRITERIA_EXT_URL, new StringType(cvs.getInclusionCriteria()));
      }
      if (cvs.getExclusionCriteria() != null) {
         vs.addExtension(Constants.VS_EXCLUSION_CRITERIA_EXT_URL, new StringType(cvs.getExclusionCriteria()));
      }
      if (config.getAuthor() != null) {
         vs.addExtension(Constants.VS_AUTHOR_EXT_URL, new ContactDetail().setName(
                 config.getAuthor().getName()).setTelecom(Collections.singletonList(
                         new ContactPoint()
                                 .setSystem(ContactPoint.ContactPointSystem.fromCode(config.getAuthor().getContactType()))
                                 .setValue(config.getAuthor().getContactValue()))));
      }
      else if (cmd.getContact() != null) {
         vs.addExtension(Constants.VS_AUTHOR_EXT_URL, cmd.getContact());
      }
      if (cvs.getKnowledgeCapability() != null && !cvs.getKnowledgeCapability().isEmpty()) {
         for (String kc : cvs.getKnowledgeCapability()) {
            vs.addExtension(Constants.KNOWLEDGE_CAPABILITY_EXT_URL, new CodeType(kc));
         }
      }
      if (cvs.getKnowledgeRepresentationLevel() != null && !cvs.getKnowledgeRepresentationLevel().isEmpty()) {
         for (String krl : cvs.getKnowledgeRepresentationLevel()) {
            vs.addExtension(Constants.KNOWLEDGE_REPRESENTATION_LEVEL_EXT_URL, new CodeType(krl));
         }
      }
      if (cvs.getUsageWarning() != null) {
         vs.addExtension(Constants.USAGE_WARNING_EXT_URL, new StringType(cvs.getUsageWarning()));
      }

      // Knowledge Artifact Elements
      vs.setUrl(cvs.getCanonical());
      vs.setName(cvs.getName());
      vs.setTitle(cvs.getTitle());
      if (cvs.getStatus() != null) {
         vs.setStatus(Enumerations.PublicationStatus.fromCode(cvs.getStatus()));
      }
      else {
         vs.setStatus(Enumerations.PublicationStatus.ACTIVE);
      }
      vs.setExperimental(cvs.getExperimental() == null || cvs.getExperimental());
      vs.setDate(cmd.getDate());
      vs.setPublisher(cmd.getPublisher());
      if (cmd.getContact() != null) {
         vs.setContact(Collections.singletonList(cmd.getContact()));
      }
      vs.setDescription(cvs.getDescription());
      if (cmd.getJurisdiction() != null && !cmd.getJurisdiction().isEmpty()) {
         vs.setJurisdiction(cmd.getJurisdiction());
      }
      vs.setPurpose(cvs.getPurpose());
      vs.setCopyright(cmd.getCopyright());

      return vs;
   }

   public static void updateValueSet(ValueSet vs, Config config, Config.ValueSets cvs, CommonMetaData cmd, boolean isVsac) {
      // Base Resource Elements
      vs.setId(cvs.getId());
      if (cvs.getProfiles() != null && !cvs.getProfiles().isEmpty()) {
         vs.setMeta(new Meta().setProfile(
                 cvs.getProfiles().stream().map(CanonicalType::new).collect(Collectors.toList())));
      }
      else {
         vs.setMeta(new Meta().addProfile(Constants.CPG_COMPUTABLE_VS_PROFILE_URL)
                 .addProfile(Constants.CPG_EXECUTABLE_VS_PROFILE_URL));
      }

      // Extensions
      if (cvs.getClinicalFocus() != null) {
         vs.addExtension(Constants.CLINICAL_FOCUS_EXT_URL, new StringType(cvs.getClinicalFocus()));
      }
      if (cvs.getDataElementScope() != null) {
         vs.addExtension(Constants.DATA_ELEMENT_SCOPE_EXT_URL, new StringType(cvs.getDataElementScope()));
      }
      if (cvs.getInclusionCriteria() != null) {
         vs.addExtension(Constants.VS_INCLUSION_CRITERIA_EXT_URL, new StringType(cvs.getInclusionCriteria()));
      }
      if (cvs.getExclusionCriteria() != null) {
         vs.addExtension(Constants.VS_EXCLUSION_CRITERIA_EXT_URL, new StringType(cvs.getExclusionCriteria()));
      }
      if (config.getAuthor() != null) {
         if (isVsac) {
            vs.getExtension().remove(vs.getExtensionByUrl(Constants.VS_AUTHOR_EXT_URL));
         }
         vs.addExtension(Constants.VS_AUTHOR_EXT_URL, new ContactDetail().setName(
                 config.getAuthor().getName()).setTelecom(Collections.singletonList(
                 new ContactPoint()
                         .setSystem(ContactPoint.ContactPointSystem.fromCode(config.getAuthor().getContactType()))
                         .setValue(config.getAuthor().getContactValue()))));
      }
      else if (cmd.getContact() != null) {
         vs.addExtension(Constants.VS_AUTHOR_EXT_URL, cmd.getContact());
      }
      if (cvs.getKnowledgeCapability() != null && !cvs.getKnowledgeCapability().isEmpty()) {
         for (String kc : cvs.getKnowledgeCapability()) {
            vs.addExtension(Constants.KNOWLEDGE_CAPABILITY_EXT_URL, new CodeType(kc));
         }
      }
      if (cvs.getKnowledgeRepresentationLevel() != null && !cvs.getKnowledgeRepresentationLevel().isEmpty()) {
         for (String krl : cvs.getKnowledgeRepresentationLevel()) {
            vs.addExtension(Constants.KNOWLEDGE_REPRESENTATION_LEVEL_EXT_URL, new CodeType(krl));
         }
      }
      if (cvs.getUsageWarning() != null) {
         vs.addExtension(Constants.USAGE_WARNING_EXT_URL, new StringType(cvs.getUsageWarning()));
      }

      // Knowledge Artifact Elements
      vs.setUrl(cvs.getCanonical());
      vs.setVersion(null); // strip versions - IG will populate this value
      if (cvs.getName() != null) {
         vs.setName(cvs.getName());
      }
      if (cvs.getTitle() != null) {
         vs.setTitle(cvs.getTitle());
      }
      if (cvs.getStatus() != null) {
         vs.setStatus(Enumerations.PublicationStatus.fromCode(cvs.getStatus()));
      }
      else {
         vs.setStatus(Enumerations.PublicationStatus.ACTIVE);
      }
      vs.setExperimental(cvs.getExperimental() == null || cvs.getExperimental());
      if (!isVsac) {
         vs.setDate(cmd.getDate());
      }
      vs.setPublisher(cmd.getPublisher());
      if (cmd.getContact() != null) {
         vs.setContact(Collections.singletonList(cmd.getContact()));
      }
      if (cvs.getDescription() != null) {
         vs.setDescription(cvs.getDescription());
      }
      if (cmd.getJurisdiction() != null && !cmd.getJurisdiction().isEmpty()) {
         vs.setJurisdiction(cmd.getJurisdiction());
      }
      if (cvs.getPurpose() != null) {
         vs.setPurpose(cvs.getPurpose());
      }
      vs.setCopyright(cmd.getCopyright());
   }

   public static ResponseHandler<String> getDefaultResponseHandler() {
      return response -> {
         int status = response.getStatusLine().getStatusCode();
         if (status >= 200 && status < 300) {
            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toString(entity) : null;
         } else {
            throw new ClientProtocolException("Unexpected response status: " + status);
         }
      };
   }

   public static List<Coding> removeExcludedCodes(List<Coding> codes, List<Coding> excludeCodes) {
      List<Coding> prunedCodes = new ArrayList<>();
      for (var code : codes) {
         if (excludeCodes.stream().noneMatch(x -> x.getCode().equals(code.getCode()))) {
            prunedCodes.add(code);
         }
      }
      return prunedCodes;
   }
}
