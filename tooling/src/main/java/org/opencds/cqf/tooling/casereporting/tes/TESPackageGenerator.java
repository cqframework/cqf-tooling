package org.opencds.cqf.tooling.casereporting.tes;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.parser.XmlParser;
import ca.uhn.fhir.util.BundleBuilder;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.terminology.SpreadsheetHelper;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.opencds.cqf.tooling.operations.bundle.BundleToResources.bundleToResources;

public class TESPackageGenerator extends Operation {
    private static final Logger logger = LoggerFactory.getLogger(TESPackageGenerator.class);
    private static final String PUBLISHER = "Association of Public Health Laboratories (APHL)";
    private static final String CANONICALBASE = "http://tes.aimsplatform.org/fhir";
    private static final String MANIFESTID = "tes-content-library";
    private static final String MANIFESTURL = CANONICALBASE + "/" + MANIFESTID;
    private static final String VSMUSAGECONTEXTTYPESYSTEMURL = "http://aphl.org/fhir/vsm/CodeSystem/usage-context-type";
    private static final String USAGECONTEXTTYPESYSTEMURL = "http://terminology.hl7.org/CodeSystem/usage-context-type";
    private static final String USPHUSAGECONTEXTURL = "http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context";
    private static final String SEARCHPARAMSYSTEMLIBRARYDEPENDSON = "http://hl7.org/fhir/Library#relatedArtifact.dependsOn";
    private static final String SEARCHPARAMSYSTEMLIBRARYCONTEXTTYPEVALUE = "http://hl7.org/fhir/ValueSet#useContext.context-type-value";
    private static final String SEARCHPARAMUSECONTEXTVALUEGROUPERTYPECONDITIONGROUPER = "grouper-type$http://aphl.org/fhir/vsm/CodeSystem/usage-context-type|condition-grouper";
    private static final int CONDITIONGROUPINGSSHEETINDEX = 0;
    private static final int CONDITIONGROUPINGTITLEINDEX = 1;
    private static final int REPORTINGSPECIFICATIONNAMEINDEX = 2;
    private static final int REPORTINGSPECIFICATIONCONDITIONCODEINDEX = 3;
    private static final int REPORTINGSPECIFICATIONCONDITIONDESCRIPTIONINDEX = 4;
    private FhirContext fhirContext;
    private String version;
    private String pathToInputBundle;
    private String pathToConditionGrouperWorkbook;
    private String pathToConditionCodeValueSet;
    private String outputFileName;
    private HashSet<IOUtils.Encoding> outputFileEncodings;
    public Set<IOUtils.Encoding> getOutputFileEncodings() {
        if (outputFileEncodings == null) {
            outputFileEncodings = new HashSet<>();
        }

        if (outputFileEncodings.isEmpty()) {
            outputFileEncodings.add(IOUtils.Encoding.JSON);
        }
        return this.outputFileEncodings;
    }
    private boolean prettyPrintOutput;

    public TESPackageGenerator() {
        fhirContext = FhirContext.forR4();
    }

    @Override
    public void execute(String[] args) {
        parseParameters(args);
        Bundle bundle = generatePackage();
    }

    private void parseParameters(String[] args) {
        this.setOutputPath("src/main/resources/org/opencds/cqf/tooling/casereporting/tes/output"); // default

        for (String arg : args) {
            if (arg.equals("-TransformErsd")) {
                continue;
            }

            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "version": case "v": version = value; break; // -version (-v)
                case "outputpath": case "op": this.setOutputPath(value); break; // -outputpath (-op)
                case "outputfilename": case "ofn": outputFileName = value; break; // -outputfilename (-ofn)
                case "pathtoinputbundle": case "ptib": pathToInputBundle = value; break; // -pathtoinputbundle (-ptib)
                case "pathtoconditiongrouperworkbook": case "ptcgw": pathToConditionGrouperWorkbook = value; break; // -pathtoconditiongrouperworkbook (-ptcgw)
                case "pathtoconditioncodevalueset": case "ptccvs": pathToConditionCodeValueSet = value; break; // -pathtoconditioncodevalueset (-ptccvs)
                case "encoding": case "e": // -encoding (-e)
                    IOUtils.Encoding encoding = IOUtils.Encoding.parse(value.toLowerCase());
                    if (encoding == IOUtils.Encoding.JSON || encoding == IOUtils.Encoding.XML) {
                        this.getOutputFileEncodings().add(encoding); break;
                    } else {
                        throw new IllegalArgumentException("Invalid encoding: " + value);
                    }
                case "prettyprintoutput": case "ppo": prettyPrintOutput = Boolean.parseBoolean(value); break; // -prettyprintoutput (-ppo)
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }

        if (pathToInputBundle == null) {
            throw new IllegalArgumentException("The path to the transaction bundle that contains the Reporting Specification Grouper ValueSets is required");
        }
    }

