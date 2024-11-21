package org.opencds.cqf.tooling.measure.r4;

import org.cqframework.cql.cql2elm.CqlCompilerException;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.model.CompiledLibrary;
import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.fhir.convertors.advisors.impl.BaseAdvisor_40_50;
import org.hl7.fhir.convertors.conv40_50.VersionConvertor_40_50;
import org.hl7.fhir.r4.formats.FormatUtilities;
import org.hl7.fhir.r5.model.Measure;
import org.opencds.cqf.tooling.common.r4.CqfmSoftwareSystemHelper;
import org.opencds.cqf.tooling.measure.MeasureProcessor;
import org.opencds.cqf.tooling.measure.MeasureRefreshProcessor;
import org.opencds.cqf.tooling.parameter.RefreshMeasureParameters;
import org.opencds.cqf.tooling.processor.CqlProcessor;
import org.opencds.cqf.tooling.utilities.CanonicalUtils;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class R4MeasureProcessor extends MeasureProcessor {

    private String measurePath;
    private String measureOutputDirectory;
    private IOUtils.Encoding encoding;
    private Boolean shouldApplySoftwareSystemStamp;
    private Boolean includePopulationDataRequirements;
    private static CqfmSoftwareSystemHelper cqfmHelper;

    private String getMeasurePath(String measurePath) {
        File f = new File(measurePath);
        if (!f.exists() && f.getParentFile().isDirectory() && f.getParentFile().exists()) {
            return f.getParentFile().toString();
        }
        return measurePath;
    }
    /*
        Refresh all measure resources in the given measurePath
        If the path is not specified, or is not a known directory, process
        all known measure resources overriding any currently existing files.
    */
    protected List<String> refreshMeasures(String measurePath, IOUtils.Encoding encoding) {
        return refreshMeasures(measurePath, null, encoding);
    }

    /*
        Refresh all measure resources in the given measurePath
        If the path is not specified, or is not a known directory, process
        all known measure resources
    */
    protected List<String> refreshMeasures(String measurePath, String measureOutputDirectory, IOUtils.Encoding encoding) {
        File file = measurePath != null ? new File(measurePath) : null;
        Map<String, String> fileMap = new HashMap<String, String>();
        List<org.hl7.fhir.r5.model.Measure> measures = new ArrayList<>();

        if (file == null || !file.exists()) {
            for (String path : IOUtils.getMeasurePaths(this.fhirContext)) {
                loadMeasure(fileMap, measures, new File(path));
            }
        }
        else if (file.isDirectory()) {
            for (File libraryFile : file.listFiles()) {
                if(IOUtils.isXMLOrJson(measurePath, libraryFile.getName())) {
                    loadMeasure(fileMap, measures, libraryFile);
                }
            }
        }
        else {
            loadMeasure(fileMap, measures, file);
        }

        List<String> refreshedMeasureNames = new ArrayList<>();
        List<org.hl7.fhir.r5.model.Measure> refreshedMeasures = refreshGeneratedContent(measures);
        VersionConvertor_40_50 versionConvertor = new VersionConvertor_40_50(new BaseAdvisor_40_50());
        for (org.hl7.fhir.r5.model.Measure refreshedMeasure : refreshedMeasures) {
            org.hl7.fhir.r4.model.Measure measure = (org.hl7.fhir.r4.model.Measure) versionConvertor.convertResource(refreshedMeasure);
            if (measure.hasIdentifier() && !measure.getIdentifier().isEmpty()) {
                this.getIdentifiers().addAll(measure.getIdentifier());
            }
            String filePath = null;
            IOUtils.Encoding fileEncoding = null;
            if (fileMap.containsKey(refreshedMeasure.getId()))
            {
                filePath = fileMap.get(refreshedMeasure.getId());
                fileEncoding = IOUtils.getEncoding(filePath);
            } else {
                filePath = getMeasurePath(measurePath);
                fileEncoding = encoding;
            }
            cqfmHelper.ensureCQFToolingExtensionAndDevice(measure, fhirContext);
            // Issue 96
            // Passing the includeVersion here to handle not using the version number in the filename
            if (new File(filePath).exists()) {
                // TODO: This prevents mangled names from being output
                // It would be nice for the tooling to generate library shells, we have enough information to,
                // but the tooling gets confused about the ID and the filename and what gets written is garbage
                String outputPath = filePath;
                if (measureOutputDirectory != null) {
                    File measureDirectory = new File(measureOutputDirectory);
                    if (!measureDirectory.exists()) {
                        //TODO: add logger and log non existant directory for writing
                    } else {
                        outputPath = measureDirectory.getAbsolutePath();
                    }
                }
                IOUtils.writeResource(measure, outputPath, fileEncoding, fhirContext, this.versioned, true);
                String refreshedMeasureName;
                if (this.versioned && refreshedMeasure.getVersion() != null) {
                    refreshedMeasureName = refreshedMeasure.getName() + "-" + refreshedMeasure.getVersion();
                } else {
                    refreshedMeasureName = refreshedMeasure.getName();
                }
                refreshedMeasureNames.add(refreshedMeasureName);
            }
        }

        return refreshedMeasureNames;
    }

    private void loadMeasure(Map<String, String> fileMap, List<org.hl7.fhir.r5.model.Measure> measures, File measureFile) {
        try {
            org.hl7.fhir.r4.model.Resource resource = FormatUtilities.loadFile(measureFile.getAbsolutePath());
            VersionConvertor_40_50 versionConvertor = new VersionConvertor_40_50(new BaseAdvisor_40_50());
            org.hl7.fhir.r5.model.Measure measure = (org.hl7.fhir.r5.model.Measure) versionConvertor.convertResource(resource);
            fileMap.put(measure.getId(), measureFile.getAbsolutePath());
            measures.add(measure);
        } catch (Exception ex) {
            logMessage(String.format("Error reading measure: %s. Error: %s", measureFile.getAbsolutePath(), ex.getMessage()));
        }
    }

    @Override
    protected List<Measure> refreshGeneratedContent(List<Measure> sourceMeasures) {
        return internalRefreshGeneratedContent(sourceMeasures);
    }

    private List<Measure> internalRefreshGeneratedContent(List<Measure> sourceMeasures) {
        // for each Measure, refresh the measure based on the primary measure library
        List<Measure> resources = new ArrayList<>();
        MeasureRefreshProcessor processor = new MeasureRefreshProcessor();
        LibraryManager libraryManager = getCqlProcessor().getLibraryManager();
        CqlTranslatorOptions cqlTranslatorOptions = getCqlProcessor().getCqlTranslatorOptions();
        for (Measure measure : sourceMeasures) {
            // Do not attempt to refresh if the measure does not have a library
            if (measure.hasLibrary()) {
                resources.add(refreshGeneratedContent(measure, processor, libraryManager, cqlTranslatorOptions));
            } else {
                resources.add(measure);
            }
        }

        return resources;
    }

    private Measure refreshGeneratedContent(Measure measure, MeasureRefreshProcessor processor, LibraryManager libraryManager, CqlTranslatorOptions cqlTranslatorOptions) {

        String libraryUrl = ResourceUtils.getPrimaryLibraryUrl(measure, fhirContext);
        VersionedIdentifier primaryLibraryIdentifier = CanonicalUtils.toVersionedIdentifier(libraryUrl);

        List<CqlCompilerException> errors = new CopyOnWriteArrayList<>();
        CompiledLibrary compiledLibrary = libraryManager.resolveLibrary(primaryLibraryIdentifier, errors);

        logger.info(CqlProcessor.buildStatusMessage(errors, measure.getName(), verboseMessaging));

        boolean hasSevereErrors = CqlProcessor.hasSevereErrors(errors);

        //refresh measures without severe errors:
        if (!hasSevereErrors) {
            if (includePopulationDataRequirements != null) {
                processor.includePopulationDataRequirements = includePopulationDataRequirements;
            }
            return processor.refreshMeasure(measure, libraryManager, compiledLibrary, cqlTranslatorOptions.getCqlCompilerOptions());
        }

        return measure;
    }

    @Override
    public List<String> refreshMeasureContent(RefreshMeasureParameters params) {
        if (params.parentContext != null) {
            initialize(params.parentContext);
        }
        else {
            initializeFromIni(params.ini);
        }

        measurePath = params.measurePath;
        measureOutputDirectory = params.measureOutputDirectory;
        fhirContext = params.fhirContext;
        encoding = params.encoding;
        versioned = params.versioned;
        shouldApplySoftwareSystemStamp = params.shouldApplySoftwareSystemStamp;
        includePopulationDataRequirements = params.includePopulationDataRequirements;

        R4MeasureProcessor.cqfmHelper = new CqfmSoftwareSystemHelper(rootDir);

        if (measureOutputDirectory != null) {
            return refreshMeasures(measurePath, measureOutputDirectory, encoding);
        } else {
            return refreshMeasures(measurePath, encoding);
        }
    }
}
