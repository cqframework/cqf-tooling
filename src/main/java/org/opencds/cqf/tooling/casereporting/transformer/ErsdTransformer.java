package org.opencds.cqf.tooling.casereporting.transformer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.parser.XmlParser;
import org.apache.poi.ss.usermodel.Workbook;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.casereporting.validation.UsPublicHealthValidatorModule;
import org.opencds.cqf.tooling.parameter.TransformErsdParameters;
import org.opencds.cqf.tooling.terminology.SpreadsheetHelper;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;

import javax.xml.crypto.dsig.Transform;

public class ErsdTransformer extends Operation {
    private static final Logger logger = LoggerFactory.getLogger(ErsdTransformer.class);
    private FhirContext ctx;
    private FhirValidator validator;
    private IValidatorModule module = new UsPublicHealthValidatorModule();
    private final String usPhPlanDefinitionProfileUrl = "http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-plandefinition";
    private final String usPhSpecificationLibraryProfileUrl = "http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-specification-library";
    private final String usPhTriggeringValueSetLibraryProfileUrl = "http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-triggering-valueset-library";
    private final String usPhTriggeringValueSetProfileUrl = "http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-triggering-valueset";

//    private TransformErsdParameters inputParameters;
    private JsonParser jsonParser;
    private XmlParser xmlParser;

    public ErsdTransformer() {
        ctx = FhirContext.forR4();
        validator = ctx.newValidator();
        // validator.setValidateAgainstStandardSchema(true);
        // validator.setValidateAgainstStandardSchematron(true);
        validator.registerValidatorModule(module);
        jsonParser = (JsonParser)ctx.newJsonParser();
        xmlParser = (XmlParser)ctx.newXmlParser();
    }

    @Override
    public void execute(String[] args) {
        transform(args);
    }

    private TransformErsdParameters gatherParameters(String[] args) {
        TransformErsdParameters params = new TransformErsdParameters();
        params.encodings = new HashSet<>();

        params.outputPath = "src/main/resources/org/opencds/cqf/tooling/casereporting/output"; // default
        for (String arg : args) {
            if (arg.equals("-TransformErsd")) {
                continue;
            }

            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1];

            switch (flag.replace("-", "").toLowerCase()) {
                case "outputpath": case "op": params.outputPath = value; break; // -outputpath (-op)
                case "pathtobundle": case "ptb": params.pathToBundle = value; break; // -pathtobundle (-ptb)
                case "pathtoplandefinition": case "ptpd": params.pathToV2PlanDefinition = value; break; // -pathtoplandefinition (-ptpd)
                case "encoding": case "e": // -encoding (-e)
                    IOUtils.Encoding encoding = IOUtils.Encoding.parse(value.toLowerCase());
                    if (encoding == IOUtils.Encoding.JSON || encoding == IOUtils.Encoding.XML) {
                        params.encodings.add(encoding); break;
                    } else {
                        throw new IllegalArgumentException("Invalid encoding: " + value);
                    }
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }

        if (params.encodings.isEmpty()) {
            params.encodings.add(IOUtils.Encoding.JSON);
        }

        if (params.pathToBundle == null) {
            throw new IllegalArgumentException("The path to the ersd bundle is required");
        }

        return params;
    }

    public void transform(String[] args) {
        TransformErsdParameters inputParameters = gatherParameters(args);
        transform(inputParameters);
    }

    public void transform(TransformErsdParameters params) {
        Bundle sourceBundle = getAndValidateBundle(params.pathToBundle);
        PlanDefinition v2PlanDefinition = getV2PlanDefinition(params.pathToV2PlanDefinition);
        if (v2PlanDefinition != null) {
            if (sourceBundle.getEntry().stream().filter(x -> x.hasResource() && x.getResource().fhirType().equals("PlanDefinition")).count() > 1) {
                throw new IllegalArgumentException("The input Bundle includes more than one PlanDefinition and you have " +
                        "specified a path to a v2 PlanDefinition (via the pathtoplandefinition argument) but the " +
                        "PlanDefinition to be replaced cannot be determined and so it will not be replaced. Please " +
                        "manually replace the PlanDefinition in the input Bundle and then run the transformer again.");
            }

            BundleEntryComponent bundleEntry =
                    sourceBundle.getEntry().stream().filter(x -> x.hasResource() && x.getResource().fhirType().equals("PlanDefinition")).findFirst().get();
            bundleEntry.setFullUrl(v2PlanDefinition.getUrl());
            bundleEntry.setResource(v2PlanDefinition);
        }

        Library specificationLibrary = createSpecificationLibrary();
        resolveRelatedArtifacts(sourceBundle, specificationLibrary);
        Bundle specificationBundle = resolveSpecificationBundle(sourceBundle, specificationLibrary);

        if (params.encodings == null) {
            params.encodings = new HashSet<>();
        }
        if (params.encodings.isEmpty()) {
            params.encodings.add(IOUtils.Encoding.JSON);
        }

        for (IOUtils.Encoding encoding: params.encodings) {
            IOUtils.writeBundle(specificationBundle, params.outputPath, encoding, FhirContext.forR4Cached());
        }
    }

