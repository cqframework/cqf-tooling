package org.opencds.cqf.modelinfo;

import org.hl7.elm.r1.Except;
import org.hl7.elm_modelinfo.r1.ChoiceTypeSpecifier;
import org.hl7.elm_modelinfo.r1.ClassInfoElement;
import org.hl7.elm_modelinfo.r1.ListTypeSpecifier;
import org.hl7.elm_modelinfo.r1.NamedTypeSpecifier;
import org.hl7.elm_modelinfo.r1.TypeInfo;
import org.hl7.elm_modelinfo.r1.TypeSpecifier;

import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Element;
import org.hl7.fhir.dstu3.model.ElementDefinition;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.dstu3.model.ElementDefinition.TypeRefComponent;
import org.hl7.fhir.dstu3.model.Enumerations.BindingStrength;
import org.hl7.fhir.dstu3.model.StructureDefinition.StructureDefinitionKind;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.lang.model.element.TypeElement;

import org.antlr.v4.parse.ANTLRParser.ruleReturns_return;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.opencds.cqf.Operation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import guru.nidi.graphviz.attribute.RankDir;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.LinkSource;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.model.Node;
import guru.nidi.graphviz.model.PortNode;

import com.google.common.io.Files;
import com.google.gson.*;

import static guru.nidi.graphviz.model.Factory.*;

public class StructureDefinitionToModelInfo extends Operation {

    public class ClassInfo extends TypeInfo {

        private String name;
        private String label;
        private Boolean retriveable;
        private List<ClassInfoElement> elements;
        private String primaryCodePath;

        ClassInfo(String name, String label, String baseType, Boolean retrievable, List<ClassInfoElement> elements, String primaryCodePath)  {
            this.setBaseType(baseType);
            this.name = name;
            this.label = label;
            this.retriveable = retrievable;
            this.elements = elements;
            this.primaryCodePath = primaryCodePath;
        }

        public String getName() {
            return this.name;
        }

        public String getLabel() {
            return this.label;
        }

        public Boolean getRetriveable() {
            return this.retriveable;
        }

        public List<ClassInfoElement> getElements() {
            return this.elements;
        }

        private String getPrimaryCodePath() {
            return this.primaryCodePath;
        }
    }



    private Map<String, TypeInfo> typeInfos = new HashMap<String, TypeInfo>();

    public static <T> Predicate<T> distinctByKey(
        Function<? super T, ?> keyExtractor) {  
        Map<Object, Boolean> seen = new ConcurrentHashMap<>(); 
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null; 
    }

    private static Set<String> codeableTypes = new HashSet<String>() {
        {
            add("System.String"); 
            add("System.Code");
            add("System.Concept");
            add("FHIR.string");
            add("FHIR.code");
            add("FHIR.Coding");
            add("FHIR.CodeableConcept");
        }
    };


    private static Map<String, Boolean> modelInfoSettings = new HashMap<String, Boolean>() {
        {
            put("UseCQLPrimitives", false);
            put("CreateExtensionElements", false);
            put("CreateReferenceElements", false);
        }
    };


    private static Map<String, String> models = new HashMap<String, String>() {
        {
            put("System", "urn:hl7-org:elm-types:r1");
            put("FHIR", "http://hl7.org/fhir");
            put("USCore", "http://hl7.org/fhir/us/core");
            put("QICore", "http://hl7.org/fhir/us/qicore");
        }
    };

    private static Map<String, String> ranks = new HashMap<String, String>() {
        {
            put("System", "10");
            put("FHIR-Primitive", "20"); // Special case for primitives
            put("FHIR", "30");
            put("USCore", "40");
            put("QICore", "50");
        }
    };

    // private static Map<String, String> rankToColor = new HashMap<String,
    // String>() {
    // {
    // put("System", "1");
    // put("FHIR-Primitive", "2"); // Special case for primitives
    // put("FHIR", "3");
    // put("USCore", "4");
    // put("QICore", "5");
    // }
    // };

    private static Map<String, String> primitiveTypeMappings = new HashMap<String, String>() {
        {
            put("FHIR.base64Binary", "System.String");
            put("FHIR.boolean", "System.Boolean");
            put("FHIR.code", "System.String");
            put("FHIR.date", "System.DateTime");
            put("FHIR.dateTime", "System.DateTime");
            put("FHIR.decimal", "System.Decimal");
            put("FHIR.id", "System.String");
            put("FHIR.instant", "System.DateTime");
            put("FHIR.integer", "System.Integer");
            put("FHIR.markdown", "System.String");
            put("FHIR.oid", "System.String");
            put("FHIR.positiveInt", "System.Integer");
            put("FHIR.string", "System.String");
            put("FHIR.time", "System.Time");
            put("FHIR.unsignedInt", "System.Integer");
            put("FHIR.uri", "System.String");
            put("FHIR.uuid", "System.String");
            put("FHIR.xhtml", "System.String");
        }
    };

