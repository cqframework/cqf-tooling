package org.opencds.cqf.tooling.measure.r4;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.r4.model.Measure;
import org.opencds.cqf.tooling.operation.RefreshGeneratedContentOperation;
import org.opencds.cqf.tooling.utilities.IOUtils;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;

public class RefreshR4MeasureOperation extends RefreshGeneratedContentOperation {

    public RefreshR4MeasureOperation() {
        super("src/main/resources/org/opencds/cqf/tooling/measure/output/r4", "-RefreshR4Measure", FhirContext.forCached(FhirVersionEnum.R4));
    }

    @SuppressWarnings("this-escape")
    public RefreshR4MeasureOperation(String pathToMeasures) {
        super(pathToMeasures, "-RefreshR4Measure", FhirContext.forCached(FhirVersionEnum.R4), null, pathToMeasures);
    }

    @Override
    public void refreshGeneratedContent() {
        File measureDir = new File(this.getPathToMeasures());
        if (measureDir.isDirectory()) {
            for (File f : Optional.ofNullable(measureDir.listFiles()).orElseThrow(NoSuchElementException::new)) {
                refreshMeasure(f.getAbsolutePath());
            }
        }
        else if (measureDir.isFile()){
            refreshMeasure(measureDir.getAbsolutePath());
        }
    }

    private void refreshMeasureFromFile(File f) {
        if (f.isDirectory()) {
            for (var file : Objects.requireNonNull(f.listFiles())) {
                refreshMeasureFromFile(file);
            }
        } else if (f.isFile()) {
            output(refreshMeasure(f.getAbsolutePath()), IOUtils.getEncoding(f.getAbsolutePath()));
        }
    }

    public Measure refreshMeasure(String measurePath) {
        R4MeasureProcessor refresher = new R4MeasureProcessor();
        var measureName = refresher.refreshMeasures(measurePath, getOutputPath(), IOUtils.getEncoding(measurePath));
        return (Measure) IOUtils.readResource(FilenameUtils.concat(getOutputPath(), measureName.get(0)), FhirContext.forR4Cached());
    }

}
