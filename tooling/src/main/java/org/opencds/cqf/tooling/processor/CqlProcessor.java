package org.opencds.cqf.tooling.processor;

import org.cqframework.cql.cql2elm.*;
import org.cqframework.cql.cql2elm.model.CompiledLibrary;
import org.cqframework.cql.cql2elm.quick.FhirLibrarySourceProvider;
import org.cqframework.cql.elm.requirements.fhir.DataRequirementsProcessor;
import org.cqframework.cql.elm.tracking.TrackBack;
import org.cqframework.fhir.npm.ILibraryReader;
import org.cqframework.fhir.npm.NpmLibrarySourceProvider;
import org.cqframework.fhir.npm.NpmModelInfoProvider;
import org.fhir.ucum.UcumService;
import org.hl7.cql.model.NamespaceInfo;
import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r5.context.ILoggingService;
import org.hl7.fhir.r5.model.DataRequirement;
import org.hl7.fhir.r5.model.ParameterDefinition;
import org.hl7.fhir.r5.model.RelatedArtifact;
import org.hl7.fhir.utilities.npm.NpmPackage;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueType;
import org.opencds.cqf.tooling.utilities.ResourceUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class CqlProcessor {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(CqlProcessor.class);

    /**
     * information about a cql file
     */
    public class CqlSourceFileInformation {
        private final String path;
        private CqlTranslatorOptions options;
        private VersionedIdentifier identifier;
        private byte[] cql;
        private byte[] elm;
        private byte[] jsonElm;
        private List<ValidationMessage> errors = new ArrayList<>();
        private List<RelatedArtifact> relatedArtifacts = new ArrayList<>();
        private List<DataRequirement> dataRequirements = new ArrayList<>();
        private List<ParameterDefinition> parameters = new ArrayList<>();

        public CqlSourceFileInformation(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public CqlTranslatorOptions getOptions() {
            return options;
        }
        public void setOptions(CqlTranslatorOptions options) {
            this.options = options;
        }
        public VersionedIdentifier getIdentifier() {
            return identifier;
        }
        public void setIdentifier(VersionedIdentifier identifier) {
            this.identifier = identifier;
        }
        public byte[] getCql() {
            return cql;
        }
        public void setCql(byte[] cql) {
            this.cql = cql;
        }
        public byte[] getElm() {
            return elm;
        }
        public void setElm(byte[] elm) {
            this.elm = elm;
        }
        public byte[] getJsonElm() {
            return jsonElm;
        }
        public void setJsonElm(byte[] jsonElm) {
            this.jsonElm = jsonElm;
        }
        public List<ValidationMessage> getErrors() {
            return errors;
        }
        public List<RelatedArtifact> getRelatedArtifacts() {
            return relatedArtifacts;
        }
        public List<DataRequirement> getDataRequirements() {
            return dataRequirements;
        }
        public List<ParameterDefinition> getParameters() {
            return parameters;
        }
    }

    /**
     * all the NPM packages this IG depends on (including base).
     * This list is in a maintained order such that you can just
     * do for (NpmPackage p : packages) and that will resolve the
     * library in the right order
     *
     */
    private List<NpmPackage> packages;

    /**
     * All the file paths cql files might be found in (absolute local file paths)
     *
     * will be at least one error
     */
    private List<String> folders;

    /**
     * Version indepedent reader
     */
    private ILibraryReader reader;

    /**
     * use this to write to the standard IG log
     */
    private ILoggingService logger;

    /**
     * UcumService used by the translator to validate UCUM units
     */
    private UcumService ucumService;

    /**
     * Map of translated files by fully qualified file name.
     * Populated during execute
     */
    private Map<String, CqlSourceFileInformation> fileMap;

    /**
     * The packageId for the implementation guide, used to construct a NamespaceInfo for the CQL translator
     * Libraries that don't specify a namespace will be built in this namespace
     * Libraries can specify a namespace, but must use this name to do it
     */
    @SuppressWarnings("unused")
    private String packageId;

    /**
     * The canonical base of the IG, used to construct a NamespaceInfo for the CQL translator
     * Libraries translated in this IG will have this namespaceUri as their system
     * Library resources published in this IG will then have URLs of [canonicalBase]/Library/[libraryName]
     */
    @SuppressWarnings("unused")
    private String canonicalBase;

    private NamespaceInfo namespaceInfo;

    private boolean verboseMessaging;

    public CqlProcessor(List<NpmPackage> packages, List<String> folders, ILibraryReader reader, ILoggingService logger, UcumService ucumService, String packageId, String canonicalBase, Boolean verboseMessaging) {
        super();
        this.packages = packages;
        this.folders = folders;
        this.reader = reader;
        this.logger = logger;
        this.ucumService = ucumService;
        this.packageId = packageId;
        this.canonicalBase = canonicalBase;
        if (packageId != null && !packageId.isEmpty() && canonicalBase != null && !canonicalBase.isEmpty()) {
            this.namespaceInfo = new NamespaceInfo(packageId, canonicalBase);
        }
        this.verboseMessaging = verboseMessaging;
    }

    /**
     * Do the compile. Do not return any exceptions related to content; only throw exceptions for infrastructural issues
     *
     * note that it's not an error if there's no .cql files - this is called without checking for their existence
     *
     * Any exception will stop the build cold.
     */
    public void execute() throws FHIRException {
        try {
            fileMap = new HashMap<>();

            // foreach folder
            for (String folder : folders) {
                translateFolder(folder);
            }
        }
        catch (Exception E) {
            logger.logMessage(String.format("Errors occurred attempting to translate CQL content: %s", E.getMessage()));
        }
    }

    /**
     * Return CqlSourceFileInformation for the given filename
     * @param filename Fully qualified name of the source file
     * @return
     */
    public CqlSourceFileInformation getFileInformation(String filename) {
        if (filename == null) {
            return null;
        }

        if (fileMap == null) {
            throw new IllegalStateException("CQL File map is not available, execute has not been called");
        }

        if (!fileMap.containsKey(filename)) {
            for (Map.Entry<String, CqlSourceFileInformation> entry: fileMap.entrySet()) {
                if (filename.equalsIgnoreCase(entry.getKey())) {
                    logger.logDebugMessage(ILoggingService.LogCategory.PROGRESS, String.format("File with a similar name but different casing was found. File found: '%s'", entry.getKey()));
                }
            }
            return null;
        }

        return this.fileMap.remove(filename);
    }

    public Collection<CqlSourceFileInformation> getAllFileInformation() {
        if (fileMap == null) {
            throw new IllegalStateException("CQL File map is not available, execute has not been called");
        }

        return this.fileMap.values();
    }

    public Map<String, CqlSourceFileInformation> getFileMap() {
        return this.fileMap;
    }

    /**
     * Called at the end after all getFileInformation have been called
     * return any errors that didn't have any particular home, and also
     * errors for any files that were linked but haven't been accessed using
     * getFileInformation - these have been omitted from the IG, and that's
     * an error
     *
     * @return
     */
    public List<ValidationMessage> getGeneralErrors() {
        List<ValidationMessage> result = new ArrayList<>();

        if (fileMap != null) {
            for (Map.Entry<String, CqlSourceFileInformation> entry : fileMap.entrySet()) {
                result.add(new ValidationMessage(ValidationMessage.Source.Publisher, ValidationMessage.IssueType.PROCESSING, entry.getKey(), "CQL source was not associated with a library resource in the IG.", ValidationMessage.IssueSeverity.ERROR));
            }
        }

        return result;
    }

    private void checkCachedManager() {
        if (cachedOptions == null) {
            if (hasMultipleBinaryPaths) {
                throw new RuntimeException("CqlProcessor has been used with multiple Cql paths, ambiguous options and manager");
            }
            else {
                throw new RuntimeException("CqlProcessor has not been executed, no cached options or manager");
            }
        }
    }

    private boolean hasMultipleBinaryPaths = false;
    private CqlTranslatorOptions cachedOptions;
    public CqlTranslatorOptions getCqlTranslatorOptions() {
        checkCachedManager();
        return cachedOptions;
    }

    private LibraryManager cachedLibraryManager;
    public LibraryManager getLibraryManager() {
        checkCachedManager();
        return cachedLibraryManager;
    }

    private void translateFolder(String folder) {
        System.out.printf("Translating CQL source in folder %s%n", folder);

        CqlTranslatorOptions options = ResourceUtils.getTranslatorOptions(folder);

        // Setup
        // Construct DefaultLibrarySourceProvider
        // Construct FhirLibrarySourceProvider
        ModelManager modelManager = new ModelManager();
        LibraryManager libraryManager = new LibraryManager(modelManager, options.getCqlCompilerOptions());
        if (options.getCqlCompilerOptions().getValidateUnits()) {
            libraryManager.setUcumService(ucumService);
        }
        if (packages != null) {
            modelManager.getModelInfoLoader().registerModelInfoProvider(new NpmModelInfoProvider(packages, reader, logger), true);
            libraryManager.getLibrarySourceLoader().registerProvider(new NpmLibrarySourceProvider(packages, reader, logger));
        }
        libraryManager.getLibrarySourceLoader().registerProvider(new DefaultLibrarySourceProvider(Paths.get(folder)));
        libraryManager.getLibrarySourceLoader().registerProvider(new FhirLibrarySourceProvider());
        modelManager.getModelInfoLoader().registerModelInfoProvider(new DefaultModelInfoProvider(Paths.get(folder)));

        loadNamespaces(libraryManager);

        // foreach *.cql file
        boolean hadCqlFiles = false;
        for (File file : new File(folder).listFiles(getCqlFilenameFilter())) {
            hadCqlFiles = true;
            translateFile(libraryManager, file, options.getCqlCompilerOptions());
        }

        if (hadCqlFiles) {
            if (cachedOptions == null) {
                if (!hasMultipleBinaryPaths) {
                    cachedOptions = options;
                    cachedLibraryManager = libraryManager;
                }
            }
            else {
                if (!hasMultipleBinaryPaths) {
                    hasMultipleBinaryPaths = true;
                    cachedOptions = null;
                    cachedLibraryManager = null;
                }
            }
        }
    }

    private void loadNamespaces(LibraryManager libraryManager) {
        if (namespaceInfo != null) {
            libraryManager.getNamespaceManager().addNamespace(namespaceInfo);
        }

        if (packages != null) {
            for (NpmPackage p : packages) {
                if (p.name() != null && !p.name().isEmpty() && p.canonical() != null && !p.canonical().isEmpty()) {
                    NamespaceInfo ni = new NamespaceInfo(p.name(), p.canonical());
                    if (libraryManager.getNamespaceManager().resolveNamespaceUri(ni.getName()) != null) {
                        logger.logMessage(String.format("Skipped loading namespace info for name %s because it is already registered", ni.getName()));
                    }
                    else if (libraryManager.getNamespaceManager().getNamespaceInfoFromUri(ni.getUri()) != null) {
                        logger.logMessage(String.format("Skipped loading namespace info for uri %s because it is already registered", ni.getUri()));
                    }
                    else {
                        libraryManager.getNamespaceManager().addNamespace(ni);
                    }
                }
            }
        }
    }

    public static ValidationMessage.IssueType severityToIssueType(CqlCompilerException.ErrorSeverity severity) {
        switch (severity) {
            case Info: return ValidationMessage.IssueType.INFORMATIONAL;
            case Warning:
            case Error: return ValidationMessage.IssueType.PROCESSING;
            default: return ValidationMessage.IssueType.UNKNOWN;
        }
    }

    public static ValidationMessage.IssueSeverity severityToIssueSeverity(CqlCompilerException.ErrorSeverity severity) {
        switch (severity) {
            case Info: return ValidationMessage.IssueSeverity.INFORMATION;
            case Warning: return ValidationMessage.IssueSeverity.WARNING;
            case Error: return ValidationMessage.IssueSeverity.ERROR;
            default: return ValidationMessage.IssueSeverity.NULL;
        }
    }

    public static ValidationMessage exceptionToValidationMessage(File file, CqlCompilerException exception) {
        TrackBack tb = exception.getLocator();
        if (tb != null) {
            return new ValidationMessage(ValidationMessage.Source.Publisher, severityToIssueType(exception.getSeverity()),
                    tb.getStartLine(), tb.getStartChar(), tb.getLibrary().getId(), exception.getMessage(),
                    severityToIssueSeverity(exception.getSeverity()));
        }
        else {
            return new ValidationMessage(ValidationMessage.Source.Publisher, severityToIssueType(exception.getSeverity()),
                    file.toString(), exception.getMessage(), severityToIssueSeverity(exception.getSeverity()));
        }
    }

    private void translateFile(LibraryManager libraryManager, File file, CqlCompilerOptions options) {
//        logger.logMessage(String.format("Translating CQL source in file %s", file.toString()));
        CqlSourceFileInformation result = new CqlSourceFileInformation(file.getAbsolutePath());
        fileMap.put(file.getAbsoluteFile().toString(), result);

        if (options.getValidateUnits()) {
            libraryManager.setUcumService(ucumService);
        }

        try {

            // translate toXML
            CqlTranslator translator = CqlTranslator.fromFile(namespaceInfo, file, libraryManager);

            // record errors and warnings
            for (CqlCompilerException exception : translator.getExceptions()) {
                result.getErrors().add(exceptionToValidationMessage(file, exception));
            }

            List<CqlCompilerException> severeErrorList = listBySeverity(translator.getErrors(), CqlCompilerException.ErrorSeverity.Error);


            if (!severeErrorList.isEmpty()) {
                var messages = severeErrorList.stream().map(x -> x.getMessage()).reduce("", (x, y) -> x + "\n" + y);
                log.warn("CQL Processing failed with errors count: {}, messages: {}", severeErrorList.size(), messages);
                result.getErrors().add(new ValidationMessage(ValidationMessage.Source.Publisher, IssueType.EXCEPTION, file.getName(),
                        String.format("CQL Processing failed with (%d) errors.", translator.getErrors().size()), IssueSeverity.ERROR));
            }
            else {
                try {
                    result.setOptions(new CqlTranslatorOptions().withCqlCompilerOptions(options));
                    // convert to base64 bytes
                    // NOTE: Publication tooling requires XML content
                    result.setCql(Files.readAllBytes(file.toPath()));
                    result.setElm(translator.toXml().getBytes());
                    result.setIdentifier(translator.toELM().getIdentifier());
                    result.setJsonElm(translator.toJson().getBytes());

                    // Add the translated library to the library manager (NOTE: This should be a "cacheLibrary" call on the LibraryManager, available in 1.5.3+)
                    // Without this, the data requirements processor will try to load the current library, resulting in a re-translation
                    CompiledLibrary compiledLibrary = translator.getTranslatedLibrary();
                    libraryManager.getCompiledLibraries().put(compiledLibrary.getIdentifier(), compiledLibrary);

                    DataRequirementsProcessor drp = new DataRequirementsProcessor();
                    org.hl7.fhir.r5.model.Library requirementsLibrary =
                            drp.gatherDataRequirements(libraryManager, translator.getTranslatedLibrary(), options, null, false, false);

                    // TODO: Report context, requires 1.5 translator (ContextDef)
                    // NOTE: In STU3, only Patient context is supported

                    // TODO: Extract direct reference code data
                    //result.extension.addAll(requirementsLibrary.getExtensionsByUrl("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-directReferenceCode"));

                    // Extract relatedArtifact data (models, libraries, code systems, and value sets)
                    for (RelatedArtifact relatedArtifact : requirementsLibrary.getRelatedArtifact()) {
                        // NOTE: see similar logic in MeasureRefreshProcessor.removeModelInfoDependencies
                        if (relatedArtifact.hasResource() && (
                                relatedArtifact.getResource().startsWith("http://hl7.org/fhir/Library/QICore-ModelInfo")
                                    || relatedArtifact.getResource().startsWith("http://fhir.org/guides/cqf/common/Library/FHIR-ModelInfo")
                                    || relatedArtifact.getResource().startsWith("http://hl7.org/fhir/Library/USCore-ModelInfo")
                        )) {
                            // Do not report dependencies on model info loaded from the translator, or
                            // from CQF Common (because these should be loaded from Using CQL now)
                            continue;
                        }

                        result.relatedArtifacts.add(relatedArtifact);
                    }

                    // Extract parameter data and validate result types are supported types
                    result.parameters.addAll(requirementsLibrary.getParameter());
                    for (ValidationMessage paramMessage : drp.getValidationMessages()) {
                        result.getErrors().add(new ValidationMessage(paramMessage.getSource(), paramMessage.getType(), file.getName(),
                                paramMessage.getMessage(), paramMessage.getLevel()));
                    }

                    // Extract dataRequirement data
                    result.dataRequirements.addAll(requirementsLibrary.getDataRequirement());

                } catch (Exception ex) {
                    logger.logMessage(String.format("CQL Translation succeeded for file: '%s', but ELM generation failed with the following error: %s", file.getAbsolutePath(), ex.getMessage()));
                }
            }

            //output Success/Warn/Info/Fail message to user:
            logger.logMessage(buildStatusMessage(translator.getErrors(), file.getName(), verboseMessaging));
        }
        catch (Exception e) {
            result.getErrors().add(new ValidationMessage(ValidationMessage.Source.Publisher, IssueType.EXCEPTION, file.getName(), "CQL Processing failed with exception: "+e.getMessage(), IssueSeverity.ERROR));
        }


    }

    private FilenameFilter getCqlFilenameFilter() {
        return new FilenameFilter() {
            @Override
            public boolean accept(File path, String name) {
                return name.endsWith(".cql");
            }
        };
    }


    private static List<String> listTranslatorErrors(List<CqlCompilerException> translatorErrors) {
        List<String> errors = new ArrayList<>();
        for (CqlCompilerException error : translatorErrors) {
            errors.add(error.getSeverity().toString() + ": " +
                    (error.getLocator() == null ? "" : String.format("[%d:%d, %d:%d] ",
                            error.getLocator().getStartLine(),
                            error.getLocator().getStartChar(),
                            error.getLocator().getEndLine(),
                            error.getLocator().getEndChar()))
                    + error.getMessage().replace("\n", "").replace("\r", ""));
        }
        Collections.sort(errors);
        return errors;
    }

    private static List<CqlCompilerException> listBySeverity(List<CqlCompilerException> errors, CqlCompilerException.ErrorSeverity errorSeverity) {
        return errors.stream()
                .filter(exception -> exception.getSeverity() == errorSeverity)
                .collect(Collectors.toList());
    }

    public static String buildStatusMessage(List<CqlCompilerException> errors, String resourceName, boolean verboseMessaging){
        return buildStatusMessage(errors, resourceName, verboseMessaging, true, "\n\t");
    }

    public static String buildStatusMessage(List<CqlCompilerException> errors, String resourceName, boolean verboseMessaging, boolean withStatusIndicator, String delimiter){
        String successMsg = "[SUCCESS] CQL Processing of ";
        String statusIndicatorMinor = " completed successfully";
        String statusIndicator;

        //empty list means no errors, so success
        if (errors == null || errors.isEmpty()){
            return successMsg + resourceName + statusIndicatorMinor;
        }

        //separate out exceptions by their severity to determine the messaging to the user:
        List<CqlCompilerException> infosList = listBySeverity(errors, CqlCompilerException.ErrorSeverity.Info);
        List<CqlCompilerException> warningsList = listBySeverity(errors, CqlCompilerException.ErrorSeverity.Warning);
        List<CqlCompilerException> errorList = listBySeverity(errors, CqlCompilerException.ErrorSeverity.Error);

        if (!errorList.isEmpty()) {
            statusIndicator = "[FAIL] ";
            statusIndicatorMinor = " failed";
        } else if (!warningsList.isEmpty()) {
            statusIndicator = "[WARN] ";
        } else if (!infosList.isEmpty()) {
            statusIndicator = "[INFO] ";
        } else {
            return successMsg + resourceName + statusIndicatorMinor;
        }
        List<String> fullSortedList = new ArrayList<>();
        fullSortedList.addAll(CqlProcessor.listTranslatorErrors(infosList));
        fullSortedList.addAll(CqlProcessor.listTranslatorErrors(warningsList));
        fullSortedList.addAll(CqlProcessor.listTranslatorErrors(errorList));
        Collections.sort(fullSortedList);
        String fullSortedListMsg = String.join(delimiter, fullSortedList);

        String errorsStatus =  errorList.size() + " Error(s)" ;
        String infoStatus =  infosList.size() + " Information Message(s)" ;
        String warningStatus =  warningsList.size() + " Warning(s)" ;

        return (withStatusIndicator ? statusIndicator : "") + "CQL Processing of " + resourceName + statusIndicatorMinor + " with " + errorsStatus + ", "
                +  warningStatus + ", and " + infoStatus + (verboseMessaging ? ": " + delimiter + fullSortedListMsg : "");
    }

    public static boolean hasSevereErrors(List<CqlCompilerException> errors) {
        return errors.stream().anyMatch(error -> error.getSeverity() == CqlCompilerException.ErrorSeverity.Error);
    }
}
