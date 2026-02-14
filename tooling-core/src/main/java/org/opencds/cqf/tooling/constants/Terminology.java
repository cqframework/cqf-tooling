package org.opencds.cqf.tooling.constants;

public class Terminology {

   private Terminology() {}

   public static final String CPG_COMPUTABLE_VS_PROFILE_URL = "http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-computablevalueset";
   public static final String CPG_EXECUTABLE_VS_PROFILE_URL = "http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-executablevalueset";
   public static final String CPG_PUBLISHABLE_VS_PROFILE_URL = "http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-publishablevalueset";
   public static final String RXNORM_SYSTEM_URL = "http://www.nlm.nih.gov/research/umls/rxnorm";
   public static final String LOINC_SYSTEM_URL = "http://loinc.org";
   public static final String RULES_TEXT_EXT_URL = "http://hl7.org/fhir/StructureDefinition/valueset-rules-text";
   public static final String CLINICAL_FOCUS_EXT_URL = "http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/cdc-valueset-clinical-focus";
   public static final String DATA_ELEMENT_SCOPE_EXT_URL = "http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/cdc-valueset-dataelement-scope";
   public static final String VS_INCLUSION_CRITERIA_EXT_URL = "http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/cdc-valueset-inclusion-criteria";
   public static final String VS_EXCLUSION_CRITERIA_EXT_URL = "http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/cdc-valueset-exclusion-criteria";
   public static final String VS_AUTHOR_EXT_URL = "http://hl7.org/fhir/StructureDefinition/valueset-author";
   public static final String KNOWLEDGE_CAPABILITY_EXT_URL = "http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-knowledgeCapability";
   public static final String KNOWLEDGE_REPRESENTATION_LEVEL_EXT_URL = "http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-knowledgeRepresentationLevel";
   public static final String USAGE_WARNING_EXT_URL = "http://hl7.org/fhir/uv/cpg/StructureDefinition/cpg-usageWarning";
   public static final String DEFAULT_USAGE_WARNING_VALUE = "This value set contains a point-in-time expansion enumerating the codes that meet the value set intent. As new versions of the code systems used by the value set are released, the contents of this expansion will need to be updated to incorporate newly defined codes that meet the value set intent. Before, and periodically during production use, the value set expansion contents SHOULD be updated. The value set expansion specifies the timestamp when the expansion was produced, SHOULD contain the parameters used for the expansion, and SHALL contain the codes that are obtained by evaluating the value set definition. If this is ONLY an executable value set, a distributable definition of the value set must be obtained to compute the updated expansion.";
}
