package org.opencds.cqf.tooling.casereporting.transformer;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.parser.XmlParser;
import ca.uhn.fhir.validation.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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

public class ErsdTransformer extends Operation {
    private static final Logger logger = LoggerFactory.getLogger(ErsdTransformer.class);
    private static final String PUBLISHER = "Association of Public Health Laboratories (APHL)";
    private static final String RCTCLIBRARYURL = "http://ersd.aimsplatform.org/fhir/Library/rctc";
    private static final String USPHUSAGECONTEXTTYPESYSTEMURL = "http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context-type";
    private static final String USPHUSAGECONTEXTSYSTEMURL = "http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context";
    private static final String SPECIFICATIONLIBRARYURL =
            "http://ersd.aimsplatform.org/fhir/Library/ersd-specification";
    private static final String SPECIFICATIONLIBRARYNAME = "ERSDSpecification";
    private static final String SPECIFICATIONLIBRARYTITLE = "eRSD Specification";

    private FhirContext ctx;
    private FhirValidator validator;
    private IValidatorModule module = new UsPublicHealthValidatorModule();
    private final String artifactIsOwnedExtensionUrl = "http://hl7.org/fhir/StructureDefinition/artifact-isOwned";
    private final String crmiManifestLibraryProfileUrl =
            "http://hl7.org/fhir/uv/crmi/StructureDefinition/crmi-manifestlibrary";
    private final String usPhPlanDefinitionProfileUrl =
            "http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-plandefinition";
    private final String usPhSpecificationLibraryProfileUrl =
            "http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-specification-library";
    private final String usPhTriggeringValueSetLibraryProfileUrl =
            "http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-triggering-valueset-library";
    private final String usPhTriggeringValueSetProfileUrl =
            "http://hl7.org/fhir/us/ecr/StructureDefinition/us-ph-triggering-valueset";
    private final String valueSetStewardExtensionUrl = "http://hl7.org/fhir/StructureDefinition/valueset-steward";
    private final String artifactReleaseLabelExtensionUrl =
            "http://hl7.org/fhir/StructureDefinition/artifact-releaseLabel";
    private final String eCR1eRSDValueSetLibraryProfileUrl =
            "http://hl7.org/fhir/us/ecr/StructureDefinition/ersd-valueset-library";
    private final String eCR1eRSDValueSetProfileUrl = "http://hl7.org/fhir/us/ecr/StructureDefinition/ersd-valueset";
    private final String crmiIntendedusageContextExtensionUrl = "http://hl7.org/fhir/uv/crmi/StructureDefinition/crmi-intendedUsageContext";

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
                case "outputpath":
                case "op":
                    params.outputPath = value;
                    break; // -outputpath (-op)
                case "outputfilename":
                case "ofn":
                    params.outputFileName = value;
                    break; // -outputfilename (-ofn)
                case "pathtobundle":
                case "ptb":
                    params.pathToBundle = value;
                    break; // -pathtobundle (-ptb)
                case "pathtoplandefinition":
                case "ptpd":
                    params.pathToV2PlanDefinition = value;
                    break; // -pathtoplandefinition (-ptpd)
                case "encoding":
                case "e": // -encoding (-e)
                    IOUtils.Encoding encoding = IOUtils.Encoding.parse(value.toLowerCase());
                    if (encoding == IOUtils.Encoding.JSON || encoding == IOUtils.Encoding.XML) {
                        params.outputFileEncodings.add(encoding);
                        break;
                    } else {
                        throw new IllegalArgumentException("Invalid encoding: " + value);
                    }
                case "prettyprintoutput":
                case "ppo":
                    params.prettyPrintOutput = Boolean.parseBoolean(value);
                    break; // -prettyprintoutput (-ppo)
                default:
                    throw new IllegalArgumentException("Unknown flag: " + flag);
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
            if (sourceBundle.getEntry().stream()
                            .filter(x -> x.hasResource()
                                    && x.getResource().fhirType().equals("PlanDefinition"))
                            .count()
                    > 1) {
                throw new IllegalArgumentException(
                        "The input Bundle includes more than one PlanDefinition and you have "
                                + "specified a path to a v2 PlanDefinition (via the pathtoplandefinition argument) but the "
                                + "PlanDefinition to be replaced cannot be determined and so it will not be replaced. Please "
                                + "manually replace the PlanDefinition in the input Bundle and then run the transformer again.");
            }

