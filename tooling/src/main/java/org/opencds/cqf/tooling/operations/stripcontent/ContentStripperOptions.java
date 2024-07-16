package org.opencds.cqf.tooling.operations.stripcontent;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// Intentionally package-private. This is a package-internal API for ContentStripper
class ContentStripperOptions {
    static final String CQL_CONTENT_TYPE = "text/cql";
    static final String ELM_JSON_CONTENT_TYPE =  "application/elm+json";
    static final String ELM_XML_CONTENT_TYPE = "application/elm+xml";

    static final Set<String> DEFAULT_STRIPPED_CONTENT_TYPES = new HashSet<>(
            Arrays.asList(ELM_JSON_CONTENT_TYPE, ELM_XML_CONTENT_TYPE));

    static final Set<String> DEFAULT_STRIPPED_EXTENSION_URLS = new HashSet<>(
            Arrays.asList("http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-parameter",
                    "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-dataRequirement",
                    "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-logicDefinition",
                    "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-softwaresystem",
                    "http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/cqfm-directReferenceCode",
                    "http://hl7.org/fhir/StructureDefinition/cqf-cqlOptions"));

    private ContentStripperOptions() {
        // Intentionally empty, forces use of the static factory
    }

    public static ContentStripperOptions defaultOptions() {
        return new ContentStripperOptions();
    }

    private File cqlExportDirectory;
    public File cqlExportDirectory() {
        return cqlExportDirectory;
    }
    public ContentStripperOptions cqlExportDirectory(File cqlExportDirectory) {
        this.cqlExportDirectory = cqlExportDirectory;
        return this;
    }

    private Set<String> strippedContentTypes = DEFAULT_STRIPPED_CONTENT_TYPES;
    public Set<String> strippedContentTypes() {
        return this.strippedContentTypes;
    }

    public ContentStripperOptions strippedContentTypes(Set<String> strippedContentTypes) {
        this.strippedContentTypes = strippedContentTypes;
        return this;
    }

    private Set<String> strippedExtensionUrls = DEFAULT_STRIPPED_EXTENSION_URLS;
    public Set<String> strippedExtensionUrls() {
        return this.strippedExtensionUrls;
    }

    public ContentStripperOptions strippedExtensionUrls(Set<String> strippedExtensionUrls) {
        this.strippedExtensionUrls = strippedExtensionUrls;
        return this;
    }
  
}