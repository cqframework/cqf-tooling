package org.opencds.cqf.tooling.processor;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.cqframework.cql.cql2elm.*;
import org.cqframework.cql.cql2elm.model.TranslatedLibrary;
import org.cqframework.cql.elm.tracking.TrackBack;
import org.fhir.ucum.UcumService;
import org.hl7.cql.model.IntervalType;
import org.hl7.cql.model.ListType;
import org.hl7.cql.model.NamedType;
import org.hl7.elm.r1.AccessModifier;
import org.hl7.elm.r1.Code;
import org.hl7.elm.r1.CodeDef;
import org.hl7.elm.r1.CodeRef;
import org.hl7.elm.r1.CodeSystemDef;
import org.hl7.elm.r1.CodeSystemRef;
import org.hl7.elm.r1.Concept;
import org.hl7.elm.r1.ConceptDef;
import org.hl7.elm.r1.ConceptRef;
import org.hl7.elm.r1.Expression;
import org.hl7.elm.r1.ExpressionDef;
import org.hl7.elm.r1.FunctionDef;
import org.hl7.elm.r1.IncludeDef;
import org.hl7.elm.r1.ParameterDef;
import org.hl7.elm.r1.Retrieve;
import org.hl7.elm.r1.UsingDef;
import org.hl7.elm.r1.ValueSetDef;
import org.hl7.elm.r1.ValueSetRef;
import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r5.context.IWorkerContext.ILoggingService;
import org.hl7.fhir.r5.model.DataRequirement;
import org.hl7.fhir.r5.model.Enumerations;
import org.hl7.fhir.r5.model.ParameterDefinition;
import org.hl7.fhir.r5.model.RelatedArtifact;
import org.hl7.fhir.utilities.npm.NpmPackage;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueType;
import org.opencds.cqf.tooling.npm.ILibraryReader;
import org.opencds.cqf.tooling.npm.NpmLibrarySourceProvider;
import org.opencds.cqf.tooling.npm.NpmModelInfoProvider;
import org.opencds.cqf.tooling.utilities.ResourceUtils;

public class CqlProcessor {

    /**
     * information about a cql file
     */
    public class CqlSourceFileInformation {
        private VersionedIdentifier identifier;
        private byte[] elm;
        private byte[] jsonElm;
        private List<ValidationMessage> errors = new ArrayList<>();
        private List<RelatedArtifact> relatedArtifacts = new ArrayList<>();
        private List<DataRequirement> dataRequirements = new ArrayList<>();
        private List<ParameterDefinition> parameters = new ArrayList<>();
        public VersionedIdentifier getIdentifier() {
            return identifier;
        }
        public void setIdentifier(VersionedIdentifier identifier) {
            this.identifier = identifier;
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

    public CqlProcessor(List<NpmPackage> packages, List<String> folders, ILibraryReader reader, ILoggingService logger, UcumService ucumService, String packageId, String canonicalBase) {
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
            logger.logMessage("Translating CQL source");
            fileMap = new HashMap<>();

            // foreach folder
            for (String folder : folders) {
                translateFolder(folder);
            }
        }
        catch (Exception E) {
            logger.logDebugMessage(ILoggingService.LogCategory.PROGRESS, String.format("Errors occurred attempting to translate CQL content: %s", E.getMessage()));
        }
    }

    /**
     * Return CqlSourceFileInformation for the given filename
     * @param filename Fully qualified name of the source file
     * @return
     */
    public CqlSourceFileInformation getFileInformation(String filename) {
        if (fileMap == null) {
            throw new IllegalStateException("CQL File map is not available, execute has not been called");
        }

        if (!fileMap.containsKey(filename)) {
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
        logger.logMessage(String.format("Translating CQL source in folder %s", folder));

        CqlTranslatorOptions options = ResourceUtils.getTranslatorOptions(folder);

        // Setup
        // Construct DefaultLibrarySourceProvider
        // Construct FhirLibrarySourceProvider
        ModelManager modelManager = new ModelManager();
        LibraryManager libraryManager = new LibraryManager(modelManager);
        if (packages != null) {
            modelManager.getModelInfoLoader().registerModelInfoProvider(new NpmModelInfoProvider(packages, reader, logger), true);
            libraryManager.getLibrarySourceLoader().registerProvider(new NpmLibrarySourceProvider(packages, reader, logger));
        }
        libraryManager.getLibrarySourceLoader().registerProvider(new FhirLibrarySourceProvider());
        libraryManager.getLibrarySourceLoader().registerProvider(new DefaultLibrarySourceProvider(Paths.get(folder)));
        modelManager.getModelInfoLoader().registerModelInfoProvider(new DefaultModelInfoProvider(Paths.get(folder)));

        loadNamespaces(libraryManager);

        // foreach *.cql file
        boolean hadCqlFiles = false;
        for (File file : new File(folder).listFiles(getCqlFilenameFilter())) {
            hadCqlFiles = true;
            translateFile(modelManager, libraryManager, file, options);
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
                    libraryManager.getNamespaceManager().addNamespace(ni);
                }
            }
        }
    }

