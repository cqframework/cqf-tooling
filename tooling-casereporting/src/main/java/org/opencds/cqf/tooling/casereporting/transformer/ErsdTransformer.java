package org.opencds.cqf.tooling.casereporting.transformer;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.parser.XmlParser;
import ca.uhn.fhir.validation.*;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.RelatedArtifact.RelatedArtifactType;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.casereporting.validation.UsPublicHealthValidatorModule;
import org.opencds.cqf.tooling.parameter.TransformErsdParameters;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class ErsdTransformer extends Operation {
    private static final Logger logger = LoggerFactory.getLogger(ErsdTransformer.class);
    private static final String PUBLISHER = "Association of Public Health Laboratories (APHL)";
    private static final String RCTCLIBRARYURL = "http://ersd.aimsplatform.org/fhir/Library/rctc";
    private static final String SPECIFICATIONLIBRARYURL = "http://ersd.aimsplatform.org/fhir/Library/ERSDSpecificationLibrary";
    private FhirContext ctx;
    private FhirValidator validator;
    private IValidatorModule module = new UsPublicHealthValidatorModule();
    private final String usPhPlanDefinitionProfileUrl = "http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-plandefinition";
    private final String usPhSpecificationLibraryProfileUrl = "http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-specification-library";
    private final String usPhTriggeringValueSetLibraryProfileUrl = "http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-triggering-valueset-library";
    private final String usPhTriggeringValueSetProfileUrl = "http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-triggering-valueset";
    private final String valueSetStewardExtensionUrl = "http://hl7.org/fhir/StructureDefinition/valueset-steward";
    private final String artifactReleaseLabelExtensionUrl = "http://hl7.org/fhir/StructureDefinition/artifact-releaseLabel";
    private final String eCR1eRSDValueSetLibraryProfileUrl = "http://hl7.org/fhir/us/ecr/StructureDefinition/ersd-valueset-library";
    private final String eCR1eRSDValueSetProfileUrl = "http://hl7.org/fhir/us/ecr/StructureDefinition/ersd-valueset";
    private PlanDefinition v2PlanDefinition;
    private String version;
    private Period effectivePeriod;
    private Date artifactsDate;
    private String releaseLabel;

    public ErsdTransformer() {
        ctx = FhirContext.forR4();
        validator = ctx.newValidator();
        // validator.setValidateAgainstStandardSchema(true);
        // validator.setValidateAgainstStandardSchematron(true);
        validator.registerValidatorModule(module);
    }

    @Override
    public void execute(String[] args) {
        transform(args);
    }

    private TransformErsdParameters gatherParameters(String[] args) {
        TransformErsdParameters params = new TransformErsdParameters();
        params.outputFileEncodings = new HashSet<>();

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
                case "outputfilename": case "ofn": params.outputFileName = value; break; // -outputfilename (-ofn)
                case "pathtobundle": case "ptb": params.pathToBundle = value; break; // -pathtobundle (-ptb)
                case "pathtoplandefinition": case "ptpd": params.pathToV2PlanDefinition = value; break; // -pathtoplandefinition (-ptpd)
                case "encoding": case "e": // -encoding (-e)
                    IOUtils.Encoding encoding = IOUtils.Encoding.parse(value.toLowerCase());
                    if (encoding == IOUtils.Encoding.JSON || encoding == IOUtils.Encoding.XML) {
                        params.outputFileEncodings.add(encoding); break;
                    } else {
                        throw new IllegalArgumentException("Invalid encoding: " + value);
                    }
                case "prettyprintoutput": case "ppo": params.prettyPrintOutput = Boolean.parseBoolean(value); break; // -prettyprintoutput (-ppo)
                default: throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }

        if (params.outputFileEncodings.isEmpty()) {
            params.outputFileEncodings.add(IOUtils.Encoding.JSON);
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
        v2PlanDefinition = getV2PlanDefinition(params.pathToV2PlanDefinition);

        if (v2PlanDefinition != null) {
            if (sourceBundle.getEntry().stream().filter(x -> x.hasResource() && x.getResource().fhirType().equals("PlanDefinition")).count() > 1) {
                throw new IllegalArgumentException("The input Bundle includes more than one PlanDefinition and you have " +
                        "specified a path to a v2 PlanDefinition (via the pathtoplandefinition argument) but the " +
                        "PlanDefinition to be replaced cannot be determined and so it will not be replaced. Please " +
                        "manually replace the PlanDefinition in the input Bundle and then run the transformer again.");
            }

            BundleEntryComponent bundleEntry =
                sourceBundle.getEntry().stream().filter(x -> x.hasResource()
                        && x.getResource().fhirType().equals("PlanDefinition")).findFirst().get();
            bundleEntry.setFullUrl(v2PlanDefinition.getUrl());
            bundleEntry.setResource(v2PlanDefinition);
        }

        Library specificationLibrary = createSpecificationLibrary();
        resolveRelatedArtifacts(sourceBundle, specificationLibrary);
        Bundle specificationBundle = resolveSpecificationBundle(sourceBundle, specificationLibrary);

        if (params.outputFileEncodings == null) {
            params.outputFileEncodings = new HashSet<>();
        }
        if (params.outputFileEncodings.isEmpty()) {
            params.outputFileEncodings.add(IOUtils.Encoding.JSON);
        }

        for (IOUtils.Encoding encoding: params.outputFileEncodings) {
            IOUtils.writeBundle(specificationBundle, params.outputPath, encoding, FhirContext.forR4Cached(), params.outputFileName, params.prettyPrintOutput);
        }
    }

    private PlanDefinition getV2PlanDefinition(String pathToPlanDefinition) {
        PlanDefinition planDef = null;
        if (pathToPlanDefinition != null && !pathToPlanDefinition.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("PlanDefinitionPath: '{}'", pathToPlanDefinition);
            }
            planDef = (PlanDefinition)IOUtils.readResource(pathToPlanDefinition, ctx, true);
            if (planDef != null) {
                planDef.setDate(this.artifactsDate);
                planDef.setVersion(this.version);
                planDef.setEffectivePeriod(this.effectivePeriod);
                Extension releaseLabelExtension = new Extension();
                releaseLabelExtension.setUrl(artifactReleaseLabelExtensionUrl);
                releaseLabelExtension.setValue(new StringType(this.releaseLabel));
                planDef.addExtension(releaseLabelExtension);
            }
        }

        return planDef;
    }

    private Bundle getAndValidateBundle(String pathToBundle) {
        Bundle sourceBundle = null;
        File bundleFile = new File(pathToBundle);
        if (bundleFile.isFile()) {
            try {
                if (bundleFile.getName().endsWith("json")) {
                    sourceBundle = (Bundle)((JsonParser)ctx.newJsonParser()).parseResource(new FileInputStream(bundleFile));
                }
                else if (bundleFile.getName().endsWith("xml")) {
                    sourceBundle = (Bundle)((XmlParser)ctx.newXmlParser()).parseResource(new FileInputStream(bundleFile));
                }
                else {
                    throw new IllegalArgumentException("Unsupported input bundle encoding. Currently, only .json and .xml supported for the input bundle.");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("Error parsing " + bundleFile.getName());
            }
        }
        if (sourceBundle == null) {
            return null;
        }

//        // The structure of the eRSD bundle is a bundle with a single entry - the bundle that contains the artifacts.
//        // We need to ensure that the bundle is structured as expected and then extract that inner bundle to work with.
//        // Rather than requiring the user to trim off that outer bundle as a manual step, we'll just do that here.
//        Bundle sourceArtifactBundle;
//        if (sourceBundle.getEntry().size() == 1
//                && sourceBundle.getEntry().get(0).hasResource()
//                && sourceBundle.getEntry().get(0).getResource().fhirType().equals("Bundle")
//            ) {
//            sourceArtifactBundle = (Bundle)sourceBundle.getEntry().get(0).getResource();
//        } else {
//            throw new IllegalArgumentException("The bundle provided is not structured as expected. Expectation is a Bundle with a single entry - another bundle that contains the artifacts.");
//        }

        Library rctc =
            (Library)sourceBundle.getEntry().stream()
                .filter(x -> x.hasResource()
                    && x.getResource().fhirType().equals("Library")
                    && ((Library)x.getResource()).getUrl().equals(RCTCLIBRARYURL)).findFirst().get().getResource();

        this.effectivePeriod = rctc.getEffectivePeriod();
        this.artifactsDate = rctc.getDate();
        Extension releaseLabelExtension = rctc.getExtensionByUrl(artifactReleaseLabelExtensionUrl);
        if (releaseLabelExtension != null) {
            this.releaseLabel = releaseLabelExtension.getValueAsPrimitive().getValueAsString();
        }

        if (rctc.hasVersion()) {
            this.version = rctc.getVersion();
        } else {
            throw new IllegalArgumentException("The 'rctc' Library included in the input bundle does not include a version and should");
        }

        return sourceBundle;
    }

    private Library createSpecificationLibrary() {
        Library specificationLibrary = new Library();
        specificationLibrary.setId(new IdType("Library", "ersd-specification-library"));
        specificationLibrary.getMeta().addProfile(usPhSpecificationLibraryProfileUrl);

        Extension releaseLabelExtension = new Extension();
        releaseLabelExtension.setUrl(artifactReleaseLabelExtensionUrl);
        releaseLabelExtension.setValue(new StringType(this.releaseLabel));
        specificationLibrary.addExtension(releaseLabelExtension);

        if (this.effectivePeriod != null) {
            specificationLibrary.setEffectivePeriod(this.effectivePeriod);
        }
        specificationLibrary.setUrl(SPECIFICATIONLIBRARYURL);
        specificationLibrary.setVersion(this.version);
        specificationLibrary.setName("ERSDSpecificationLibrary");
        specificationLibrary.setTitle("eRSD Specification Library");
        specificationLibrary.setStatus(PublicationStatus.ACTIVE);
        specificationLibrary.setExperimental(false);
        specificationLibrary.setType(new CodeableConcept(
                new Coding("http://terminology.hl7.org/CodeSystem/library-type", "asset-collection", null)));
        specificationLibrary.setDate(this.artifactsDate);
        specificationLibrary.setPublisher(PUBLISHER);
        specificationLibrary.setDescription(
                "A Library that is the package manifest for the Electronic Reporting and Surveillance Distribution (eRSD) 3rd Edition. It defines the components and dependencies that make up an eRSD specification version including reporting parameters, a Reportable Trigger Codes (RCTC) Library of the trigger code value sets, and the trigger code value sets themselves.");

        UsageContext reportingUsageContext =
            new UsageContext(
                new Coding("http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context-type", "reporting", null),
                new CodeableConcept(new Coding("http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context", "triggering", null))
            );
        specificationLibrary.addUseContext(reportingUsageContext);

        UsageContext specificationTypeUsageContext =
                new UsageContext(
                        new Coding("http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context-type", "specification-type", null),
                        new CodeableConcept(new Coding("http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context", "program", null))
                );
        specificationLibrary.addUseContext(specificationTypeUsageContext);

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
        // res.setVersion(this.version);
        // res.setPublisher(PUBLISHER);
        // res.setExperimental(false);
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
        if (resolveProfile(res, eCR1eRSDValueSetLibraryProfileUrl)) {
            res.getMeta().getProfile().removeIf(p -> p.getValue().equals(eCR1eRSDValueSetLibraryProfileUrl));
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
        res.setPublisher(PUBLISHER);
        res.setExperimental(false);
        res.setTitle("Reportable Condition Trigger Codes");

        UsageContext reportingUsageContext =
            new UsageContext(
                    new Coding("http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context-type", "reporting", null),
                    new CodeableConcept(new Coding("http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context", "triggering", null))
            );
        res.addUseContext(reportingUsageContext);

        UsageContext specificationTypeUsageContext =
            new UsageContext(
                    new Coding("http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context-type", "specification-type", null),
                    new CodeableConcept(new Coding("http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context", "value-set-library", null))
            );
        res.addUseContext(specificationTypeUsageContext);

        return null;
    }

    private IBaseOperationOutcome resolveTriggeringValueSet(ValueSet res, Library specificationLibrary) {
        if (!resolveProfile(res, usPhTriggeringValueSetProfileUrl)) {
            res.getMeta().addProfile(usPhTriggeringValueSetProfileUrl);
        }
        if (resolveProfile(res, eCR1eRSDValueSetProfileUrl)) {
            res.getMeta().getProfile().removeIf(p -> p.getValue().equals(eCR1eRSDValueSetProfileUrl));
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
        res.addUseContext(
            new UsageContext(
                new Coding("http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context-type", "reporting", null),
                new CodeableConcept(new Coding("http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context", "triggering", null))
            )
        );

        // Update Grouping ValueSet references (in useContexts) to PlanDefinition
        List<UsageContext> useContexts = res.getUseContext();
        res.getUseContext().removeIf(uc -> uc.hasValueReference() && uc.getValueReference().hasReference() && uc.getValueReference().getReference().contains("skeleton"));

        boolean hasPriorityUseContext = false;
        for (UsageContext uc : useContexts) {
            hasPriorityUseContext = uc.getCode().getCode().equalsIgnoreCase("priority");
            if (hasPriorityUseContext) {
                break;
            }
        }

        if (!hasPriorityUseContext) {
            res.addUseContext(
                new UsageContext(
                    new Coding("http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context-type", "priority", null),
                    new CodeableConcept(new Coding("http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context", "routine", null))
                )
            );
        }

        Extension stewardExtension = res.getExtensionByUrl(valueSetStewardExtensionUrl);
        if (stewardExtension != null && stewardExtension.hasValue()) {
            String stewardName = ((ContactDetail) stewardExtension.getValue()).getName();
            if (stewardName != null && !stewardName.isEmpty() && !stewardName.isBlank()) {
                res.setPublisher(stewardName);
            }
        }


        // Groupers need to have an include per ValueSet in the compose rather than all in one Include together.
        List<String> grouperUrls = new ArrayList<>();
        grouperUrls.add("http://hl7.org/fhir/us/ecr/ValueSet/dxtc");
        grouperUrls.add("http://hl7.org/fhir/us/ecr/ValueSet/ostc");
        grouperUrls.add("http://hl7.org/fhir/us/ecr/ValueSet/lotc");
        grouperUrls.add("http://hl7.org/fhir/us/ecr/ValueSet/lrtc");
        grouperUrls.add("http://hl7.org/fhir/us/ecr/ValueSet/mrtc");
        grouperUrls.add("http://hl7.org/fhir/us/ecr/ValueSet/sdtc");
        String url = res.getUrl();
        if (grouperUrls.contains(url)) {
            res.setVersion(this.version);
            res.setPublisher(PUBLISHER);

            ValueSet.ValueSetComposeComponent compose = res.getCompose();
            List<ValueSet.ConceptSetComponent> includes = compose.getInclude();
            if (includes.size() > 1) {
                throw new RuntimeException(String.format("Expected the %s grouping ValueSet to have a single include in its compose.", url));
            }

            ValueSet.ConceptSetComponent include = includes.get(0);

            List<CanonicalType> referencedValueSets = include.getValueSet();
            for (CanonicalType referencedValueSet : referencedValueSets) {
                ArrayList<CanonicalType> newInclude = new ArrayList<>();
                newInclude.add(referencedValueSet);
                compose.addInclude(new ValueSet.ConceptSetComponent().setValueSet(newInclude));
            }

            compose.getInclude().remove(include);
        }

        res.setExperimental(false);
        return null;
    }

    private Bundle resolveSpecificationBundle(Bundle bundle, Library specificationLibrary) {
        logger.info(FhirContext.forR4Cached().newJsonParser().setPrettyPrint(true)
                .encodeResourceToString(specificationLibrary));
        List<BundleEntryComponent> entries = new ArrayList<BundleEntryComponent>();
        entries.add(new BundleEntryComponent().setResource(specificationLibrary)
                .setFullUrl(SPECIFICATIONLIBRARYURL));
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
