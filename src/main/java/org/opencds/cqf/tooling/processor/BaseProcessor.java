package org.opencds.cqf.tooling.processor;

import java.io.File;
import java.io.IOException;

import org.fhir.ucum.UcumService;
import org.hl7.fhir.convertors.VersionConvertor_40_50;
import org.hl7.fhir.r4.formats.FormatUtilities;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.model.ImplementationGuide;
import org.hl7.fhir.utilities.IniFile;
import org.hl7.fhir.utilities.Utilities;
import org.opencds.cqf.tooling.npm.NpmPackageManager;

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
        }
    }

    public void initialize(String rootDir, String igPath) {
        this.rootDir = rootDir;

        try {
            igPath = Utilities.path(rootDir, igPath);
        }
        catch (IOException e) {
            logMessage(String.format("Exceptions occurred extracting path from ig", e.getMessage()));
        }

        try {
            sourceIg = (ImplementationGuide) VersionConvertor_40_50.convertResource(FormatUtilities.loadFile(igPath));
        }
        catch (IOException e) {
            logMessage(String.format("Exceptions occurred loading IG file", e.getMessage()));
        }
        fhirVersion = sourceIg.getFhirVersion().get(0).getCode();
        packageId = sourceIg.getPackageId();
        canonicalBase = determineCanonical(sourceIg.getUrl());
        try {
            packageManager = new NpmPackageManager(sourceIg, fhirVersion);
        }
        catch (IOException e) {
            logMessage(String.format("Exceptions occurred loading npm package manager:", e.getMessage()));
        }
    }

    /*
    Initializes from an ig.ini file in the root directory
     */
    public void initialize(String iniFile) {
        IniFile ini = new IniFile(new File(iniFile).getAbsolutePath());
        String rootDir = Utilities.getDirectoryForFile(ini.getFileName());
        String igPath = ini.getStringProperty("IG", "ig");
        initialize(rootDir, igPath);
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
