package org.opencds.cqf.modelinfo;

import org.hl7.elm.r1.Except;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.ElementDefinition;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.dstu3.model.StructureDefinition.StructureDefinitionKind;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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


    public class TypeInfo {
        public String name;
        public String modelName;
        // TODO: Model info?
    }

    

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

    private static Map<String, String> cqlTypeMappsings = new HashMap<String, String>() {
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
        Map<String, StructureDefinition> resourcesById = indexResources(resources);

        System.out.println("Creating dependency graph for StructureDefinitions");
        MutableGraph g = createDependencyGraph(resourcesById);

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

        throw new Exception("Couldn't resolve model");
    }

    private String resolveTypeName(String url) throws Exception {

        String modelName = resolveModelName(url);
        return getTypeName(modelName, getTail(url));

    }

    private String getTypeName(String modelName, String typeName) {
        return modelName != null ? modelName + "." + typeName : typeName;

    }

    public static void main(String[] args) {
        Operation op = new StructureDefinitionToModelInfo();
        op.execute(args);
    }
}
