package org.opencds.cqf.validation.profiles.cqfmeasures.r4;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.ValidationResult;
import org.apache.commons.io.FileUtils;
import org.hl7.fhir.r4.hapi.ctx.DefaultProfileValidationSupport;
import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.r4.hapi.validation.PrePopulatedValidationSupport;
import org.hl7.fhir.r4.hapi.validation.ValidationSupportChain;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.validation.profiles.cqfmeasures.ProfileValidation;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class CqfMeasureProfileValidation extends ProfileValidation {

    private String pathToResource; // -pathtoresource (-ptr)

    public CqfMeasureProfileValidation() {
        super(FhirContext.forR4());
    }

    public ValidationResult validate(String resource) {
        ValidationResult result = getValidator().validateWithResult(resource);
        return result;
    }

    @Override
    public void execute(String[] args) {
        setOutputPath("src/main/resources/org/opencds/cqf/validation/output"); // default

        for (String arg : args) {
            if (arg.equals("-ValidateCqfMeasuresR4Profiles")) continue;
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "outputpath":
                case "op":
                    setOutputPath(value);
                    break; // -outputpath (-op)
                case "pathtoresource":
                case "ptr":
                    pathToResource = value;
                    break;
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }

        if (pathToResource == null) {
            throw new IllegalArgumentException("The path to the resource is required");
        }

        String resource;
        try {
            resource = FileUtils.readFileToString(new File(pathToResource), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot find file: " + pathToResource);
        }

        output((Resource) validate(resource).toOperationOutcome());
    }

    public void populateProfiles() throws FileNotFoundException {
        PrePopulatedValidationSupport prePopulatedValidationSupport = new PrePopulatedValidationSupport();
        FhirInstanceValidator module = new FhirInstanceValidator();
        String path = this.getClass().getResource("").getPath();
        Iterator<File> fileIterator = FileUtils.iterateFiles(new File(path + "profiles"), new String[]{"json"}, false);
        while (fileIterator.hasNext()) {
            prePopulatedValidationSupport.addStructureDefinition((StructureDefinition) getParser().parseResource(new FileReader(fileIterator.next())));
        }
        path = this.getClass().getResource("").getPath();
        fileIterator = FileUtils.iterateFiles(new File(path + "valuesets"), new String[]{"json"}, false);
        while (fileIterator.hasNext()) {
            prePopulatedValidationSupport.addValueSet((ValueSet) getParser().parseResource(new FileReader(fileIterator.next())));
        }
        path = this.getClass().getResource("").getPath();
        fileIterator = FileUtils.iterateFiles(new File(path + "codesystems"), new String[]{"json"}, false);
        while (fileIterator.hasNext()) {
            prePopulatedValidationSupport.addCodeSystem((CodeSystem) getParser().parseResource(new FileReader(fileIterator.next())));
        }
        ValidationSupportChain supportChain = new ValidationSupportChain(prePopulatedValidationSupport, new DefaultProfileValidationSupport());
        module.setValidationSupport(supportChain);
        getValidator().registerValidatorModule(module);
    }

    private void output(Resource resource) {
        try (FileOutputStream writer = new FileOutputStream(getOutputPath() + "/" + "validation-result.json")) {
            writer.write(
                    getParser().setPrettyPrint(true).encodeResourceToString(resource).getBytes()
            );
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
