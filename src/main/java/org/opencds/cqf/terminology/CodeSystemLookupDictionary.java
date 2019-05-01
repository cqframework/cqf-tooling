package org.opencds.cqf.terminology;

public class CodeSystemLookupDictionary {

    /*
    CodeSystem Name           | URL                                               | OID
    --------------------------------------------------------------------------------------------------------------------------------
    ActCode	                    http://hl7.org/fhir/v3/ActCode	                    2.16.840.1.113883.5.4
    ActMood	                    http://hl7.org/fhir/v3/ActMood	                    2.16.840.1.113883.5.1001
    ActPriority	                http://hl7.org/fhir/v3/ActPriority	                2.16.840.1.113883.5.7
    ActReason	                http://hl7.org/fhir/v3/ActReason	                2.16.840.1.113883.5.8
    ActRelationshipType	        http://hl7.org/fhir/v3/ActRelationshipType	        2.16.840.1.113883.5.1002
    ActStatus	                http://hl7.org/fhir/v3/ActStatus	                2.16.840.1.113883.5.14
    AddressUse	                http://hl7.org/fhir/v3/AddressUse	                2.16.840.1.113883.5.1119
    AdministrativeGender	    http://hl7.org/fhir/v3/AdministrativeGender	        2.16.840.1.113883.5.1
    AdministrativeSex	        http://hl7.org/fhir/v2/0001	                        2.16.840.1.113883.18.2
    CPT	                        http://www.ama-assn.org/go/cpt	                    2.16.840.1.113883.6.12
    CVX	                        http://hl7.org/fhir/sid/cvx	                        2.16.840.1.113883.12.292
    Confidentiality	            http://hl7.org/fhir/v3/Confidentiality	            2.16.840.1.113883.5.25
    DischargeDisposition        http://hl7.org/fhir/discharge-disposition	        2.16.840.1.113883.12.112
    EntityNamePartQualifier	    http://hl7.org/fhir/v3/EntityNamePartQualifier	    2.16.840.1.113883.5.43
    EntityNameUse	            http://hl7.org/fhir/v3/EntityNameUse	            2.16.840.1.113883.5.45
    ICD10CM	                    http://hl7.org/fhir/sid/icd-10	                    2.16.840.1.113883.6.90
    ICD10PCS	                http://www.icd10data.com/icd10pcs	                2.16.840.1.113883.6.4
    ICD9CM	                    http://hl7.org/fhir/sid/icd-9-cm	                2.16.840.1.113883.6.103,2.16.840.1.113883.6.104
    LOINC	                    http://loinc.org	                                2.16.840.1.113883.6.1
    LanguageAbilityMode	        http://hl7.org/fhir/v3/LanguageAbilityMode	        2.16.840.1.113883.5.60
    LanguageAbilityProficiency	http://hl7.org/fhir/v3/LanguageAbilityProficiency	2.16.840.1.113883.5.61
    LivingArrangement	        http://hl7.org/fhir/v3/LivingArrangement	        2.16.840.1.113883.5.63
    MaritalStatus	            http://hl7.org/fhir/v3/MaritalStatus	            2.16.840.1.113883.5.2
    NCI	                        http://ncimeta.nci.nih.gov	                        2.16.840.1.113883.3.26.1.1
    NDFRT	                    http://hl7.org/fhir/ndfrt	                        2.16.840.1.113883.3.26.1.5
    NUCCPT	                    http://nucc.org/provider-taxonomy	                2.16.840.1.113883.6.101
    NullFlavor	                http://hl7.org/fhir/v3/NullFlavor	                2.16.840.1.113883.5.1008
    ObservationInterpretation	http://hl7.org/fhir/v3/ObservationInterpretation	2.16.840.1.113883.5.83
    ObservationValue	        http://hl7.org/fhir/v3/ObservationValue	            2.16.840.1.113883.5.1063
    ParticipationFunction	    http://hl7.org/fhir/v3/ParticipationFunction	    2.16.840.1.113883.5.88
    ParticipationMode	        http://hl7.org/fhir/v3/ParticipationMode	        2.16.840.1.113883.5.1064
    ParticipationType	        http://hl7.org/fhir/v3/ParticipationType	        2.16.840.1.113883.5.90
    RXNORM	                    http://www.nlm.nih.gov/research/umls/rxnorm	        2.16.840.1.113883.6.88
    ReligiousAffiliation	    http://hl7.org/fhir/v3/ReligiousAffiliation	        2.16.840.1.113883.5.1076
    RoleClass	                http://hl7.org/fhir/v3/RoleClass	                2.16.840.1.113883.5.110
    RoleCode	                http://hl7.org/fhir/v3/RoleCode	                    2.16.840.1.113883.5.111
    RoleStatus	                http://hl7.org/fhir/v3/RoleStatus	                2.16.840.1.113883.5.1068
    SNOMEDCT	                http://snomed.info/sct	                            2.16.840.1.113883.6.96
     */

