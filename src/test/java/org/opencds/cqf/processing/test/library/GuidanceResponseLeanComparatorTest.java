package org.opencds.cqf.processing.test.library;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.GuidanceResponse;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.GuidanceResponse.GuidanceResponseStatus;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.opencds.cqf.processing.test.library.src.GuidanceResponseComparator;
import org.opencds.cqf.processing.test.library.src.GuidanceResponseLeanComparator;
import org.testng.annotations.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class GuidanceResponseLeanComparatorTest {
    private GuidanceResponse loadGuidanceResponse(FhirContext fhirContext, String path) {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(path);
        IParser parser = path.endsWith("json") ? fhirContext.newJsonParser() : fhirContext.newXmlParser();
        IBaseResource resource = parser.parseResource(stream);

        if (resource == null) {
            throw new IllegalArgumentException(String.format("Unable to read a resource from %s.", path));
        }

        Class<?> guidanceResponseClass = fhirContext.getResourceDefinition("GuidanceResponse").getImplementingClass();
        if (!guidanceResponseClass.equals(resource.getClass())) {
            throw new IllegalArgumentException(String.format("Resource at %s is not FHIR %s GuidanceResponse", path,
                    fhirContext.getVersion().getVersion().getFhirVersionString()));
        }

        return (GuidanceResponse) resource;
    }

    private Parameters loadParameters(FhirContext fhirContext, String path) {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(path);
        IParser parser = path.endsWith("json") ? fhirContext.newJsonParser() : fhirContext.newXmlParser();
        IBaseResource resource = parser.parseResource(stream);

        if (resource == null) {
            throw new IllegalArgumentException(String.format("Unable to read a resource from %s.", path));
        }

        Class<?> parametersClass = fhirContext.getResourceDefinition("Parameters").getImplementingClass();
        if (!parametersClass.equals(resource.getClass())) {
            throw new IllegalArgumentException(String.format("Resource at %s is not FHIR %s Parameters", path,
                    fhirContext.getVersion().getVersion().getFhirVersionString()));
        }

        return (Parameters) resource;
    }

    @Test
    public void test_True() {
        Observation observation = new Observation();
        observation.setStatus(ObservationStatus.AMENDED);
        observation.setCode(new CodeableConcept(new Coding("system", "code", "display")));
        Observation observation2 = new Observation();
        observation2.setStatus(ObservationStatus.AMENDED);
        observation2.setCode(new CodeableConcept(new Coding("system", "code", "display")));

        Parameters actualParameters = new Parameters();
        List<ParametersParameterComponent> parameterList = new ArrayList<ParametersParameterComponent>();
        ParametersParameterComponent parameterComponentResource = new ParametersParameterComponent();
        parameterComponentResource.setName("param1");
        parameterComponentResource.setResource(observation);
        parameterList.add(parameterComponentResource);

        ParametersParameterComponent parameterComponentPrimitive = new ParametersParameterComponent();
        parameterComponentPrimitive.setName("param2");
        parameterComponentPrimitive.setValue(new IntegerType(45));
        parameterList.add(parameterComponentPrimitive);
        
        ParametersParameterComponent parameterComponentElement = new ParametersParameterComponent();
        Quantity quantity = new Quantity();
        quantity.setValue(5.0);
        quantity.setUnit("mm");
        quantity.setSystem("unit system");
        parameterComponentElement.setName("param3");
        parameterComponentElement.setValue(quantity);
        parameterList.add(parameterComponentElement);
        actualParameters.setParameter(parameterList);

        Parameters expectedParameters = new Parameters();
        List<ParametersParameterComponent> parameterList2 = new ArrayList<ParametersParameterComponent>();
        ParametersParameterComponent parameterComponentResource2 = new ParametersParameterComponent();
        parameterComponentResource2.setName("param1");
        parameterComponentResource2.setResource(observation2);
        parameterList2.add(parameterComponentResource2);

        ParametersParameterComponent parameterComponentPrimitive2 = new ParametersParameterComponent();
        parameterComponentPrimitive2.setName("param2");
        parameterComponentPrimitive2.setValue(new IntegerType(45));
        parameterList2.add(parameterComponentPrimitive2);
        
        ParametersParameterComponent parameterComponentElement2 = new ParametersParameterComponent();
        Quantity quantity2 = new Quantity();
        quantity2.setValue(5.0);
        quantity2.setUnit("mm");
        quantity2.setSystem("unit system");
        parameterComponentElement2.setName("param3");
        parameterComponentElement2.setValue(quantity2);
        parameterList2.add(parameterComponentElement2);
        expectedParameters.setParameter(parameterList2);

        GuidanceResponse guidanceResponse = new GuidanceResponse();
        guidanceResponse.setStatus(GuidanceResponseStatus.SUCCESS);
        guidanceResponse.setSubject(new Reference("Patient"));
        guidanceResponse.setOutputParameters(new Reference(expectedParameters));

        Pair<GuidanceResponse, Parameters> expected = Pair.of(guidanceResponse, expectedParameters);
        Pair<GuidanceResponse, Parameters> actual = Pair.of(null, actualParameters);

        GuidanceResponseComparator comparator = new GuidanceResponseLeanComparator();
        assertTrue(comparator.compare(expected, actual));
    }

    @Test
    public void test_False() {
        Observation observation = new Observation();
        observation.setStatus(ObservationStatus.AMENDED);
        observation.setCode(new CodeableConcept(new Coding("system", "code", "display")));
        Observation observation2 = new Observation();
        observation2.setStatus(ObservationStatus.AMENDED);
        observation2.setCode(new CodeableConcept(new Coding("system", "code", "display")));

        Parameters actualParameters = new Parameters();
        List<ParametersParameterComponent> parameterList = new ArrayList<ParametersParameterComponent>();
        ParametersParameterComponent parameterComponentResource = new ParametersParameterComponent();
        parameterComponentResource.setName("param1");
        parameterComponentResource.setResource(observation);
        parameterList.add(parameterComponentResource);

        ParametersParameterComponent parameterComponentPrimitive = new ParametersParameterComponent();
        parameterComponentPrimitive.setName("param2");
        parameterComponentPrimitive.setValue(new IntegerType(45));
        parameterList.add(parameterComponentPrimitive);
        
        ParametersParameterComponent parameterComponentElement = new ParametersParameterComponent();
        Quantity quantity = new Quantity();
        quantity.setValue(5.0);
        quantity.setUnit("mm");
        quantity.setSystem("unit system");
        parameterComponentElement.setName("param3");
        parameterComponentElement.setValue(quantity);
        parameterList.add(parameterComponentElement);
        actualParameters.setParameter(parameterList);

        Parameters expectedParameters = new Parameters();
        List<ParametersParameterComponent> parameterList2 = new ArrayList<ParametersParameterComponent>();
        ParametersParameterComponent parameterComponentResource2 = new ParametersParameterComponent();
        parameterComponentResource2.setName("param1");
        parameterComponentResource2.setResource(observation2);
        parameterList2.add(parameterComponentResource2);

        ParametersParameterComponent parameterComponentPrimitive2 = new ParametersParameterComponent();
        parameterComponentPrimitive2.setName("param2");
        parameterComponentPrimitive2.setValue(new IntegerType(45));
        parameterList2.add(parameterComponentPrimitive2);
        
        ParametersParameterComponent parameterComponentElement2 = new ParametersParameterComponent();
        Quantity quantity2 = new Quantity();
        quantity2.setValue(6.0);
        quantity2.setUnit("mm");
        quantity2.setSystem("unit system");
        parameterComponentElement2.setName("param3");
        parameterComponentElement2.setValue(quantity2);
        parameterList2.add(parameterComponentElement2);
        expectedParameters.setParameter(parameterList2);

        GuidanceResponse guidanceResponse = new GuidanceResponse();
        guidanceResponse.setStatus(GuidanceResponseStatus.SUCCESS);
        guidanceResponse.setSubject(new Reference("Patient"));
        guidanceResponse.setOutputParameters(new Reference(expectedParameters));

        Pair<GuidanceResponse, Parameters> expected = Pair.of(guidanceResponse, expectedParameters);
        Pair<GuidanceResponse, Parameters> actual = Pair.of(null, actualParameters);

        GuidanceResponseComparator comparator = new GuidanceResponseLeanComparator();
        assertFalse(comparator.compare(expected, actual));
    }
}