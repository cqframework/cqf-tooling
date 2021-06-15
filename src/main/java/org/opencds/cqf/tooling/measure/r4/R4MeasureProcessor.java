package org.opencds.cqf.tooling.measure.r4;

import org.hl7.fhir.convertors.VersionConvertor_40_50;
import org.hl7.fhir.r4.formats.FormatUtilities;
import org.opencds.cqf.tooling.common.r4.CqfmSoftwareSystemHelper;
import org.opencds.cqf.tooling.measure.MeasureProcessor;
import org.opencds.cqf.tooling.parameter.RefreshMeasureParameters;
import org.opencds.cqf.tooling.utilities.IOUtils;

import java.io.File;
import java.util.*;
/**
 * @author Adam Stevenson
 */
public class R4MeasureProcessor extends MeasureProcessor {

    private String measurePath;
    private IOUtils.Encoding encoding;
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
        all known measure resources
    */
    protected List<String> refreshMeasures(String measurePath, IOUtils.Encoding encoding) {
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
                loadMeasure(fileMap, measures, libraryFile);
            }
        }
        else {
            loadMeasure(fileMap, measures, file);
        }

        List<String> refreshedMeasureNames = new ArrayList<String>();
        List<org.hl7.fhir.r5.model.Measure> refreshedMeasures = super.refreshGeneratedContent(measures);
        for (org.hl7.fhir.r5.model.Measure refreshedMeasure : refreshedMeasures) {
            org.hl7.fhir.r4.model.Measure measure = (org.hl7.fhir.r4.model.Measure) VersionConvertor_40_50.convertResource(refreshedMeasure);
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
                IOUtils.writeResource(measure, filePath, fileEncoding, fhirContext, this.versioned);
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
            org.hl7.fhir.r5.model.Measure measure = (org.hl7.fhir.r5.model.Measure) VersionConvertor_40_50.convertResource(resource);
            fileMap.put(measure.getId(), measureFile.getAbsolutePath());
            measures.add(measure);
        } catch (Exception ex) {
            logMessage(String.format("Error reading measure: %s. Error: %s", measureFile.getAbsolutePath(), ex.getMessage()));
        }
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
        fhirContext = params.fhirContext;
        encoding = params.encoding;
        versioned = params.versioned;

        R4MeasureProcessor.cqfmHelper = new CqfmSoftwareSystemHelper(rootDir);

        return refreshMeasures(measurePath, encoding);
    }
}
