package org.opencds.cqf.terminology;

public class CodeSystemLookupDictionary {

    /*

    Using HL7's Terminology 1.0.0 Publication IG: https://terminology.hl7.org/codesystems.html

    CodeSystem Name                 |  OID                                  | URL
    ------------------------------------------------------------------------------------------------------------------------------------------------------------------------

        ActCode                     2.16.840.1.113883.5.4           ->      https://terminology.hl7.org/1.0.0/CodeSystem-v3-ActCode.html
        ActMood                     2.16.840.1.113883.5.1001        ->      https://terminology.hl7.org/1.0.0/CodeSystem-v3-ActMood.html
        ActPriority                 2.16.840.1.113883.5.7           ->      https://terminology.hl7.org/1.0.0/CodeSystem-v3-ActPriority.html
        ActReason                   2.16.840.1.113883.5.8           ->      https://terminology.hl7.org/1.0.0/CodeSystem-v3-ActReason.html
        ActRelationshipType         2.16.840.1.113883.5.1002        ->      https://terminology.hl7.org/1.0.0/CodeSystem-v3-ActRelationshipType.html
        ActStatus                   2.16.840.1.113883.5.14          ->      https://terminology.hl7.org/1.0.0/CodeSystem-v3-ActStatus.html
        AddressUse                  2.16.840.1.113883.5.1119        ->      https://terminology.hl7.org/1.0.0/CodeSystem-v3-AddressUse.html
        AdministrativeGender        2.16.840.1.113883.5.1           ->      https://terminology.hl7.org/1.0.0/CodeSystem-v3-AdministrativeGender.html
        AdministrativeSex           2.16.840.1.113883.18.2          ->      https://terminology.hl7.org/1.0.0/CodeSystem-v2-0001.html
        CPT                         2.16.840.1.113883.6.12          ->      https://terminology.hl7.org/CodeSystem-v3-cpt-4.html
        CVX                         2.16.840.1.113883.12.292        ->      https://terminology.hl7.org/CodeSystem-v2-0292.html
        Confidentiality             2.16.840.1.113883.5.25          ->      https://terminology.hl7.org/CodeSystem-v3-Confidentiality.html
        DischargeDisposition        2.16.840.1.113883.12.112        ->      urn:oid:2.16.840.1.113883.12.112
        DischargeDisposition        2.16.840.1.113883.4.642.1.1093  ->      https://terminology.hl7.org/CodeSystem-discharge-disposition.html
        EntityNamePartQualifier     2.16.840.1.113883.5.43          ->      https://terminology.hl7.org/CodeSystem-v3-EntityNamePartQualifier.html
        EntityNameUse               2.16.840.1.113883.5.45          ->      https://terminology.hl7.org/CodeSystem-v3-EntityNameUse.html
        HCPCS                       2.16.840.1.113883.6.14          ->      https://terminology.hl7.org/CodeSystem-HCPCS.html
        HCPCS Level I: CPT          2.16.840.1.113883.6.14          ->      https://terminology.hl7.org/CodeSystem-HCPCS.html
        HCPCS Level II              2.16.840.1.113883.6.285         ->      urn:oid:2.16.840.1.113883.6.285
        ICD10                       2.16.840.1.113883.6.3           ->      https://terminology.hl7.org/CodeSystem-icd10.html
        ICD10PCS                    2.16.840.1.113883.6.4           ->      https://terminology.hl7.org/CodeSystem-icd10PCS.html
        ICS10CM                     2.16.840.1.113883.6.90          ->      https://terminology.hl7.org/CodeSystem-icd10CM.html
        ICD9                        2.16.840.1.113883.6.42          ->      https://terminology.hl7.org/CodeSystem-icd9.html
        ICD9CM                      2.16.840.1.113883.6.2           ->      https://terminology.hl7.org/CodeSystem-icd9cm.html
        LOINC                       2.16.840.1.113883.6.1           ->      https://terminology.hl7.org/NamingSystem-v3-loinc.html
        LanguageAbilityMode         2.16.840.1.113883.5.60          ->      https://terminology.hl7.org/CodeSystem-v3-LanguageAbilityMode.html
        LanguageAbilityProficiency  2.16.840.1.113883.5.61          ->      https://terminology.hl7.org/CodeSystem-v3-LanguageAbilityProficiency.html
        LivingArrangement           2.16.840.1.113883.5.63          ->      https://terminology.hl7.org/CodeSystem-v3-LivingArrangement.html
        MaritalStatus               2.16.840.1.113883.5.2           ->      https://terminology.hl7.org/CodeSystem-v3-MaritalStatus.html
        NCI                         2.16.840.1.113883.3.26.1.1      ->      https://terminology.hl7.org/CodeSystem-v3-nciThesaurus.html
        NDFRT                       2.16.840.1.113883.3.26.1.5      ->      https://terminology.hl7.org/CodeSystem-nciVersionOfNDF-RT.html
        NUCCPT                      2.16.840.1.113883.6.101         ->      https://terminology.hl7.org/CodeSystem-v3-nuccProviderCodes.html
        NullFlavor                  2.16.840.1.113883.5.1008        ->      https://terminology.hl7.org/CodeSystem-v3-NullFlavor.html
        ObservationInterpretation   2.16.840.1.113883.5.83          ->      https://terminology.hl7.org/CodeSystem-v3-ObservationInterpretation.html
        ObservationValue            2.16.840.1.113883.5.1063        ->      https://terminology.hl7.org/CodeSystem-v3-ObservationValue.html
        ParticipationFunction       2.16.840.1.113883.5.88          ->      https://terminology.hl7.org/CodeSystem-v3-ParticipationFunction.html
        ParticipationMode           2.16.840.1.113883.5.1064        ->      https://terminology.hl7.org/CodeSystem-v3-ParticipationMode.html
        ParticipationType           2.16.840.1.113883.5.90          ->      https://terminology.hl7.org/CodeSystem-v3-ParticipationType.html
        RXNORM                      2.16.840.1.113883.6.88          ->      https://terminology.hl7.org/NamingSystem-v3-rxNorm.html
        ReligiousAffiliation        2.16.840.1.113883.5.1076        ->      https://terminology.hl7.org/CodeSystem-v3-ReligiousAffiliation.html
        RoleClass                   2.16.840.1.113883.5.110         ->      https://terminology.hl7.org/CodeSystem-v3-RoleClass.html
        RoleCode                    2.16.840.1.113883.5.111         ->      https://terminology.hl7.org/CodeSystem-v3-RoleCode.html
        RoleStatus                  2.16.840.1.113883.5.1068        ->      https://terminology.hl7.org/CodeSystem-v3-RoleStatus.html
        SNOMEDCT                    2.16.840.1.113883.6.96          ->      https://terminology.hl7.org/NamingSystem-v3-snomed-CT.html
        UBREV                       2.16.840.1.113883.6.21          ->      https://terminology.hl7.org/CodeSystem-nubc-UB92.html
        UBTOB                       2.16.840.1.113883.6.21          ->      https://terminology.hl7.org/CodeSystem-nubc-UB92.html
        POS                         2.16.840.1.113883.6.50          ->      https://terminology.hl7.org/CodeSystem-POS.html
        CDCREC                      2.16.840.1.113883.6.238         ->      https://terminology.hl7.org/CodeSystem-PHRaceAndEthnicityCDC.html
        CDT                         2.16.840.1.113883.6.13          ->      https://terminology.hl7.org/CodeSystem-CD2.html
        mediaType                   2.16.840.1.113883.5.79          ->      https://terminology.hl7.org/CodeSystem-v3-mediatypes.html
        SOP                         2.16.840.1.113883.3.221.5       ->      urn:oid:2.16.840.1.113883.3.221.5
        UCUM                        1.3.6.1.4.1.12009.10.3.1        ->      urn:oid:1.3.6.1.4.1.12009.10.3.1
        UCUM                        2.16.840.1.113883.6.8           ->      https://terminology.hl7.org/NamingSystem-v3-ucum.html
        UMLS                        2.16.840.1.113883.6.86          ->      https://terminology.hl7.org/CodeSystem-umls.html

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
            case "2.16.840.1.113883.6.285": return "urn:oid:2.16.840.1.113883.6.285";
            case "2.16.840.1.113883.6.3": return "http://terminology.hl7.org/CodeSystem/icd10";
            case "2.16.840.1.113883.6.4": return "http://www.cms.gov/Medicare/Coding/ICD10";
            case "2.16.840.1.113883.6.90": return "http://hl7.org/fhir/sid/icd-10-cm";
            case "2.16.840.1.113883.6.42": return "http://terminology.hl7.org/CodeSystem/icd9";
            case "2.16.840.1.113883.6.2": return "http://terminology.hl7.org/CodeSystem/icd9cm";
            case "2.16.840.1.113883.6.1": return "http://loinc.org";
            case "2.16.840.1.113883.5.60": return "http://terminology.hl7.org/CodeSystem/v3-LanguageAbilityMode";
            case "2.16.840.1.113883.5.61": return "http://terminology.hl7.org/CodeSystem/v3-LanguageAbilityProficiency";
            case "2.16.840.1.113883.5.63": return "http://terminology.hl7.org/CodeSystem/v3-LivingArrangement";
            case "2.16.840.1.113883.5.2": return "http://terminology.hl7.org/CodeSystem/v3-MaritalStatus";
            case "2.16.840.1.113883.3.26.1.1": return "http://ncithesaurus-stage.nci.nih.gov";
            case "2.16.840.1.113883.3.26.1.5": return "http://terminology.hl7.org/CodeSystem/nciVersionOfNDF-RT";
            case "2.16.840.1.113883.6.101": return "http://nucc.org/provider-taxonomy";
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
            case "2.16.840.1.113883.6.238": return "http://terminology.hl7.org/CodeSystem/PHRaceAndEthnicityCDC";
            case "2.16.840.1.113883.6.13": return "http://terminology.hl7.org/CodeSystem/CD2";
            case "2.16.840.1.113883.5.79": return "http://terminology.hl7.org/CodeSystem/v3-mediatypes";
            case "2.16.840.1.113883.3.221.5": return "urn:oid:2.16.840.1.113883.3.221.5";
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
            case "HCPCS Level II": return "urn:oid:2.16.840.1.113883.6.285";
            case "ICD10": return "http://terminology.hl7.org/CodeSystem/icd10";
            case "ICD10CM": return "http://hl7.org/fhir/sid/icd-10-cm";
            case "ICD10PCS": return "http://www.cms.gov/Medicare/Coding/ICD10";
            case "ICD9": return "http://terminology.hl7.org/CodeSystem/icd9";
            case "ICD9CM": return "http://terminology.hl7.org/CodeSystem/icd9cm";
            case "LOINC": return "http://loinc.org";
            case "LanguageAbilityMode": return "http://terminology.hl7.org/CodeSystem/v3-LanguageAbilityMode";
            case "LanguageAbilityProficiency": return "http://terminology.hl7.org/CodeSystem/v3-LanguageAbilityProficiency";
            case "LivingArrangement": return "http://terminology.hl7.org/CodeSystem/v3-LivingArrangement";
            case "MaritalStatus": return "http://terminology.hl7.org/CodeSystem/v3-MaritalStatus";
            case "NCI": return "http://ncithesaurus-stage.nci.nih.gov";
            case "NDFRT": return "http://terminology.hl7.org/CodeSystem/nciVersionOfNDF-RT";
            case "NUCCPT": return "http://nucc.org/provider-taxonomy";
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
            case "SNOMED CT US Edition": return "http://snomed.info/sct";
            case "UBREV": return "http://terminology.hl7.org/CodeSystem/nubc-UB92";
            case "UBTOB": return "http://terminology.hl7.org/CodeSystem/nubc-UB92";
            case "POS": return "http://terminology.hl7.org/CodeSystem/POS";
            case "CDCREC": return "http://terminology.hl7.org/CodeSystem/PHRaceAndEthnicityCDC";
            case "Modifier": return "http://www.ama-assn.org/go/cpt";
            case "CDT": return "http://terminology.hl7.org/CodeSystem/CD2";
            case "mediaType": return "http://terminology.hl7.org/CodeSystem/v3-mediatypes";
            case "SOP":  return "urn:oid:2.16.840.1.113883.3.221.5";
            case "UCUM": return "http://unitsofmeasure.org";
            case "UMLS": return "http://terminology.hl7.org/CodeSystem/umls";
            default: throw new IllegalArgumentException("Unknown CodeSystem name: " + name);
        }
    }
}