    private Bundle generatePackage() {
        TESPackageGenerateParameters inputParameters = new TESPackageGenerateParameters();
        inputParameters.version = version;
        inputParameters.pathToInputBundle = pathToInputBundle;
        inputParameters.pathToConditionGrouperWorkbook = pathToConditionGrouperWorkbook;
        inputParameters.pathToConditionCodeValueSet = pathToConditionCodeValueSet;
        inputParameters.outputPath = this.getOutputPath();
        inputParameters.outputFileName = outputFileName;
        inputParameters.outputFileEncodings = outputFileEncodings;
        inputParameters.prettyPrintOutput = prettyPrintOutput;

        return generatePackage(inputParameters);
    }

    private void loadOperationArguments(TESPackageGenerateParameters params) {
        version = params.version;
        pathToInputBundle = params.pathToInputBundle;
        pathToConditionGrouperWorkbook = params.pathToConditionGrouperWorkbook;
        pathToConditionCodeValueSet = params.pathToConditionCodeValueSet;
        setOutputPath(params.outputPath);
        outputFileName = params.outputFileName;
        outputFileEncodings = params.outputFileEncodings;
        prettyPrintOutput = params.prettyPrintOutput;
    }

    public Bundle generatePackage(TESPackageGenerateParameters params) {
        loadOperationArguments(params);
        List<ConditionGroupingEntry> conditionGroupingEntries = loadConditionGroupingDefinitions(pathToConditionGrouperWorkbook);
        List<ValueSet> conditionGroupers = generateConditionGroupers(conditionGroupingEntries);
        List<ValueSet> reportingSpecificationGroupers = loadReportingSpecificationGroupers(params.pathToInputBundle);
        addReportingSpecificationGrouperReferencesToConditionGroupers(conditionGroupingEntries, conditionGroupers, reportingSpecificationGroupers);
        Library manifest = generateManifest(conditionGroupers, reportingSpecificationGroupers);

        // Write ConditionGroupers out to files (should be an argument)
        for (IOUtils.Encoding encoding : getOutputFileEncodings()) {
            IOUtils.writeResources(conditionGroupers, this.getOutputPath(), encoding, FhirContext.forR4Cached());
        }

        List<IBaseResource> resourcesToBundle = new ArrayList<>();
        resourcesToBundle.add(manifest);
        resourcesToBundle.addAll(conditionGroupers);
        resourcesToBundle.addAll(reportingSpecificationGroupers);

        Bundle outputBundle = null;
        outputBundle = buildAndWriteBundle(resourcesToBundle);

        if (!params.pathToConditionCodeValueSet.isEmpty()) {
            try {
                generateConditionCodeUsageComparison(params.pathToConditionCodeValueSet, reportingSpecificationGroupers);
            } catch (Exception ex) {
                throw new RuntimeException("Unable to create condition code difference workbook file.");
            }

        }

        runValidation(manifest, conditionGroupers, reportingSpecificationGroupers);

        return outputBundle;
    }

