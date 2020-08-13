package org.opencds.cqf.tooling.measure.r4;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.hl7.fhir.r4.model.Measure;
import org.opencds.cqf.tooling.common.r4.CqfmSoftwareSystemHelper;
import org.opencds.cqf.tooling.measure.RefreshGeneratedContent;
import org.opencds.cqf.tooling.utilities.IOUtils;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.parser.XmlParser;

public class RefreshR4Measure extends RefreshGeneratedContent {

    private JsonParser jsonParser;
    private XmlParser xmlParser;
    private CqfmSoftwareSystemHelper cqfmHelper = new CqfmSoftwareSystemHelper();

    public RefreshR4Measure() {
        super("src/main/resources/org/opencds/cqf/tooling/measure/output/r4", "-RefreshR4Measure", FhirContext.forR4());
        jsonParser = (JsonParser)this.getContext().newJsonParser();
        xmlParser = (XmlParser)this.getContext().newXmlParser();
    }

    public RefreshR4Measure(String pathToMeasures) {
        super(pathToMeasures, "-RefreshR4Measure", FhirContext.forR4(), null, pathToMeasures);
        jsonParser = (JsonParser)this.getContext().newJsonParser();
        xmlParser = (XmlParser)this.getContext().newXmlParser();
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
        cqfmHelper.ensureToolingExtensionAndDevice(measure, this.getContext());
        return measure;
    }
}
