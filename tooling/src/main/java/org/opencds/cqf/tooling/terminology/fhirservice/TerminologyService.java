package org.opencds.cqf.tooling.terminology.fhirservice;

import org.hl7.fhir.CodeableConcept;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.model.codesystems.ConceptSubsumptionOutcome;

/**
 * This interface is based on the terminology services interface defined in the FHIRPath extension for FHIR:
 * https://hl7.org/fhir/fhirpath.html#txapi
 *
 * However, for simplicity and coherence in the Java space, it is not a direct rendering, for example,
 * parameters are defined explicitly, rather than encoded as a URL string
 *
 * In addition, for simplicity it is bound directly to FHIR R4, where most of the resources involved are normative.
 *
 * Also, for this interface, the overloads for client-provided resources are not implemented, on the grounds that
 * from the perspective of a terminology client, value set and code system references are all that will be available.
 *
 * And lastly, in the interest of providing a simple, federated client, the interface does not use server-specific ideas,
 * it is always based on the globally unique canonical url for value sets and code systems, and always provided using the
 * string representation of a canonical URL, which may or may not include a pipe-separated version segment.
 */
public interface TerminologyService {
    // https://hl7.org/fhir/valueset-operation-expand.html
    // TODO: Consider activeOnly, as well as includeDraft and expansion parameters (see Measure Terminology Service in the QM IG)
    // TODO: Consider whether to expose paging support, or make it transparent at this layer
    ValueSet expand(String url);
    ValueSet expand(String url, Iterable<String> systemVersion);

    // https://hl7.org/fhir/codesystem-operation-lookup.html
    // TODO: Define LookupResult class
    Parameters lookup(String code, String systemUrl);
    Parameters lookup(Coding coding);

    // https://hl7.org/fhir/valueset-operation-validate-code.html
    // TODO: Define ValidateResult class
    Parameters validateCodeInValueSet(String url, String code, String systemUrl, String display);
    Parameters validateCodingInValueSet(String url, Coding code);
    Parameters validateCodeableConceptInValueSet(String url, CodeableConcept concept);

    // https://hl7.org/fhir/codesystem-operation-validate-code.html
    Parameters validateCodeInCodeSystem(String url, String code, String systemUrl, String display);
    Parameters validateCodingInCodeSystem(String url, Coding code);
    Parameters validateCodeableConceptInCodeSystem(String url, CodeableConcept concept);

    // https://hl7.org/fhir/codesystem-operation-subsumes.html
    ConceptSubsumptionOutcome subsumes(String codeA, String codeB, String systemUrl);
    ConceptSubsumptionOutcome subsumes(Coding codeA, Coding codeB);

    IBaseResource getResource(String url);

    // https://hl7.org/fhir/conceptmap-operation-translate.html
    // TODO: Translation support
}