    private void runValidation(Library manifest, List<ValueSet> conditionGroupers, List<ValueSet> reportingSpecificationGroupers) {
        for (ValueSet vs : conditionGroupers) {
            if (!vs.hasCompose()) {
                System.out.println(String.format("%s has no compose", vs.getTitle()));
            }
        }
    }
    private void generateConditionCodeUsageComparison(String pathToConditionCodeValueSet, List<ValueSet> reportingSpecificationGroupers) throws IOException {
        ValueSet conditionCodeValueSet = loadConditionCodeValueSet(pathToConditionCodeValueSet);

        if (conditionCodeValueSet != null) {
            List<Coding> conditionCodeValueSetCodes = extractFlatCodeListFromValueSet(conditionCodeValueSet);
            List<Coding> reportingSpecificationGrouperCodes = new ArrayList<>();

            for (ValueSet reportingSpecificationGrouper : reportingSpecificationGroupers) {
                Optional<UsageContext> maybeUseContext =
                        reportingSpecificationGrouper.getUseContext().stream().filter(uc -> uc.getCode().getCode().equalsIgnoreCase("focus")).findFirst();

                if (maybeUseContext.isPresent()) {
                    var useContext = maybeUseContext.get().getValueCodeableConcept().getCodingFirstRep();
                    reportingSpecificationGrouperCodes.add(new Coding(useContext.getSystem(), useContext.getCode(), maybeUseContext.get().getValueCodeableConcept().getText()));
                }
            }

            // Create a new workbook and sheet
            String filePath = getOutputPath() + "/condition-code-diff.xlsx";
            Workbook workbook = new XSSFWorkbook(); //WorkbookFactory.create(conditionCodeDiffWorkbookFile);

            // Generate a Sheet with list of codes that are in the RCKMS Condition Code ValueSet,
            // but not implemented in any current reporting specifications
            Sheet sheet1 = workbook.createSheet("In VS, not in Production");
            writeDifferencesToSheet(conditionCodeValueSetCodes, reportingSpecificationGrouperCodes, sheet1);

            // Generate a Sheet with list of codes that are associated with current reporting specification implementations,
            // but not in the RCKMS Condition Code ValueSet.
            Sheet sheet2 = workbook.createSheet("In Production, not in VS");
            writeDifferencesToSheet(reportingSpecificationGrouperCodes, conditionCodeValueSetCodes, sheet2);

            // Save the workbook to the file system
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
                System.out.println("Excel file written successfully.");
            } catch (IOException e) {
                System.out.println("Error writing Excel file: " + e.getMessage());
            } finally {
                try {
                    workbook.close();
                } catch (IOException e) {
                    System.out.println("Error closing workbook: " + e.getMessage());
                }
            }
        }
    }

    private void writeDifferencesToSheet(List<Coding> baseList, List<Coding> comparisonList, Sheet sheet) {
        int rowCounter = 0;
        Row headerRow = sheet.createRow(rowCounter);
        Cell headerSystemCell = headerRow.createCell(0);
        headerSystemCell.setCellValue("System");
        Cell headerCodeCell = headerRow.createCell(1);
        headerCodeCell.setCellValue("Code");
        Cell headerDisplayCell = headerRow.createCell(2);
        headerDisplayCell.setCellValue("Text");
        rowCounter++;

        for (int i = 0; i < baseList.size(); i++) {
            String code = baseList.get(i).getCode();
            if (comparisonList.stream().noneMatch(rscoding -> rscoding.getCode().equalsIgnoreCase(code))) {
                Row row = sheet.createRow(rowCounter);
                Cell systemCell = row.createCell(0);
                systemCell.setCellValue(baseList.get(i).getSystem());
                Cell codeCell = row.createCell(1);
                codeCell.setCellValue(baseList.get(i).getCode());
                Cell displayCell = row.createCell(2);
                displayCell.setCellValue(baseList.get(i).getDisplay());
                rowCounter++;
            }
        }
    }

    private static List<Coding> extractFlatCodeListFromValueSet(ValueSet conditionCodeValueSet) {
        List<Coding> conditionCodeValueSetCodeAndDisplay = new ArrayList<>();

        if (conditionCodeValueSet.hasCompose() && conditionCodeValueSet.getCompose().hasInclude()) {
            for (ValueSet.ConceptSetComponent include : conditionCodeValueSet.getCompose().getInclude()) {
                for (ValueSet.ConceptReferenceComponent concept : include.getConcept()) {
                    conditionCodeValueSetCodeAndDisplay.add(new Coding(include.getSystem(), concept.getCode(), concept.getDisplay()));
                }
            }
        }

        return conditionCodeValueSetCodeAndDisplay;
    }

    private ValueSet loadConditionCodeValueSet(String pathToConditionCodeValueSet) {
        ValueSet conditionCodeValueSet = null;
        File valueSetFile = new File(pathToConditionCodeValueSet);
        if (valueSetFile.isFile()) {
            try {
                if (valueSetFile.getName().endsWith("json")) {
                    conditionCodeValueSet = (ValueSet)((JsonParser) fhirContext.newJsonParser()).parseResource(new FileInputStream(valueSetFile));
                }
                else if (valueSetFile.getName().endsWith("xml")) {
                    conditionCodeValueSet = (ValueSet)((XmlParser) fhirContext.newXmlParser()).parseResource(new FileInputStream(valueSetFile));
                }
                else {
                    throw new IllegalArgumentException("Unsupported input file encoding. Currently, only .json and .xml supported for the input file.");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("Error parsing " + valueSetFile.getName());
            }
        }
        return conditionCodeValueSet;
    }

    private List<ValueSet> generateConditionGroupers(List<ConditionGroupingEntry> conditionGroupingEntries) {
        List<ValueSet> conditionGroupers = new ArrayList<>();

        for (ConditionGroupingEntry conditionGroupingEntry : conditionGroupingEntries) {
            if (conditionGroupers.stream().noneMatch(cg -> cg.getTitle().equalsIgnoreCase(conditionGroupingEntry.getConditionGroupingTitle()))) {
                String id = UUID.randomUUID().toString();
                ValueSet conditionGrouperValueSet = new ValueSet();
                conditionGrouperValueSet.setId(id);
                conditionGrouperValueSet.setMeta(new Meta().addProfile("http://aphl.org/fhir/vsm/StructureDefinition/vsm-conditiongroupervalueset"));
                conditionGrouperValueSet.getMeta().addTag(SEARCHPARAMSYSTEMLIBRARYDEPENDSON, MANIFESTURL + "|" + this.version, null);
                conditionGrouperValueSet.getMeta().addTag(SEARCHPARAMSYSTEMLIBRARYCONTEXTTYPEVALUE, SEARCHPARAMUSECONTEXTVALUEGROUPERTYPECONDITIONGROUPER, null);
                conditionGrouperValueSet.setUrl(CANONICALBASE + "/ValueSet/" + id);
                conditionGrouperValueSet.setVersion(this.version);
                conditionGrouperValueSet.setName(namify(conditionGroupingEntry.getConditionGroupingTitle()));
                conditionGrouperValueSet.setTitle(conditionGroupingEntry.getConditionGroupingTitle());
                conditionGrouperValueSet.setStatus(Enumerations.PublicationStatus.ACTIVE);
                conditionGrouperValueSet.setExperimental(false);
                conditionGrouperValueSet.setDate(new Date());
                conditionGrouperValueSet.setPublisher(PUBLISHER);
                UsageContext conditionGrouperUseContext =
                    new UsageContext(
                        new Coding(VSMUSAGECONTEXTTYPESYSTEMURL, "grouper-type", null),
                        new CodeableConcept(new Coding(USPHUSAGECONTEXTURL, "condition-grouper", null))
                    );
                conditionGrouperValueSet.addUseContext(conditionGrouperUseContext);
                conditionGroupers.add(conditionGrouperValueSet);
            }
        }
        return conditionGroupers;
    }

    private void addReportingSpecificationGrouperReferencesToConditionGroupers(List<ConditionGroupingEntry> conditionGroupingEntries, List<ValueSet> conditionGroupers, List<ValueSet> reportingSpecificationGroupers) {
        for (ValueSet reportingSpecificationGrouper : reportingSpecificationGroupers) {
            var relevantConditionGroupingEntry =
                conditionGroupingEntries.stream().filter(cge -> (cge.getReportingSpecificationName()).equalsIgnoreCase(reportingSpecificationGrouper.getTitle())).collect(Collectors.toList()).stream().findFirst().orElse(null);
            //conditionGroupingEntries.stream().filter(cge -> (cge.getReportingSpecificationName() + " Reporting Specification Grouper").equalsIgnoreCase(reportingSpecificationGrouper.getTitle())).collect(Collectors.toList()).stream().findFirst().orElse(null);

            if (relevantConditionGroupingEntry != null) {
                var relevantConditionGrouper =
                        conditionGroupers.stream().filter(cg -> cg.getTitle().equalsIgnoreCase(relevantConditionGroupingEntry.getConditionGroupingTitle())).collect(Collectors.toList()).stream().findFirst().orElse(null);
                if (relevantConditionGrouper != null) {
                    if (!relevantConditionGrouper.hasCompose()) {
                        relevantConditionGrouper.setCompose(new ValueSet.ValueSetComposeComponent());
                    }
                    if (relevantConditionGrouper.getCompose().getInclude().stream().noneMatch(i -> i.getValueSet().contains(new CanonicalType(relevantConditionGrouper.getUrl())))) {
                        relevantConditionGrouper.getCompose().addInclude(new ValueSet.ConceptSetComponent().addValueSet(reportingSpecificationGrouper.getUrl()));
                    }
                }
            }
        }
    }

    private Bundle buildAndWriteBundle(List<IBaseResource> resourcesToBundle) {//Library manifest, List<ValueSet> conditionGroupers, List<ValueSet> inputBundleResources) {
        BundleBuilder builder = new BundleBuilder(this.fhirContext);
        builder.setBundleField("id", "tes-content-bundle");
        for (IBaseResource resourceToBundle : resourcesToBundle) {
            builder.addTransactionUpdateEntry(resourceToBundle);
        }

        Bundle bundle = (Bundle)builder.getBundle();
        for (IOUtils.Encoding encoding : getOutputFileEncodings()) {
            IOUtils.writeResource(bundle, this.getOutputPath(), encoding, FhirContext.forR4Cached());
        }

        return bundle;
    }

    private List<ConditionGroupingEntry> loadConditionGroupingDefinitions(String pathToConditionGrouperWorkbook) {
        List<ConditionGroupingEntry> conditionGroupingEntries = new ArrayList<>();
        try {
            Workbook workbook = SpreadsheetHelper.getWorkbook(pathToConditionGrouperWorkbook);
            conditionGroupingEntries = processConditionGroupingsSheet(workbook);
            return conditionGroupingEntries;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return conditionGroupingEntries;
    }

    private List<ConditionGroupingEntry> processConditionGroupingsSheet(Workbook workbook) {
        List<ConditionGroupingEntry> conditionGroupingEntries = new ArrayList<>();

        Iterator<Row> rowIterator;

        Sheet groupingCodesSheet = workbook.getSheetAt(CONDITIONGROUPINGSSHEETINDEX);
        rowIterator = groupingCodesSheet.iterator();
        // Skip header row
        rowIterator.next();

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            String conditionGroupingName = SpreadsheetHelper.protectedString(SpreadsheetHelper.getCellAsString(row, CONDITIONGROUPINGTITLEINDEX)
                .replace("\u00a0"," ")
                .replace("\u202F"," ")
                .trim(), false);
            String reportingSpecificationName =
                SpreadsheetHelper.protectedString(SpreadsheetHelper.getCellAsString(row, REPORTINGSPECIFICATIONNAMEINDEX)
                    .replace("\u00a0"," ")
                    .replace("\u202F"," ")
                    .trim(), false);
            String reportingSpecificationCode =
                SpreadsheetHelper.protectedString(SpreadsheetHelper.getCellAsString(row, REPORTINGSPECIFICATIONCONDITIONCODEINDEX)
                    .replace("\u00a0"," ")
                    .replace("\u202F"," ")
                    .trim(), false);
            String reportingSpecificationDescription =
                SpreadsheetHelper.protectedString(SpreadsheetHelper.getCellAsString(row, REPORTINGSPECIFICATIONCONDITIONDESCRIPTIONINDEX)
                    .replace("\u00a0"," ")
                    .replace("\u202F"," ")
                    .trim(), false);

            if (!Objects.requireNonNull(conditionGroupingName).isEmpty()
                    || !Objects.requireNonNull(reportingSpecificationName).isEmpty()
                    || !Objects.requireNonNull(reportingSpecificationCode).isEmpty()
                    || !Objects.requireNonNull(reportingSpecificationDescription).isEmpty()) {
                ConditionGroupingEntry conditionGroupingEntry =
                    new ConditionGroupingEntry(conditionGroupingName, reportingSpecificationName, reportingSpecificationCode, reportingSpecificationDescription);
                conditionGroupingEntries.add(conditionGroupingEntry);
            }
        }

        return conditionGroupingEntries;
    }

    private static class ConditionGroupingEntry {
        private String conditionGroupingTitle;
        public String getConditionGroupingTitle() { return conditionGroupingTitle; }
        private String reportingSpecificationName;
        public String getReportingSpecificationName() { return reportingSpecificationName; }
        private String reportingSpecificationConditionCode;
        public String getReportingSpecificationConditionCode() { return reportingSpecificationConditionCode; }
        private String reportingSpecificationConditionDescription;
        public String getReportingSpecificationConditionDescription() { return reportingSpecificationConditionDescription; }

        public ConditionGroupingEntry(String conditionGroupingTitle, String reportingSpecificationName, String reportingSpecificationConditionCode, String reportingSpecificationConditionDescription) {
            this.conditionGroupingTitle = conditionGroupingTitle;
            this.reportingSpecificationName = reportingSpecificationName;
            this.reportingSpecificationConditionCode = reportingSpecificationConditionCode;
            this.reportingSpecificationConditionDescription = reportingSpecificationConditionDescription;
        }
    }

    private void validateRSGroupers(List<ValueSet> rsGroupers) {
        for (ValueSet rsGrouper : rsGroupers) {
            List<UsageContext> useContexts = rsGrouper.getUseContext();

            if (useContexts.stream().noneMatch(uc ->
                uc.hasCode()
                && uc.getCode().getSystem().equalsIgnoreCase(VSMUSAGECONTEXTTYPESYSTEMURL)
                && uc.getCode().hasCode()
                && uc.getCode().getCode().equalsIgnoreCase("grouper-type")
                && uc.hasValueCodeableConcept()
                && uc.getValueCodeableConcept().hasCoding(VSMUSAGECONTEXTTYPESYSTEMURL, "reporting-specification-grouper"))) {
                logger.info("ValueSet with id {} is not a reporting-specification-grouper and will be skipped.", rsGrouper.getIdPart());
            } else {
                logger.info("Creating a Condition Grouper ValueSet for Reporting Specification Grouper ID: {}", rsGrouper.getIdPart());
            }
        }
    }

    private static String namify(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Input string cannot be null or empty");
        }

        // Convert to PascalCase by capitalizing each word and removing invalid characters
        String sanitized = input.trim()
            .replaceAll("[^a-zA-Z0-9\\s]", "")
            .replaceAll("\\s+", "_");

        // Ensure the name doesn't exceed 255 characters
        if (sanitized.length() > 255) {
            sanitized = sanitized.substring(0, 255);
        }

        // Return the sanitized name
        return sanitized;
    }

    private List<ValueSet> loadReportingSpecificationGroupers(String pathToBundle) {
        Bundle sourceBundle = null;
        File bundleFile = new File(pathToBundle);
        if (bundleFile.isFile()) {
            try {
                if (bundleFile.getName().endsWith("json")) {
                    sourceBundle = (Bundle)((JsonParser) fhirContext.newJsonParser()).parseResource(new FileInputStream(bundleFile));
                }
                else if (bundleFile.getName().endsWith("xml")) {
                    sourceBundle = (Bundle)((XmlParser) fhirContext.newXmlParser()).parseResource(new FileInputStream(bundleFile));
                }
                else {
                    throw new IllegalArgumentException("Unsupported input bundle encoding. Currently, only .json and .xml supported for the input bundle.");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("Error parsing " + bundleFile.getName());
            }
        }

        List<ValueSet> rsGrouperValueSets = new ArrayList<>();
        if (sourceBundle != null) {
            List<IBaseResource> rsGrouperResources = bundleToResources(fhirContext, sourceBundle);

            for (IBaseResource resource : rsGrouperResources) {
                if (resource instanceof ValueSet) {
                    rsGrouperValueSets.add((ValueSet) resource); // Safe casting
                } else {
                    logger.info("A rsGrouper of type '{}', with ID '{}' was encountered. The Condition Grouper generator expects only ValueSet resources. This rsGrouper will be skipped.", resource.getIdElement().getResourceType(), resource.getIdElement().getIdPart());
                }
            }

            validateRSGroupers(rsGrouperValueSets);
        } else {
            logger.info("Bundle at '{}' could not be found or loaded.", this.pathToInputBundle);
        }

        return rsGrouperValueSets;
    }

    private Library generateManifest(List<ValueSet> components, List<ValueSet> dependencies) {
        Library manifest = new Library();

        manifest.setId(MANIFESTID);
        manifest.addExtension(
            new Extension()
                .setUrl("http://hl7.org/fhir/StructureDefinition/artifact-releaseLabel")
                .setValue(new StringType("<ReleaseLabel>")));
        manifest.setUrl(MANIFESTURL);
        manifest.setVersion(this.version);
        manifest.setName("TESContentLibrary");
        manifest.setTitle("TES Content Library");
        manifest.setStatus(Enumerations.PublicationStatus.ACTIVE);
        manifest.setExperimental(false);
        manifest.setType(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/library-type", "asset-collection)", null)));
        manifest.setPublisher(PUBLISHER);
        manifest.setDescription("This is the package manifest Library for a TES content release.");
        UsageContext specificationTypeUseContext =
            new UsageContext(
                new Coding("http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context-type", "specification-type", null),
                new CodeableConcept(new Coding(USPHUSAGECONTEXTURL, "value-set-library", null))
            );
        manifest.addUseContext(specificationTypeUseContext);
        UsageContext specificationCategoryUseContext =
            new UsageContext(
                new Coding(VSMUSAGECONTEXTTYPESYSTEMURL, "specification-category", null),
                new CodeableConcept(new Coding(VSMUSAGECONTEXTTYPESYSTEMURL, "tes-release", null))
            );
        manifest.addUseContext(specificationCategoryUseContext);
        manifest.setPurpose("Collection of RCKMS Reporting Specification terminology.");
        manifest.setEffectivePeriod(new Period().setStart(new Date()));

        List<RelatedArtifact> relatedArtifactsToAdd = new ArrayList<>();

        // For each condition grouper, add both a composed-of and depends-on
        for (ValueSet component : components) {
            var componentRelatedArtifact = new RelatedArtifact().setType(RelatedArtifact.RelatedArtifactType.COMPOSEDOF);
            componentRelatedArtifact.addExtension(
                new Extension()
                    .setUrl("http://hl7.org/fhir/StructureDefinition/crmi-isOwned")
                    .setValue(new BooleanType(true)));
            componentRelatedArtifact.setResource(component.getUrl() + "|" + component.getVersion());
            relatedArtifactsToAdd.add(componentRelatedArtifact);
        }

        for (ValueSet component : components) {
            var dependencyRelatedArtifact = new RelatedArtifact().setType(RelatedArtifact.RelatedArtifactType.DEPENDSON);
            dependencyRelatedArtifact.setResource(component.getUrl() + "|" + component.getVersion());
            relatedArtifactsToAdd.add(dependencyRelatedArtifact);
        }

        for (ValueSet dependency : dependencies) {
            var dependencyRelatedArtifact = new RelatedArtifact().setType(RelatedArtifact.RelatedArtifactType.DEPENDSON);
            dependencyRelatedArtifact.setResource(dependency.getUrl() + "|" + dependency.getVersion());
            relatedArtifactsToAdd.add(dependencyRelatedArtifact);
        }

        manifest.setRelatedArtifact(relatedArtifactsToAdd);

        for (IOUtils.Encoding encoding : getOutputFileEncodings()) {
            IOUtils.writeResource(manifest, this.getOutputPath(), encoding, FhirContext.forR4Cached());
        }

        return manifest;
    }
}