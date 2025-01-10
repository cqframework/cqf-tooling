package org.opencds.cqf.tooling.terminology;

public class CodeSystemLookupDictionary {

    /*

    Using HL7's Terminology 1.0.0 Publication IG: http://terminology.hl7.org/codesystems.html

    */

    public static String getUrlFromOid(String oid) {
        switch (oid) {
            case "2.16.840.1.113883.5.4": return "http://terminology.hl7.org/CodeSystem/v3-ActCode";
            case "2.16.840.1.113883.5.1001": return "http://terminology.hl7.org/CodeSystem/v3-ActMood";
            case "2.16.840.1.113883.5.7": return "http://terminology.hl7.org/CodeSystem/v3-ActPriority";
            case "2.16.840.1.113883.5.8": return "http://terminology.hl7.org/CodeSystem/v3-ActReason";
            case "2.16.840.1.113883.5.1002": return "http://terminology.hl7.org/CodeSystem/v3-ActRelationshipType";
            case "2.16.840.1.113883.5.14": return "http://terminology.hl7.org/CodeSystem/v3-ActStatus";
            case "2.16.840.1.113883.5.1119": return "http://terminology.hl7.org/CodeSystem/v3-AddressUse";
            case "2.16.840.1.113883.5.1": return "http://terminology.hl7.org/CodeSystem/v3-AdministrativeGender";
            case "2.16.840.1.113883.18.2": return "http://terminology.hl7.org/CodeSystem/v2-0001";
            case "2.16.840.1.113883.6.12": return "http://www.ama-assn.org/go/cpt";
            case "2.16.840.1.113883.12.292": return "http://hl7.org/fhir/sid/cvx";
            case "2.16.840.1.113883.5.25": return "http://terminology.hl7.org/CodeSystem/v3-Confidentiality";
            case "2.16.840.1.113883.12.112": return "urn:oid:2.16.840.1.113883.12.112";
            case "2.16.840.1.113883.4.642.1.1093": return "http://terminology.hl7.org/CodeSystem/discharge-disposition";
            case "2.16.840.1.113883.5.43": return "http://terminology.hl7.org/CodeSystem/v3-EntityNamePartQualifier";
            case "2.16.840.1.113883.5.45": return "http://terminology.hl7.org/CodeSystem/v3-EntityNameUse";
            case "2.16.840.1.113883.6.14": return "http://terminology.hl7.org/CodeSystem/HCPCS";
            case "2.16.840.1.113883.6.259": return "https://www.cdc.gov/nhsn/cdaportal/terminology/codesystem/hsloc.html";
            case "2.16.840.1.113883.6.285": return "http://www.nlm.nih.gov/research/umls/hcpcs";
            case "2.16.840.1.113883.6.3": return "http://terminology.hl7.org/CodeSystem/icd10";
            case "2.16.840.1.113883.6.4": return "http://www.cms.gov/Medicare/Coding/ICD10";
            case "2.16.840.1.113883.6.90": return "http://hl7.org/fhir/sid/icd-10-cm";
            case "2.16.840.1.113883.6.42": return "http://terminology.hl7.org/CodeSystem/icd9";
            case "2.16.840.1.113883.6.2": return "http://terminology.hl7.org/CodeSystem/icd9cm";
            case "2.16.840.1.113883.6.104": return "urn:oid:2.16.840.1.113883.6.104";
            case "2.16.840.1.113883.6.1": return "http://loinc.org";
            case "2.16.840.1.113883.5.60": return "http://terminology.hl7.org/CodeSystem/v3-LanguageAbilityMode";
            case "2.16.840.1.113883.5.61": return "http://terminology.hl7.org/CodeSystem/v3-LanguageAbilityProficiency";
            case "2.16.840.1.113883.5.63": return "http://terminology.hl7.org/CodeSystem/v3-LivingArrangement";
            case "2.16.840.1.113883.5.2": return "http://terminology.hl7.org/CodeSystem/v3-MaritalStatus";
            case "2.16.840.1.113883.6.69": return "http://hl7.org/fhir/sid/ndc";
            case "2.16.840.1.113883.3.26.1.1": return "http://ncithesaurus-stage.nci.nih.gov";
            case "2.16.840.1.113883.3.26.1.5": return "http://terminology.hl7.org/CodeSystem/nciVersionOfNDF-RT";
            case "2.16.840.1.113883.6.101": return "http://nucc.org/provider-taxonomy";
            case "2.16.840.1.113883.6.301.11": return "https://www.cms.gov/Medicare/Medicare-Fee-for-Service-Payment/HospitalAcqCond/Coding";
            case "2.16.840.1.113883.5.1008": return "http://terminology.hl7.org/CodeSystem/v3-NullFlavor";
            case "2.16.840.1.113883.5.83": return "http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation";
            case "2.16.840.1.113883.5.1063": return "http://terminology.hl7.org/CodeSystem/v3-ObservationValue";
            case "2.16.840.1.113883.5.88": return "http://terminology.hl7.org/CodeSystem/v3-ParticipationFunction";
            case "2.16.840.1.113883.5.1064": return "http://terminology.hl7.org/CodeSystem/v3-ParticipationMode";
            case "2.16.840.1.113883.5.90": return "http://terminology.hl7.org/CodeSystem/v3-ParticipationType";
            case "2.16.840.1.113883.6.88": return "http://www.nlm.nih.gov/research/umls/rxnorm";
            case "2.16.840.1.113883.5.1076": return "http://terminology.hl7.org/CodeSystem/v3-ReligiousAffiliation";
            case "2.16.840.1.113883.5.110": return "http://terminology.hl7.org/CodeSystem/v3-RoleClass";
            case "2.16.840.1.113883.5.111": return "http://terminology.hl7.org/CodeSystem/v3-RoleCode";
            case "2.16.840.1.113883.5.1068": return "http://terminology.hl7.org/CodeSystem/v3-RoleStatus";
            case "2.16.840.1.113883.6.96": return "http://snomed.info/sct";
            case "2.16.840.1.113883.6.21": return "http://terminology.hl7.org/CodeSystem/nubc-UB92";
            case "2.16.840.1.113883.6.301.3": return "http://terminology.hl7.org/CodeSystem/v2-0456";
            case "2.16.840.1.113883.6.50": return "http://terminology.hl7.org/CodeSystem/POS";
            case "2.16.840.1.113883.6.238": return "urn:oid:2.16.840.1.113883.6.238";
            case "2.16.840.1.113883.6.13": return "http://www.ada.org/cdt";
            case "2.16.840.1.113883.5.79": return "http://terminology.hl7.org/CodeSystem/v3-mediatypes";
            case "2.16.840.1.113883.3.221.5": return "https://nahdo.org/sopt";
            case "1.3.6.1.4.1.12009.10.3.1": return "urn:oid:1.3.6.1.4.1.12009.10.3.1";
            case "2.16.840.1.113883.6.8": return "http://unitsofmeasure.org";
            case "2.16.840.1.113883.6.86": return "http://terminology.hl7.org/CodeSystem/umls";
            default: throw new IllegalArgumentException("Unknown CodeSystem oid: " + oid);
        }
    }

