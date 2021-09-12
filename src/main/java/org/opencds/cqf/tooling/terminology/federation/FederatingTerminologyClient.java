package org.opencds.cqf.tooling.terminology.federation;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.model.codesystems.ConceptSubsumptionOutcome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FederatingTerminologyClient implements TerminologyService {

    private FhirContext context;
    public FederatingTerminologyClient(FhirContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context is required");
        }
        this.context = context;
    }

    private List<Endpoint> terminologyEndpoints = new ArrayList<Endpoint>();
    public FederatingTerminologyClient withTerminologyEndpoint(Endpoint endpoint) {
        if (endpoint == null) {
            throw new IllegalArgumentException("endpoint is required");
        }
        terminologyEndpoints.add(endpoint);
        return this;
    }

    private Map<String, Endpoint> preferredTerminologyEndpoints = new HashMap<String, Endpoint>();
    public FederatingTerminologyClient withPreferredTerminologyEndpoint(String baseUrl, Endpoint endpoint) {
        if (baseUrl == null) {
            throw new IllegalArgumentException("baseUrl is required");
        }
        if (endpoint == null) {
            throw new IllegalArgumentException("endpoint is required");
        }

        if (!(baseUrl.endsWith("/"))) {
            baseUrl = baseUrl + "/";
        }

        preferredTerminologyEndpoints.put(baseUrl, endpoint);
        return this;
    }

    private FhirTerminologyClient getClient(Endpoint endpoint) {
        // TODO: Cache terminology clients...
        return new FhirTerminologyClient(context, endpoint);
    }

    private Endpoint getPreferredEndpoint(String url) {
        if (url == null) {
            throw new IllegalArgumentException("url is required");
        }

        for (Map.Entry<String, Endpoint> e : preferredTerminologyEndpoints.entrySet()) {
            if (url.startsWith(e.getKey())) {
                return e.getValue();
            }
        }

        return null;
    }

    @Override
    public ValueSet expand(String url) {
        // TODO: Genericize this logic, it will be the same for all operations
        ValueSet result = null;
        Endpoint ep = getPreferredEndpoint(url);
        if (ep != null) {
            // TODO: Exception handling behavior
            result = getClient(ep).expand(url);
        }

        if (result == null) {
            for (Endpoint tep : terminologyEndpoints) {
                result = getClient(tep).expand(url);
                if (result != null) {
                    break;
                }
            }
        }

        return result;
    }

    @Override
    public ValueSet expand(String url, Iterable<String> systemVersion) {
        ValueSet result = null;
        Endpoint ep = getPreferredEndpoint(url);
        if (ep != null) {
            result = getClient(ep).expand(url, systemVersion);
        }

        if (result == null) {
            for (Endpoint tep : terminologyEndpoints) {
                result = getClient(tep).expand(url);
                if (result != null) {
                    break;
                }
            }
        }

        return result;
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
    public Parameters validateCodeInValueSet(String url, String code, String systemUrl, String display) {
        throw new UnsupportedOperationException("validateCodeInValueSet(url, code, systemUrl, display)");
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
}
