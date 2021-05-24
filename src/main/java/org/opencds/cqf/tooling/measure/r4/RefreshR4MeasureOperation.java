package org.opencds.cqf.tooling.measure.r4;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.parser.XmlParser;

import org.apache.commons.io.FilenameUtils;
import org.hl7.fhir.r4.model.Measure;
import org.opencds.cqf.tooling.common.r4.CqfmSoftwareSystemHelper;
import org.opencds.cqf.tooling.operation.RefreshGeneratedContentOperation;
import org.opencds.cqf.tooling.utilities.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.Optional;

public class RefreshR4MeasureOperation extends RefreshGeneratedContentOperation {

    private JsonParser jsonParser;
    private XmlParser xmlParser;
    private CqfmSoftwareSystemHelper cqfmHelper = new CqfmSoftwareSystemHelper();

    public RefreshR4MeasureOperation() {
        super("src/main/resources/org/opencds/cqf/tooling/measure/output/r4", "-RefreshR4Measure", FhirContext.forR4());
        jsonParser = (JsonParser)this.getFhirContext().newJsonParser();
        xmlParser = (XmlParser)this.getFhirContext().newXmlParser();
    }

    public RefreshR4MeasureOperation(String pathToMeasures) {
        super(FilenameUtils.getPath(pathToMeasures), "-RefreshR4Measure", FhirContext.forR4(), null, pathToMeasures);
        jsonParser = (JsonParser)this.getFhirContext().newJsonParser();
        xmlParser = (XmlParser)this.getFhirContext().newXmlParser();
        xmlParser = (XmlParser)this.getFhirContext().newXmlParser();
    }

    @Override
    public void refreshGeneratedContent() {
        File measureDir = new File(this.getPathToMeasures());
        if (measureDir.isDirectory()) {
            for (File f : Optional.ofNullable(measureDir.listFiles()).<NoSuchElementException>orElseThrow(() -> new NoSuchElementException())) {
                refreshMeasureFromFile(f);
            }
        }
        else if (measureDir.isFile()){
            refreshMeasureFromFile(measureDir);
        }
    }

    private void refreshMeasureFromFile(File f) {
        Measure measure = null;
        IOUtils.Encoding encoding = null;

        if (f.isFile()) {
            try {
                if (f.getName().endsWith("xml")) {
                    measure = (Measure)xmlParser.parseResource(new FileInputStream(f));
                    encoding = IOUtils.Encoding.XML;
                }
                else {
                    measure = (Measure)jsonParser.parseResource(new FileInputStream(f));
                    encoding = IOUtils.Encoding.JSON;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("Error parsing " + f.getName());
            }
        }
        if (measure == null) {
            return;
        }
        output(refreshMeasure(measure), encoding);
    }

    public Measure refreshMeasure(Measure measure) {
        cqfmHelper.ensureCQFToolingExtensionAndDevice(measure, this.getFhirContext());
        R4MeasureProcessor refresher = new R4MeasureProcessor();
        //refresher.refreshMeasure(measure, );
        return measure;
    }

}
