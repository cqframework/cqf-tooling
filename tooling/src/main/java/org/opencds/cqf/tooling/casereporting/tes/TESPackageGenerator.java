package org.opencds.cqf.tooling.casereporting.tes;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.parser.XmlParser;
import ca.uhn.fhir.util.BundleBuilder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.constants.CaseReporting;
import org.opencds.cqf.tooling.terminology.SpreadsheetHelper;
import org.opencds.cqf.tooling.utilities.CanonicalUtils;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.opencds.cqf.tooling.operations.bundle.BundleToResources.bundleToResources;

public class TESPackageGenerator extends Operation {
    private static final Logger logger = LoggerFactory.getLogger(TESPackageGenerator.class);
    private FhirContext fhirContext;
    private String version;
    private String releaseLabel;
    private String pathToInputBundle;
    private String pathToConditionGrouperWorkbook;
    private String pathToConditionCodeValueSet;
    private String outputFileName;
    private Set<IOUtils.Encoding> outputFileEncodings;
    public Set<IOUtils.Encoding> getOutputFileEncodings() {
        if (outputFileEncodings == null) {
            outputFileEncodings = new HashSet<>();
        }

        if (outputFileEncodings.isEmpty()) {
            outputFileEncodings.add(IOUtils.Encoding.JSON);
        }
        return this.outputFileEncodings;
    }
    private boolean writeConditionGroupers;
    private boolean writeReportingSpecificationGroupers;
    private boolean writeAdditionalContextGroupers;

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
            if (arg.equals("-CaseReportingTESGeneratePackage")) {
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
                case "releaselabel": case "rl": releaseLabel = value; break; // -releaselabel (-rl)
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
                case "writeconditiongroupers": case "wcg": writeConditionGroupers = Boolean.parseBoolean(value); break; // -writeconditiongroupers (-wcg)
                case "writereportingspecificationgroupers": case "wrsg": writeReportingSpecificationGroupers = Boolean.parseBoolean(value); break; // -writeReportingSpecificationGroupers (-wrsg)
                case "writeadditionalcontextgroupers": case "wgrsg": writeAdditionalContextGroupers = Boolean.parseBoolean(value); break; // -writeadditionalcontextgroupers (-wacg)
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
        inputParameters.releaseLabel = releaseLabel;
        inputParameters.pathToInputBundle = pathToInputBundle;
        inputParameters.pathToConditionGrouperWorkbook = pathToConditionGrouperWorkbook;
        inputParameters.pathToConditionCodeValueSet = pathToConditionCodeValueSet;
        inputParameters.outputPath = this.getOutputPath();
        inputParameters.outputFileName = outputFileName;
        inputParameters.outputFileEncodings = outputFileEncodings;
        inputParameters.writeConditionGroupers = writeConditionGroupers;
        inputParameters.writeReportingSpecificationGroupers = writeReportingSpecificationGroupers;
        inputParameters.writeAdditionalContextGroupers = writeAdditionalContextGroupers;

        return generatePackage(inputParameters);
    }

    private void loadOperationArguments(TESPackageGenerateParameters params) {
        version = params.version;
        releaseLabel = params.releaseLabel;
        pathToInputBundle = params.pathToInputBundle;
        pathToConditionGrouperWorkbook = params.pathToConditionGrouperWorkbook;
        pathToConditionCodeValueSet = params.pathToConditionCodeValueSet;
        setOutputPath(params.outputPath);
        outputFileName = params.outputFileName;
        outputFileEncodings = params.outputFileEncodings;
        writeConditionGroupers = params.writeConditionGroupers;
        writeReportingSpecificationGroupers = params.writeReportingSpecificationGroupers;
        writeAdditionalContextGroupers = params.writeAdditionalContextGroupers;
    }

