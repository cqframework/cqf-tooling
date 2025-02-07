package org.opencds.cqf.tooling.cli;

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
        - The default output directory is src/main/java/resources/org/opencds/cqf/tooling/{package}/output
            - If an output directory path is specified in the params, it MUST NOT have any spaces
        - The default encoding for org.opencds.cqf.qdm.output is JSON
            - XML is also supported

    This project provides tooling for the following use cases:
        - QDM to QiCore mapping generation
            - command: mvn exec:java -Dexec.args="[-QdmToQiCore] (output directory path)"
            - Example: mvn -Dexec.mainClass="org.opencds.cqf.tooling.cli.Main" exec:java -Dexec.args="-QdmToQiCore /Users/daviddieppois/Documents/git/cqf-tooling/tooling-cli/results"
            - This tooling generates HTML pages from http://wiki.hl7.org/index.php?title=Harmonization_of_Health_Quality_Information_models
              for the QiCore implementation guide

        - QiCore QUICK page generation
            - command: mvn exec:java -Dexec.args="[-QiCoreQUICK] [path to QiCore output directory] (output directory path)"
            - /Users/christopherschuler/Documents/workspace/harmoniq/repos/qi-core/output
            - Example: mvn -Dexec.mainClass="org.opencds.cqf.tooling.cli.Main" exec:java -Dexec.args="-QiCoreQUICK /Users/daviddieppois/Documents/git/cqf-tooling/tooling-cli/results"

        - VSAC Excel spreadsheet to FHIR ValueSet resource conversion
            - command: mvn exec:java -Dexec.args="[-VsacXlsxToValueSet] [-pathtospreadsheet | -pts] (-outputpath | -op) (-encoding | -e) (-metasheetnum | -msn) (-metanamerow | -mnr) (-metaoidrow | -mor) (-metastewardrow | -msr) (-codesheetnum | -csn) (-codelistrow | -clr) (-codecol | -cc) (-descriptioncol | -dc) (-systemnamecol | -snc) (-versioncol | -vc) (-systemoidcol | -soc)"
            - Example: mvn exec:java -Dexec.args="-VsacXlsxToValueSet -pts=/Users/christopherschuler/Documents/workspace/exampleValueSet.xlsx"
            - This tooling converts an exported VSAC Excel spreadsheet (.xlsx extension) to a FHIR ValueSet resource and prints to output directory
            - This operation is configurable and can be used with spreadsheets that do not conform to VSAC export format (although we suggest using the -XlsxToValueSet operation as it is much more flexible and configurable).

        - TemplateValuesetGenerator - WAS: XLSX Opioid ValueSet terminology generator
            - command: mvn exec:java -Dexec.args="[-TemplateToValueSetGenerator] [-pathtospreadsheet=<path> | -pts] (-outputpath | -op) (-encoding | -e) (-outputprefix | -opp) (-outputversion | -opv)"
            - Example: mvn -Dexec.mainClass="org.opencds.cqf.tooling.cli.Main" exec:java -Dexec.args="-TemplateToValueSetGenerator -pts=/Users/daviddieppois/Documents/git/cqf-tooling/tooling-cli/src/main/resources/CDC_Opioid_Terminology_Master.xlsx -op=/Users/daviddieppois/Documents/git/cqf-tooling/tooling-cli/r4"
            - This tooling converts the CDC Opioid XLSX spreadsheet from MD Partners to valuesets.
            - Outputversion, if not specified, defaults to R4.

        - Generic Excel spreadsheet to FHIR ValueSet resource conversion
            - command: mvn exec:java -Dexec.args="[-XlsxToValueSet] [-pathtospreadsheet | -pts] (-outputpath | -op) (-encoding | -e)"
            - Example: TODO
            - This tooling converts an Excel spreadsheet (.xlsx extension) to a FHIR ValueSet resource
            - This is highly configurable
                - TODO

        - CQL to FHIR Library conversion
            - command: mvn exec:java -Dexec.args="[-CqlToSTU3Library|-CqlToR4Library] [-pathtolibrarydirectory | -ptld] (-encoding | -e) (-outputpath | -op)"
            - Example: java -Dexec.args="-CqlToR4Library -ptld=/Users/christopherschuler/Src/cqframework/opioid-cds-r4/pages/cql/ -op=/Users/christopherschuler/Src/cqframework/opioid-cds-r4/resources"
            - 2nd Example: mvn -Dexec.mainClass="org.opencds.cqf.tooling.cli.Main" -Dexec.args="-CqlToR4Library -ptld=/Users/daviddieppois/Documents/git/cqf-tooling/tooling-cli/output -op=/Users/daviddieppois/Documents/git/cqf-tooling/tooling-cli/r4"
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

        - Update CQL for an existing Library resource
            - command: mvn exec:java -Dexec.args="[-UpdateCql] [-pathtolibrary | -ptl] [-pathtolibraryresource | -ptlr] (-encoding | -e) (-outputpath | -op)"
            - Example: -UpdateCql -ptl="C:\Users\Bryn\Documents\Src\SS\Pilots\Opioid\opioid-cds\pages\cql\OpioidCDS_STU3_Common.cql" -ptlr="C:\Users\Bryn\Documents\Src\SS\Pilots\Opioid\opioid-cds\resources\library-opioidcds-stu3-common-0-1-0.xml"

        - CQL to FHIR Measure conversion
            - command: mvn exec:java -Dexec.args="[-CqlToMeasure] [path to CQL library] (id) (encoding) (contentType encoding) (-ip=initial population criteria) {-nX=numerator X criteria} {-dX=denominator X criteria} {sX=stratifier X criteria} (org.opencds.cqf.qdm.output directory path)"
            - Example: mvn -Dexec.mainClass="org.opencds.cqf.tooling.cli.Main" -Dexec.args="-CqlToMeasure -ptld=/Users/daviddieppois/Documents/git/cqf-tooling/tooling-cli/output/Antibiotic.cql"
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

        - Refresh Measure(s)
            - command: mvn exec:java -Dexec.args="[-RefreshStu3Measure|RefreshR4Measure] [-ptm| pathToMeasures] [-ptl|pathToLibraries] (-e|encoding) (-o|-output) (-ss|-stamp)"
            - Example: mvn -Dexec.mainClass="org.opencds.cqf.tooling.cli.Main" -Dexec.args="-RefreshStu3Measure -ptm=/Users/daviddieppois/Documents/git/cqf-tooling/tooling/src/test/resources/org/opencds/cqf/tooling/stu3/input/resources/measure -ptl=/Users/daviddieppois/Documents/git/cqf-tooling/tooling/src/test/resources/org/opencds/cqf/tooling/stu3/input/resources/library -o=/Users/daviddieppois/Documents/git/cqf-tooling/tooling-cli/output"

        - Refresh Library
            - command: mvn exec:java -Dexec.args"-RefreshLibrary -ini -fv|fhir-version -lp|libraryPath (-ss|-stamp)
            - Example: mvn exec:java -Dexec.args="-RefreshLibrary -ini=C:\Users\Bryn\Documents\Src\HL7\sample-ig\ig.ini -fv=fhir4 -lp=C:\Users\Bryn\Documents\Src\HL7\sample-ig\input\examples\Library-example.json" -ss=false

        - Bundle Resources
            - mvn exec:java -Dexec.args="[-BundleResources] [-pathtodirectory | -ptd] (-outputpath | -op) (-version | -v) (-encoding | -e) (-bundleid | -bid) "
            - Example: mvn exec:java -Dexec.args="-BundleResources -ptd=/Users/adam/Src/cqframework/opioid-cds-r4/quickstartcontent -op=/Users/adam/Src/cqframework/opioid-cds-r4/quickstartcontentbundle -v=r4"
            - This Operation consolidates all resources from files in the 'pathtodirectory' directory into a single FHIR Bundle with
            - an ID that is the value specified in the 'bunldeid' argument and outputs that generated bundle in file format
            - of the type specified by the 'encoding' argument to the 'outputpath' directory.
            - Arguments:
            -   [-pathtodirectory | -ptd] - Path to the directory containing the resource files to be consolidated into the new bundle
            -   (-outputpath | -op) - The directory path to which the generated Bundle file should be written
            -       Default output path: src/main/resources/org/opencds/cqf/tooling/bundle/output
            -   (-version | -v) - FHIR version { dstu2, stu3, r4 }
            -       Default version: stu3
            -   (-encoding | -e) - The file format to be used for representing the resulting Bundle { json, xml }
            -       Default Value: json
            -   (-bundleid | -bid) - A valid FHIR ID to be used as the ID for the resulting FHIR Bundle. The Publisher
            -       validation for Bundle requires a Bundle to have an ID. If no ID is provided, the output Bundle
            -       will not have an ID value.


        - Bundle consolidation
            - mvn exec:java -Dexec.args="[-BundlesToBundle] [input directory path] (output encoding) (output file name) (org.opencds.cqf.qdm.output directory path)"
            - Example: mvn exec:java -Dexec.args="-BundlesToBundle /Users/christopherschuler/Documents/workspace/Bundles xml master-bundle /Users/christopherschuler/Documents/workspace/master-bundles"
            - This tooling consolidates several FHIR Bundles into a single Bundle
            - Accepts Bundles with .json or .xml extensions

        - Bundle decomposition
            - mvn exec:java -Dexec.args="[-BundleToResources] [Bundle file path] (output encoding)
            - Example: mvn -Dexec.mainClass="org.opencds.cqf.tooling.cli.Main" exec:java -Dexec.args="-BundleToResources -p=/Users/daviddieppois/Documents/git/cqf-tooling/tooling-cli/src/main/resources/libraryevaluationtest-bundle.json -e=json -op=/Users/daviddieppois/Documents/git/cqf-tooling/tooling-cli/output"
            - This tooling decomposes a Bundle entry into separate resource files
            - Accepts Bundles with .json or .xml extensions

        - Bundle transaction - Converts a collection bundle to a transaction bundle
            - mvn exec:java -Dexec.args="-MakeTransaction [Bundle file path] (output encoding)
            - Example: mvn -Dexec.mainClass="org.opencds.cqf.tooling.cli.Main" exec:java -Dexec.args="-MakeTransaction -p=/Users/daviddieppois/Documents/git/cqf-tooling/tooling/src/main/resources/libraryevaluationtest-bundle.json -e=json -op=/Users/daviddieppois/Documents/git/cqf-tooling/tooling-cli/bundleTransaction"
            - This tooling converts a collection bundle to a transaction bundle
            - Accepts bundles with .json or .xml extensions

        - MAT Bundle extraction
            - mvn exec:java -Dexec.args="[-ExtractMatBundle] [Bundle file path] (-v) (-dir)
            - Example: mvn exec:java -Dexec.args="-ExtractMatBundle /Users/mholck/Development/ecqm-content-r4/bundles/mat/EXM124/EXM124.json -v=r4"
            - This tooling extracts out the resources and CQL from a MAT export bundle and puts them in the appropriate directories
            - Accepts Bundles with .json or .xml extensions
            - version = FHIR version { stu3, r4 }
                Default version: r4
            - dir = Directory indicator. To process the input location as a directory of files, the input should point to a valid directory and the -dir flag should be present in the arguments list.

        - Generate StructureDefinitions from ModelInfo
            - command: mvn exec:java -Dexec.args="[-GenerateSDs] [path to modelinfo xml] (-outputpath | -op) (-encoding | -e)"
            - TODO

        - Generate ModelInfo from StructureDefinitions
             - See documentation in StructureDefinitionToModelInfo for argument documentation and examples

        - Extension builder
            - TODO

        - JsonSchema Generator
            - This tooling converts minimized FHIR profiles into JsonSchemas for form rendering (STU3 and DSTU2 support)
            - mvn exec:java -Dexec.args="-JsonSchemaGenerator"

        - Accelerator Kit Processor
            - This tooling converts a WHO accelerator kit data dictionary to a set of profiles, questionnaires, plan definitions, and libraries
            - mvn exec:java -Dexec.args="-ProcessAcceleratorKit [-pathtospreadsheet | -pts] [-dataelementpages | -dep] (-outputpath | -op) (-encoding | -e)"
            - Example: mvn exec:java -Dexec.args="-ProcessAcceleratorKit -pts=ANC-Primary-Data-Dictionary.xlsx -dep=""ANC Reg,Quick Check,Profile,S&F,PE,Tests,C&T"""

        - Decision Table Processor
            - This tooling converts a WHO accelerator kit decision table to a set of PlanDefinition resources
            - mvn exec:java -Dexec.args="-ProcessDecisionTables [-pathtospreadsheet | -pts] [-decisiontablepages | -dtp] (-outputpath | -op) (-encoding | -e)"
            - Example: mvn exec:java -Dexec.args="-ProcessDecisionTables -pts=ANC-Decision-Logic.xlsx -dtp=""ANC.DT.01 Danger signs,ANC.DT.02 Check symptoms"""

        - Jurisdiction List Converter
            - This tooling converts an RCKMS list of jurisdictions from an Excel file to a CodeSystem
            - mvn exec:java -Dexec.args="-JurisdictionListConverter [-pathtosource | -pts] (outputpath | -op)"
            - Example: mvn exec:java -Dexec.args="-JurisdictionsXlsxToCodeSystem -pts=C:/Users/me/aphl-ig/input/vocabulary/codesystem/sources/rckms-jurisdictions.xlsx -op=C:/Users/me/aphl-ig/input/vocabulary/codesystem"

        - Measure Test
            - Executes a particular Measure Test case
            -
            - Example: mvn exec: java -Dexec.args="-ExecuteMeasureTest -test-path /home/jp/repos/connectathon/fhir401/input/tests/EXM104-9.1.000/tests-numer-EXM104-bundle.json -content-path /home/jp/repos/connectathon/fhir401/input/bundles/EXM104-9.1.000-bundle.json -fhir-server http://192.168.2.194:8082/cqf-ruler-r4/fhir"

        - SpreadsheetToCQL
            - command: mvn exec:java -Dexec.args="[-SpreadsheetToCQL] [-pathtospreadsheet | -pts] (-outputpath | -op)"
            - This tooling converts an Excel spreadsheet (.xlsx extension) to a CQL expression with the data from the spreadsheet rows

        - GenerateCQLFromDroolOperation
            - command: mvn exec: java -Dexec.args="-GenerateCQLFromDrool -ifp=../CQLGenerationDocs/NonGeneratedDocs/default.json -op=../CQLGenerationDocs/GeneratedDocs/elm -fv=4.0.0 -t=CONDITION"
            - this tooling generates cql elm libraries given a Data Input Source File

        - VmrToFhirTransformer
            - command: mvn exec: java -Dexec.args="-VmrToFhir -ifp=./src/test/resources/org/opencds/cqf/tooling/operation/VmrToFhir -op=./src/test/resources/org/opencds/cqf/tooling/operation/VmrToFhir/vMROutput.xml -e=xml"
            - this tooling transforms vMR data to FHIR data

        - EnsureExecutableValueSet
            - command: mvn exec: java -Dexec.args="-EnsureExecutableValueSet [-valuesetpath | -vsp] (-outputpath | -op) (-declarecpg | -cpg) (-force | -f)"
            - This tooling generates an expansion if one is not present (and the compose consists only of includes without filters)
            - The -cpg flag indicates whether to mark the value set as executable with CPG profile indicators
            - The -force flag indicates that even if the value set has an expansion, this should recompute it

        - EnsureComputableValueSet
            - command: mvn exec: java -Dexec.args="-EnsureComputableValueSet [-valuesetpath | -vsp] (-outputpath | -op) (-declarecpg | -cpg) (-force | -f) (-skipversion | -sv)"
            - This tooling infers a compose if one is not present (and there is an expansion)
            - The -cpg flag indicates whether to mark the value set as computable with CPG profile indicators
            - The -force flag indicates that even if the value set has a compose, this should reinfer it
            - The -skipversion flag indicates that code system versions that are present in the expansion should not be expressed in the inferred compose

        - PostmanCollection
            - command: mvn exec: java -Dexec.args="-PostmanCollection (-pathtobundledir | -ptbd) (-outputpath | -op) (-version | -v) [-host] [-path] [-protocol] [-name]"
            - This tooling generates a postman collection based on the measure transaction bundle
            - The operation expects -ptbd is a directory containing one or more directories each of them contains measure output bundle
            - The -op is the output directory for collection
            - The -v expects values like r4 or dstu3
            - The -host is the FHIR Restful server base ex, "-host=cqm-sandbox.alphora.com"
            - The -path is the server path after base ex, "-path=cqf-ruler-r4/fhir/"
            - The -protocol can be http or https
            - The -name is the name for the postman collection

        - TransformErsd
            - command: mvn exec: java -Dexec.args="-TransformErsd (-pathtobundle | -ptb) (-outputpath | -op) [-pathtoplandefinition | -ptpd] [-encoding | -e]"
            - This Operation transforms a US eCR eRSD version 1 bundle to eRSD version 2
            - The operation expects -ptb is a path to a file containing the source eRSD v1 bundle
            - The -op is the output directory for output v2
            -   The default output path is:
                    <location of the CQF Tooling jar being invoked> + "src/main/resources/org/opencds/cqf/tooling/casereporting/output"
            - The -ptpd is the path to a file that contains the eRSD-v2-compliant PlanDefinition that should be used to replace the
                plan definition in the input eRSD v1 bundle. If the argument is not specified, the PlanDefinition in the input bundle
                will be preserved.
            - The -e is the desired output encoding(s) for the output bundle. The supported output encodings are: { "json", "xml" }.
                The "encoding" argument can be specified multiple times and the transformer will output a bundle for each encoding.
                So if you want both json and xml bundles, you would specify both -e=json and -e=xml. If no encoding argument
                is supplied, the transformer will assume "json" as the default and output a single JSON-encoded bundle.

        - CaseReportingTESTESPackageGenerate
            - command: mvn exec: java -Dexec.args="-CaseReporting.TES.TESPackageGenerate (-pathtoinputbundle | -ptib) (-outputpath | -op) [-encoding | -e]"
            - This Operation takes an input Bundle, parses out the resources it contains and for each Reporting
            - Specification Grouper ValueSet a corresponding Condition Grouper ValueSet is created and output.
            - The operation supports the following parameters:
            - The -v is the version value to be assigned to the version element of the generated artifacts
            - The -rl is a string value that will be used as value for the release label extension on generated artifacts
            - The -op is the output directory for output
            -   The default output path is:
                    <location of the CQF Tooling jar being invoked> + "src/main/resources/org/opencds/cqf/tooling/casereporting/output"
            - The -ptib is a path to a file containing the source bundle
            - The -ptcgw is the path to the workbook file that contains the mappings from reporting specification groupers to condition groupers
            - The -ptccvs is the path to the RCKMS Condition Code value set. It is use to evaluate which condition codes are used and which are not to report, in output, those difference
            - The -e is the desired output encoding(s) for the output bundle. The supported output encodings are: { "json", "xml" }.
                The "encoding" argument can be specified multiple times and the transformer will output a bundle for each encoding.
                So if you want both json and xml bundles, you would specify both -e=json and -e=xml. If no encoding argument
                is supplied, the transformer will assume "json" as the default and output a single JSON-encoded bundle.
            - The -wcg flag is a boolean that indicates whether or not the generated condition grouper valuesets should be written
            - to their own dedicated output file, in addition to being included in the generated bundle file.
        */

