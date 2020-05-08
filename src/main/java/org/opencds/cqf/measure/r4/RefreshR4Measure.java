package org.opencds.cqf.measure.r4;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.parser.XmlParser;
import org.hl7.fhir.r4.model.Measure;
import org.opencds.cqf.common.r4.CqfmSoftwareSystemHelper;
import org.opencds.cqf.measure.RefreshGeneratedContent;
import org.opencds.cqf.utilities.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Optional;

public class RefreshR4Measure extends RefreshGeneratedContent {

    private JsonParser jsonParser;
    private XmlParser xmlParser;
    private CqfmSoftwareSystemHelper cqfmHelper = new CqfmSoftwareSystemHelper();

    public RefreshR4Measure() {
        super("src/main/resources/org/opencds/cqf/measure/output/r4", "-RefreshR4Measure", FhirContext.forR4());
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
            for (File f : Optional.ofNullable(measureDir.listFiles()).orElseThrow()) {
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
        cqfmHelper.ensureToolingExtensionAndDevice(measure);
        return measure;
    }
}
