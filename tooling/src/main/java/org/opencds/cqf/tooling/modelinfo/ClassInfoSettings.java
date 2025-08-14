package org.opencds.cqf.tooling.modelinfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@SuppressWarnings("serial")
public class ClassInfoSettings {
    public String modelName;
    public String modelPrefix;
    public String helpersLibraryName;
    public boolean useCQLPrimitives = false;
    public boolean includeMetaData = true;
    public boolean createSliceElements = false;
    public boolean createExtensionElements = false;
    public boolean createReferenceElements = false;

    public boolean flatten = false;

    public Set<String> codeableTypes = new HashSet<String>();
    public Map<String, String> primitiveTypeMappings = new HashMap<String, String>();
    public Map<String, String> cqlTypeMappings = new HashMap<String, String>();
    public Map<String, String> primaryCodePath = new HashMap<String, String>();
    public Map<String, String> typeNameMappings;

    public Map<String, String> urlToModel =  new HashMap<String, String>() {
        { 
            put("urn:hl7-org:elm-types:r1", "System");
            put("http://hl7.org/fhir", "FHIR");
            put("http://hl7.org/fhir/us/core", "USCore");
            put("http://hl7.org/fhir/us/qicore", "QICore");
        }
    };
}