    public static ValidationMessage.IssueType severityToIssueType(CqlTranslatorException.ErrorSeverity severity) {
        switch (severity) {
            case Info: return ValidationMessage.IssueType.INFORMATIONAL;
            case Warning:
            case Error: return ValidationMessage.IssueType.PROCESSING;
            default: return ValidationMessage.IssueType.UNKNOWN;
        }
    }

    public static ValidationMessage.IssueSeverity severityToIssueSeverity(CqlTranslatorException.ErrorSeverity severity) {
        switch (severity) {
            case Info: return ValidationMessage.IssueSeverity.INFORMATION;
            case Warning: return ValidationMessage.IssueSeverity.WARNING;
            case Error: return ValidationMessage.IssueSeverity.ERROR;
            default: return ValidationMessage.IssueSeverity.NULL;
        }
    }

    public static ValidationMessage exceptionToValidationMessage(File file, CqlTranslatorException exception) {
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

    private void translateFile(ModelManager modelManager, LibraryManager libraryManager, File file, CqlTranslatorOptions options) {
        logger.logMessage(String.format("Translating CQL source in file %s", file.toString()));
        CqlSourceFileInformation result = new CqlSourceFileInformation();
        fileMap.put(file.getAbsoluteFile().toString(), result);

        try {

            // translate toXML
            CqlTranslator translator = CqlTranslator.fromFile(namespaceInfo, file, modelManager, libraryManager,
                    options.getValidateUnits() ? ucumService : null, options);

            // record errors and warnings
            for (CqlTranslatorException exception : translator.getExceptions()) {
                result.getErrors().add(exceptionToValidationMessage(file, exception));
            }

            if (translator.getErrors().size() > 0) {
                result.getErrors().add(new ValidationMessage(ValidationMessage.Source.Publisher, IssueType.EXCEPTION, file.getName(),
                        String.format("CQL Processing failed with (%d) errors.", translator.getErrors().size()), IssueSeverity.ERROR));
                logger.logMessage(String.format("Translation failed with (%d) errors; see the error log for more information.", translator.getErrors().size()));

                for (CqlTranslatorException error : translator.getErrors()) {
                    logger.logMessage(String.format("Error: %s", error.getMessage()));
                }
            }
            else {
                try {
                    // convert to base64 bytes
                    // NOTE: Publication tooling requires XML content
                    result.setElm(translator.toXml().getBytes());
                    result.setIdentifier(translator.toELM().getIdentifier());
                    if (options.getFormats().contains(CqlTranslator.Format.JSON)) {
                        result.setJsonElm(translator.toJson().getBytes());
                    }
                    if (options.getFormats().contains(CqlTranslator.Format.JXSON)) {
                        result.setJsonElm(translator.toJxson().getBytes());
                    }

                    // Add the translated library to the library manager (NOTE: This should be a "cacheLibrary" call on the LibraryManager, available in 1.5.3+)
                    // Without this, the data requirements processor will try to load the current library, resulting in a re-translation
                    TranslatedLibrary translatedLibrary = translator.getTranslatedLibrary();
                    String libraryPath = NamespaceManager.getPath(translatedLibrary.getIdentifier().getSystem(), translatedLibrary.getIdentifier().getId());
                    libraryManager.getTranslatedLibraries().put(libraryPath, translatedLibrary);

                    DataRequirementsProcessor drp = new DataRequirementsProcessor();
                    org.hl7.fhir.r5.model.Library requirementsLibrary =
                            drp.gatherDataRequirements(libraryManager, translator.getTranslatedLibrary(), options, null, false);

                    // TODO: Report context, requires 1.5 translator (ContextDef)
                    // NOTE: In STU3, only Patient context is supported

                    // TODO: Extract direct reference code data
                    //result.extension.addAll(requirementsLibrary.getExtensionsByUrl("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-directReferenceCode"));

                    // Extract relatedArtifact data (models, libraries, code systems, and value sets)
                    result.relatedArtifacts.addAll(requirementsLibrary.getRelatedArtifact());

                    // Extract parameter data and validate result types are supported types
                    result.parameters.addAll(requirementsLibrary.getParameter());
                    for (ValidationMessage paramMessage : drp.getValidationMessages()) {
                        result.getErrors().add(new ValidationMessage(paramMessage.getSource(), paramMessage.getType(), file.getName(),
                                paramMessage.getMessage(), paramMessage.getLevel()));
                    }

                    // Extract dataRequirement data
                    result.dataRequirements.addAll(requirementsLibrary.getDataRequirement());

                    logger.logMessage("CQL translation completed successfully.");
                } catch (Exception ex) {
                    logger.logMessage(String.format("CQL Translation succeeded for file: '%s', but ELM generation failed with the following error: %s", file.getAbsolutePath(), ex.getMessage()));
                }
            }
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
}
