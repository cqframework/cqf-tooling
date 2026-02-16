# ProfileConformance Operation

The purpose of this operation is to determine whether the provided data conforms to a specified set of profiles.

## Arguments:
- -pathtopatientdata | -ptpd (required) - Path to the patient data represented as either a FHIR Bundle resource or as 
flat files within a directory.
- -packageurls | -purls (required) - Urls for the FHIR packages to use for validation as a comma-separated list (required).
- -version | -v (optional) - FHIR version { stu3, r4, r5 }.
    - Default version: r4
- -encoding | -e (optional) - The file format to be used for representing the resulting FHIR resources { json, xml }.
    - Default encoding: json
- -outputpath | -op (optional) - The directory path where the validated FHIR resources should be written.
    - Default output path: src/main/resources/org/opencds/cqf/tooling/validation/output