    public static String getUrlFromOid(String oid) {
        switch (oid) {
            case "2.16.840.1.113883.5.4": return "http://hl7.org/fhir/v3/ActCode";
            case "2.16.840.1.113883.5.1001": return "http://hl7.org/fhir/v3/ActMood";
            case "2.16.840.1.113883.5.7": return "http://hl7.org/fhir/v3/ActPriority";
            case "2.16.840.1.113883.5.8": return "http://hl7.org/fhir/v3/ActReason";
            case "2.16.840.1.113883.5.1002": return "http://hl7.org/fhir/v3/ActRelationshipType";
            case "2.16.840.1.113883.5.14": return "http://hl7.org/fhir/v3/ActStatus";
            case "2.16.840.1.113883.5.1119": return "http://hl7.org/fhir/v3/AddressUse";
            case "2.16.840.1.113883.5.1": return "http://hl7.org/fhir/v3/AdministrativeGender";
            case "2.16.840.1.113883.18.2": return "http://hl7.org/fhir/v2/0001";
            case "2.16.840.1.113883.6.12": return "http://www.ama-assn.org/go/cpt";
            case "2.16.840.1.113883.12.292": return "http://hl7.org/fhir/sid/cvx";
            case "2.16.840.1.113883.5.25": return "http://hl7.org/fhir/v3/Confidentiality";
            case "2.16.840.1.113883.12.112": return "http://hl7.org/fhir/discharge-disposition";
            case "2.16.840.1.113883.5.43": return "http://hl7.org/fhir/v3/EntityNamePartQualifier";
            case "2.16.840.1.113883.5.45": return "http://hl7.org/fhir/v3/EntityNameUse";
            case "2.16.840.1.113883.6.285": return "https://www.cms.gov/Medicare/Coding/MedHCPCSGenInfo/index.html";
            case "2.16.840.1.113883.6.90": return "http://hl7.org/fhir/sid/icd-10";
            case "2.16.840.1.113883.6.4": return "http://www.icd10data.com/icd10pcs";
            case "2.16.840.1.113883.6.103": case "2.16.840.1.113883.6.104": return "http://hl7.org/fhir/sid/icd-9-cm";
            case "2.16.840.1.113883.6.1": return "http://loinc.org";
            case "2.16.840.1.113883.5.60": return "http://hl7.org/fhir/v3/LanguageAbilityMode";
            case "2.16.840.1.113883.5.61": return "http://hl7.org/fhir/v3/LanguageAbilityProficiency";
            case "2.16.840.1.113883.5.63": return "http://hl7.org/fhir/v3/LivingArrangement";
            case "2.16.840.1.113883.5.2": return "http://hl7.org/fhir/v3/MaritalStatus";
            case "2.16.840.1.113883.3.26.1.1": return "http://ncimeta.nci.nih.gov";
            case "2.16.840.1.113883.3.26.1.5": return "http://hl7.org/fhir/ndfrt";
            case "2.16.840.1.113883.6.101": return "http://nucc.org/provider-taxonomy";
            case "2.16.840.1.113883.5.1008": return "http://hl7.org/fhir/v3/NullFlavor";
            case "2.16.840.1.113883.5.83": return "http://hl7.org/fhir/v3/ObservationInterpretation";
            case "2.16.840.1.113883.5.1063": return "http://hl7.org/fhir/v3/ObservationValue";
            case "2.16.840.1.113883.5.88": return "http://hl7.org/fhir/v3/ParticipationFunction";
            case "2.16.840.1.113883.5.1064": return "http://hl7.org/fhir/v3/ParticipationMode";
            case "2.16.840.1.113883.5.90": return "http://hl7.org/fhir/v3/ParticipationType";
            case "2.16.840.1.113883.6.88": return "http://www.nlm.nih.gov/research/umls/rxnorm";
            case "2.16.840.1.113883.5.1076": return "http://hl7.org/fhir/v3/ReligiousAffiliation";
            case "2.16.840.1.113883.5.110": return "http://hl7.org/fhir/v3/RoleClass";
            case "2.16.840.1.113883.5.111": return "http://hl7.org/fhir/v3/RoleCode";
            case "2.16.840.1.113883.5.1068": return "http://hl7.org/fhir/v3/RoleStatus";
            case "2.16.840.1.113883.6.96": return "http://snomed.info/sct";
            case "2.16.840.1.113883.6.301.3": return "http://www.nubc.org";
            case "2.16.840.1.113883.6.50": return "https://www.cms.gov/Medicare/Coding/place-of-service-codes/index.html";
            case "2.16.840.1.113883.6.301.1": return "http://www.nubc.org";
            case "2.16.840.1.113883.6.238": return "http://www.cdc.gov/phin/resources/vocabulary/index.html";
            case "2.16.840.1.113883.3.221.5": return "http://www.phdsc.org/standards/pdfs/SourceofPaymentTypologyVersion6FINALSeptember2015.pdf";
            default: throw new IllegalArgumentException("Unknown CodeSystem oid: " + oid);
        }
    }

