package org.opencds.cqf.tooling.processor;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.fhir.ucum.UcumService;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r5.elementmodel.Manager;
import org.hl7.fhir.r5.model.ImplementationGuide;
import org.hl7.fhir.convertors.VersionConvertor_30_50;
import org.hl7.fhir.convertors.VersionConvertor_40_50;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.utilities.IniFile;
import org.hl7.fhir.utilities.TextFile;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.VersionUtilities;
import org.opencds.cqf.tooling.npm.LibraryLoader;
import org.opencds.cqf.tooling.npm.NpmPackageManager;
import org.opencds.cqf.tooling.utilities.IGUtils;

public class BaseProcessor implements IProcessorContext, IWorkerContext.ILoggingService {

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
        }
    }

    public void initializeFromIg(String rootDir, String igPath, String fhirVersion) {
        this.rootDir = rootDir;

        try {
            igPath = Utilities.path(rootDir, igPath);
        }
        catch (IOException e) {
            logMessage(String.format("Exceptions occurred extracting path from ig", e.getMessage()));
        }

        if (fhirVersion != null) {
            loadSourceIG(igPath, fhirVersion);
        } else {
            try {
                loadSourceIG(igPath);
            }
            catch (Exception e) {
                logMessage("Error Parsing File " + igPath + ": " + e.getMessage());
            }
        }

        // TODO: Perhaps we should validate the passed in fhirVersion against the fhirVersion in the IG?

        this.fhirVersion = sourceIg.getFhirVersion().get(0).getCode();
        packageId = sourceIg.getPackageId();
        canonicalBase = determineCanonical(sourceIg.getUrl());
        try {
            packageManager = new NpmPackageManager(sourceIg, this.fhirVersion);
        }
        catch (IOException e) {
            logMessage(String.format("Exceptions occurred loading npm package manager:", e.getMessage()));
        }

        // Setup binary paths (cql source directories)
        try {
            binaryPaths = IGUtils.extractBinaryPaths(rootDir, sourceIg);
        }
        catch (IOException e) {
            logMessage(String.format("Errors occurred extracting binary path from IG: ", e.getMessage()));
            throw new IllegalArgumentException("Could not obtain binary path from IG");
        }
    }

    /*
    Initializes from an ig.ini file in the root directory
     */
    public void initializeFromIni(String iniFile) {
        IniFile ini = new IniFile(new File(iniFile).getAbsolutePath());
        String rootDir = Utilities.getDirectoryForFile(ini.getFileName());
        String igPath = ini.getStringProperty("IG", "ig");
        String specifiedFhirVersion = ini.getStringProperty("IG", "fhir-version");
        if (specifiedFhirVersion == null || specifiedFhirVersion == "") {
            logMessage("fhir-version was not specified in the ini file. Trying FHIR version 4.0.1");
            specifiedFhirVersion = "4.0.1";
        }
        try {
            initializeFromIg(rootDir, igPath, specifiedFhirVersion);
        }
        catch (Exception e) {
            logMessage(String.format("Exceptions occurred initializing refresh from ini file '%s':%s", iniFile, e.getMessage()));
        }
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
            LibraryLoader reader = new LibraryLoader(fhirVersion);
            try {
                ucumService = new UcumEssenceService(UcumEssenceService.class.getResourceAsStream("/ucum-essence.xml"));
            } catch (UcumException e) {
                System.err.println("Could not create UCUM validation service:");
                e.printStackTrace();
            }
            if (packageManager == null) {
                throw new IllegalArgumentException("packageManager is null");
            }
            cqlProcessor = new CqlProcessor(packageManager.getNpmList(), binaryPaths, reader, this, ucumService,
                    packageId, canonicalBase);
        }

        return cqlProcessor;
    }

    private ImplementationGuide loadSourceIG(String igPath) throws Exception {
        try {
            try {
                sourceIg = (ImplementationGuide) org.hl7.fhir.r5.formats.FormatUtilities.loadFile(igPath);
            } catch (Exception e) {
                try {
                    sourceIg = (ImplementationGuide) VersionConvertor_40_50.convertResource(org.hl7.fhir.r4.formats.FormatUtilities.loadFile(igPath));
                } catch (Exception ex) {
                    byte[] src = TextFile.fileToBytes(igPath);
                    Manager.FhirFormat fmt = org.hl7.fhir.r5.formats.FormatUtilities.determineFormat(src);

                    org.hl7.fhir.dstu3.formats.ParserBase parser = org.hl7.fhir.dstu3.formats.FormatUtilities.makeParser(fmt.toString());
                    sourceIg = (ImplementationGuide) VersionConvertor_30_50.convertResource(parser.parse(src));
                }
            }
        } catch (Exception e) {
            throw new Exception("Error Parsing File " + igPath + ": " + e.getMessage(), e);
        }

        return sourceIg;
    }

    private ImplementationGuide loadSourceIG(String igPath, String specifiedFhirVersion) {
        try {
            if (VersionUtilities.isR3Ver(specifiedFhirVersion)) {
                byte[] src = TextFile.fileToBytes(igPath);
                Manager.FhirFormat fmt = org.hl7.fhir.r5.formats.FormatUtilities.determineFormat(src);
                org.hl7.fhir.dstu3.formats.ParserBase parser = org.hl7.fhir.dstu3.formats.FormatUtilities.makeParser(fmt.toString());
                sourceIg = (ImplementationGuide) VersionConvertor_30_50.convertResource(parser.parse(src));
            } else if (VersionUtilities.isR4Ver(specifiedFhirVersion)) {
                org.hl7.fhir.r4.model.Resource res = org.hl7.fhir.r4.formats.FormatUtilities.loadFile(igPath);
                sourceIg = (ImplementationGuide) VersionConvertor_40_50.convertResource(res);
            } else if (VersionUtilities.isR5Ver(specifiedFhirVersion)) {
                sourceIg = (ImplementationGuide) org.hl7.fhir.r5.formats.FormatUtilities.loadFile(igPath);
            } else {
                throw new FHIRException("Unknown Version '"+specifiedFhirVersion+"'");
            }
        }
        catch (IOException e) {
            logMessage(String.format("Exceptions occurred loading IG file", e.getMessage()));
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
        System.out.println(msg);
    }

    @Override
    public void logDebugMessage(IWorkerContext.ILoggingService.LogCategory category, String msg) {
        logMessage(msg);
    }
}