    public Bundle generatePackage(TESPackageGenerateParameters params) {
        loadOperationArguments(params);

        // Load the RCKMS Reporting Specification Groupers with their mappings to Condition Grouper and generate those Condition Groupers
        List<ConditionGrouperEntry> conditionGroupingEntries = loadConditionGroupingDefinitions(pathToConditionGrouperWorkbook);
        List<ValueSet> conditionGroupers = generateConditionGroupers(conditionGroupingEntries);
        List<ValueSet> reportingSpecificationGroupers = loadReportingSpecificationGroupers(params.pathToInputBundle);

        var grouperToConditionGrouperMap = new HashMap<String, String>();

        //For each of these, add a mapping entry that is RSGrouper.URL -> ConditionGrouper.URL
        for (ValueSet reportingSpecificationGrouper : reportingSpecificationGroupers) {
            conditionGroupingEntries.stream()
                .filter(cge -> ("ReportingSpecificationGrouper" + cge.getReportingSpecificationConditionCode().replace('\u00A0', ' ').trim())
                        .equalsIgnoreCase(reportingSpecificationGrouper.getName().replace('\u00A0', ' ').trim()))
                .collect(Collectors.toList())
                .stream()
                .findFirst().flatMap(relevantConditionGroupingEntry -> conditionGroupers.stream()
                    .filter(cg -> cg.getTitle().equalsIgnoreCase(relevantConditionGroupingEntry.getConditionGrouperTitle()))
                    .collect(Collectors.toList())
                    .stream()
                    .findFirst())
                    .ifPresent(relevantConditionGrouper -> {
                        grouperToConditionGrouperMap.put(
                            getVersionedReferenceForValueSet(reportingSpecificationGrouper),
                            relevantConditionGrouper.getUrl()
                        );
                        }
                    );
        }

        // Load the additional context Reporting Specification Groupers with their Condition Grouper mapping
        // (should be mapped to existing Condition Groupers and not introduce new ones).
        List<AdditionalContextGrouperEntry> additionalContextGrouperEntries =
                loadAdditionalContextGroupersFromSheet(pathToConditionGrouperWorkbook);
        List<ValueSet> additionalContextGroupers = generateAdditionalContextGroupers(additionalContextGrouperEntries);

        //For each of these, add a mapping entry that is RSGrouper.URL -> ConditionGrouper.URL
        for (ValueSet additionalContextGrouper : additionalContextGroupers) {
            var relevantAdditionalContextGrouperEntry = additionalContextGrouperEntries.stream()
                .filter(rsg -> rsg.getReportingSpecificationGrouperTitle().equalsIgnoreCase(additionalContextGrouper.getTitle()))
                .findFirst()
                .orElse(null);

            if (relevantAdditionalContextGrouperEntry != null) {
                var relevantConditionGrouper = conditionGroupers
                    .stream()
                    .filter(cg -> cg.getUrl().equals(relevantAdditionalContextGrouperEntry.getConditionGrouperUrl()))
                    .findFirst()
                    .orElse(null);

                if (relevantConditionGrouper != null) {
                    var reference = getVersionedReferenceForValueSet(additionalContextGrouper);
                    grouperToConditionGrouperMap.put(reference, relevantConditionGrouper.getUrl());
                }
            }
        }

//        List<ValueSet> reportingSpecificationGroupers = new ArrayList<>();
//        reportingSpecificationGroupers.addAll(additionalContextGroupers);
//        reportingSpecificationGroupers.addAll(reportingSpecificationGroupers);

        addReportingSpecificationGrouperReferencesToConditionGroupers(grouperToConditionGrouperMap, conditionGroupers);

        var components = new ArrayList<ValueSet>(conditionGroupers);
        components.addAll(additionalContextGroupers);

        var dependencies = new ArrayList<>(reportingSpecificationGroupers);

        Library manifest = generateManifest(components, dependencies);

        if (writeConditionGroupers) {
            for (IOUtils.Encoding encoding : getOutputFileEncodings()) {
                for (ValueSet grouper : conditionGroupers) {
                    String name = CanonicalUtils.getTail(grouper.getUrl());
                    IOUtils.writeResource(grouper, this.getOutputPath() + "/condition-groupers", encoding, FhirContext.forR4Cached(), true, name);
                }
            }
        }

        if (writeReportingSpecificationGroupers) {
            for (IOUtils.Encoding encoding : getOutputFileEncodings()) {
                for (ValueSet grouper : reportingSpecificationGroupers) {
                    String name = CanonicalUtils.getTail(grouper.getUrl());
                    IOUtils.writeResource(grouper, this.getOutputPath() + "/reporting-specification-groupers", encoding, FhirContext.forR4Cached(), true, name);
                }
            }
        }

        if (writeAdditionalContextGroupers) {
            for (IOUtils.Encoding encoding : getOutputFileEncodings()) {
                for (ValueSet grouper : additionalContextGroupers) {
                    String name = CanonicalUtils.getTail(grouper.getUrl());
                    IOUtils.writeResource(grouper, this.getOutputPath() + "/additional-context-groupers", encoding, FhirContext.forR4Cached(), true, name);
                }
            }
        }

        writeGeneratedRSGrouperUrlsToWorkbook(additionalContextGroupers);

        List<IBaseResource> resourcesToBundle = new ArrayList<>();
        resourcesToBundle.add(manifest);
        resourcesToBundle.addAll(conditionGroupers);
        resourcesToBundle.addAll(reportingSpecificationGroupers);
        resourcesToBundle.addAll(additionalContextGroupers);

        Bundle outputBundle = null;
        outputBundle = buildAndWriteBundle(resourcesToBundle);

        if (!params.pathToConditionCodeValueSet.isEmpty()) {
            try {
                generateConditionCodeUsageComparison(params.pathToConditionCodeValueSet, reportingSpecificationGroupers);
            } catch (Exception ex) {
                throw new RuntimeException("Unable to create condition code difference workbook file.");
            }
        }

        runSimpleValidation(conditionGroupers);

        return outputBundle;
    }

