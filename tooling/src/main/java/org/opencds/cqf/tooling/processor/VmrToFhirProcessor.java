package org.opencds.cqf.tooling.processor;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.util.BundleBuilder;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.opencds.cqf.tooling.parameter.VmrToFhirParameters;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.opencds.cqf.tooling.vmrToFhir.VmrToFhirTransformer;
import org.opencds.vmr.v1_0.schema.CDSOutput;
import org.opencds.vmr.v1_0.schema.EvaluatedPerson;
import org.opencds.vmr.v1_0.schema.EvaluatedPerson.ClinicalStatements;
import org.opencds.vmr.v1_0.schema.EvaluatedPerson.Demographics;
import org.opencds.vmr.v1_0.schema.VMR;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * Provides Transformation processing from vMR to Fhir.
 *
 * @author Joshua Reynolds
 * @since 2021-04-05
 */
public class VmrToFhirProcessor {
    /**
     * Transforms Vmr CDSOutput data to FHIR equivalent
     * @param params the {@link VmrToFhirParameters VmrToFhirParameters}
     */
    public static void transform(VmrToFhirParameters params) {
        FhirContext context = FhirContext.forVersion(FhirVersionEnum.valueOf(params.fhirVersion));
        File file = new File(params.vmrDataPath);
        CDSOutput cdsOutput = unmarshallCdsOutput(file);
        VMR vmrOutput = cdsOutput.getVmrOutput();
        EvaluatedPerson evaluatedPerson = vmrOutput.getPatient();
        Demographics deomographics = evaluatedPerson.getDemographics();
        ClinicalStatements clinicalStatements = evaluatedPerson.getClinicalStatements();
        VmrToFhirTransformer transformer = new VmrToFhirTransformer();
        Patient patient = transformer.transform(deomographics);
        List<IAnyResource> resources = transformer.transform(clinicalStatements);
        BundleBuilder bundleBuilder = new BundleBuilder(context);
        bundleBuilder.setBundleField("id", new IdType(UUID.randomUUID().toString()).getIdPart());
        writeOutput(params.fhirOutputPath, context, patient, resources, bundleBuilder);
    }

    private static void writeOutput(String fhirOutputPath, FhirContext context, Patient patient, List<IAnyResource> resources,
            BundleBuilder bundleBuilder) {
        File outputDirectory = new File(IOUtils.concatFilePath(fhirOutputPath, patient.getIdElement().getIdPart()));
        if (!outputDirectory.isDirectory()) {
            outputDirectory.mkdirs();
        }
        File patientDirectory = new File(outputDirectory, "Patient");
        if (!patientDirectory.isDirectory()) {
            patientDirectory.mkdirs();
        }
        IOUtils.writeResource(patient, patientDirectory.getAbsolutePath(), Encoding.JSON, context);
        File dataDirectory = new File(outputDirectory, "Data");
        if (!dataDirectory.isDirectory()) {
            dataDirectory.mkdirs();
        }
        resources.stream().forEach(resource -> bundleBuilder.addCollectionEntry(resource));
        IOUtils.writeBundle(bundleBuilder.getBundle(), dataDirectory.getAbsolutePath(), Encoding.JSON, context);
    }

    @SuppressWarnings("rawtypes")
    private static CDSOutput unmarshallCdsOutput(File file) {
        try {
            Unmarshaller unmarshaller = JAXBContext.newInstance(CDSOutput.class, VMR.class).createUnmarshaller();
            InputStream inputStream = new FileInputStream(file);
            Object unmarshalledObject = unmarshaller.unmarshal(inputStream);
            if (unmarshalledObject instanceof JAXBElement) {
                return (CDSOutput) ((JAXBElement) unmarshalledObject).getValue();
            } else {
                 return (CDSOutput) unmarshalledObject;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
