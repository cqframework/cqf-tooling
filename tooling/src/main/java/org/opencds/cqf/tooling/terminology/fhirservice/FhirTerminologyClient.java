package org.opencds.cqf.tooling.terminology.fhirservice;

import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import org.hl7.fhir.CodeableConcept;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.codesystems.ConceptSubsumptionOutcome;
import org.opencds.cqf.tooling.terminology.SpreadsheetValidateVSandCS;
import org.opencds.cqf.tooling.utilities.CanonicalUtils;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.AdditionalRequestHeadersInterceptor;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.gclient.IOperationUntyped;
import ca.uhn.fhir.rest.gclient.IOperationUntypedWithInputAndPartialOutput;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.Process;

import javax.swing.*;

public class FhirTerminologyClient implements TerminologyService {

    private static final Logger logger = LoggerFactory.getLogger(FhirTerminologyClient.class);

    private IGenericClient client;
    public FhirTerminologyClient(IGenericClient client) {
        if (client == null) {
            throw new IllegalArgumentException("client is required");
        }

        this.client = client;
    }

    private FhirContext context;
    private Endpoint endpoint;
    public FhirTerminologyClient(FhirContext context, Endpoint endpoint, String userName, String password) {
        if (context == null) {
            throw new IllegalArgumentException("context is required");
        }
        this.context = context;

        if (endpoint == null) {
            throw new IllegalArgumentException("endpoint is required");
        }
        this.endpoint = endpoint;

        this.client = context.newRestfulGenericClient(endpoint.getAddress());

        BasicAuthInterceptor authInterceptor = new BasicAuthInterceptor(userName, password);
        client.registerInterceptor(new LoggingInterceptor());
        client.registerInterceptor(authInterceptor);
        if (endpoint.hasHeader()) {
            AdditionalRequestHeadersInterceptor interceptor = new AdditionalRequestHeadersInterceptor();
            for (StringType header : endpoint.getHeader()) {
                String[] headerValues = header.getValue().split(":");
                if (headerValues.length == 2) {
                    interceptor.addHeaderValue(headerValues[0], headerValues[1]);
                }
                // TODO: Log malformed headers in the endpoint
            }
            client.registerInterceptor(interceptor);
        }
    }

    private RuntimeException toException(OperationOutcome outcome) {
        // TODO: Improve outcome to exception processing
        if (outcome.hasIssue()) {
            return new RuntimeException(String.format("%s.%s", outcome.getIssueFirstRep().getCode(), outcome.getIssueFirstRep().getDetails()));
        }
        else {
            return new RuntimeException("Errors occurred but no details were returned");
        }
    }

    private boolean treatCanonicalTailAsLogicalId = false;
    public boolean getTreatCanonicalTailAsLogicalId() {
        return this.treatCanonicalTailAsLogicalId;
    }
    public FhirTerminologyClient setTreatCanonicalTailAsLogicalId(boolean treatCanonicalTailAsLogicalId) {
        this.treatCanonicalTailAsLogicalId = treatCanonicalTailAsLogicalId;
        return this;
    }

    private Object prepareExpand(String url) {
        String canonical = url;
        String version = CanonicalUtils.getVersion(url);
        IOperationUntyped operation = null;
        IOperationUntypedWithInputAndPartialOutput<Parameters> operationWithInput = null;
        if (treatCanonicalTailAsLogicalId) {
            operation = this.client.operation()
                    .onInstance(String.format("ValueSet/%s", CanonicalUtils.getId(canonical)))
                    .named("expand");
            if (version != null) {
                operationWithInput = operation.withParameter(Parameters.class, "valueSetVersion", new StringType().setValue(version));
            }
        }
        else {
            operation = this.client.operation()
                    .onType(ValueSet.class)
                    .named("expand");
            operationWithInput = operation.withParameter(Parameters.class, "url", new UriType().setValue(canonical));
            if (version != null) {
                operationWithInput = operationWithInput.andParameter("valueSetVersion", new StringType().setValue(version));
            }
        }
        return operationWithInput != null ? operationWithInput : operation;
    }