    private static Map<String, String> cqlTypeMappings = new HashMap<String, String>() {
        {
            put("FHIR.base64Binary", "System.String");
            put("FHIR.boolean", "System.Boolean");
            put("FHIR.code", "System.String");
            put("FHIR.date", "System.DateTime");
            put("FHIR.dateTime", "System.DateTime");
            put("FHIR.decimal", "System.Decimal");
            put("FHIR.id", "System.String");
            put("FHIR.instant", "System.DateTime");
            put("FHIR.integer", "System.Integer");
            put("FHIR.markdown", "System.String");
            put("FHIR.oid", "System.String");
            put("FHIR.positiveInt", "System.Integer");
            put("FHIR.string", "System.String");
            put("FHIR.time", "System.Time");
            put("FHIR.unsignedInt", "System.Integer");
            put("FHIR.uri", "System.String");
            put("FHIR.uuid", "System.String");
            put("FHIR.xhtml", "System.String");
            put("FHIR.Coding", "System.Code");
            put("FHIR.CodeableConcept", "System.Concept");
            put("FHIR.Period", "System.DateTime");
            put("FHIR.Range", "System.Quantity");
            put("FHIR.Quantity", "System.Quantity");
            put("FHIR.Age", "System.Quantity");
            put("FHIR.Distance", "System.Quantity");
            put("FHIR.SimpleQuantity", "System.Quantity");
            put("FHIR.Duration", "System.Quantity");
            put("FHIR.Count", "System.Quantity");
            put("FHIR.Money", "System.Quantity");
        }
    };

    private static Map<String, String> primaryCodePath = new HashMap<String, String>() {
        {
            put("ActivityDefinition", "topic");
            put("AdverseEvent", "type");
            put("AllergyIntolerance", "code");
            put("Appointment", "serviceType");
            put("Basic", "code");
            put("CarePlan", "category");
            put("CareTeam", "category");
            put("ChargeItemDefinition", "code");
            put("Claim", "type");
            put("ClinicalImpression", "code");
            put("Communication", "category");
            put("CommunicationRequest", "category");
            put("Composition", "type");
            put("Condition", "code");
            put("Consent", "category");
            put("Coverage", "type");
            put("DetectedIssue", "category");
            put("Device", "type");
            put("DeviceMetric", "type");
            put("DeviceRequest", "codeCodeableConcept");
            put("DeviceUseStatement", "device.code");
            put("DiagnosticReport", "code");
            put("Encounter", "type");
            put("EpisodeOfCare", "type");
            put("ExplanationOfBenefit", "type");
            put("Flag", "code");
            put("Goal", "category");
            put("GuidanceResponse", "module");
            put("HealthcareService", "type");
            put("Immunization", "vaccineCode");
            put("Library", "topic");
            put("Measure", "topic");
            put("MeasureReport", "measure.topic");
            put("Medication", "code");
            put("MedicationAdministration", "medicationCodeableConcept");
            put("MedicationDispense", "medicationCodeableConcept");
            put("MedicationRequest", "medicationCodeableConcept");
            put("MedicationStatement", "medicationCodeableConcept");
            put("MessageDefinition", "event");
            put("Observation", "code");
            put("OperationOutcome", "issue.code");
            put("Procedure", "code");
            put("ProcedureRequest", "code");
            put("Questionnaire", "name");
            put("ReferralRequest", "type");
            put("RiskAssessment", "code");
            put("SearchParameter", "target");
            put("Sequence", "type");
            put("Specimen", "type");
            put("Substance", "code");
            put("SupplyDelivery", "type");
            put("SupplyRequest", "category");
            put("Task", "code");
        }
    };

    Map<String, StructureDefinition> structureDefinitions = null;

    // TODO: System TypeInfos

    @Override
    public void execute(String[] args) {
        if (args.length > 2) {
            setOutputPath(args[2]);
        } else {
            setOutputPath("src/main/resources/org/opencds/cqf/modelinfo/output");
        }

        String inputPath = null;
        if (args.length > 1) {
            inputPath = args[1];
        } else {
            inputPath = "../FHIR-Spec";
        }

        List<StructureDefinition> resources = new ArrayList<StructureDefinition>();

        System.out.println("Reading 3.0.1 StructureDefinitions");
        resources.addAll(readStructureDefFromFolder(inputPath + "/3.0.1"));

        // System.out.println("Reading US-Core 1.0.1 StructureDefinitions");
        // resources.addAll(readStructureDefFromFolder(inputPath + "/US-Core/1.0.1"));

        // System.out.println("Reading QI-Core 2.0.0 StructureDefinitions");
        // resources.addAll(readStructureDefFromFolder(inputPath + "/QI-Core/2.0.0"));

        // System.out.println("Reading QI-Core 3.1.0 StructureDefinitions");
        // resources.addAll(readStructureDefFromFolder(inputPath + "/QI-Core/3.1.0"));

        System.out.println("Indexing StructureDefinitions by Id");
        structureDefinitions = indexResources(resources);

        System.out.println("Creating dependency graph for StructureDefinitions");
        MutableGraph g = createDependencyGraph(structureDefinitions);

        try {
            // writeOutput("bubba.txt", "test");
            Graphviz.fromGraph(g).render(Format.SVG).toFile(new File("example/fhir-graph.svg"));
        } catch (IOException e) {
            System.err.println("Encountered the following exception while creating file " + "bubba" + e.getMessage());
            e.printStackTrace();
            return;
        }

    }

