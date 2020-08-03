package org.opencds.cqf.tooling.processor;

import org.hl7.fhir.convertors.VersionConvertor_40_50;
import org.hl7.fhir.r4.formats.FormatUtilities;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.model.ImplementationGuide;
import org.hl7.fhir.utilities.IniFile;
import org.hl7.fhir.utilities.Utilities;
import org.opencds.cqf.tooling.npm.NpmPackageManager;

import java.io.File;
import java.io.IOException;

public class BaseProcessor implements IWorkerContext.ILoggingService {

    protected IniFile ini;
    protected String rootDir;
    protected String igPath;
    protected ImplementationGuide sourceIg;
    protected String fhirVersion;
    protected String packageId;
    protected String canonicalBase;
    protected NpmPackageManager packageManager;

    public void initialize(String iniFile) throws IOException {
        ini = new IniFile(new File(iniFile).getAbsolutePath());
        rootDir = Utilities.getDirectoryForFile(ini.getFileName());
        igPath = Utilities.path(rootDir, ini.getStringProperty("IG", "ig"));
        sourceIg = (ImplementationGuide) VersionConvertor_40_50.convertResource(FormatUtilities.loadFile(igPath));
        fhirVersion = sourceIg.getFhirVersion().toString();
        packageId = sourceIg.getPackageId();
        canonicalBase = determineCanonical(sourceIg.getUrl());
        packageManager = new NpmPackageManager(sourceIg);
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
