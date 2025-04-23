package org.opencds.cqf.tooling.plandefinition.r4;

import org.hl7.fhir.convertors.advisors.impl.BaseAdvisor_40_50;
import org.hl7.fhir.convertors.conv40_50.VersionConvertor_40_50;
import org.hl7.fhir.r4.formats.FormatUtilities;
import org.opencds.cqf.tooling.common.r4.SoftwareSystemHelper;
import org.opencds.cqf.tooling.library.LibraryProcessor;
import org.opencds.cqf.tooling.parameter.RefreshPlanDefinitionParameters;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class PlanDefinitionProcessor extends org.opencds.cqf.tooling.plandefinition.PlanDefinitionProcessor {
    private static final Logger logger = LoggerFactory.getLogger(PlanDefinitionProcessor.class);
    private static SoftwareSystemHelper cqfmHelper;

    private RefreshPlanDefinitionParameters params;
    public PlanDefinitionProcessor(LibraryProcessor libraryProcessor) {
        super(libraryProcessor);
    }

    private String getPlanDefinitionPath(String planDefinitionPath) {
        var f = new File(planDefinitionPath);
        if (!f.exists() && f.getParentFile().isDirectory() && f.getParentFile().exists()) {
            return f.getParentFile().toString();
        }
        return planDefinitionPath;
    }
    /*
        Refresh all PlanDefinition resources in the given planDefinitionPath
        If the path is not specified, or is not a known directory, process
        all known PlanDefinition resources overriding any currently existing files.
    */
    protected List<String> refreshPlanDefinitions(String planDefinitionPath, IOUtils.Encoding encoding) {
        return refreshPlanDefinitions(planDefinitionPath, null, encoding);
    }

    /*
        Refresh all PlanDefinition resources in the given planDefinitionPath
        If the path is not specified, or is not a known directory, process
        all known PlanDefinition resources
    */
    protected List<String> refreshPlanDefinitions(String planDefinitionPath, String planDefinitionOutputDirectory, IOUtils.Encoding encoding) {
        var file = planDefinitionPath != null ? new File(planDefinitionPath) : null;
        var fileMap = new HashMap<String, String>();
        var planDefinitions = new ArrayList<org.hl7.fhir.r5.model.PlanDefinition>();

        if (file == null || !file.exists()) {
            for (var path : IOUtils.getPlanDefinitionPaths(params.fhirContext)) {
                loadPlanDefinition(fileMap, planDefinitions, new File(path));
            }
        }
        else if (file.isDirectory()) {
            for (var libraryFile : Objects.requireNonNull(file.listFiles())) {
                if (IOUtils.isXMLOrJson(planDefinitionPath, libraryFile.getName())) {
                    loadPlanDefinition(fileMap, planDefinitions, libraryFile);
                }
            }
        }
        else {
            loadPlanDefinition(fileMap, planDefinitions, file);
        }

        var refreshedPlanDefinitionNames = new ArrayList<String>();
        var refreshedPlanDefinitions = super.refreshGeneratedContent(planDefinitions);
        var versionConvertor = new VersionConvertor_40_50(new BaseAdvisor_40_50());
        for (var refreshedPlanDefinition : refreshedPlanDefinitions) {
            var planDefinition = (org.hl7.fhir.r4.model.PlanDefinition) versionConvertor.convertResource(refreshedPlanDefinition);
            if (planDefinition.hasIdentifier() && !planDefinition.getIdentifier().isEmpty()) {
                this.getIdentifiers().addAll(planDefinition.getIdentifier());
            }
            String filePath;
            IOUtils.Encoding fileEncoding;
            if (fileMap.containsKey(refreshedPlanDefinition.getId()))
            {
                filePath = fileMap.get(refreshedPlanDefinition.getId());
                fileEncoding = IOUtils.getEncoding(filePath);
            } else {
                filePath = getPlanDefinitionPath(planDefinitionPath);
                fileEncoding = encoding;
            }
            cqfmHelper.ensureCQFToolingExtensionAndDevice(planDefinition, params.fhirContext);
            // Issue 96
            // Passing the includeVersion here to handle not using the version number in the filename
            if (new File(filePath).exists()) {
                // TODO: This prevents mangled names from being output
                // It would be nice for the tooling to generate library shells, we have enough information to,
                // but the tooling gets confused about the ID and the filename and what gets written is garbage
                var outputPath = filePath;
                if (planDefinitionOutputDirectory != null) {
                    var planDefinitionDirectory = new File(planDefinitionOutputDirectory);
                    if (!planDefinitionDirectory.exists()) {
                        logger.warn("Unable to determine PlanDefinition directory. Will write PlanDefinition to {}", outputPath);
                    } else {
                        outputPath = planDefinitionDirectory.getAbsolutePath();
                    }
                }
                IOUtils.writeResource(planDefinition, outputPath, fileEncoding, params.fhirContext, params.versioned);
                String refreshedPlanDefinitionName;
                if (params.versioned && refreshedPlanDefinition.getVersion() != null) {
                    refreshedPlanDefinitionName = refreshedPlanDefinition.getName() + "-" + refreshedPlanDefinition.getVersion();
                } else {
                    refreshedPlanDefinitionName = refreshedPlanDefinition.getName();
                }
                refreshedPlanDefinitionNames.add(refreshedPlanDefinitionName);
            }
        }

        return refreshedPlanDefinitionNames;
    }

    private void loadPlanDefinition(Map<String, String> fileMap, List<org.hl7.fhir.r5.model.PlanDefinition> planDefinitions, File planDefinitionFile) {
        try {
            var resource = FormatUtilities.loadFile(planDefinitionFile.getAbsolutePath());
            var versionConvertor = new VersionConvertor_40_50(new BaseAdvisor_40_50());
            var planDefinition = (org.hl7.fhir.r5.model.PlanDefinition) versionConvertor.convertResource(resource);
            fileMap.put(planDefinition.getId(), planDefinitionFile.getAbsolutePath());
            planDefinitions.add(planDefinition);
        } catch (Exception ex) {
            logMessage(String.format("Error reading PlanDefinition: %s. Error: %s", planDefinitionFile.getAbsolutePath(), ex.getMessage()));
        }
    }

    @Override
    public List<String> refreshPlanDefinitionContent(RefreshPlanDefinitionParameters params) {
        if (params.parentContext != null) {
            initialize(params.parentContext);
        }
        else {
            initializeFromIni(params.ini);
        }

        this.params = params;

        cqfmHelper = new SoftwareSystemHelper(rootDir);

        if (params.planDefinitionOutputDirectory != null) {
            return refreshPlanDefinitions(params.planDefinitionPath, params.planDefinitionOutputDirectory, params.encoding);
        } else {
            return refreshPlanDefinitions(params.planDefinitionPath, params.encoding);
        }
    }
}