    private PlanDefinition getV2PlanDefinition(String pathToPlanDefinition) {
        PlanDefinition planDef = null;
        if (pathToPlanDefinition != null && !pathToPlanDefinition.isEmpty()) {
            planDef = (PlanDefinition)IOUtils.readResource(pathToPlanDefinition, ctx, true);
        }
        return planDef;
    }

    private Bundle getAndValidateBundle(String pathToBundle) {
        Bundle sourceBundle = null;
        File bundleFile = new File(pathToBundle);
        if (bundleFile.isFile()) {
            try {
                if (bundleFile.getName().endsWith("xml")) {
                    sourceBundle = (Bundle)xmlParser.parseResource(new FileInputStream(bundleFile));
                }
                else {
                    sourceBundle = (Bundle)jsonParser.parseResource(new FileInputStream(bundleFile));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("Error parsing " + bundleFile.getName());
            }
        }
        if (sourceBundle == null) {
            return null;
        }

        // The structure of the eRSD bundle is a bundle with a single entry - the bundle that contains the artifacts.
        // We need to ensure that the bundle is structured as expected and then extract that inner bundle to work with.
        // Rather than requiring the user to trim off that outer bundle as a manual step, we'll just do that here.
        Bundle sourceArtifactBundle;
        if (sourceBundle.getEntry().size() == 1
                && sourceBundle.getEntry().get(0).hasResource()
                && sourceBundle.getEntry().get(0).getResource().fhirType().equals("Bundle")) {
            sourceArtifactBundle = (Bundle)sourceBundle.getEntry().get(0).getResource();
        } else {
            throw new RuntimeException("The bundle provided is not structured as expected. Expectation is a Bundle with a single entry - another bundle that contains the artifacts.");
        }

        return sourceArtifactBundle;
    }

    private Library createSpecificationLibrary() {
        Library specificationLibrary = new Library();
        specificationLibrary.setId(new IdType("Library", "SpecificationLibrary", "1.0.0"));
        specificationLibrary.getMeta().addProfile(usPhSpecificationLibraryProfileUrl);
        specificationLibrary.setName("SpecificationLibrary");
        specificationLibrary.setTitle("Specification Library");
        specificationLibrary.setVersion("1.0.0");
        specificationLibrary.setDescription(
                "Defines the asset-collection library containing the US Public Health specification assets.");
        specificationLibrary.setStatus(PublicationStatus.ACTIVE);
        specificationLibrary.setExperimental(true);
        specificationLibrary.setPublisher("eCR");
        specificationLibrary.setUrl("http://ersd.aimsplatform.org/fhir/Library/SpecificationLibrary");
        specificationLibrary.setType(new CodeableConcept(
                new Coding("http://terminology.hl7.org/CodeSystem/library-type", "asset-collection", null)));
        boolean foundSpecificationType = false;
        String useContextCode = "specification-type";
        if (specificationLibrary.hasUseContext() && !specificationLibrary.getUseContext().isEmpty()) {
            for (UsageContext useContext : specificationLibrary.getUseContext()) {
                if (useContext.getCode().getCode().equals(useContextCode)) {
                    foundSpecificationType = true;
                }
            }
        }
        CodeableConcept type = new CodeableConcept(
                new Coding("http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context", "program", null));
        UsageContext usageContext = new UsageContext(
                new Coding("http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context-type", useContextCode, null),
                type);
        if (!foundSpecificationType) {
            specificationLibrary.addUseContext(usageContext);
        }
        return specificationLibrary;
    }

    private void resolveRelatedArtifacts(Bundle bundle, Library specificationLibrary) {
        bundle.getEntry().stream()
                .filter(x -> x.hasResource() && x.getResource().fhirType().equals("PlanDefinition"))
                .forEach(x -> resolvePlanDefinition((PlanDefinition) x.getResource(), specificationLibrary));
        bundle.getEntry().stream()
                .filter(x -> x.hasResource() && x.getResource().fhirType().equals("Library"))
                .forEach( x -> resolveTriggeringValueSetLibrary((Library) x.getResource(), specificationLibrary, bundle));
        bundle.getEntry().stream()
                .filter(x -> x.hasResource() && x.getResource().fhirType().equals("ValueSet"))
                .forEach(x -> resolveTriggeringValueSet((ValueSet) x.getResource(), specificationLibrary));
    }

    private IBaseOperationOutcome resolvePlanDefinition(PlanDefinition res, Library specificationLibrary) {
        if (!resolveProfile(res, usPhPlanDefinitionProfileUrl)) {
            res.getMeta().addProfile(usPhPlanDefinitionProfileUrl);
        }
        ValidationResult result = validator.validateWithResult(res);
        boolean isValid = true;
        for (SingleValidationMessage message : result.getMessages()) {
            if (message.getSeverity().equals(ResultSeverityEnum.ERROR)) {
                isValid = false;
            }
        }
        if (!isValid) {
            return result.toOperationOutcome();
        }

        RelatedArtifact relatedArtifact = new RelatedArtifact();
        relatedArtifact.setType(RelatedArtifactType.COMPOSEDOF);
        relatedArtifact.setResource(res.getUrlElement().asStringValue());
        specificationLibrary.addRelatedArtifact(relatedArtifact);
        // res.setType(new CodeableConcept(new
        // Coding("http://terminology.hl7.org/CodeSystem/plan-definition-type",
        // "workflow-definition", "Workflow Definition")));
        // res.getAction().forEach(action -> {
        // action.getTrigger().forEach(trigger -> {
        // trigger.setType(TriggerType.NAMEDEVENT);
        // });
        // });
        // res.setVersion("1.0.0");
        // res.setPublisher("eCR");
        // res.setExperimental(true);
        // res.setDescription("Example Description");
        return null;
    }

    private boolean resolveProfile(Resource res, String profile) {
        boolean resolvedProfile = false;
        if (res.hasMeta() && res.getMeta().hasProfile()) {
            for (CanonicalType canonical : res.getMeta().getProfile()) {
                if (canonical.asStringValue().equals(profile)) {
                    resolvedProfile = true;
                }
            }
        }
        return resolvedProfile;
    }

    private IBaseOperationOutcome resolveTriggeringValueSetLibrary(Library res, Library specificationLibrary,
            Bundle bundle) {
        if (!resolveProfile(res, usPhTriggeringValueSetLibraryProfileUrl)) {
            res.getMeta().addProfile(usPhTriggeringValueSetLibraryProfileUrl);
        }
        ValidationResult result = validator.validateWithResult(res);
        boolean isValid = true;
        for (SingleValidationMessage message : result.getMessages()) {
            if (message.getSeverity().equals(ResultSeverityEnum.ERROR)) {
                isValid = false;
            }
        }
        if (!isValid) {
            return result.toOperationOutcome();
        }
        // bundle.getEntry().stream()
        // .filter(x -> (x.hasResource() &&
        // x.getResource().fhirType().equals("ValueSet")))
        // .map(x -> (ValueSet) x.getResource())
        // .forEach(vs -> {
        // RelatedArtifact relatedArtifact = new RelatedArtifact();
        // relatedArtifact.setType(RelatedArtifactType.COMPOSEDOF);
        // relatedArtifact.setResource(vs.getUrlElement().asStringValue());
        // res.addRelatedArtifact(relatedArtifact);
        // });
        RelatedArtifact relatedArtifact = new RelatedArtifact();
        relatedArtifact.setType(RelatedArtifactType.COMPOSEDOF);
        relatedArtifact.setResource(res.getUrlElement().asStringValue());
        specificationLibrary.addRelatedArtifact(relatedArtifact);
        res.setPublisher("eCR");
        res.setExperimental(false);
        return null;
    }

    private IBaseOperationOutcome resolveTriggeringValueSet(ValueSet res, Library specificationLibrary) {
        if (!resolveProfile(res, usPhTriggeringValueSetProfileUrl)) {
            res.getMeta().addProfile(usPhTriggeringValueSetProfileUrl);
        }
        ValidationResult result = validator.validateWithResult(res);
        boolean isValid = true;
        for (SingleValidationMessage message : result.getMessages()) {
            if (message.getSeverity().equals(ResultSeverityEnum.ERROR)) {
                isValid = false;
            }
        }
        if (!isValid) {
            return result.toOperationOutcome();
        } else {
            RelatedArtifact relatedArtifact = new RelatedArtifact();
            relatedArtifact.setType(RelatedArtifactType.COMPOSEDOF);
            relatedArtifact.setResource(res.getUrlElement().asStringValue());
            // specificationLibrary.addRelatedArtifact(relatedArtifact);
        }
        res.addUseContext(
            new UsageContext(
                new Coding("http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context-type", "reporting", null),
                new CodeableConcept(new Coding("http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context", "triggering", null))
            )
        );
        res.setVersion("1.0.0");
        res.setPublisher("eCR");
        res.setExperimental(true);
        return null;
    }

    private Bundle resolveSpecificationBundle(Bundle bundle, Library specificationLibrary) {
        System.out.println(FhirContext.forR4Cached().newJsonParser().setPrettyPrint(true)
                .encodeResourceToString(specificationLibrary));
        List<BundleEntryComponent> entries = new ArrayList<BundleEntryComponent>();
        entries.add(new BundleEntryComponent().setResource(specificationLibrary)
                .setFullUrl("http://ersd.aimsplatform.org/fhir/Library/SpecificationLibrary"));
        for (BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof ValueSet) {
                ValueSet v = (ValueSet) entry.getResource();
                // v.setCompose(new ValueSetComposeComponent());
                v.setText(new Narrative());
                v.getExpansion().getContains().forEach(x -> x.setDisplay(""));
            }
        }
        bundle.getEntry().forEach(entry -> entries.add(entry));
        bundle.setEntry(entries);
        return bundle;
    }
}