    private void writeOutput(String fileName, String content) throws IOException {
        try (FileOutputStream writer = new FileOutputStream(getOutputPath() + "/" + fileName)) {
            writer.write(content.getBytes());
            writer.flush();
        }
    }

    private String urlToId(String url) {
        int index = url.lastIndexOf("/StructureDefinition/");
        if (index > -1) {
            return url.substring(index + 21, url.length());
        }

        return url;

    }

    private MutableGraph createDependencyGraph(Map<String, StructureDefinition> resourcesById) {

        Map<String, MutableNode> nodes = new HashMap<>();

        // Add a few nodes for base spec
        MutableNode system = mutNode("System").add(Shape.RECTANGLE);
        MutableNode fhir = mutNode("FHIR").add(Shape.RECTANGLE);
        MutableNode uscore = mutNode("USCore").add(Shape.RECTANGLE);
        MutableNode qicore = mutNode("QICore").add(Shape.RECTANGLE);

        fhir = fhir.addLink(system);
        uscore = uscore.addLink(fhir);
        qicore = qicore.addLink(uscore);

        nodes.put("System", system);
        nodes.put("FHIR", fhir);
        nodes.put("USCore", uscore);
        nodes.put("QICore", qicore);

        // Add a node representing each Id;
        for (Entry<String, StructureDefinition> entry : resourcesById.entrySet()) {
            String key = entry.getKey();
            // TODO: Set node properties...
            nodes.put(key, mutNode(key).add(Shape.RECTANGLE));
        }

        for (Entry<String, StructureDefinition> entry : resourcesById.entrySet()) {
            String key = entry.getKey();
            StructureDefinition sd = entry.getValue();
            MutableNode node = nodes.get(key);

            if (sd.getKind() == StructureDefinitionKind.PRIMITIVETYPE) {
                fhir = nodes.get("FHIR");
                node.addLink(fhir);
            }

            if (sd.hasSnapshot()) {
                List<ElementDefinition> elements = sd.getSnapshot().getElement();

                for (ElementDefinition ed : elements) {
                    PortNode p = node.port(ed.getPath());
                    Set<String> dependencies = new HashSet<>();

                    for (ElementDefinition.TypeRefComponent trc : ed.getType()) {
                        if (trc.hasProfile()) {
                            dependencies.add(trc.getProfile());
                        }

                        if (trc.hasTargetProfile()) {
                            dependencies.add(trc.getTargetProfile());
                        }
                        // System.out.println(trc.getProfile());
                    }

                    if (ed.hasExtension()) {
                        dependencies.addAll(getExtensionRecursive(ed.getExtension()));
                    }

                    for (String url : dependencies) {
                        String id = urlToId(url);
                        if (nodes.containsKey(id)) {
                            MutableNode dependency = nodes.get(id);
                            node.addLink(dependency);
                        }
                    }
                }
            }
        }

        // return resourceDependenciesById

        // List<LinkSource> links = new ArrayList<LinkSource>();

        // // Next, create links as appropriate;
        // for (Entry<String, Set<String>> entry : resourceDependenciesById.entrySet())
        // {

        // String key = entry.getKey();
        // Node node = null;
        // if (nodes.containsKey(key)) {
        // node = nodes.get(key);
        // }
        // else {
        // System.out.println("Missing node for: " + key);
        // continue;
        // }

        // for (String dependency : entry.getValue()) {
        // Node dependencyNode = null;
        // if (nodes.containsKey(dependency)) {
        // dependencyNode = nodes.get(dependency);
        // }
        // else {
        // System.out.println("Missing node for dependency: " + dependency);
        // continue;
        // }

        // node = node.link(dependencyNode);
        // }

        // nodes.put(key, node);
        // }

        List<LinkSource> links = new ArrayList<>();
        links.addAll(nodes.values());

        return mutGraph("FHIR").setDirected(true).add(links);
    }

    private Set<String> getExtensionRecursive(List<Extension> extensions) {
        Set<String> urls = new HashSet<>();

        for (Extension ex : extensions) {
            if (ex.hasUrl() && !urls.contains(ex.getUrl())) {
                urls.add(ex.getUrl());
            }

            if (ex.hasExtension()) {
                Set<String> dependencies = getExtensionRecursive(ex.getExtension());

                urls.addAll(dependencies);
            }
        }

        return urls;
    }

    private Map<String, StructureDefinition> indexResources(List<StructureDefinition> resources) {
        Map<String, StructureDefinition> resourcesById = new HashMap<String, StructureDefinition>();
        for (StructureDefinition sd : resources) {
            String id = urlToId(sd.getUrl());
            if (!resourcesById.containsKey(id)) {
                resourcesById.put(id, sd);
            } else {
                System.out.println("Duplicate url found for: " + sd.getUrl());
            }
        }

        return resourcesById;
    }