    private ValueSet processResultAsValueSet(Object result, String operation) {
        if (result instanceof ValueSet) {
            return (ValueSet)result;
        }
        else if (result instanceof OperationOutcome) {
            throw toException((OperationOutcome)result);
        }
        else if (result == null) {
            throw new RuntimeException(String.format("No result returned when invoking %s", operation));
        }
        else {
            throw new RuntimeException(String.format("Unexpected result type %s when invoking %s", result.getClass().getName(), operation));
        }
    }

    @Override
    @SuppressWarnings("unchecked") // Probably shouldn't be doing this, but it tells me I have an unchecked cast, but it won't let me check the instance of the parameterized generic...
    public ValueSet expand(String url) {
        Object operationObject = prepareExpand(url);
        IOperationUntyped operation = operationObject instanceof IOperationUntyped ? (IOperationUntyped)operationObject : null;
        IOperationUntypedWithInputAndPartialOutput<Parameters> operationWithInput = operationObject instanceof IOperationUntypedWithInputAndPartialOutput
                ? (IOperationUntypedWithInputAndPartialOutput<Parameters>)operationObject : null;

        Object result = operationWithInput != null ? operationWithInput.execute() : operation.withNoParameters(Parameters.class).execute();
        return processResultAsValueSet(result, "expand");
    }

    @Override
    @SuppressWarnings("unchecked") // Probably shouldn't be doing this, but it tells me I have an unchecked cast, but it won't let me check the instance of the parameterized generic...
    public ValueSet expand(String url, Iterable<String> systemVersion) {
        Object operationObject = prepareExpand(url);
        IOperationUntyped operation = operationObject instanceof IOperationUntyped ? (IOperationUntyped)operationObject : null;
        IOperationUntypedWithInputAndPartialOutput<Parameters> operationWithInput = operationObject instanceof IOperationUntypedWithInputAndPartialOutput
                ? (IOperationUntypedWithInputAndPartialOutput<Parameters>)operationObject : null;
        if (systemVersion != null) {
            for (String sv : systemVersion) {
                if (operationWithInput == null) {
                    operationWithInput = operation.withParameter(Parameters.class, "system-version", new CanonicalType().setValue(sv));
                }
                else {
                    operationWithInput = operationWithInput.andParameter("system-version", new CanonicalType().setValue(sv));
                }
            }
        }

        Object result = operationWithInput != null ? operationWithInput.execute() : operation.withNoParameters(Parameters.class).execute();
        return processResultAsValueSet(result, "expand");
    }

    @Override
    public Parameters lookup(String code, String systemUrl) {
        throw new UnsupportedOperationException("lookup(code, systemUrl)");
    }

    @Override
    public Parameters lookup(Coding coding) {
        throw new UnsupportedOperationException("lookup(coding)");
    }

