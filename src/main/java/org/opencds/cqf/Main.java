package org.opencds.cqf;

/*

    The purpose of this project is to provide tooling for several CDS use cases including Implementation Guide and Measure authoring. See below for
    a more comprehensive list of use cases.
    This project is currently configured for ONLY STU3 FHIR.

    Legend:
        - () = optional param
        - [] = required param
        - {} = 0..* params
        - | = OR (typically used for shorthand args)

    Note:
        - The default output directory is src/main/java/resources/org/opencds/cqf/{package}/output
            - If an output directory path is specified in the params, it MUST NOT have any spaces
        - The default encoding for org.opencds.cqf.qdm.output is JSON
            - XML is also supported

    This project provides tooling for the following use cases:
        - QDM to QiCore mapping generation
            - command: mvn exec:java -Dexec.args="[-QdmToQiCore] (output directory path)"
            - Example: mvn exec:java -Dexec.args="-QdmToQiCore /Users/christopherschuler/Documents/workspace/QdmToQiCoreFiles"
            - This tooling generates HTML pages from http://wiki.hl7.org/index.php?title=Harmonization_of_Health_Quality_Information_models
              for the QiCore implementation guide

        - QiCore QUICK page generation
            - command: mvn exec:java -Dexec.args="[-QiCoreQUICK] [path to QiCore output directory] (output directory path)"
            - /Users/christopherschuler/Documents/workspace/harmoniq/repos/qi-core/output

        - VSAC Excel spreadsheet to FHIR ValueSet resource conversion
            - command: mvn exec:java -Dexec.args="[-VsacXlsxToValueSet] [-pathtospreadsheet | -pts] (-outputpath | -op) (-encoding | -e) (-metasheetnum | -msn) (-metanamerow | -mnr) (-metaoidrow | -mor) (-metastewardrow | -msr) (-codesheetnum | -csn) (-codelistrow | -clr) (-codecol | -cc) (-descriptioncol | -dc) (-systemnamecol | -snc) (-versioncol | -vc) (-systemoidcol | -soc)"
            - Example: mvn exec:java -Dexec.args="-VsacXlsxToValueSet -pts=/Users/christopherschuler/Documents/workspace/exampleValueSet.xlsx"
            - This tooling converts an exported VSAC Excel spreadsheet (.xlsx extension) to a FHIR ValueSet resource and prints to output directory
            - This operation is configurable and can be used with spreadsheets that do not conform to VSAC export format (although we suggest using the -XlsxToValueSet operation as it is much more flexible and configurable).

        - XLSX Opioid ValueSet terminology generator
            - command: mvn exec:java -Dexec.args="[-OpioidXlsxToValueSet] [-pathtospreadsheet=<path> | -pts] (-outputpath | -op) (-encoding | -e) (-outputprefix | -opp) (-outputversion | -opv)"
            - Example: mvn exec:java -Dexec.args="-OpioidXlsxToValueSet -pts=C:\Users\Carter\Desktop\Opioid.xlsx -opv=stu3"
            - This tooling converts the CDC Opioid XLSX spreadsheet to valuesets.
            - Outputversion, if not specified, defaults to R4.

        - Generic Excel spreadsheet to FHIR ValueSet resource conversion
            - command: mvn exec:java -Dexec.args="[-XlsxToValueSet] [-pathtospreadsheet | -pts] (-outputpath | -op) (-encoding | -e)"
            - Example: TODO
            - This tooling converts an Excel spreadsheet (.xlsx extension) to a FHIR ValueSet resource
            - This is highly configurable
                - TODO

        - CQL to FHIR Library conversion
            - command: mvn exec:java -Dexec.args="[-CqlToLibrary] [-pathtolibrarydirectory | -ptld] (-encoding | -e) (-outputpath | -op)"
            - Example: TODO
            - This tooling converts CQL libraries to FHIR Library resources
            - The following elements will be populated in the FHIR Library resource:
                - id (auto-generated)
                - version (if declared in the CQL)
                - name (if declared in the CQL)
                - status (draft by default)
                - type (logic-library by default)
                - dataRequirement
                - content.contentType (both application/elm+xml and text/cql by default)
                - content.data (base64 encoded String)

        - CQL to FHIR Measure conversion
            - command: mvn exec:java -Dexec.args="[-CqlToMeasure] [path to CQL library] (id) (encoding) (contentType encoding) (-ip=initial population criteria) {-nX=numerator X criteria} {-dX=denominator X criteria} {sX=stratifier X criteria} (org.opencds.cqf.qdm.output directory path)"
            - NOTE: The 'X' for the numerator, denominator and stratifier params MUST be an Integer value
                - If the numerator, denominator and stratifier params are not provided, the following criteria naming conventions MUST be followed:
                    - initial population: "Initial Population"
                    - numerator: "Numerator X", where X is an Integer (if there is only one numerator expression the X may be omitted)
                    - denominator: "Denominator X", where X is an Integer (if there is only one denominator expression the X may be omitted)
                    - stratifier: "Stratifier X", where X is an Integer (if there is only one stratifier expression the X may be omitted)
            - Example: TODO
            - This tooling converts a CQL library to a FHIR Measure resource
                - Additionally, a FHIR Library resource will also be generated
                - Output will be a transaction Bundle that includes the Measure and Library resources
            - The following elements will be populated in the FHIR Measure resource:
                - id (auto-generated by default)
                - status (draft by default)
                - group.identifier
                - group.population.code
                - group.population.criteria
            - See the documentation for CQL to FHIR Library conversion to see which Library elements will be populated

        - Bundle consolidation
            - mvn exec:java -Dexec.args="[-BundlesToBundle] [input directory path] (output encoding) (output file name) (org.opencds.cqf.qdm.output directory path)"
            - Example: mvn exec:java -Dexec.args="-BundlesToBundle /Users/christopherschuler/Documents/workspace/Bundles xml master-bundle /Users/christopherschuler/Documents/workspace/master-bundles"
            - This tooling consolidates several FHIR Bundles into a single Bundle
            - Accepts Bundles with .json or .xml extensions

        - Bundle decomposition
            - mvn exec:java -Dexec.args="[-BundleToResources] [Bundle file path] (output encoding)
            - Example: mvn exec:java -Dexec.args="-BundleToResources /Users/christopherschuler/Documents/workspace/Bundles/bundle-1.json json /Users/christopherschuler/Documents/workspace/resources"
            - This tooling decomposes a Bundle entry into separate resource files
            - Accepts Bundles with .json or .xml extensions

        - Generate StructureDefinitions from ModelInfo
            - command: mvn exec:java -Dexec.args="[-GenerateSDs] [path to modelinfo xml] (-outputpath | -op) (-encoding | -e)"
            - TODO

        - Generate ModelInfo from StructureDefinitions
             - command: mvn exec:java -Dexec.args="[-GenerateMIs] [path to structuredefinitions xml]  (output directory path)"
             - TODO

        - Extension builder
            - TODO

        - JsonSchema Generator
            - This tooling converts minimized FHIR profiles into JsonSchemas for form rendering (STU3 and DSTU2 support)
            - mvn exec:java -Dexec.args="-JsonSchemaGenerator"

        - Accelerator Kit Processor
            - This tooling converts a WHO accelerator kit data dictionary to a set of profiles, questionnaires, plan definitions, and libraries
            - mvn exec:java -Dexec.args="-ProcessAcceleratorKit [-pathtospreadsheet | -pts] [-dataelementpages | -dep] (-outputpath | -op) (-encoding | -e)"
            - Example: mvn exec:java -Dexec.args="-ProcessAcceleratorKit -pts=ANC-Primary-Data-Dictionary.xlsx -dep=""ANC Reg,Quick Check,Profile,S&F,PE,Tests,C&T"""
*/

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Error: Requests must include which operation to run as a command line argument. See docs for examples on how to use this project.");
        }

        String operation = args[0];
        if (!operation.startsWith("-")) {
            throw new IllegalArgumentException("Invalid operation: " + operation);
        }

        OperationFactory.createOperation(operation.substring(1)).execute(args);
    }
}
