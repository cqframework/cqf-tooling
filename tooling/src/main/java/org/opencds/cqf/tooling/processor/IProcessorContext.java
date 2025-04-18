package org.opencds.cqf.tooling.processor;

import org.cqframework.fhir.npm.NpmPackageManager;
import org.fhir.ucum.UcumService;
import org.hl7.fhir.r5.model.ImplementationGuide;

import java.util.List;

public interface IProcessorContext {
    String getRootDir();

    ImplementationGuide getSourceIg();

    String getFhirVersion();

    String getPackageId();

    String getCanonicalBase();

    UcumService getUcumService();

    NpmPackageManager getPackageManager();

    List<String> getBinaryPaths();

    CqlProcessor getCqlProcessor();

    Boolean getVerboseMessaging();
}