            BundleEntryComponent bundleEntry = sourceBundle.getEntry().stream()
                    .filter(x -> x.hasResource() && x.getResource().fhirType().equals("PlanDefinition"))
                    .findFirst()
                    .get();
            bundleEntry.setFullUrl(v2PlanDefinition.getUrl());
            bundleEntry.setResource(v2PlanDefinition);
        }

        Library specificationLibrary = createSpecificationLibrary();
        resolveRelatedArtifacts(sourceBundle, specificationLibrary);
        addComponentReferences(specificationLibrary);
        Bundle specificationBundle = resolveSpecificationBundle(sourceBundle, specificationLibrary);

        if (params.outputFileEncodings == null) {
            params.outputFileEncodings = new HashSet<>();
        }
        if (params.outputFileEncodings.isEmpty()) {
            params.outputFileEncodings.add(IOUtils.Encoding.JSON);
        }

        for (IOUtils.Encoding encoding : params.outputFileEncodings) {
            IOUtils.writeBundle(
                    specificationBundle,
                    params.outputPath,
                    encoding,
                    FhirContext.forR4Cached(),
                    params.outputFileName,
                    params.prettyPrintOutput);
        }
    }

    private PlanDefinition getV2PlanDefinition(String pathToPlanDefinition) {
        PlanDefinition planDef = null;
        if (pathToPlanDefinition != null && !pathToPlanDefinition.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("PlanDefinitionPath: '{}'", pathToPlanDefinition);
            }
            planDef = (PlanDefinition) IOUtils.readResource(pathToPlanDefinition, ctx, true);
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
                    sourceBundle =
                            (Bundle) ((JsonParser) ctx.newJsonParser()).parseResource(new FileInputStream(bundleFile));
                } else if (bundleFile.getName().endsWith("xml")) {
                    sourceBundle =
                            (Bundle) ((XmlParser) ctx.newXmlParser()).parseResource(new FileInputStream(bundleFile));
                } else {
                    throw new IllegalArgumentException(
                            "Unsupported input bundle encoding. Currently, only .json and .xml supported for the input bundle.");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("Error parsing " + bundleFile.getName());
            }
        }
        if (sourceBundle == null) {
            return null;
        }


        Library rctc = (Library) sourceBundle.getEntry().stream()
                .filter(x -> x.hasResource()
                        && x.getResource().fhirType().equals("Library")
                        && ((Library) x.getResource()).getUrl().equals(RCTCLIBRARYURL))
                .findFirst()
                .get()
                .getResource();

        this.effectivePeriod = rctc.getEffectivePeriod();
        this.artifactsDate = rctc.getDate();
        Extension releaseLabelExtension = rctc.getExtensionByUrl(artifactReleaseLabelExtensionUrl);
        if (releaseLabelExtension != null) {
            this.releaseLabel = releaseLabelExtension.getValueAsPrimitive().getValueAsString();
        }

        if (rctc.hasVersion()) {
            this.version = rctc.getVersion();
        } else {
            throw new IllegalArgumentException(
                    "The 'rctc' Library included in the input bundle does not include a version and should");
        }

        return sourceBundle;
    }

    private Library createSpecificationLibrary() {
        Library specificationLibrary = new Library();
        specificationLibrary.setId(new IdType("Library", "ersd-specification"));
        specificationLibrary.getMeta().addProfile(usPhSpecificationLibraryProfileUrl);
        specificationLibrary.getMeta().addProfile(crmiManifestLibraryProfileUrl);

        Extension releaseLabelExtension = new Extension();
        releaseLabelExtension.setUrl(artifactReleaseLabelExtensionUrl);
        releaseLabelExtension.setValue(new StringType(this.releaseLabel));
        specificationLibrary.addExtension(releaseLabelExtension);

        if (this.effectivePeriod != null) {
            specificationLibrary.setEffectivePeriod(this.effectivePeriod);
        }
        specificationLibrary.setUrl(SPECIFICATIONLIBRARYURL);
        specificationLibrary.setVersion(this.version);
        specificationLibrary.setName(SPECIFICATIONLIBRARYNAME);
        specificationLibrary.setTitle(SPECIFICATIONLIBRARYTITLE);
        specificationLibrary.setStatus(PublicationStatus.ACTIVE);
        specificationLibrary.setExperimental(false);
        specificationLibrary.setType(new CodeableConcept(
                new Coding("http://terminology.hl7.org/CodeSystem/library-type", "asset-collection", null)));
        specificationLibrary.setDate(this.artifactsDate);
        specificationLibrary.setPublisher(PUBLISHER);
        specificationLibrary.setDescription(
                "A Library that is the package manifest for the Electronic Reporting and Surveillance Distribution (eRSD) 3rd Edition. It defines the components and dependencies that make up an eRSD specification version including reporting parameters, a Reportable Trigger Codes (RCTC) Library of the trigger code value sets, and the trigger code value sets themselves.");

        UsageContext reportingUsageContext = new UsageContext(
                new Coding(USPHUSAGECONTEXTTYPESYSTEMURL, "reporting", null),
                new CodeableConcept(
                        new Coding(USPHUSAGECONTEXTSYSTEMURL, "triggering", null)));
        specificationLibrary.addUseContext(reportingUsageContext);

        UsageContext specificationTypeUsageContext = new UsageContext(
                new Coding(
                        USPHUSAGECONTEXTTYPESYSTEMURL, "specification-type", null),
                new CodeableConcept(
                        new Coding(USPHUSAGECONTEXTSYSTEMURL, "program", null)));
        specificationLibrary.addUseContext(specificationTypeUsageContext);

        return specificationLibrary;
    }

    private void addComponentReferences(Library specificationLibrary) {
        var componentRelatedArtifacts = new ArrayList<RelatedArtifact>();
        var artifactIsOwnedExtension = new Extension(artifactIsOwnedExtensionUrl, new BooleanType(true));

        var priorityUsageContext = new UsageContext();

        var typeCoding = new Coding();
        typeCoding.setSystem(USPHUSAGECONTEXTTYPESYSTEMURL);
        typeCoding.setCode("priority");

        priorityUsageContext.setCode(typeCoding);

        var valueConcept = new CodeableConcept();
        valueConcept
                .addCoding()
                .setSystem(USPHUSAGECONTEXTSYSTEMURL)
                .setCode("routine");

        priorityUsageContext.setValue(valueConcept);

        var priorityUsageContextExtension = new Extension();
        priorityUsageContextExtension.setUrl(crmiIntendedusageContextExtensionUrl);
        priorityUsageContextExtension.setValue(priorityUsageContext);

        var dxtcRelatedArtifact = new RelatedArtifact();
        dxtcRelatedArtifact.addExtension((priorityUsageContextExtension));
        dxtcRelatedArtifact.addExtension(artifactIsOwnedExtension);
        dxtcRelatedArtifact.setType(RelatedArtifactType.COMPOSEDOF);
        dxtcRelatedArtifact.setDisplay(
                String.format("ValueSet Diagnosis_Problem Triggers for Public Health Reporting, %s", this.version));
        dxtcRelatedArtifact.setResource(
                String.format("http://ersd.aimsplatform.org/fhir/ValueSet/dxtc|%s", this.version));
        componentRelatedArtifacts.add(dxtcRelatedArtifact);

        var lotcRelatedArtifact = new RelatedArtifact();
        lotcRelatedArtifact.addExtension((priorityUsageContextExtension));
        lotcRelatedArtifact.addExtension(artifactIsOwnedExtension);
        lotcRelatedArtifact.setType(RelatedArtifactType.COMPOSEDOF);
        lotcRelatedArtifact.setDisplay(
                String.format("ValueSet Lab Order Test Triggers for Public Health Reporting, %s", this.version));
        lotcRelatedArtifact.setResource(
                String.format("http://ersd.aimsplatform.org/fhir/ValueSet/lotc|%s", this.version));
        componentRelatedArtifacts.add(lotcRelatedArtifact);

        var lrtcRelatedArtifact = new RelatedArtifact();
        lrtcRelatedArtifact.addExtension((priorityUsageContextExtension));
        lrtcRelatedArtifact.addExtension(artifactIsOwnedExtension);
        lrtcRelatedArtifact.setType(RelatedArtifactType.COMPOSEDOF);
        lrtcRelatedArtifact.setDisplay(
                String.format("ValueSet Lab Obs Test Triggers for Public Health Reporting, %s", this.version));
        lrtcRelatedArtifact.setResource(
                String.format("http://ersd.aimsplatform.org/fhir/ValueSet/lrtc|%s", this.version));
        componentRelatedArtifacts.add(lrtcRelatedArtifact);

        var mrtcRelatedArtifact = new RelatedArtifact();
        mrtcRelatedArtifact.addExtension((priorityUsageContextExtension));
        mrtcRelatedArtifact.addExtension(artifactIsOwnedExtension);
        mrtcRelatedArtifact.setType(RelatedArtifactType.COMPOSEDOF);
        mrtcRelatedArtifact.setDisplay(
                String.format("ValueSet Medications Triggers for Public Health Reporting, %s", this.version));
        mrtcRelatedArtifact.setResource(
                String.format("http://ersd.aimsplatform.org/fhir/ValueSet/mrtc|%s", this.version));
        componentRelatedArtifacts.add(mrtcRelatedArtifact);

        var ostcRelatedArtifact = new RelatedArtifact();
        ostcRelatedArtifact.addExtension((priorityUsageContextExtension));
        ostcRelatedArtifact.addExtension(artifactIsOwnedExtension);
        ostcRelatedArtifact.setType(RelatedArtifactType.COMPOSEDOF);
        ostcRelatedArtifact.setDisplay(String.format(
                "ValueSet Organism_Substance Release Triggers for Public Health Reporting, %s", this.version));
        ostcRelatedArtifact.setResource(
                String.format("http://ersd.aimsplatform.org/fhir/ValueSet/ostc|%s", this.version));
        componentRelatedArtifacts.add(ostcRelatedArtifact);

        var sdtcRelatedArtifact = new RelatedArtifact();
        sdtcRelatedArtifact.addExtension((priorityUsageContextExtension));
        sdtcRelatedArtifact.addExtension(artifactIsOwnedExtension);
        sdtcRelatedArtifact.setType(RelatedArtifactType.COMPOSEDOF);
        sdtcRelatedArtifact.setDisplay(
                String.format("ValueSet Suspected_Disorder Triggers for Public Health Reporting, %s", this.version));
        sdtcRelatedArtifact.setResource(
                String.format("http://ersd.aimsplatform.org/fhir/ValueSet/sdtc|%s", this.version));
        componentRelatedArtifacts.add(sdtcRelatedArtifact);

        specificationLibrary.getRelatedArtifact().addAll(componentRelatedArtifacts);
    }

    private void resolveRelatedArtifacts(Bundle bundle, Library specificationLibrary) {
        bundle.getEntry().stream()
                .filter(x -> x.hasResource() && x.getResource().fhirType().equals("PlanDefinition"))
                .forEach(x -> resolvePlanDefinition((PlanDefinition) x.getResource(), specificationLibrary));
        bundle.getEntry().stream()
                .filter(x -> x.hasResource() && x.getResource().fhirType().equals("Library"))
                .forEach(
                        x -> resolveTriggeringValueSetLibrary((Library) x.getResource(), specificationLibrary, bundle));
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

        var artifactIsOwnedExtension = new Extension(artifactIsOwnedExtensionUrl, new BooleanType(true));
        var planDefinitionRelatedArtifact = new RelatedArtifact();
        planDefinitionRelatedArtifact.addExtension(artifactIsOwnedExtension);
        planDefinitionRelatedArtifact.setType(RelatedArtifactType.COMPOSEDOF);
        planDefinitionRelatedArtifact.setDisplay(
                String.format("PlanDefinition US eCR Specification, %s", this.version));
        planDefinitionRelatedArtifact.setResource(
                String.format("%s|%s", res.getUrlElement().getValueAsString(), this.version));
        specificationLibrary.addRelatedArtifact(planDefinitionRelatedArtifact);

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

    private IBaseOperationOutcome resolveTriggeringValueSetLibrary(
            Library res, Library specificationLibrary, Bundle bundle) {
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

        var artifactIsOwnedExtension = new Extension(artifactIsOwnedExtensionUrl, new BooleanType(true));
        var rctcLibraryRelatedArtifact = new RelatedArtifact();
        rctcLibraryRelatedArtifact.addExtension(artifactIsOwnedExtension);
        rctcLibraryRelatedArtifact.setType(RelatedArtifactType.COMPOSEDOF);
        rctcLibraryRelatedArtifact.setDisplay(
                String.format("Library Reportable Condition Trigger Codes, %s", this.version));
        rctcLibraryRelatedArtifact.setResource(
                String.format("%s|%s", res.getUrlElement().asStringValue(), this.version));
        specificationLibrary.addRelatedArtifact(rctcLibraryRelatedArtifact);
        res.setPublisher(PUBLISHER);
        res.setExperimental(false);
        res.setTitle("Reportable Condition Trigger Codes");

        UsageContext reportingUsageContext = new UsageContext(
                new Coding(USPHUSAGECONTEXTTYPESYSTEMURL, "reporting", null),
                new CodeableConcept(
                        new Coding(USPHUSAGECONTEXTSYSTEMURL, "triggering", null)));
        res.addUseContext(reportingUsageContext);

        UsageContext specificationTypeUsageContext = new UsageContext(
                new Coding(
                        USPHUSAGECONTEXTTYPESYSTEMURL, "specification-type", null),
                new CodeableConcept(new Coding(
                        USPHUSAGECONTEXTSYSTEMURL, "value-set-library", null)));
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

        String canonical = res.getUrl();

        // Ensure relatedArtifact exists in the manifest library
        RelatedArtifact relatedArtifact = specificationLibrary.getRelatedArtifact().stream()
                .filter(ra -> ra.getType() == RelatedArtifact.RelatedArtifactType.DEPENDSON
                        && ra.hasResource()
                        && ra.getResource().equals(canonical))
                .findFirst()
                .orElse(null);

        if (relatedArtifact == null) {
            relatedArtifact = new RelatedArtifact();
            relatedArtifact.setType(RelatedArtifact.RelatedArtifactType.DEPENDSON);
            relatedArtifact.setResource(canonical);
            specificationLibrary.addRelatedArtifact(relatedArtifact);
        }

        //Collect existing useContexts
        List<UsageContext> useContexts = new ArrayList<>(res.getUseContext());

        boolean hasPriority = false;
        boolean hasReporting = false;

        for (UsageContext uc : useContexts) {
            // Exclude skeleton program usageContext entirely
            if (uc.hasCode()
                    && "program".equalsIgnoreCase(uc.getCode().getCode())
                    && uc.hasValueReference()
                    && uc.getValueReference().hasReference()
                    && uc.getValueReference().getReference().contains("plandefinition-ersd-skeleton")) {
                continue;
            }

            if ("priority".equalsIgnoreCase(uc.getCode().getCode())) {
                hasPriority = true;
            }

            if ("reporting".equalsIgnoreCase(uc.getCode().getCode())) {
                hasReporting = true;
            }

            Extension ext = new Extension();
            ext.setUrl(crmiIntendedusageContextExtensionUrl);
            ext.setValue(uc);

            relatedArtifact.addExtension(ext);
        }

        // Ensure priority exists
        if (!hasPriority) {

            UsageContext priorityContext = new UsageContext(
                    new Coding(USPHUSAGECONTEXTTYPESYSTEMURL, "priority", null),
                    new CodeableConcept(
                            new Coding(USPHUSAGECONTEXTSYSTEMURL, "routine", null)));

            Extension ext = new Extension();
            ext.setUrl(crmiIntendedusageContextExtensionUrl);
            ext.setValue(priorityContext);

            relatedArtifact.addExtension(ext);
        }

        // Ensure reporting exists
        if (!hasReporting) {
            UsageContext reportingContext = new UsageContext(
                    new Coding(USPHUSAGECONTEXTTYPESYSTEMURL, "reporting", null),
                    new CodeableConcept(new Coding(
                            USPHUSAGECONTEXTSYSTEMURL, "triggering", null)));

            Extension ext = new Extension();
            ext.setUrl(crmiIntendedusageContextExtensionUrl);
            ext.setValue(reportingContext);

            relatedArtifact.addExtension(ext);
        }

        // Remove all useContexts from the ValueSet
        res.getUseContext().clear();

        Extension stewardExtension = res.getExtensionByUrl(valueSetStewardExtensionUrl);
        if (stewardExtension != null && stewardExtension.hasValue()) {
            String stewardName = ((ContactDetail) stewardExtension.getValue()).getName();
            if (stewardName != null && !stewardName.isBlank()) {
                res.setPublisher(stewardName);
            }
        }

        res.setExperimental(false);

        return null;
    }

    private Bundle resolveSpecificationBundle(Bundle bundle, Library specificationLibrary) {
        logger.info(FhirContext.forR4Cached()
                .newJsonParser()
                .setPrettyPrint(true)
                .encodeResourceToString(specificationLibrary));
        List<BundleEntryComponent> entries = new ArrayList<BundleEntryComponent>();
        entries.add(new BundleEntryComponent().setResource(specificationLibrary).setFullUrl(SPECIFICATIONLIBRARYURL));
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
