# LoincHierarchy Operation

The purpose of this operation is to generate a simple FHIR ValueSet resource given a query, username, and password using 
the LOINC Hierarchy API (https://loinc.org/tree/).

The returned ValueSet resource will be very simple; including either the rules text (query) as an extension or compose 
filters and the resulting API call and post-processed codes in the ValueSet.compose element.

## Arguments:
- -query | -q (required) - The expression that provides an alternative definition of the content of the value set in some form that is not computable - e.g. instructions that could only be followed by a human.
- -username | -user (required) - The LOINC account username.
- -password | -pass (required) - The LOINC account password.
- -version | -v (optional) - FHIR version { stu3, r4, r5 }
    - Default version: r4
- -encoding | -e (optional) - The file format to be used for representing the resulting FHIR ValueSet resource { json, xml }.
    - Default encoding: json
- -outputpath | -op (optional) - The directory path to which the resulting FHIR ValueSet resource should be written.
    - Default output path: src/main/resources/org/opencds/cqf/tooling/terminology/output