    @Override
    public Parameters validateValueSet(String url, String pathToIG, String jarPath, String outputPath, String fhirVersion){
        String vsToValidate = getAndSaveValueSetFromURL(url, outputPath);
        if(vsToValidate != null){
            String callJar = "java -jar " + jarPath + " " + vsToValidate + " -version " + fhirVersion + " -ig " + pathToIG;
            StringBuffer sbResults = new StringBuffer();

            try{
                Process proc = Runtime.getRuntime().exec(callJar);
                final BufferedReader reader = new BufferedReader(
                        new InputStreamReader(proc.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    sbResults.append(line + System.lineSeparator());
                }
                reader.close();
                final BufferedReader errReader = new BufferedReader(
                        new InputStreamReader(proc.getErrorStream()));
                String errLine;
                while ((errLine = errReader.readLine()) != null) {
                    sbResults.append(errLine + System.lineSeparator());
                }
                System.out.println(sbResults.toString());
                errReader.close();
                saveResults(sbResults.toString(), vsToValidate.substring(vsToValidate.lastIndexOf(File.separator)+1, vsToValidate.lastIndexOf(".")), outputPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            return null;
        }
        return new Parameters();
    }

    private void saveResults(String results, String fileName, String outputPath){
        createOutputLocations(outputPath);
        boolean wasFailure = results.toUpperCase().contains("*FAILURE*");
        String outputPathFileLocation = null;
        if(wasFailure){
            fileName = fileName + "_FAIL" + ".txt";
            outputPathFileLocation = outputPath + File.separator + "Fail";
        }else{
            fileName = fileName + "_PASS" + ".txt";
            outputPathFileLocation = outputPath + File.separator + "Pass";
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPathFileLocation + File.separator + fileName))) {
            if(wasFailure){
                writer.write(results.substring(results.indexOf("*FAILURE*")));
            }else {
                writer.write(results.substring(results.indexOf("Success")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createOutputLocations(String outputPath){
        File passDirectory = new File(outputPath + File.separator + "Pass");
        if(!passDirectory.exists()){
            try {
                boolean created = passDirectory.mkdirs();
                if(!created){
                    throw new IOException("Unable to create a Pass directory for results.");
                }
            }catch(IOException ioEx){
                logger.error(ioEx.getMessage());
            }
        }
        File failDirectory = new File(outputPath + File.separator + "Fail");
        if(!failDirectory.exists()){
            try {
                boolean created = failDirectory.mkdirs();
                if(!created){
                    throw new IOException("Unable to create a Fail directory for results.");
                }
            }catch(IOException ioEx){
                logger.error(ioEx.getMessage());
            }
        }
    }

    private String getAndSaveValueSetFromURL(String url, String outputPath){
        // CHANGE OUTPUT TO BE IN A DIRECTORY SEPARATE FROM THE ACTUAL OUTPUT DIR, A SIBLING TO FAIL/PASS DIRS
        String pathAndVSToValidate = null;
        Bundle readBundle = this.client.search().byUrl(url).returnBundle(Bundle.class).execute();
        if(readBundle.hasEntry()) {
            ValueSet vsToValidate = (ValueSet) readBundle.getEntry().get(0).getResource();
            String vsURL = null;
            if(vsToValidate.hasId()) {
                vsURL = vsToValidate.getId();
            } else {
                vsURL = vsToValidate.getUrl();
            }
            if(vsURL != null) {
                String fileName = vsURL.substring(vsURL.lastIndexOf(File.separator)+1);
                IOUtils.writeResource(vsToValidate, outputPath, IOUtils.Encoding.JSON, context, false, fileName);
                pathAndVSToValidate = outputPath + File.separator + fileName + ".json";
            }
        }
        return pathAndVSToValidate;
    }

    @Override
    public Parameters validateCodeInValueSet(String url, String code, String systemUrl, String display) {
        // expand VS then check to see if code in VS
        Bundle readBundle = this.client.search().byUrl(url).returnBundle(Bundle.class).execute();
        if(readBundle.hasEntry()){
//            System.out.println(readBundle.getEntry().get(0).getResource().getId());
            //do more stuff, like validate
        }else{
            return null;
        }
        return new Parameters();

//        throw new UnsupportedOperationException("validateCodeInValueSet(url, code, systemUrl, display)");
    }

    @Override
    public Parameters validateCodingInValueSet(String url, Coding code) {
        throw new UnsupportedOperationException("validateCodingInValueSet(url, code)");
    }

    @Override
    public Parameters validateCodeableConceptInValueSet(String url, CodeableConcept concept) {
        throw new UnsupportedOperationException("validateCodeableConceptInValueSet(url, concept)");
    }

    @Override
    public Parameters validateCodeInCodeSystem(String url, String code, String systemUrl, String display) {
        throw new UnsupportedOperationException("validateCodeInCodeSystem(url, code, systemUrl, display)");
    }

    @Override
    public Parameters validateCodingInCodeSystem(String url, Coding code) {
        throw new UnsupportedOperationException("validateCodingInCodeSystem(url, code)");
    }

    @Override
    public Parameters validateCodeableConceptInCodeSystem(String url, CodeableConcept concept) {
        throw new UnsupportedOperationException("validateCodeableConceptInCodeSystem(url, concept)");
    }

    @Override
    public ConceptSubsumptionOutcome subsumes(String codeA, String codeB, String systemUrl) {
        throw new UnsupportedOperationException("subsumes(codeA, codeB, systemUrl)");
    }

    @Override
    public ConceptSubsumptionOutcome subsumes(Coding codeA, Coding codeB) {
        throw new UnsupportedOperationException("subsumes(codeA, codeB)");
    }
/*
  Build a FHIRTerminologyClient component that facilitates use of an R4 FHIR Terminology Service. Specifically implement:

    resolveValueSet(canonical) (with or without a version reference)
    resolveCodeSystem(canonical) with or without a version reference)
    inValueSet(CodeableConcept, canonical)
    inCodeSystem(CodeableConcept, canonical)
    expandValueSet(canonical)
    */
}