    private List<StructureDefinition> readStructureDefFromFolder(String path) {
        Collection<File> files = getFiles(path);

        IParser parser = FhirContext.forDstu3().newJsonParser();

        List<StructureDefinition> objects = new ArrayList<StructureDefinition>();

        for (File f : files) {

            try {
                String content = Files.asCharSource(f, Charset.forName("UTF-8")).read();
                IBaseResource resource = parser.parseResource(content);

                if (resource instanceof StructureDefinition) {
                    objects.add((StructureDefinition) resource);
                } else if (resource instanceof Bundle) {
                    objects.addAll(unrollBundles((Bundle) resource));
                }
            } catch (IOException e) {

            }
        }

        return objects;
    }

    private Collection<File> getFiles(String path) {
        File folder = new File(path);
        return FileUtils.listFiles(folder, new WildcardFileFilter("*.json"), null);
    }

    private List<StructureDefinition> unrollBundles(Bundle bundle) {
        List<StructureDefinition> resources = new ArrayList<StructureDefinition>();
        if (bundle.hasEntry()) {
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                if (entry.hasResource()) {
                    Resource r = entry.getResource();
                    if (r.getResourceType() == ResourceType.StructureDefinition) {
                        resources.add((StructureDefinition) r);
                    } else if (r.getResourceType() == ResourceType.Bundle) {
                        resources.addAll(unrollBundles((Bundle) r));
                    }
                }
            }
        }