    public static  String getUrlFromName(String name) {
        switch (name) {
            case "ActCode": return "http://terminology.hl7.org/CodeSystem/v3-ActCode";
            case "ActMood": return "http://terminology.hl7.org/CodeSystem/v3-ActMood";
            case "ActPriority": return "http://terminology.hl7.org/CodeSystem/v3-ActPriority";
            case "ActReason": return "http://terminology.hl7.org/CodeSystem/v3-ActReason";
            case "ActRelationshipType": return "http://terminology.hl7.org/CodeSystem/v3-ActRelationshipType";
            case "ActStatus": return "http://terminology.hl7.org/CodeSystem/v3-ActStatus";
            case "AddressUse": return "http://terminology.hl7.org/CodeSystem/v3-AddressUse";
            case "AdministrativeGender": return "http://terminology.hl7.org/CodeSystem/v3-AdministrativeGender";
            case "AdministrativeSex": return "http://terminology.hl7.org/CodeSystem/v2-0001";
            case "CPT": return "http://www.ama-assn.org/go/cpt";
            case "CPT-CAT-II": return "http://www.ama-assn.org/go/cpt";
            case "CVX": return "http://hl7.org/fhir/sid/cvx";
            case "Confidentiality": return "http://terminology.hl7.org/CodeSystem/v3-Confidentiality";
            case "DischargeDisposition": return "http://terminology.hl7.org/CodeSystem/discharge-disposition";
            case "EntityNamePartQualifier": return "http://terminology.hl7.org/CodeSystem/v3-EntityNamePartQualifier";
            case "EntityNameUse": return "http://terminology.hl7.org/CodeSystem/v3-EntityNameUse";
            case "HCPCS": return "http://terminology.hl7.org/CodeSystem/HCPCS";
            case "HCPCS Level I: CPT": return "http://terminology.hl7.org/CodeSystem/HCPCS";
            case "HCPCS Level II": return "http://www.nlm.nih.gov/research/umls/hcpcs";
            case "HSLOC": return "https://www.cdc.gov/nhsn/cdaportal/terminology/codesystem/hsloc.html";
            case "ICD10": return "http://terminology.hl7.org/CodeSystem/icd10";
            case "ICD10CM": return "http://hl7.org/fhir/sid/icd-10-cm";
            case "ICD10PCS": return "http://www.cms.gov/Medicare/Coding/ICD10";
            case "ICD9": return "http://terminology.hl7.org/CodeSystem/icd9";
            case "ICD9CM": return "http://terminology.hl7.org/CodeSystem/icd9cm";
            case "ICD9PCS": return "urn:oid:2.16.840.1.113883.6.104";
            case "LOINC": return "http://loinc.org";
            case "LanguageAbilityMode": return "http://terminology.hl7.org/CodeSystem/v3-LanguageAbilityMode";
            case "LanguageAbilityProficiency": return "http://terminology.hl7.org/CodeSystem/v3-LanguageAbilityProficiency";
            case "LivingArrangement": return "http://terminology.hl7.org/CodeSystem/v3-LivingArrangement";
            case "MaritalStatus": return "http://terminology.hl7.org/CodeSystem/v3-MaritalStatus";
            case "NCI": return "http://ncithesaurus-stage.nci.nih.gov";
            case "NDC": return "http://hl7.org/fhir/sid/ndc";
            case "NDFRT": return "http://terminology.hl7.org/CodeSystem/nciVersionOfNDF-RT";
            case "NUCCPT": return "http://nucc.org/provider-taxonomy";
            case "PresentOnAdmission": return "https://www.cms.gov/Medicare/Medicare-Fee-for-Service-Payment/HospitalAcqCond/Coding";
            case "Provider Taxonomy": return "http://nucc.org/provider-taxonomy";
            case "NullFlavor": return "http://terminology.hl7.org/CodeSystem/v3-NullFlavor";
            case "ObservationInterpretation": return "http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation";
            case "ObservationValue": return "http://terminology.hl7.org/CodeSystem/v3-ObservationValue";
            case "ParticipationFunction": return "http://terminology.hl7.org/CodeSystem/v3-ParticipationFunction";
            case "ParticipationMode": return "http://terminology.hl7.org/CodeSystem/v3-ParticipationMode";
            case "ParticipationType": return "http://terminology.hl7.org/CodeSystem/v3-ParticipationType";
            case "RXNORM": return "http://www.nlm.nih.gov/research/umls/rxnorm";
            case "ReligiousAffiliation": return "http://terminology.hl7.org/CodeSystem/v3-ReligiousAffiliation";
            case "RoleClass": return "http://terminology.hl7.org/CodeSystem/v3-RoleClass";
            case "RoleCode": return "http://terminology.hl7.org/CodeSystem/v3-RoleCode";
            case "RoleStatus": return "http://terminology.hl7.org/CodeSystem/v3-RoleStatus";
            case "SNOMEDCT": return "http://snomed.info/sct";
            case "UBREV": return "http://terminology.hl7.org/CodeSystem/nubc-UB92";
            case "UBTOB": return "http://terminology.hl7.org/CodeSystem/nubc-UB92";
            case "POS": return "http://terminology.hl7.org/CodeSystem/POS";
            case "PYXIS": return "http://content.alphora.com/fhir/dqm/CodeSystem/Pyxis";
            case "CDCREC": return "urn:oid:2.16.840.1.113883.6.238";
            case "Modifier": return "http://www.ama-assn.org/go/cpt";
            case "CDT": return "http://www.ada.org/cdt";
            case "mediaType": return "http://terminology.hl7.org/CodeSystem/v3-mediatypes";
            case "SOP":  return "https://nahdo.org/sopt";
            case "UCUM": return "http://unitsofmeasure.org";
            case "UMLS": return "http://terminology.hl7.org/CodeSystem/umls";
            default: throw new IllegalArgumentException("Unknown CodeSystem name: " + name);
        }
    }
}
