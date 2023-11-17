package org.opencds.cqf.tooling.operations.measure;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.util.BundleUtil;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.cql2elm.DefaultLibrarySourceProvider;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.cql2elm.quick.FhirLibrarySourceProvider;
import org.cqframework.cql.elm.requirements.fhir.DataRequirementsProcessor;
import org.cqframework.fhir.utilities.exception.IGInitializationException;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.*;
import org.opencds.cqf.tooling.exception.InvalidOperationArgs;
import org.opencds.cqf.tooling.igtools.IGLoggingService;
import org.opencds.cqf.tooling.npm.LibraryLoader;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.operations.library.LibraryPackage;
import org.opencds.cqf.tooling.processor.CqlProcessor;
import org.opencds.cqf.tooling.utilities.*;
import org.opencds.cqf.tooling.utilities.constants.CqfmConstants;
import org.opencds.cqf.tooling.utilities.converters.ResourceAndTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.*;

@Operation(name = "RefreshMeasure")
public class MeasureRefresh implements ExecutableOperation {
    private static final Logger logger = LoggerFactory.getLogger(MeasureRefresh.class);

    @OperationParam(alias = { "ptm", "pathtomeasure" }, setter = "setPathToMeasure", required = true,
            description = "Path to the FHIR Measure resource to refresh (required).")
    private String pathToMeasure;
    @OperationParam(alias = { "ptcql", "pathtocql" }, setter = "setPathToCql", required = true,
            description = "Path to the CQL content referenced or depended on by the FHIR Library resource to refresh (required).")
    private String pathToCql;
    @OperationParam(alias = { "e", "encoding" }, setter = "setEncoding", defaultValue = "json",
            description = "The file format to be used for representing the resulting FHIR Library { json, xml } (default json)")
    private String encoding;
    @OperationParam(alias = { "v", "version" }, setter = "setVersion", defaultValue = "r4",
            description = "FHIR version { stu3, r4, r5 } (default r4)")
    private String version;
    @OperationParam(alias = { "op", "outputpath" }, setter = "setOutputPath",
            description = "The directory path to which the generated FHIR resources should be written (default is to replace existing resources within the IG)")
    private String outputPath;

    private final FhirContext fhirContext;
    private IGUtils.IGInfo igInfo;
    private CqlProcessor cqlProcessor;

    private final List<LibraryPackage> libraryPackages;

    public MeasureRefresh(FhirContext fhirContext, String pathToCql) {
        this.fhirContext = fhirContext;
        this.pathToCql = pathToCql;
        this.libraryPackages = new ArrayList<>();
        LibraryLoader libraryLoader = new LibraryLoader(this.fhirContext.getVersion().getVersion().getFhirVersionString());
        UcumEssenceService ucumService;
        try {
            ucumService = new UcumEssenceService(UcumEssenceService.class.getResourceAsStream("/ucum-essence.xml"));
        } catch (UcumException e) {
            throw new IGInitializationException("Could not create UCUM validation service", e);
        }
        this.cqlProcessor = new CqlProcessor(null,
                Collections.singletonList(this.pathToCql), libraryLoader, new IGLoggingService(logger),
                ucumService, null, null);
    }

    public MeasureRefresh(IGUtils.IGInfo igInfo, CqlProcessor cqlProcessor) {
        this.igInfo = igInfo;
        this.fhirContext = igInfo.getFhirContext();
        this.cqlProcessor = cqlProcessor;
        this.libraryPackages = new ArrayList<>();
    }

    @Override
    public void execute() {
        IBaseResource measureToRefresh = IOUtils.readResource(pathToMeasure, fhirContext);
        if (!measureToRefresh.fhirType().equalsIgnoreCase("measure")) {
            throw new InvalidOperationArgs("Expected resource of type Measure, found " + measureToRefresh.fhirType());
        }

        if (cqlProcessor.getAllFileInformation().isEmpty()) {
            cqlProcessor.execute();
        }

        try {
            refreshMeasure(measureToRefresh);

            if (outputPath == null) {
                outputPath = pathToMeasure;
            }

            IOUtils.writeResource(measureToRefresh, outputPath, IOUtils.Encoding.valueOf(encoding), fhirContext);
        } catch (Exception e) {
            logger.error("Error refreshing measure: {}", pathToMeasure, e);
        }
    }

