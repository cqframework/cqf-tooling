# ExtractMatBundle Operation

This operation extracts the resources from an exported MAT FHIR Bundle and writes them to the containing directory in 
the following format:

```
- Parent directory
    |- exported MAT FHIR Bundle Resource
    |- bundles
        |- input
            |- resources (contains the library and measure subdirectories as well as any other resources contained in the MAT Bundle)
                |- library (contains all FHIR Library resources where Library.name = filename + encoding)
                |- measure (contains all FHIR Measure resources where Measure.name = filename + encoding)
            |- cql (contains the extracted CQL content from the Library resources where Library.name = filename + '.cql')
```

## Arguments:
- -pathtobundle | -ptb (required) - Path to the exported MAT FHIR Bundle resource 
- -version | -v (optional) - FHIR version { stu3, r4, r5 }
    - Default version: r4
- -encoding | -e (optional) - The file format to be used for representing the resulting extracted FHIR resources { json, xml }
    - Default encoding: json
- -suppressnarratives | -sn (optional) - Whether or not to suppress Narratives in extracted Measure resources
  - Default value: true
- -outputpath | -op (optional) - The directory path to which the resulting extracted FHIR Library resources should be written
    - Default output path: parent of pathtobundle