        return resources;
    }

    private String getHead(String url) {
        int index = url.lastIndexOf("/");
        if (index == -1) {
            return null;
        } else if (index > 0) {
            return url.substring(0, index);
        } else {
            return "";
        }
    }

    private String getTail(String url) {
        int index = url.lastIndexOf("/");
        if (index == -1) {
            return null;
        } else if (index > 0) {
            return url.substring(index + 1);
        } else {
            return "";
        }
    }

    private String resolveModelName(String url) throws Exception {
        // Strips off the identifier and type name
        String model = getHead(getHead(url));
        for (Entry<String, String> e : models.entrySet()) {
            if (e.getValue().equals(model)) {
                return e.getKey();
            }

        }

        throw new Exception("Couldn't resolve model name for url: " + url);
    }

    private String resolveTypeName(String url) throws Exception {

        String modelName = resolveModelName(url);
        return getTypeName(modelName, getTail(url));

    }

    private String getTypeName(String modelName, String typeName) {
        return modelName != null ? modelName + "." + typeName : typeName;
    }

    private String getTypeName(NamedTypeSpecifier typeSpecifier) {
        return this.getTypeName(typeSpecifier.getModelName(), typeSpecifier.getName());
    }

    private TypeInfo resolveType(String name) {
        return this.typeInfos.get(name);
    }

    private TypeInfo resolveType(TypeSpecifier typeSpecifier) {
        if (typeSpecifier instanceof NamedTypeSpecifier) {
            return this.resolveType(((NamedTypeSpecifier)typeSpecifier).getName());
        }
        else {
            ListTypeSpecifier lts = (ListTypeSpecifier)typeSpecifier;
            if (lts.getElementType() != null) {
                return this.resolveType(lts.getElementType());
            }
            else {
                return this.resolveType(lts.getElementTypeSpecifier());
            }
        }
    }

    private TypeInfo resolveType(ClassInfoElement classInfoElement) {
        if (classInfoElement.getType() != null) {
            return this.resolveType(classInfoElement.getType());
        } else {
            return this.resolveType(classInfoElement.getTypeSpecifier());
        }
    }

    private String getQualifier(String name) {
        int index = name.indexOf(".");
        if (index > 0) {
            return name.substring(0, index);
        }

        return null;
    } 

    private String unQualify(String name) {
        int index = name.indexOf(".");
        if (index > 0) {
            return name.substring(index + 1);
        }

        return null;
    }

    private TypeSpecifier buildTypeSpecifier(String modelName, String typeName) {
        NamedTypeSpecifier ts = new NamedTypeSpecifier();
        ts.setModelName(modelName);
        ts.setName(typeName);

        return ts;
    }

    private TypeSpecifier buildTypeSpecifier(String typeName) {
        return this.buildTypeSpecifier(this.getQualifier(typeName), this.unQualify(typeName));
    }

    // Builds a TypeSpecifier from the given list of TypeRefComponents
    private TypeSpecifier buildTypeSpecifier(String modelName, TypeRefComponent typeRef) {
        try {
            if (typeRef.getProfile() != null) {
                return this.buildTypeSpecifier(this.resolveTypeName(typeRef.getProfile()));
            }
            else {

                    if (modelInfoSettings.get("UseCQLPrimitives")) {
                        String typeName = cqlTypeMappings.get(modelName + "." + typeRef.getCode());
                        return this.buildTypeSpecifier(typeName);
                    }
                    else {
                        return this.buildTypeSpecifier(modelName, typeRef.getCode());
                    }
            
            }
        }
        catch (Exception e) {
            System.out.println("Error building type specificer for " + modelName + "." + typeRef.getCode() +": "+  e.getMessage());
            return null;
        }
    }

    // Gets the type specifier for the given class info element
    private TypeSpecifier buildTypeSpecifier(String modelName, List<TypeRefComponent> typeReferencRefComponents) throws Exception {
       List<TypeSpecifier> specifiers = typeReferencRefComponents.stream()
            .map(x -> this.buildTypeSpecifier(modelName, x))
            .filter(distinctByKey(x -> x.toString()))
            .collect(Collectors.toList());

        if (specifiers.size() == 1) {
            return specifiers.get(0);
        }
        else if (specifiers.size() > 1) {
            ChoiceTypeSpecifier cts = new ChoiceTypeSpecifier();
            return cts.withChoice(specifiers);
        }
        else {
            return null;
        }
    }

    // Returns the given string with the first letter capitalized
    private TypeSpecifier getTypeSpecifier(ClassInfoElement classInfoElement) {
        if (classInfoElement.getType() != null) {
            return this.buildTypeSpecifier(
                this.getQualifier(classInfoElement.getType()), 
                this.unQualify(classInfoElement.getType()));
        }
        else if (classInfoElement.getTypeSpecifier() != null) {
            if (classInfoElement.getTypeSpecifier() instanceof ListTypeSpecifier) {
                ListTypeSpecifier lts = (ListTypeSpecifier)classInfoElement.getTypeSpecifier();
                if (lts.getElementType() != null) {
                    return this.buildTypeSpecifier(
                    this.getQualifier(lts.getElementType()), 
                    this.unQualify(lts.getElementType()));
                } else {
                    return lts.getElementTypeSpecifier();
                }
            }
        }

        return null;
    }

    // Returns the given path with the first letter of every path capitalized
    private String capitalize(String name) {
        if (name.length() >= 1) {
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        } 

        return name;
    }

    // Returns the name of the component type used to represent anonymous nested structures
    private String captitalizePath(String path) {
        return String.join(".", 
            Arrays.asList(path.split("."))
                .stream()
                .map(x -> this.capitalize(x))
                .collect(Collectors.toList()));
    }


    private String getComponentTypeName(String path) {
        return this.captitalizePath(path) + "Component";
    }

    // Strips the given root from the given path.
    // Throws an error if the path does not start with the root.
    private String stripRoot(String path, String root) throws Exception {
        int index = path.indexOf(root);
        if (index == -1) {
            throw new Exception ("Path " + path + " does not start with the root " + root + ".");
        }

        String result  = path.substring(root.length());

        if (result.startsWith(".")) {
            result = result.substring(0);
        }

        return result;
    }

    // Strips the [x] suffix of an element name which indicates a choice in FHIR
    private String stripChoice(String name) {
        int index = name.indexOf("[x]");
        if (index != -1) {
            return name.substring(0, index);
        }

        return name;
    }

    // Returns the value of the given string as an integer if it is integer-valued, nil otherwise.
    private Integer asInteger(String value) {
        try {
            return Integer.parseInt(value);
        } 
        catch (Exception e) {
            return null;
        }
    }

    // Returns true if the ElementDefinition describes a constraint only
    // For now, since constraints cannot be represented in ModelInfo, no element will be created for these
    // For now, we are assuming that if the element specifies a type, it should be declared
    // This may be a type constraint, which can be expressed in ModelInfo, so that's okay, but if all
    // the element is changing is the binding or cardinality, no element will be produced.
    private Boolean isConstraintElement(ElementDefinition ed) {
        if (ed.getType() != null && ed.getType().size() > 0) {
            return true;
        }

        return false;
    }

    // Returns true if the ElementDefinition describes a Backbone Element
    private Boolean isBackboneElement(ElementDefinition ed) {
        return ed.getType() != null && ed.getType().size() == 1 && ed.getType().get(0).getCode().equals("BackboneElement");
    }

    // Returns true if the ElementDefinition describes an Extension
    private Boolean isExtension(ElementDefinition ed) {
        return ed.getType() != null && ed.getType().size() == 1 && ed.getType().get(0).getCode().equals("Extension");
    }

    // Returns the type code for the element if there is only one type ref
    private String typeCode(ElementDefinition ed) {
        if (ed.getType() != null && ed.getType().size() == 1) {
            return ed.getType().get(0).getCode();
        }

        return null;
    }

    // Returns true if the type specifier is a NamedTypeSpecifier referencing FHIR.BackboneElement
    private Boolean isBackboneElement(TypeSpecifier typeSpecifier) {
        if (typeSpecifier instanceof NamedTypeSpecifier) {
            NamedTypeSpecifier nts = (NamedTypeSpecifier)typeSpecifier;
            // TODO: There's no TypeName. Is it Name or ModelName?
            String typeName = this.getTypeName(nts);
            return typeName != null && typeName.endsWith(".BackboneElement");
        }

        return false;
    }

    // Returns the set of element definitions for the given type id
    private List<ElementDefinition> getElementDefinitions(String typeId) {
        if(!structureDefinitions.containsKey(typeId)) {
            throw new RuntimeException("Could not retrieve element definitions for " + typeId);
        }

        return structureDefinitions.get(typeId).getSnapshot().getElement();
    }


    // Returns the set of element definitions for the given type
    private List<ElementDefinition> getElementDefinitions(TypeSpecifier typeSpecifier) {
        if (typeSpecifier instanceof NamedTypeSpecifier) {
            NamedTypeSpecifier nts = (NamedTypeSpecifier)typeSpecifier;
            if (nts.getName() != null) {
                return this.getElementDefinitions(nts.getName());
            }
        }
        
        return null;
    }

    // Returns the element definition for the given path
    private ElementDefinition elementForPath(List<ElementDefinition> elements, String path) {
        if (elements != null) {
            for (ElementDefinition ed : elements) {
                if (ed.getPath().equals(path)) {
                    return ed;
                }
            }
        }

        return null;
    }

    // Returns the element with the given name, if it exists
    private ClassInfoElement classInfoForName(List<ClassInfoElement> elements, String name) {
        if (elements != null) {
            for (ClassInfoElement cie : elements) {
                if (cie.getName().equals(name)) {
                    return cie;
                }
            }
        }

        return null;
    }

    // Returns the element with the given path
    private ClassInfoElement classInfoforPath(List<ClassInfoElement> elements, String path) {
        ClassInfoElement result = null;
        String[] segments = path.split(".");
        for (String p : segments) {
            result = classInfoforPath(elements, p);
            if (result != null) {
                TypeInfo elementType = resolveType(result);
                elements = ((ClassInfo)elementType).getElements();
            }
        }

        return result;
    }

    private TypeSpecifier resolveContentReference(String modelName, String path) throws Exception{
        String root = this.getQualifier(this.stripRoot(path, "#"));
        String elementPath = this.unQualify(this.stripRoot(path, "#"));

        TypeInfo rootType = this.resolveType(modelName + "." + root);
        ClassInfoElement element = this.classInfoforPath(((ClassInfo)rootType).getElements(), elementPath);
        return element.getTypeSpecifier();
    }

    private Boolean isContentReferenceTypeSpecifier(TypeSpecifier typeSpecifier) {
        if (typeSpecifier instanceof NamedTypeSpecifier) {
            NamedTypeSpecifier nts = (NamedTypeSpecifier)typeSpecifier;
            return nts.getName().startsWith("#");
        }
        else if (typeSpecifier instanceof ListTypeSpecifier) {
            ListTypeSpecifier lts = (ListTypeSpecifier)typeSpecifier;
            if (lts.getElementType().startsWith("#")) {
                return true;
            }
            else if (lts.getElementTypeSpecifier() != null) {
                return this.isContentReferenceTypeSpecifier(lts.getElementTypeSpecifier());
            }
        }

        return false;
    }


    private String getContentReference(TypeSpecifier typeSpecifier) {
        if (typeSpecifier instanceof NamedTypeSpecifier) {
            NamedTypeSpecifier nts = (NamedTypeSpecifier)typeSpecifier;
            if (nts.getName().startsWith("#")) {
                return nts.getName();
            }
        }
        else if (typeSpecifier instanceof ListTypeSpecifier) {
            ListTypeSpecifier lts = (ListTypeSpecifier)typeSpecifier;
            if (lts.getElementType().startsWith("#")) {
                return lts.getElementType();
            }
            else if (lts.getElementTypeSpecifier() != null) {
                return this.getContentReference(lts.getElementTypeSpecifier());
            }
        }

        return null;
    }

    private Boolean hasContentReferenceTypeSpecifier(ClassInfoElement element) {

        return element.getType().startsWith("#") || this.isContentReferenceTypeSpecifier(element.getTypeSpecifier());
    }

    private ClassInfoElement fixupContentRefererenceSpecifier(String modelName, ClassInfoElement element) throws Exception {
        ClassInfoElement result = null;
        if (this.hasContentReferenceTypeSpecifier(element)) {
            result = element;
            if (element.getType().startsWith("#")) {
                element.setType(this.getTypeName((NamedTypeSpecifier)this.resolveContentReference(modelName, element.getType())));
            }
            else if (element.getTypeSpecifier() instanceof ListTypeSpecifier) {
                ListTypeSpecifier lts = new ListTypeSpecifier();
                lts.setElementTypeSpecifier(this.resolveContentReference(
                    modelName, 
                    this.getContentReference(element.getTypeSpecifier())));
                
                element.setTypeSpecifier(lts);
            }
            else {
                element.setTypeSpecifier(this.resolveContentReference(
                    modelName, 
                    this.getContentReference(element.getTypeSpecifier())));
            }
        }

        return result;
    }


    // Builds a ClassInfoElement for the given ElementDefinition
    // This method assumes the given element is not a structure
    private ClassInfoElement buildClassInfoElement(String root, ElementDefinition ed, ElementDefinition structureEd, TypeSpecifier typeSpecifier) throws Exception {
        if (ed.getContentReference() != null) {
            NamedTypeSpecifier nts = new NamedTypeSpecifier();
            nts.setName(ed.getContentReference());
            typeSpecifier = nts;
        }


        // TODO: These code paths look identical to me...
        if (structureEd == null) {
            if (ed.getMax() != null && (ed.getMax().equals("*") || (this.asInteger(ed.getMax()) > 1))){
                ListTypeSpecifier lts = new ListTypeSpecifier();
                if (typeSpecifier instanceof NamedTypeSpecifier) {
                    lts.setElementType(this.getTypeName((NamedTypeSpecifier)typeSpecifier));
                }
                else {
                    lts.setElementTypeSpecifier(typeSpecifier);
                }

                typeSpecifier = lts;
            }

            String name = ed.getSliceName() != null ? ed.getSliceName() : this.stripChoice(this.stripRoot(ed.getPath(), root));

            ClassInfoElement cie = new ClassInfoElement();
            cie.setName(name);
            if (typeSpecifier instanceof NamedTypeSpecifier) { 
                cie.setElementType(this.getTypeName((NamedTypeSpecifier)typeSpecifier));
            }
            else {
                cie.setElementTypeSpecifier(typeSpecifier);
            }

            return cie;
        }
        else {
            if (ed.getMax() != null && (ed.getMax().equals("*") || (this.asInteger(ed.getMax()) > 1))){
                ListTypeSpecifier lts = new ListTypeSpecifier();
                if (typeSpecifier instanceof NamedTypeSpecifier) {
                    lts.setElementType(this.getTypeName((NamedTypeSpecifier)typeSpecifier));
                }
                else {
                    lts.setElementTypeSpecifier(typeSpecifier);
                }

                typeSpecifier = lts;
            }

            String name = ed.getSliceName() != null ? ed.getSliceName() : this.stripChoice(this.stripRoot(ed.getPath(), root));

            ClassInfoElement cie = new ClassInfoElement();
            cie.setName(name);
            if (typeSpecifier instanceof NamedTypeSpecifier) { 
                cie.setElementType(this.getTypeName((NamedTypeSpecifier)typeSpecifier));
            }
            else {
                cie.setElementTypeSpecifier(typeSpecifier);
            }

            return cie;
        }
    }

    // Returns the given extension if it exists
    private Extension extension(Element element, String url) {
        if (element != null) {
            for (Extension ex : element.getExtension()) {
                if (ex.getUrl() != null && ex.getUrl().equals(url)) {
                    return ex;
                }  
            }
        }

        return null;
    }

    // Builds the type specifier for the given element
    private TypeSpecifier buildElementTypeSpecifier(String modelName, String root, ElementDefinition ed) {
        // TODO: The D4 doesn't assume a list..
        String typeCode = ed.getTypeFirstRep().getCode();
        if (!modelInfoSettings.get("UseCQLPrimitives") && typeCode.equals("code") && ed.hasBinding() && ed.getBinding().getStrength() == BindingStrength.REQUIRED) {
            String typeName = ((StringType)(this.extension(ed.getBinding(), "http://hl7.org/fhir/StructureDefinition/elementdefinition-bindingName").getValue())).getValue();
            
            if (!this.typeInfos.containsKey(this.getTypeName(modelName, typeName))) {

                List<ClassInfoElement> elements = new ArrayList<>();
                ClassInfoElement cie = new ClassInfoElement();
                cie.setName("value");
                cie.setType("System.String");

                elements.add(cie);
                ClassInfo info = new ClassInfo(typeName, null, modelName + ".Element", false, elements, null);
                this.typeInfos.put(this.getTypeName(modelName, typeName), info);

            }

            NamedTypeSpecifier nts = new NamedTypeSpecifier();
            nts.setModelName(modelName);
            nts.setName(typeName);

            return nts;
        }
        else {
            TypeSpecifier ts = this.buildTypeSpecifier(modelName, ed.getTypeFirstRep());
            if (ts instanceof NamedTypeSpecifier) {
                ts = this.buildTypeSpecifier(primitiveTypeMappings.get(this.getTypeName(modelName, root)));
            }

            return ts;
        }
    }

    private String normalizeValueElement(String path) {
        int index = path.indexOf(".value");
        if (index != -1 && path.length() > (index + ".value".length())) {
            return path.substring(0, index);
        }
        else {
            return path;
        }
    }

    // Translates a path from the source root to the target root
    private String translate(String path, String sourceRoot, String targetRoot) {
        String result = this.normalizeValueElement(path);
        int sourceRootIndex = result.indexOf(sourceRoot);
        if (sourceRootIndex == 0) {
            result = targetRoot + result.substring(sourceRoot.length());
        }

        return result;
    }

    // Visits the given element definition and returns a ClassInfoElement. If the element is a BackboneElement
    // the visit will create an appropriate ClassInfo and record it in the TypeInfos table
    // On return, index will be updated to the index of the next element to be processed
    // This visit should not be used on the root element of a structure definition
    private ClassInfoElement visitElementDefinition(
        String modelName,
        String root,
        List<ElementDefinition> eds,
        String structureRoot,
        List<ElementDefinition> structureEds,
        int index
    ) throws Exception {
        ElementDefinition ed = eds.get(index);
        String path = ed.getPath();

        TypeSpecifier typeSpecifier = this.buildElementTypeSpecifier(modelName, root, ed);

        String typeCode = ed.getTypeFirstRep().getCode();
        StructureDefinition typeDefinition = structureDefinitions.get(typeCode);

        List<ElementDefinition> typeEds;
        if (typeCode.equals("ComplexType") && !typeDefinition.getId().equals("BackboneElement")) {
            typeEds = typeDefinition.getSnapshot().getElement();
        }
        else {
            typeEds = structureEds;
        }

        String typeRoot;
        if (typeCode.equals("ComplexType") && !typeDefinition.getId().equals("BackboneElement")) {
            typeRoot = typeDefinition.getId();
        }
        else {
            typeRoot = structureRoot;
        }

        index = index + 1;
        List<ClassInfoElement> elements = new ArrayList<>();
        while (index < eds.size()) {
            ElementDefinition e = eds.get(index);
            if (e.getPath().startsWith(path) && !e.getPath().equals(path)) {
                ClassInfoElement cie = this.visitElementDefinition(modelName, root, eds, structureRoot, structureEds, index);
                if (cie != null) {
                    elements.add(cie);
                }

            }
            else {
                break;
            }
        }

        if (elements.size() > 0) {
            if (typeDefinition.getId().equals("BackboneElement")) {
                String typeName = this.getComponentTypeName(path);
                ClassInfo info = new ClassInfo(typeName, null, modelName + ".BackboneElement", false, elements, null);
                this.typeInfos.put(this.getTypeName(modelName, typeName), info);

            }
            else if (typeDefinition.getId().equals("Extension")) {
                // If this is an extension, the elements will be constraints on the existing elements of an extension (i.e. url and value)
                // Use the type of the value element
            }
            else {
                // element has children that are being ignored.
            }
        }

        ElementDefinition typeEd = this.elementForPath(typeEds, this.translate(ed.getPath(), root, typeRoot));

        return this.buildClassInfoElement(root, ed, typeEd, typeSpecifier);
    }

    // Returns true if the type is a "codeable" type (i.e. String, Code, Concept, string, code, Coding, CodeableConcept)
    private Boolean isCodeable(String typeName) {
        return codeableTypes.contains(typeName);
    }

    // Returns the primary code path for the given type, based on the following:
    // If the type has an entry in the PrimaryCodePaths table, the value there is used
    // If the type has an element named "code" with a type of "String", "Code", "Coding", or "CodeableConcept", that element is used
    private String primaryCodePath(List<ClassInfoElement> elements, String typeName) {
        if (primaryCodePath.containsKey(typeName)) {
            return primaryCodePath.get(typeName);
        }
        else if (elements != null) {
            for (ClassInfoElement e : elements) {
                if (e.getName().toLowerCase().equals("code") && this.isCodeable(e.getType())) {
                    return e.getName();
                }
            }

        }

        return null;
    }

    // Given a StructureDefinition, creates a ClassInfo to represent it
    // This approach uses the base type to guide the walk, which requires navigating the derived profiles
    private ClassInfo buildClassInfo(String modelName, StructureDefinition sd) throws Exception {
        if (modelName == null) {
            modelName = this.resolveModelName(sd.getUrl());
        }

        String typeName = sd.getId();
        int index = 1;
        List<ClassInfoElement> elements = new ArrayList<>();
        List<ElementDefinition> eds = sd.getSnapshot().getElement();
        String path = sd.getType(); // Type is used to navigate the elements, regardless of the baseDefinition
        
        StructureDefinition structure = null;
        if (!typeName.equals(path)) {
            structure = structureDefinitions.get(path);
        }

        List<ElementDefinition> structureEds = null;
        if (structure != null) {
            structureEds = structure.getSnapshot().getElement();
        }

        while (index < eds.size()) {
            ElementDefinition e = eds.get(index);
            if (e.getPath().startsWith(path) && !e.getPath().equals(path)) {
                ClassInfoElement cie = this.visitElementDefinition(modelName, path, eds, structure.getId(), structureEds, index);
                if (cie != null) {
                    elements.add(cie);
                }

            }
            else {
                break;
            }
        }

        ClassInfo info = new ClassInfo(typeName,typeName, this.resolveTypeName(sd.getBaseDefinition()), sd.getKind() == StructureDefinitionKind.RESOURCE, elements, this.primaryCodePath(elements, typeName));

        this.typeInfos.put(this.getTypeName(modelName, typeName), info);

        return info;
    }














    public static void main(String[] args) {
        Operation op = new StructureDefinitionToModelInfo();
        op.execute(args);
    }
}