    public List<IBaseResource> refreshMeasures(IGUtils.IGInfo igInfo, CqlProcessor cqlProcessor) {
        List<IBaseResource> refreshedMeasures = new ArrayList<>();
        cqlProcessor.execute();
        if (igInfo.isRefreshMeasures()) {
            logger.info("Refreshing Measures...");
            for (var measure : RefreshUtils.getResourcesOfTypeFromDirectory(fhirContext,
                    "Measure", igInfo.getMeasureResourcePath())) {
                refreshedMeasures.add(refreshMeasure(measure));
            }
            //resolveLibraryPackages();
        }
        return refreshedMeasures;
    }

    // Measure access method
    public IBaseResource refreshMeasure(IBaseResource measureToRefresh) {
        Measure measure = (Measure) measureToRefresh;
        cqlProcessor.execute();

        logger.info("Refreshing {}", measure.getId());

        RefreshUtils.validatePrimaryLibraryReference(measure);
        String libraryUrl = measure.getLibrary().get(0).getValueAsString();
        for (CqlProcessor.CqlSourceFileInformation info : cqlProcessor.getAllFileInformation()) {
            if (libraryUrl.endsWith(info.getIdentifier().getId())) {
                // TODO: should likely verify or resolve/refresh the following elements:
                //  cqfm-artifactComment, cqfm-allocation, cqfm-softwaresystem, url, identifier, version,
                //  name, title, status, experimental, type, publisher, contact, description, useContext,
                //  jurisdiction, and profile(s) (http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/measure-cqfm)
                RefreshUtils.refreshDate(fhirContext, measure);
                RefreshUtils.addProfiles(measure, CqfmConstants.COMPUTABLE_MEASURE_PROFILE_URL);
                DataRequirementsProcessor dataRecProc = new DataRequirementsProcessor();
                Library moduleDefinitionLibrary = getModuleDefinitionLibrary(
                        measure, dataRecProc, info);
                RefreshUtils.refreshCqfmExtensions(measure, moduleDefinitionLibrary);
                RefreshUtils.attachModuleDefinitionLibrary(measure, moduleDefinitionLibrary);
            }
        }

        logger.info("Success!");
        return ResourceAndTypeConverter.convertToR5Resource(fhirContext, measure);
    }

    private Library getModuleDefinitionLibrary(Measure measure, DataRequirementsProcessor dataRecProc,
                                               CqlProcessor.CqlSourceFileInformation info) {
        Set<String> expressions = getExpressions(measure);
        return dataRecProc.gatherDataRequirements(
                cqlProcessor.getLibraryManager(),
                cqlProcessor.getLibraryManager().resolveLibrary(
                        info.getIdentifier(), new ArrayList<>()),
                cqlProcessor.getCqlTranslatorOptions().getCqlCompilerOptions(), expressions, true);
    }

    private Set<String> getExpressions(Measure measure) {
        Set<String> expressionSet = new HashSet<>();
        // TODO: check if expression is a cql expression
        measure.getSupplementalData().forEach(supData -> {
            if (supData.hasCriteria() && RefreshUtils.isExpressionIdentifier(supData.getCriteria())) {
                expressionSet.add(supData.getCriteria().getExpression());
            }
        });
        measure.getGroup().forEach(groupMember -> {
            groupMember.getPopulation().forEach(population -> {
                if (population.hasCriteria() && RefreshUtils.isExpressionIdentifier(population.getCriteria())) {
                    expressionSet.add(population.getCriteria().getExpression());
                }
            });
            groupMember.getStratifier().forEach(stratifier -> {
                if (stratifier.hasCriteria() && RefreshUtils.isExpressionIdentifier(stratifier.getCriteria())) {
                    expressionSet.add(stratifier.getCriteria().getExpression());
                }
            });
        });

        return expressionSet;
    }

    private String getCqlFromLibrary(Library library) {
        for (var content : library.getContent()) {
            if (content.hasContentType() && content.getContentType().equalsIgnoreCase("text/cql")) {
                return new String(content.getData());
            }
        }
        return null;
    }

    private void refreshContent(Library library, String cql, String elmXml, String elmJson) {
        library.setContent(Arrays.asList(
                new Attachment().setContentType("text/cql").setData(cql.getBytes()),
                new Attachment().setContentType("application/elm+xml").setData(elmXml.getBytes()),
                new Attachment().setContentType("application/elm+json").setData(elmJson.getBytes())));
    }

    public String getPathToMeasure() {
        return pathToMeasure;
    }

    public void setPathToMeasure(String pathToMeasure) {
        this.pathToMeasure = pathToMeasure;
    }

    public String getPathToCql() {
        return pathToCql;
    }

    public void setPathToCql(String pathToCql) {
        this.pathToCql = pathToCql;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
}

