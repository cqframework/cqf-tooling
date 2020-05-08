package org.opencds.cqf.validation.profiles.cqfmeasures;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.validation.FhirValidator;
import org.opencds.cqf.Operation;

public abstract class ProfileValidation extends Operation {

    private FhirContext context;
    private JsonParser parser;
    private FhirValidator validator;

    public ProfileValidation(FhirContext context) {
        this.context = context;
        parser = (JsonParser) context.newJsonParser();
        validator = context.newValidator();
    }

    public FhirContext getContext() {
        return context;
    }

    public void setContext(FhirContext context) {
        this.context = context;
    }

    public JsonParser getParser() {
        return parser;
    }

    public void setParser(JsonParser parser) {
        this.parser = parser;
    }

    public FhirValidator getValidator() {
        return validator;
    }

    public void setValidator(FhirValidator validator) {
        this.validator = validator;
    }
}