    public static  String getUrlFromName(String name) {
        switch (name) {
            case "ActCode": return "http://hl7.org/fhir/v3/ActCode";
            case "ActMood": return "http://hl7.org/fhir/v3/ActMood";
            case "ActPriority": return "http://hl7.org/fhir/v3/ActPriority";
            case "ActReason": return "http://hl7.org/fhir/v3/ActReason";
            case "ActRelationshipType": return "http://hl7.org/fhir/v3/ActRelationshipType";
            case "ActStatus": return "http://hl7.org/fhir/v3/ActStatus";
            case "AddressUse": return "http://hl7.org/fhir/v3/AddressUse";
            case "AdministrativeGender": return "http://hl7.org/fhir/v3/AdministrativeGender";
            case "AdministrativeSex": return "http://hl7.org/fhir/v2/0001";
            case "CPT": return "http://www.ama-assn.org/go/cpt";
            case "CPT-CAT-II": return "http://www.ama-assn.org/go/cpt";
            case "CVX": return "http://hl7.org/fhir/sid/cvx";
            case "Confidentiality": return "http://hl7.org/fhir/v3/Confidentiality";
            case "DischargeDisposition": return "http://hl7.org/fhir/discharge-disposition";
            case "EntityNamePartQualifier": return "http://hl7.org/fhir/v3/EntityNamePartQualifier";
            case "EntityNameUse": return "http://hl7.org/fhir/v3/EntityNameUse";
            case "HCPCS": return "https://www.cms.gov/Medicare/Coding/MedHCPCSGenInfo/index.html";
            case "ICD10CM": return "http://hl7.org/fhir/sid/icd-10";
            case "ICD10PCS": return "http://www.icd10data.com/icd10pcs";
            case "ICD9CM": return "http://hl7.org/fhir/sid/icd-9-cm";
            case "ICD9PCS": return "http://hl7.org/fhir/sid/icd-9-cm";
            case "LOINC": return "http://loinc.org";
            case "LanguageAbilityMode": return "http://hl7.org/fhir/v3/LanguageAbilityMode";
            case "LanguageAbilityProficiency": return "http://hl7.org/fhir/v3/LanguageAbilityProficiency";
            case "LivingArrangement": return "http://hl7.org/fhir/v3/LivingArrangement";
            case "MaritalStatus": return "http://hl7.org/fhir/v3/MaritalStatus";
            case "NCI": return "http://ncimeta.nci.nih.gov";
            case "NDFRT": return "http://hl7.org/fhir/ndfrt";
            case "NUCCPT": return "http://nucc.org/provider-taxonomy";
            case "NullFlavor": return "http://hl7.org/fhir/v3/NullFlavor";
            case "ObservationInterpretation": return "http://hl7.org/fhir/v3/ObservationInterpretation";
            case "ObservationValue": return "http://hl7.org/fhir/v3/ObservationValue";
            case "ParticipationFunction": return "http://hl7.org/fhir/v3/ParticipationFunction";
            case "ParticipationMode": return "http://hl7.org/fhir/v3/ParticipationMode";
            case "ParticipationType": return "http://hl7.org/fhir/v3/ParticipationType";
            case "RXNORM": return "http://www.nlm.nih.gov/research/umls/rxnorm";
            case "ReligiousAffiliation": return "http://hl7.org/fhir/v3/ReligiousAffiliation";
            case "RoleClass": return "http://hl7.org/fhir/v3/RoleClass";
            case "RoleCode": return "http://hl7.org/fhir/v3/RoleCode";
            case "RoleStatus": return "http://hl7.org/fhir/v3/RoleStatus";
            case "SNOMEDCT": return "http://snomed.info/sct";
            case "SNOMED CT US Edition": return "http://snomed.info/sct";
            case "UBREV": return "http://www.nubc.org";
            case "POS": return "https://www.cms.gov/Medicare/Coding/place-of-service-codes/index.html";
            case "UBTOB": return "http://www.nubc.org";
            case "HL7": return " http://terminology.hl7.org/CodeSystem/v2-/0001";
            case "CDCREC": return "http://www.cdc.gov/phin/resources/vocabulary/index.html";
            case "SOP": return "http://www.phdsc.org/standards/pdfs/SourceofPaymentTypologyVersion6FINALSeptember2015.pdf";
            case "MSDRG": return "MSDRG"; //TODO: CodeSystem.Oid = "NA". Get URL
            default: throw new IllegalArgumentException("Unknown CodeSystem name: " + name);
        }
    }
}
