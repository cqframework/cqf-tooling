package org.opencds.cqf.tooling.measure.r4;

import org.cqframework.cql.cql2elm.CqlCompilerException;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.hl7.fhir.convertors.advisors.impl.BaseAdvisor_40_50;
import org.hl7.fhir.convertors.conv40_50.VersionConvertor_40_50;
import org.hl7.fhir.r4.formats.FormatUtilities;
import org.hl7.fhir.r5.model.Measure;
import org.opencds.cqf.tooling.common.r4.SoftwareSystemHelper;
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

    private RefreshMeasureParameters params;
    private static SoftwareSystemHelper cqfmHelper;

    private String getMeasurePath(String measurePath) {
        var f = new File(measurePath);
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
        var file = measurePath != null ? new File(measurePath) : null;
        var fileMap = new HashMap<String, String>();
        var measures = new ArrayList<org.hl7.fhir.r5.model.Measure>();

        if (file == null || !file.exists()) {
            for (var path : IOUtils.getMeasurePaths(params.fhirContext)) {
                loadMeasure(fileMap, measures, new File(path));
            }
        }
        else if (file.isDirectory()) {
            for (var libraryFile : Objects.requireNonNull(file.listFiles())) {
                if(IOUtils.isXMLOrJson(measurePath, libraryFile.getName())) {
                    loadMeasure(fileMap, measures, libraryFile);
                }
            }
        }
        else {
            loadMeasure(fileMap, measures, file);
        }

        var refreshedMeasureNames = new ArrayList<String>();
        var refreshedMeasures = refreshGeneratedContent(measures);
        VersionConvertor_40_50 versionConvertor = new VersionConvertor_40_50(new BaseAdvisor_40_50());
        for (var refreshedMeasure : refreshedMeasures) {
            var measure = (org.hl7.fhir.r4.model.Measure) versionConvertor.convertResource(refreshedMeasure);
            if (measure.hasIdentifier() && !measure.getIdentifier().isEmpty()) {
                this.getIdentifiers().addAll(measure.getIdentifier());
            }
            String filePath;
            IOUtils.Encoding fileEncoding;
            if (fileMap.containsKey(refreshedMeasure.getId()))
            {
                filePath = fileMap.get(refreshedMeasure.getId());
                fileEncoding = IOUtils.getEncoding(filePath);
            } else {
                filePath = getMeasurePath(measurePath);
                fileEncoding = encoding;
            }
            if (this.params.shouldApplySoftwareSystemStamp) {
                cqfmHelper.ensureCQFToolingExtensionAndDevice(measure, params.fhirContext);
            }
            // Issue 96
            // Passing the includeVersion here to handle not using the version number in the filename
            if (new File(filePath).exists()) {
                // TODO: This prevents mangled names from being output
                // It would be nice for the tooling to generate library shells, we have enough information to,
                // but the tooling gets confused about the ID and the filename and what gets written is garbage
                var outputPath = filePath;
                if (measureOutputDirectory != null) {
                    var measureDirectory = new File(measureOutputDirectory);
                    if (!measureDirectory.exists()) {
                        logger.warn("Unable to determine measure directory. Will write Measures to {}", outputPath);
                    } else {
                        outputPath = measureDirectory.getAbsolutePath();
                    }
                }
                IOUtils.writeResource(measure, outputPath, fileEncoding, params.fhirContext, this.versioned, true);
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
            var resource = FormatUtilities.loadFile(measureFile.getAbsolutePath());
            var versionConvertor = new VersionConvertor_40_50(new BaseAdvisor_40_50());
            var measure = (org.hl7.fhir.r5.model.Measure) versionConvertor.convertResource(resource);
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
        var resources = new ArrayList<Measure>();
        var processor = new MeasureRefreshProcessor();
        var libraryManager = getCqlProcessor().getLibraryManager();
        var cqlTranslatorOptions = getCqlProcessor().getCqlTranslatorOptions();
        for (var measure : sourceMeasures) {
            // Do not attempt to refresh if the measure does not have a library
            if (measure.hasLibrary()) {
                resources.add(refreshGeneratedContent(measure, processor, libraryManager, cqlTranslatorOptions));
            } else {
                resources.add(measure);
            }
        }

        return resources;
    }

    private Measure refreshGeneratedContent(Measure measure, MeasureRefreshProcessor processor,
                                            LibraryManager libraryManager, CqlTranslatorOptions cqlTranslatorOptions) {
        var libraryUrl = ResourceUtils.getPrimaryLibraryUrl(measure, fhirContext);
        var primaryLibraryIdentifier = CanonicalUtils.toVersionedIdentifier(libraryUrl);

        var errors = new CopyOnWriteArrayList<CqlCompilerException>();
        var compiledLibrary = libraryManager.resolveLibrary(primaryLibraryIdentifier, errors);

        logger.info(CqlProcessor.buildStatusMessage(errors, measure.getName(), verboseMessaging));

        var hasSevereErrors = CqlProcessor.hasSevereErrors(errors);

        //refresh measures without severe errors:
        if (!hasSevereErrors) {
            if (params.includePopulationDataRequirements != null) {
                processor.includePopulationDataRequirements = params.includePopulationDataRequirements;
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

        this.params = params;
        R4MeasureProcessor.cqfmHelper = new SoftwareSystemHelper(rootDir);

        if (params.measureOutputDirectory != null) {
            return refreshMeasures(params.measurePath, params.measureOutputDirectory, params.encoding);
        } else {
            return refreshMeasures(params.measurePath, params.encoding);
        }
    }
}
