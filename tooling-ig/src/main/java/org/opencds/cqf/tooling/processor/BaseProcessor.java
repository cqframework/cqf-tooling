package org.opencds.cqf.tooling.processor;

import org.cqframework.fhir.npm.LibraryLoader;
import org.cqframework.fhir.npm.NpmPackageManager;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.fhir.ucum.UcumService;
import org.hl7.fhir.convertors.advisors.impl.BaseAdvisor_30_50;
import org.hl7.fhir.convertors.advisors.impl.BaseAdvisor_40_50;
import org.hl7.fhir.convertors.conv30_50.VersionConvertor_30_50;
import org.hl7.fhir.convertors.conv40_50.VersionConvertor_40_50;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r5.context.ILoggingService;
import org.hl7.fhir.r5.model.ImplementationGuide;
import org.hl7.fhir.utilities.IniFile;
import org.hl7.fhir.utilities.TextFile;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.VersionUtilities;
import org.hl7.fhir.utilities.npm.NpmPackage;
import org.opencds.cqf.tooling.exception.IGInitializationException;
import org.opencds.cqf.tooling.utilities.IGUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class BaseProcessor implements IProcessorContext, ILoggingService {

    protected static final Logger logger = LoggerFactory.getLogger(BaseProcessor.class);

    protected String rootDir;

    public String getRootDir() {
        return rootDir;
    }

    protected ImplementationGuide sourceIg;

    public ImplementationGuide getSourceIg() {
        return sourceIg;
    }

    protected String fhirVersion;

    public String getFhirVersion() {
        return fhirVersion;
    }

    protected String packageId;

    public String getPackageId() {
        return packageId;
    }

    protected String canonicalBase;

    public String getCanonicalBase() {
        return canonicalBase;
    }

    protected UcumService ucumService;

    public UcumService getUcumService() {
        return ucumService;
    }

    protected NpmPackageManager packageManager;

    public NpmPackageManager getPackageManager() {
        return packageManager;
    }

    protected IProcessorContext parentContext;

    public Boolean verboseMessaging = false;

    public Boolean getVerboseMessaging() {
        return verboseMessaging;
    }

    public void initialize(IProcessorContext context) {
        this.parentContext = context;

        if (parentContext != null) {
            this.rootDir = parentContext.getRootDir();
            this.sourceIg = parentContext.getSourceIg();
            this.fhirVersion = parentContext.getFhirVersion();
            this.canonicalBase = parentContext.getCanonicalBase();
            this.packageId = parentContext.getPackageId();
            this.ucumService = parentContext.getUcumService();
            this.packageManager = parentContext.getPackageManager();
            this.binaryPaths = parentContext.getBinaryPaths();
            this.cqlProcessor = parentContext.getCqlProcessor();
            this.verboseMessaging = parentContext.getVerboseMessaging();
        }
    }

    public void initializeFromIg(String rootDir, String igPath, String fhirVersion) {
        this.rootDir = rootDir;

        try {
            igPath = Utilities.path(rootDir, igPath);
        } catch (IOException e) {
            var message = String.format(
                    "Exceptions occurred creating igPath from source rootDir: %s, and igPath: %s", rootDir, igPath);
            logMessage(message);
            throw new IGInitializationException(message, e);
        }

        if (fhirVersion != null) {
            loadSourceIG(igPath, fhirVersion);
        } else {
            loadSourceIG(igPath);
        }

        // TODO: Perhaps we should validate the passed in fhirVersion against the
        // fhirVersion in the IG?

        this.fhirVersion = sourceIg.getFhirVersion().get(0).getCode();
        packageId = sourceIg.getPackageId();
        canonicalBase = determineCanonical(sourceIg.getUrl());
        packageManager = new NpmPackageManager(sourceIg);

        // Setup binary paths (cql source directories)
        binaryPaths = IGUtils.extractBinaryPaths(rootDir, sourceIg);
    }

    /*
     * Initializes from an ig.ini file in the root directory
     */
    public void initializeFromIni(String iniFile) {
        var ini = new IniFile(new File(iniFile).getAbsolutePath());
        String iniRootDir;
        try {
            iniRootDir = Utilities.getDirectoryForFile(ini.getFileName());
        } catch (IOException ioe) {
            logger.error("Error determining containing directory for INI file", ioe);
            throw new IGInitializationException("Error determining containing directory for INI file", ioe);
        }
        var igPath = ini.getStringProperty("IG", "ig");
        var specifiedFhirVersion = ini.getStringProperty("IG", "fhir-version");
        if (specifiedFhirVersion == null || specifiedFhirVersion == "") {
            logMessage("fhir-version was not specified in the ini file. Trying FHIR version 4.0.1");
            specifiedFhirVersion = "4.0.1";
        }

        initializeFromIg(iniRootDir, igPath, specifiedFhirVersion);
    }

    public void initializeFromIni(Path iniPath) throws FileNotFoundException {
        var iniFile = iniPath.toFile();
        var ini = new IniFile(new FileInputStream(iniPath.toFile()));
        String iniRootDir;
        iniRootDir = iniPath.getParent().toString();
        var igPath = ini.getStringProperty("IG", "ig");
        var specifiedFhirVersion = ini.getStringProperty("IG", "fhir-version");
        if (specifiedFhirVersion == null || specifiedFhirVersion == "") {
            logMessage("fhir-version was not specified in the ini file. Trying FHIR version 4.0.1");
            specifiedFhirVersion = "4.0.1";
        }

        initializeFromIg(iniRootDir, igPath, specifiedFhirVersion);
    }

    private List<String> binaryPaths;

    public List<String> getBinaryPaths() {
        return binaryPaths;
    }

    protected void setBinaryPaths(List<String> binaryPaths) {
        this.binaryPaths = binaryPaths;
    }

    private CqlProcessor cqlProcessor;

    public CqlProcessor getCqlProcessor() {
        if (cqlProcessor == null) {
            var reader = new LibraryLoader(fhirVersion);
            try {
                ucumService = new UcumEssenceService(UcumEssenceService.class.getResourceAsStream("/ucum-essence.xml"));
            } catch (UcumException e) {
                throw new IGInitializationException("Could not create UCUM validation service", e);
            }
            if (packageManager == null) {
                throw new IllegalStateException("packageManager is null. It should be initialized at this point.");
            }
            cqlProcessor = new CqlProcessor(new CopyOnWriteArrayList<>(cleanPackageList(packageManager.getNpmList())),
                    new CopyOnWriteArrayList<>(binaryPaths), reader, this, ucumService,
                    packageId, canonicalBase, verboseMessaging);
        }

        return cqlProcessor;
    }

    private List<NpmPackage> cleanPackageList(List<NpmPackage> originalPackageList) {
        Set<String> pathSet = new HashSet<>();
        return originalPackageList.stream().filter(e -> pathSet.add(e.getPath()))
                .collect(Collectors.toList());
    }

    private ImplementationGuide loadSourceIG(String igPath) {
        try {
            try {
                sourceIg = (ImplementationGuide) org.hl7.fhir.r5.formats.FormatUtilities.loadFile(igPath);
            } catch (IOException | FHIRException e) {
                try {
                    var versionConvertor_40_50 = new VersionConvertor_40_50(new BaseAdvisor_40_50());
                    sourceIg = (ImplementationGuide) versionConvertor_40_50
                            .convertResource(org.hl7.fhir.r4.formats.FormatUtilities.loadFile(igPath));
                } catch (IOException | FHIRException ex) {
                    var src = TextFile.fileToBytes(igPath);
                    var fmt = org.hl7.fhir.r5.formats.FormatUtilities.determineFormat(src);

                    var parser = org.hl7.fhir.dstu3.formats.FormatUtilities.makeParser(fmt.toString());
                    var versionConvertor_30_50 = new VersionConvertor_30_50(new BaseAdvisor_30_50());
                    sourceIg = (ImplementationGuide) versionConvertor_30_50.convertResource(parser.parse(src));
                }
            }
        } catch (IOException | FHIRException e) {
            throw new IGInitializationException(String.format("error initializing IG from igPath: %s", igPath), e);
        }

        return sourceIg;
    }

    private ImplementationGuide loadSourceIG(String igPath, String specifiedFhirVersion) {
        try {
            if (VersionUtilities.isR3Ver(specifiedFhirVersion)) {
                var src = TextFile.fileToBytes(igPath);
                var fmt = org.hl7.fhir.r5.formats.FormatUtilities.determineFormat(src);
                var parser = org.hl7.fhir.dstu3.formats.FormatUtilities.makeParser(fmt.toString());
                var versionConvertor_30_50 = new VersionConvertor_30_50(new BaseAdvisor_30_50());
                sourceIg = (ImplementationGuide) versionConvertor_30_50.convertResource(parser.parse(src));
            } else if (VersionUtilities.isR4Ver(specifiedFhirVersion)) {
                var res = org.hl7.fhir.r4.formats.FormatUtilities.loadFile(igPath);
                var versionConvertor_40_50 = new VersionConvertor_40_50(new BaseAdvisor_40_50());
                sourceIg = (ImplementationGuide) versionConvertor_40_50.convertResource(res);
            } else if (VersionUtilities.isR5Ver(specifiedFhirVersion)) {
                sourceIg = (ImplementationGuide) org.hl7.fhir.r5.formats.FormatUtilities.loadFile(igPath);
            } else {
                throw new FHIRException("Unknown Version '" + specifiedFhirVersion + "'");
            }
        } catch (IOException e) {
            var message = String.format("Exceptions occurred loading IG path: %s", igPath);
            logMessage(message);
            throw new IGInitializationException(message, e);
        }

        return sourceIg;
    }

    private String determineCanonical(String url) {
        if (url == null)
            return url;
        if (url.contains("/ImplementationGuide/"))
            return url.substring(0, url.indexOf("/ImplementationGuide/"));
        return url;
    }

    @Override
    public void logMessage(String msg) {
        logger.info(msg);
    }

    @Override
    public void logDebugMessage(ILoggingService.LogCategory category, String msg) {
        logger.debug("Category: {} Message: {}", category.name(), msg);
    }

    @Override
    public boolean isDebugLogging() {
        return false;
    }
}