    private void writeGeneratedRSGrouperUrlsToWorkbook(List<ValueSet> generatedRSGroupers) {
        // Create a new workbook and sheet
        String filePath = this.getOutputPath() + "/generated-rs-grouper-urls.xlsx";
        Workbook workbook = new XSSFWorkbook();

        // Generate a Sheet with list of URLs that were generated for generated RS Groupers
        Sheet sheet = workbook.createSheet("Generated RS Grouper URLs");

        int rowCounter = 0;
        Row headerRow = sheet.createRow(rowCounter);
        Cell headerSystemCell = headerRow.createCell(0);
        headerSystemCell.setCellValue("Url");
        Cell headerCodeCell = headerRow.createCell(1);
        headerCodeCell.setCellValue("RS Grouper Title");
        rowCounter++;

        for (ValueSet vs : generatedRSGroupers) {
            var includes = vs.getCompose().getInclude();
            var codeCount = 0;
            for (ValueSet.ConceptSetComponent component : includes) {
                codeCount += component.getConcept().size();
            }

            for (int i = 0; i < codeCount; i++) {
                Row row = sheet.createRow(rowCounter);
                Cell systemCell = row.createCell(0);
                systemCell.setCellValue(vs.getUrl());
                Cell codeCell = row.createCell(1);
                codeCell.setCellValue(vs.getTitle());
                rowCounter++;
            }
        }

        // Save the workbook to the file system
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
            if (logger.isInfoEnabled()) {
                logger.info("Excel file written successfully.");
            }
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error writing Excel file: '{}'.", e.getMessage());
            }
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Error closing workbook: '{}'", e.getMessage());
                }
            }
        }
    }

    private static String getVersionedReferenceForValueSet(ValueSet valueSet) {
        StringBuilder reference = new StringBuilder(valueSet.getUrl());
        if (valueSet.hasVersion() && !valueSet.getVersion().isEmpty() && !valueSet.getVersion().isBlank()) {
            reference.append("|").append(valueSet.getVersion());
        }
        return reference.toString();
    }

    private void runSimpleValidation(List<ValueSet> conditionGroupers) {
        for (ValueSet vs : conditionGroupers) {
            if (!vs.hasCompose()) {
                if (logger.isErrorEnabled()) {
                    logger.error("'{}' has no compose.", vs.getTitle());
                }
            }
        }
    }

    private void generateConditionCodeUsageComparison(String pathToConditionCodeValueSet, List<ValueSet> reportingSpecificationGroupers) {
        ValueSet conditionCodeValueSet = loadConditionCodeValueSet(pathToConditionCodeValueSet);

        if (conditionCodeValueSet != null) {
            List<Coding> conditionCodeValueSetCodes = extractFlatCodeListFromValueSet(conditionCodeValueSet);
            List<Coding> reportingSpecificationGrouperCodes = new ArrayList<>();

            for (ValueSet reportingSpecificationGrouper : reportingSpecificationGroupers) {
                Optional<UsageContext> maybeUseContext =
                    reportingSpecificationGrouper.getUseContext().stream().filter(uc -> uc.getCode().getCode().trim().equalsIgnoreCase("focus")).findFirst();

                if (maybeUseContext.isPresent()) {
                    var useContext = maybeUseContext.get().getValueCodeableConcept().getCodingFirstRep();
                    reportingSpecificationGrouperCodes.add(new Coding(useContext.getSystem(), useContext.getCode().trim(), maybeUseContext.get().getValueCodeableConcept().getText()));
                }
            }

            // Create a new workbook and sheet
            String filePath = getOutputPath() + "/condition-code-diff.xlsx";
            Workbook workbook = new XSSFWorkbook();

            // Generate a Sheet with list of codes that are in the RCKMS Condition Code ValueSet,
            // but not implemented in any current reporting specifications
            Sheet sheet1 = workbook.createSheet("In VS, not in Production");
            writeDifferencesToSheet(conditionCodeValueSetCodes, reportingSpecificationGrouperCodes, sheet1);

            // Generate a Sheet with list of codes that are associated with current reporting
            // specification implementations, but not in the RCKMS Condition Code ValueSet.
            Sheet sheet2 = workbook.createSheet("In Production, not in VS");
            writeDifferencesToSheet(reportingSpecificationGrouperCodes, conditionCodeValueSetCodes, sheet2);

            // Save the workbook to the file system
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
                if (logger.isInfoEnabled()) {
                    logger.info("Excel file written successfully.");
                }
            } catch (IOException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Error writing Excel file: '{}'.", e.getMessage());
                }
            } finally {
                try {
                    workbook.close();
                } catch (IOException e) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Error closing workbook: '{}'", e.getMessage());
                    }
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

        for (Coding coding : baseList) {
            String code = coding.getCode().trim();
            if (comparisonList.stream().noneMatch(rscoding -> rscoding.getCode().trim().equalsIgnoreCase(code))) {
                Row row = sheet.createRow(rowCounter);
                Cell systemCell = row.createCell(0);
                systemCell.setCellValue(coding.getSystem());
                Cell codeCell = row.createCell(1);
                codeCell.setCellValue(coding.getCode());
                Cell displayCell = row.createCell(2);
                displayCell.setCellValue(coding.getDisplay());
                rowCounter++;
            }
        }
    }

    private static List<Coding> extractFlatCodeListFromValueSet(ValueSet conditionCodeValueSet) {
        List<Coding> conditionCodeValueSetCodeAndDisplay = new ArrayList<>();

        if (conditionCodeValueSet.hasCompose() && conditionCodeValueSet.getCompose().hasInclude()) {
            for (ValueSet.ConceptSetComponent include : conditionCodeValueSet.getCompose().getInclude()) {
                for (ValueSet.ConceptReferenceComponent concept : include.getConcept()) {
                    conditionCodeValueSetCodeAndDisplay.add(new Coding(include.getSystem(), concept.getCode().trim(), concept.getDisplay()));
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
                if (logger.isInfoEnabled()) {
                    logger.error("Error reading condition code value set file: '{}'.", e.getMessage());
                }
            }
        }
        return conditionCodeValueSet;
    }

    private List<ValueSet> generateConditionGroupers(List<ConditionGrouperEntry> conditionGroupingEntries) {
        List<ValueSet> conditionGroupers = new ArrayList<>();

        List<Extension> extensions = new ArrayList<>();
        extensions.add(new Extension().setUrl(CaseReporting.VALUESETAUTHOREXTENSIONURL).setValue(new ContactDetail().setName(CaseReporting.GROUPERVALUESETAUTHOR)));
        extensions.add(new Extension().setUrl(CaseReporting.VALUESETSTEWARDEXTENSIONURL).setValue(new ContactDetail().setName(CaseReporting.GROUPERVALUESETSTEWARD)));

        logger.info("Generating Condition Groupers...");
        for (ConditionGrouperEntry conditionGrouperEntry : conditionGroupingEntries) {
            if (conditionGroupers.stream().noneMatch(cg -> cg.getTitle().equalsIgnoreCase(conditionGrouperEntry.getConditionGrouperTitle()))) {
                // If an identifier was not provided for the Condition Grouper in the spreadsheet (i.e. it's a new grouper)
                // then generate and use a new ID. Log the results so that the spreadsheet can be updated with these new ID (for now,
                // the spreadsheet is the source of truth for mapping between Condition Grouping title and id).
                String canonicalId = null;
                if ((conditionGrouperEntry.getConditionGrouperUrl() != null)
                        && !conditionGrouperEntry.getConditionGrouperUrl().isEmpty()) {
                    canonicalId = conditionGrouperEntry.getConditionGrouperUrl();
                } else {
                    canonicalId = CaseReporting.CANONICALBASE + "/ValueSet/" + UUID.randomUUID().toString();
                    logger.info("Condition Grouper '{}' did not have an identifier specified and has been assigned: {}", conditionGrouperEntry.getConditionGrouperTitle(), canonicalId);
                }

                ValueSet conditionGrouperValueSet = new ValueSet();
                conditionGrouperValueSet.setExtension(extensions);
                conditionGrouperValueSet.setMeta(new Meta().addProfile("http://aphl.org/fhir/vsm/StructureDefinition/vsm-conditiongroupervalueset"));
                conditionGrouperValueSet.getMeta().addTag(CaseReporting.SEARCHPARAMSYSTEMLIBRARYDEPENDSON, CaseReporting.MANIFESTURL + "|" + this.version, null);
                conditionGrouperValueSet.getMeta().addTag(CaseReporting.SEARCHPARAMSYSTEMLIBRARYCONTEXTTYPEVALUE, CaseReporting.SEARCHPARAMUSECONTEXTVALUEGROUPERTYPECONDITIONGROUPER, null);
                conditionGrouperValueSet.setUrl(canonicalId);
                conditionGrouperValueSet.setVersion(this.version);
                conditionGrouperValueSet.setName(namify(conditionGrouperEntry.getConditionGrouperTitle()));
                conditionGrouperValueSet.setTitle(conditionGrouperEntry.getConditionGrouperTitle());
                conditionGrouperValueSet.setDescription(String.format("The set of all codes from value sets used in Reporting Specifications that are associated with the '%s' condition. (NOTE: Generated Content)", conditionGrouperEntry.getConditionGrouperTitle()));
                conditionGrouperValueSet.setStatus(Enumerations.PublicationStatus.ACTIVE);
                conditionGrouperValueSet.setExperimental(false);
                conditionGrouperValueSet.setDate(new Date());
                conditionGrouperValueSet.setPublisher(CaseReporting.PUBLISHER);
                UsageContext conditionGrouperUseContext =
                    new UsageContext(
                        new Coding(CaseReporting.VSMUSAGECONTEXTTYPESYSTEMURL, "grouper-type", null),
                        new CodeableConcept(new Coding(CaseReporting.VSMUSAGECONTEXTTYPESYSTEMURL, "condition-grouper", "Condition Grouper")).setText("Condition Grouper")
                    );
                conditionGrouperValueSet.addUseContext(conditionGrouperUseContext);
                conditionGroupers.add(conditionGrouperValueSet);
            }
        }
        return conditionGroupers;
    }

    private List<ValueSet> generateAdditionalContextGroupers(List<AdditionalContextGrouperEntry> additionalContextGrouperEntries) {
        List<ValueSet> reportingSpecificationGroupers = new ArrayList<>();

        List<Extension> extensions = new ArrayList<>();
        extensions.add(new Extension().setUrl(CaseReporting.VALUESETAUTHOREXTENSIONURL).setValue(new ContactDetail().setName(CaseReporting.GROUPERVALUESETAUTHOR)));
        extensions.add(new Extension().setUrl(CaseReporting.VALUESETSTEWARDEXTENSIONURL).setValue(new ContactDetail().setName(CaseReporting.GROUPERVALUESETSTEWARD)));

        for (AdditionalContextGrouperEntry additionalContextGrouperEntry : additionalContextGrouperEntries) {
            // Get the existing RS Grouper if it exists
            // If it does not exist, create it
            // Check the compose for the code being added and add it if it's not already there.
            var relevantReportingSpecificationGrouper =
                reportingSpecificationGroupers
                    .stream()
                    .filter(cg -> cg.getTitle().equalsIgnoreCase(additionalContextGrouperEntry.getReportingSpecificationGrouperTitle()))
                    .findFirst()
                    .orElse(null);

            if (relevantReportingSpecificationGrouper == null) {
                String canonicalId = additionalContextGrouperEntry.getReportingSpecificationGrouperIdentifier();
                if (canonicalId == null || canonicalId.isEmpty()) {
                    canonicalId = CaseReporting.CANONICALBASE + "/ValueSet/" + UUID.randomUUID().toString();
                    logger.info("Reporting Specification Grouper '{}' did not have an identifier specified and has been assigned: {}", additionalContextGrouperEntry.getReportingSpecificationGrouperTitle(), canonicalId);
                }

                var vs = new ValueSet();
                vs.setExtension(extensions);
                vs.setUrl(canonicalId);
                vs.setVersion(this.version);
                vs.setName(namify(additionalContextGrouperEntry.getReportingSpecificationGrouperTitle().replace('\u00A0', ' ')).trim());
                vs.setTitle(additionalContextGrouperEntry.getReportingSpecificationGrouperTitle().replace('\u00A0', ' ').trim());
                vs.setDescription("The set of codes and value sets for artifacts that provide additional context in a report from a triggering event. (NOTE: Generated Content)");
                vs.setStatus(Enumerations.PublicationStatus.ACTIVE);
                vs.setExperimental(false);
                vs.setDate(new Date());
                vs.setPublisher(CaseReporting.PUBLISHER);
                UsageContext conditionGrouperUseContext =
                    new UsageContext(
                        new Coding(CaseReporting.VSMUSAGECONTEXTTYPESYSTEMURL, "grouper-type", null),
                        new CodeableConcept(new Coding(CaseReporting.VSMUSAGECONTEXTTYPESYSTEMURL, "additional-context-grouper", "Additional Context Grouper")).setText("Additional Context Grouper")
                    );
                vs.addUseContext(conditionGrouperUseContext);
                vs.setCompose(new ValueSet.ValueSetComposeComponent());
                reportingSpecificationGroupers.add(vs);

                relevantReportingSpecificationGrouper = vs;
            }

            var compose = relevantReportingSpecificationGrouper.getCompose();

            var includes = compose.getInclude();
            if (includes == null) {
                includes = new ArrayList<ValueSet.ConceptSetComponent>();
                compose.setInclude(includes);
            }

            var relevantInclude = includes.stream().filter(i -> i.hasSystem() && i.getSystem().equalsIgnoreCase(additionalContextGrouperEntry.getCodeSystemUrl())).findFirst().orElse(null);
            if (relevantInclude == null) {
                relevantInclude = new ValueSet.ConceptSetComponent().setSystem(additionalContextGrouperEntry.getCodeSystemUrl());
                includes.add(relevantInclude);
            }

            // If the include does not already include the code, add it.
            if (relevantInclude != null && relevantInclude.getConcept().stream().noneMatch(c -> c.hasCode() && c.getCode().equalsIgnoreCase(additionalContextGrouperEntry.getCode()))) {
                ValueSet.ConceptReferenceComponent conceptReference = new ValueSet.ConceptReferenceComponent();
                conceptReference.setCode(additionalContextGrouperEntry.getCode());
                conceptReference.setDisplay(additionalContextGrouperEntry.getCodeDisplay());

                relevantInclude.getConcept().add(conceptReference);
            }
        }
        return reportingSpecificationGroupers;
    }

    private void addReportingSpecificationGrouperReferencesToConditionGroupers(Map<String, String> mappings, List<ValueSet> conditionGroupers) {
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            var reportingSpecificationGrouperReference = entry.getKey();

            var relevantConditionGrouper =
                conditionGroupers.stream()
                    .filter(cg -> cg.getUrl().equalsIgnoreCase(entry.getValue()))
                    .findFirst()
                    .orElse(null);

            if (relevantConditionGrouper != null) {
                if (!relevantConditionGrouper.hasCompose()) {
                    relevantConditionGrouper.setCompose(new ValueSet.ValueSetComposeComponent());
                }

                if (relevantConditionGrouper.getCompose().getInclude().stream().noneMatch(i -> i.getValueSet().contains(new CanonicalType(reportingSpecificationGrouperReference)))) {
                    relevantConditionGrouper.getCompose()
                        .addInclude(new ValueSet.ConceptSetComponent().addValueSet(reportingSpecificationGrouperReference));
                }
            }
        }
    }

    private Bundle buildAndWriteBundle(List<IBaseResource> resourcesToBundle) {
        BundleBuilder builder = new BundleBuilder(this.fhirContext);
        builder.setBundleField("id", "tes-content-bundle-" + this.version);
        for (IBaseResource resourceToBundle : resourcesToBundle) {
            if (resourceToBundle instanceof MetadataResource) {
                String fullUrl = ((MetadataResource) resourceToBundle).getUrl() + "|" + ((MetadataResource) resourceToBundle).getVersion();
                String conditionalUrl = "url=" + ((MetadataResource) resourceToBundle).getUrl() + "&version=" + ((MetadataResource) resourceToBundle).getVersion();
                builder.addTransactionCreateEntry(resourceToBundle, fullUrl).conditional(conditionalUrl);
            } else {
                builder.addTransactionUpdateEntry(resourceToBundle);
            }
        }

        Bundle bundle = (Bundle)builder.getBundle();
        for (IOUtils.Encoding encoding : getOutputFileEncodings()) {
            IOUtils.writeResource(bundle, this.getOutputPath(), encoding, FhirContext.forR4Cached(), true, "tes-content-bundle");
        }

        return bundle;
    }

    private List<ConditionGrouperEntry> loadConditionGroupingDefinitions(String pathToConditionGrouperWorkbook) {
        List<ConditionGrouperEntry> conditionGroupingEntries = new ArrayList<>();
        try {
            Workbook workbook = SpreadsheetHelper.getWorkbook(pathToConditionGrouperWorkbook);
            conditionGroupingEntries = processConditionGroupingsSheet(workbook);
            return conditionGroupingEntries;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return conditionGroupingEntries;
    }

    private List<ConditionGrouperEntry> processConditionGroupingsSheet(Workbook workbook) {
        List<ConditionGrouperEntry> conditionGroupingEntries = new ArrayList<>();

        Iterator<Row> rowIterator;

        Sheet groupingCodesSheet = workbook.getSheetAt(CaseReporting.CONDITIONGROUPINGSSHEETINDEX);
        rowIterator = groupingCodesSheet.iterator();
        // Skip information/instruction row
        rowIterator.next();
        // Skip header row
        rowIterator.next();

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            String conditionGroupingIdentifier = SpreadsheetHelper.getCellAsString(row, CaseReporting.CONDITIONGROUPINGIDENTIFIERINDEX);
            String conditionGroupingTitle = Objects.requireNonNull(SpreadsheetHelper.getCellAsString(row, CaseReporting.CONDITIONGROUPINGTITLEINDEX)).trim();
            String reportingSpecificationTitle = Objects.requireNonNull(SpreadsheetHelper.getCellAsString(row, CaseReporting.REPORTINGSPECIFICATIONTITLEINDEX)).trim();
//            String reportingSpecificationTitle = Objects.requireNonNull(getCellValue(row, CaseReporting.REPORTINGSPECIFICATIONTITLEINDEX));
            row.getCell(CaseReporting.REPORTINGSPECIFICATIONCONDITIONCODEINDEX).setCellType(CellType.STRING);
            String reportingSpecificationCode = Objects.requireNonNull(SpreadsheetHelper.getCellAsString(row, CaseReporting.REPORTINGSPECIFICATIONCONDITIONCODEINDEX)).trim();
            String reportingSpecificationDescription = Objects.requireNonNull(SpreadsheetHelper.getCellAsString(row, CaseReporting.REPORTINGSPECIFICATIONCONDITIONDESCRIPTIONINDEX)).trim();

            if (!Objects.requireNonNull(conditionGroupingTitle).isEmpty()
                    || !Objects.requireNonNull(reportingSpecificationTitle).isEmpty()
                    || !Objects.requireNonNull(reportingSpecificationCode).isEmpty()
                    || !Objects.requireNonNull(reportingSpecificationDescription).isEmpty()) {
                ConditionGrouperEntry conditionGrouperEntry =
                    new ConditionGrouperEntry(conditionGroupingIdentifier, conditionGroupingTitle, reportingSpecificationTitle, reportingSpecificationCode, reportingSpecificationDescription);
                conditionGroupingEntries.add(conditionGrouperEntry);
            }
        }

        return conditionGroupingEntries;
    }

    private List<AdditionalContextGrouperEntry> loadAdditionalContextGroupersFromSheet(String pathToConditionGrouperWorkbook) {
        List<AdditionalContextGrouperEntry> rsGrouperEntries = new ArrayList<>();
        try {
            Workbook workbook = SpreadsheetHelper.getWorkbook(pathToConditionGrouperWorkbook);
            rsGrouperEntries = processAdditionalContextGroupersSheet(workbook);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return rsGrouperEntries;
    }

    private List<AdditionalContextGrouperEntry> processAdditionalContextGroupersSheet(Workbook workbook) {
        List<AdditionalContextGrouperEntry> additionalContextGrouperEntries = new ArrayList<>();

        Iterator<Row> rowIterator;

        Sheet additionalContextGroupersSheet = workbook.getSheetAt(CaseReporting.ADDITIONALCONTEXTGROUPERSHEETINDEX);
        rowIterator = additionalContextGroupersSheet.iterator();
        // Skip information/instruction row
        rowIterator.next();
        // Skip header row
        rowIterator.next();

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            String targetConditionGrouperUrl = SpreadsheetHelper.getCellAsString(row, CaseReporting.ADDITIONALCONTEXTGROUPERTARGETCONDITIONGROUPERURLCOLINDEX);
            String targetConditionGrouperName = Objects.requireNonNull(SpreadsheetHelper.getCellAsString(row, CaseReporting.ADDITIONALCONTEXTGROUPERTARGETCONDITIONGROUPERTITLECOLINDEX)).trim();
            String additionalContextGrouperUrl = SpreadsheetHelper.getCellAsString(row, CaseReporting.ADDITIONALCONTEXTGROUPERGENERATEDURLCOLINDEX);
            if (additionalContextGrouperUrl != null) {
                additionalContextGrouperUrl = additionalContextGrouperUrl.trim();
            }
            row.getCell(CaseReporting.ADDITIONALCONTEXTGROUPERTITLECOLINDEX).setCellType(CellType.STRING);
            String additionalContextGrouperName =
                Objects.requireNonNull(row.getCell(CaseReporting.ADDITIONALCONTEXTGROUPERTITLECOLINDEX).getStringCellValue().replace("\n", " ").replace('\u00A0', ' ')).trim();
            String codeSystemUrl = Objects.requireNonNull(SpreadsheetHelper.getCellAsString(row, CaseReporting.ADDITIONALCONTEXTGROUPERCODESYSTEMURLCOLINDEX)).trim();
            row.getCell(CaseReporting.ADDITIONALCONTEXTGROUPERCODECOLINDEX).setCellType(CellType.STRING);
            String code = Objects.requireNonNull(SpreadsheetHelper.getCellAsString(row, CaseReporting.ADDITIONALCONTEXTGROUPERCODECOLINDEX)).trim();
            String codeDisplay = Objects.requireNonNull(SpreadsheetHelper.getCellAsString(row, CaseReporting.ADDITIONALCONTEXTGROUPERCODEDISPLAYCOLINDEX)).trim();

            if (!Objects.requireNonNull(additionalContextGrouperName).isEmpty()
                    || !Objects.requireNonNull(codeSystemUrl).isEmpty()
                    || !Objects.requireNonNull(code).isEmpty()
                    || !Objects.requireNonNull(codeDisplay).isEmpty()) {
                AdditionalContextGrouperEntry additionalContextGrouperEntry =
                    new AdditionalContextGrouperEntry(targetConditionGrouperUrl, targetConditionGrouperName,
                        additionalContextGrouperUrl, additionalContextGrouperName,
                        codeSystemUrl, code, codeDisplay);
                additionalContextGrouperEntries.add(additionalContextGrouperEntry);
            }
        }

        return additionalContextGrouperEntries;
    }

    private void validateRSGroupers(List<ValueSet> rsGroupers) {
        for (ValueSet rsGrouper : rsGroupers) {
            List<UsageContext> useContexts = rsGrouper.getUseContext();

            if (useContexts.stream().noneMatch(uc ->
                uc.hasCode()
                && uc.getCode().getSystem().equalsIgnoreCase(CaseReporting.VSMUSAGECONTEXTTYPESYSTEMURL)
                && uc.getCode().hasCode()
                && uc.getCode().getCode().equalsIgnoreCase("grouper-type")
                && uc.hasValueCodeableConcept()
                && uc.getValueCodeableConcept().hasCoding(CaseReporting.VSMUSAGECONTEXTTYPESYSTEMURL, "reporting-specification-grouper"))) {
                if (logger.isInfoEnabled()) {
                    logger.info("ValueSet: '{}' is not a reporting-specification-grouper and will be skipped.", rsGrouper.getUrl());
                }
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("ValueSet '{}' has been validated and processed as a valid Reporting Specifciation Grouper", rsGrouper.getUrl());
                }
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
                    if (logger.isInfoEnabled()) {
                        logger.info("A rsGrouper of type '{}' was encountered. The Condition Grouper generator expects only ValueSet resources. This rsGrouper will be skipped.", resource.fhirType());
                    }
                }
            }

            validateRSGroupers(rsGrouperValueSets);
        } else {
            if (logger.isErrorEnabled()) {
                logger.error("Bundle at '{}' could not be found or loaded.", this.pathToInputBundle);
            }
        }

        return rsGrouperValueSets;
    }

    private Library generateManifest(List<ValueSet> components, List<ValueSet> dependencies) {
        Library manifest = new Library();

        if (releaseLabel != null && !releaseLabel.isEmpty()) {
            manifest.addExtension(
                new Extension()
                    .setUrl("http://hl7.org/fhir/StructureDefinition/artifact-releaseLabel")
                    .setValue(new StringType(releaseLabel)));
        }
        manifest.setUrl(CaseReporting.MANIFESTURL);
        manifest.setVersion(this.version);
        manifest.setName("TESContentLibrary");
        manifest.setTitle("TES Content Library");
        manifest.setStatus(Enumerations.PublicationStatus.ACTIVE);
        manifest.setExperimental(false);
        manifest.setType(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/library-type", "asset-collection", null)));
        manifest.setPublisher(CaseReporting.PUBLISHER);
        manifest.setDescription("This is the package manifest Library for a TES content release.");
        UsageContext specificationTypeUseContext =
            new UsageContext(
                new Coding("http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context-type", "specification-type", null),
                new CodeableConcept(new Coding(CaseReporting.USPHUSAGECONTEXTURL, "value-set-library", null))
            );
        manifest.addUseContext(specificationTypeUseContext);
        UsageContext specificationCategoryUseContext =
            new UsageContext(
                new Coding(CaseReporting.VSMUSAGECONTEXTTYPESYSTEMURL, "specification-category", null),
                new CodeableConcept(new Coding(CaseReporting.VSMUSAGECONTEXTTYPESYSTEMURL, "tes-release", null))
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
                    .setUrl("http://hl7.org/fhir/StructureDefinition/artifact-isOwned")
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
            IOUtils.writeResource(manifest, this.getOutputPath(), encoding, FhirContext.forR4Cached(), true, CanonicalUtils.getTail(manifest.getUrl()));
        }

        return manifest;
    }

    private static class TESGrouperEntry {
        private String conditionGrouperUrl;
        public String getConditionGrouperUrl() {
            return conditionGrouperUrl;
        }

        private String conditionGrouperTitle;
        public String getConditionGrouperTitle() {
            return conditionGrouperTitle;
        }

        private String reportingSpecificationGrouperTitle;
        public String getReportingSpecificationGrouperTitle() {
            return reportingSpecificationGrouperTitle;
        }

        public TESGrouperEntry(String conditionGrouperUrl, String conditionGrouperTitle, String reportingSpecificationGrouperTitle) {
            this.conditionGrouperUrl = conditionGrouperUrl;
            this.conditionGrouperTitle = conditionGrouperTitle;
            this.reportingSpecificationGrouperTitle = reportingSpecificationGrouperTitle;
        }
    }

    private static class ConditionGrouperEntry extends TESGrouperEntry {
//        private String conditionGrouperUrl;
//        public String getConditionGrouperUrl() {
//            return conditionGrouperUrl;
//        }

        private String conditionGroupingName;

        public String getConditionGroupingName() {
            return conditionGroupingName;
        }

//        private String conditionGrouperTitle;
//        public String getConditionGrouperTitle() {
//            return conditionGrouperTitle;
//        }

//        private String reportingSpecificationGrouperTitle;
//        public String getReportingSpecificationGrouperTitle() {
//            return reportingSpecificationGrouperTitle;
//        }

        private String reportingSpecificationConditionCode;

        public String getReportingSpecificationConditionCode() {
            return reportingSpecificationConditionCode;
        }

        private String reportingSpecificationConditionDescription;

        public String getReportingSpecificationConditionDescription() {
            return reportingSpecificationConditionDescription;
        }

        public ConditionGrouperEntry(String conditionGrouperUrl, String conditionGrouperTitle, String reportingSpecificationGrouperTitle, String reportingSpecificationConditionCode, String reportingSpecificationConditionDescription) {
            super(conditionGrouperUrl, conditionGrouperTitle, reportingSpecificationGrouperTitle);
//            this.conditionGrouperUrl = conditionGrouperUrl;
//            this.conditionGrouperTitle = conditionGrouperTitle;
//            this.reportingSpecificationGrouperTitle = reportingSpecificationGrouperTitle;
            this.reportingSpecificationConditionCode = reportingSpecificationConditionCode;
            this.reportingSpecificationConditionDescription = reportingSpecificationConditionDescription;
        }
    }

    private static class AdditionalContextGrouperEntry extends TESGrouperEntry {
//        private String conditionGrouperUrl;
//
//        public String getConditionGrouperUrl() {
//            return conditionGrouperUrl;
//        }

//        private String conditionGrouperTitle;
//        public String getConditionGrouperTitle() {
//            return conditionGrouperTitle;
//        }

        private String reportingSpecificationGrouperIdentifier;

        public String getReportingSpecificationGrouperIdentifier() {
            return reportingSpecificationGrouperIdentifier;
        }

//        private String reportingSpecificationGrouperTitle;
//        public String getReportingSpecificationGrouperTitle() {
//            return reportingSpecificationGrouperTitle;
//        }

        private String codeSystemUrl;

        public String getCodeSystemUrl() {
            return codeSystemUrl;
        }

        private String code;

        public String getCode() {
            return code;
        }

        private String codeDisplay;

        public String getCodeDisplay() {
            return codeDisplay;
        }

        public AdditionalContextGrouperEntry(String conditionGrouperUrl, String conditionGrouperTitle, String reportingSpecificationGrouperIdentifier, String reportingSpecificationGrouperTitle, String codeSystemUrl, String code, String codeDisplay) {
            super(conditionGrouperUrl, conditionGrouperTitle, reportingSpecificationGrouperTitle);
//            this.conditionGrouperUrl = conditionGrouperUrl;
//            this.conditionGrouperTitle = conditionGrouperTitle;
//            this.reportingSpecificationGrouperTitle = reportingSpecificationGrouperTitle;
            this.reportingSpecificationGrouperIdentifier = reportingSpecificationGrouperIdentifier;
            this.codeSystemUrl = codeSystemUrl;
            this.code = code;
            this.codeDisplay = codeDisplay;
        }
    }
}