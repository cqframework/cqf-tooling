package org.opencds.cqf.tooling.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.opencds.cqf.tooling.parameter.VmrToFhirParameters;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.opencds.cqf.tooling.vmrToFhir.VmrToFhirTransformer;
import org.opencds.vmr.v1_0.schema.CDSOutput;
import org.opencds.vmr.v1_0.schema.EvaluatedPerson;
import org.opencds.vmr.v1_0.schema.VMR;
import org.opencds.vmr.v1_0.schema.EvaluatedPerson.ClinicalStatements;
import org.opencds.vmr.v1_0.schema.EvaluatedPerson.Demographics;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.util.BundleBuilder;

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
        FhirContext context = FhirVersionEnum.forVersionString(params.fhirVersion).newContext();
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
        CDSOutput cdsOutput = null;
        String cdsCanonical = CDSOutput.class.getCanonicalName();
        String classPackageName = cdsCanonical.substring(0, cdsCanonical.lastIndexOf("."));
        try {
            Unmarshaller unmarshaller = JAXBContext.newInstance(classPackageName).createUnmarshaller();
            InputStream inputStream = new FileInputStream(file);
            Object unmarshalledObject = unmarshaller.unmarshal(inputStream);
            if (unmarshalledObject instanceof JAXBElement) {
                cdsOutput = (CDSOutput) ((JAXBElement) unmarshalledObject).getValue();
            } else {
                cdsOutput = (CDSOutput) unmarshalledObject;
            }
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
            return cdsOutput;
        }
}