//import org.opencds.cqf.tooling.exception.InvalidOperationArgs;
//import org.opencds.cqf.tooling.exception.InvalidOperationInitialization;
//import org.opencds.cqf.tooling.exception.OperationNotFound;
//import org.opencds.cqf.tooling.operations.ExecutableOperation;
//import org.opencds.cqf.tooling.operations.Operation;
//import org.reflections.Reflections;

import org.opencds.cqf.tooling.common.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import java.lang.reflect.InvocationTargetException;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Set;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

// TODO: Uncomment this block and imports once operation refactor is ready
//    private static Map<String, Class<?>> operationClassMap;
//
//    public static void main(String[] args) {
//        if (args == null || args.length == 0) {
//            logger.error("cqf-tooling version: {}", Main.class.getPackage().getImplementationVersion());
//            throw new OperationNotFound(
//                    "Requests must include which operation to run as a command line argument. See docs for examples on how to use this project.");
//        }
//
//        // NOTE: we may want to use the Spring Context Library to find the annotated classes
//        if (operationClassMap == null) {
//            operationClassMap = new HashMap<>();
//            Reflections reflections = newReflections("org.opencds.cqf.tooling.operations");
//            Set<Class<?>> operationClasses = reflections
//                    .getTypesAnnotatedWith(Operation.class);
//            operationClasses.forEach(clazz -> operationClassMap.put(clazz.getAnnotation(Operation.class).name(), clazz));
//        }
//
//        String operation = args[0];
//        if (!operation.startsWith("-")) {
//            throw new InvalidOperationArgs(
//                    "Invalid operation syntax: " + operation + ". Operations must be declared with a \"-\" prefix");
//        }
//
//        try {
//            ExecutableOperation executableOperation = OperationFactory.createOperation(
//                    operation, operationClassMap.get(operation.substring(1)), args);
//            if (executableOperation != null) {
//                executableOperation.execute();
//            }
//        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
//            throw new InvalidOperationInitialization(e.getMessage(), e);
//        }
//    }

    public static void main(String[] args) {
        //ensure any and all executors are shutdown cleanly when app is shutdown:
        Runtime.getRuntime().addShutdownHook(new Thread(ThreadUtils::shutdownRunningExecutors));

        if (args.length == 0) {
            System.err.println("cqf-tooling version: " + Main.class.getPackage().getImplementationVersion());
            System.err.println("Requests must include which operation to run as a command line argument. See docs for examples on how to use this project.");
            return;
        }

        String operation = args[0];
        if (!operation.startsWith("-")) {
            throw new IllegalArgumentException("Invalid operation: " + operation);
        }

        OperationFactory.createOperation(operation.substring(1)).execute(args);
    }
}
