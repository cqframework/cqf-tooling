package org.opencds.cqf.tooling.measure;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.io.FilenameUtils;
import org.cqframework.cql.cql2elm.CqlCompilerException;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.hl7.fhir.r5.model.Measure;
import org.opencds.cqf.tooling.measure.r4.R4MeasureProcessor;
import org.opencds.cqf.tooling.measure.stu3.STU3MeasureProcessor;
import org.opencds.cqf.tooling.parameter.RefreshMeasureParameters;
import org.opencds.cqf.tooling.processor.BaseProcessor;
import org.opencds.cqf.tooling.processor.CqlProcessor;
import org.opencds.cqf.tooling.processor.IGProcessor;
import org.opencds.cqf.tooling.utilities.CanonicalUtils;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;
import org.opencds.cqf.tooling.utilities.ResourceUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MeasureProcessor extends BaseProcessor {
    public static final String RESOURCE_PREFIX = "measure-";
    protected List<Object> identifiers;

    public static String getId(String baseId) {
        return RESOURCE_PREFIX + baseId;
    }

    public List<String> refreshIgMeasureContent(BaseProcessor parentContext, Encoding outputEncoding, Boolean versioned, FhirContext fhirContext,
                                                String measureToRefreshPath, Boolean shouldApplySoftwareSystemStamp, Boolean shouldIncludePopDataRequirements) throws IOException {

        return refreshIgMeasureContent(parentContext, outputEncoding, null, versioned, fhirContext, measureToRefreshPath,
                shouldApplySoftwareSystemStamp, shouldIncludePopDataRequirements);
    }



    public List<String> refreshIgMeasureContent(BaseProcessor parentContext, Encoding outputEncoding, String measureOutputDirectory,
                                                Boolean versioned, FhirContext fhirContext, String measureToRefreshPath,
                                                Boolean shouldApplySoftwareSystemStamp, Boolean shouldIncludePopDataRequirements) throws IOException {

        logger.info("[Refreshing Measures]");

        MeasureProcessor measureProcessor;
        switch (fhirContext.getVersion().getVersion()) {
            case DSTU3:
                measureProcessor = new STU3MeasureProcessor();
                break;
            case R4:
                measureProcessor = new R4MeasureProcessor();
                break;
            default:
                throw new IllegalArgumentException(
                        "Unknown fhir version: " + fhirContext.getVersion().getVersion().getFhirVersionString());
        }

        var measurePath = FilenameUtils.concat(parentContext.getRootDir(), IGProcessor.MEASURE_PATH_ELEMENT);
        var params = new RefreshMeasureParameters();
        params.measurePath = measurePath;
        params.parentContext = parentContext;
        params.fhirContext = fhirContext;
        params.encoding = outputEncoding;
        params.versioned = versioned;
        params.measureOutputDirectory = measureOutputDirectory;
        params.shouldApplySoftwareSystemStamp = shouldApplySoftwareSystemStamp;
        params.includePopulationDataRequirements = shouldIncludePopDataRequirements;
        var contentList = measureProcessor.refreshMeasureContent(params);

        if (!measureProcessor.getIdentifiers().isEmpty()) {
            this.getIdentifiers().addAll(measureProcessor.getIdentifiers());
        }
        return contentList;
    }

    protected List<Object> getIdentifiers() {
        if (identifiers == null) {
            identifiers = new ArrayList<>();
        }
        return identifiers;
    }

    protected boolean versioned;
    protected FhirContext fhirContext;

    public List<String> refreshMeasureContent(RefreshMeasureParameters params) throws IOException {
        return new ArrayList<>();
    }

    protected List<Measure> refreshGeneratedContent(List<Measure> sourceMeasures) {
        return internalRefreshGeneratedContent(sourceMeasures);
    }

    private List<Measure> internalRefreshGeneratedContent(List<Measure> sourceMeasures) {
        // for each Measure, refresh the measure based on the primary measure library
       var resources = new ArrayList<Measure>();
        var processor = new MeasureRefreshProcessor();
        var libraryManager = getCqlProcessor().getLibraryManager();
        var cqlTranslatorOptions = getCqlProcessor().getCqlTranslatorOptions();
        for (var measure : sourceMeasures) {
            // Do not attempt to refresh if the measure does not have a library
            if (measure.hasLibrary()) {
                resources.add(refreshGeneratedContent(measure, processor, libraryManager, cqlTranslatorOptions));
            } else {
                resources.add(measure);
            }
        }

        return resources;
    }

    private Measure refreshGeneratedContent(Measure measure, MeasureRefreshProcessor processor,
                                            LibraryManager libraryManager, CqlTranslatorOptions cqlTranslatorOptions) {
        var libraryUrl = ResourceUtils.getPrimaryLibraryUrl(measure, fhirContext);
        var primaryLibraryIdentifier = CanonicalUtils.toVersionedIdentifier(libraryUrl);
        var errors = new CopyOnWriteArrayList<CqlCompilerException>();
        var compiledLibrary = libraryManager.resolveLibrary(primaryLibraryIdentifier, errors);
        logger.info(CqlProcessor.buildStatusMessage(errors, measure.getName(), verboseMessaging));
        boolean hasSevereErrors = CqlProcessor.hasSevereErrors(errors);

        //refresh measures without severe errors:
        if (!hasSevereErrors) {
            return processor.refreshMeasure(measure, libraryManager, compiledLibrary, cqlTranslatorOptions.getCqlCompilerOptions());
        }

        return measure;
    }
}
