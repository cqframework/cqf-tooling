package org.opencds.cqf.tooling.operations.stripcontent;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkArgument;
import ca.uhn.fhir.context.FhirVersionEnum;

/**
 * This class executes the StripContent command line operation. It picks the correct version
 * of a ContentStripper based on the FHIR version specified in the command line arguments. It then
 * iterates over all the files in the input directory and runs the stripFile method on each file.
 */
public class StripContentExecutor {

    private FhirVersionEnum versionEnum;
    private File inputDirectory;
    private File outputDirectory;
    private String cqlExportDirectory;

    public StripContentExecutor(StripContentParams params) {
        checkNotNull(params, "params must be provided");
        checkArgument(params.inputDirectory() != null, "inputDirectory must be provided");
        checkArgument(params.outputDirectory() != null, "outputDirectory must be provided");
        this.versionEnum = versionForString(params.fhirVersion());
        this.inputDirectory = validateDirectory(params.inputDirectory());
        this.outputDirectory = new File(params.outputDirectory());
        this.cqlExportDirectory = params.cqlExportDirectory();
    }

    public void execute() {
        var files = listResourceFiles(inputDirectory);
        var contentStripper = createContentStripper();
        var options = createContentStripperOptions();
        for (File file : files) {
            // Keep the same filename, but change the directory to the output directory
            var outputFile = outputDirectory.toPath().resolve(file.getName()).toFile();
            contentStripper.stripFile(file, outputFile, options);
        }
    }

    private ContentStripperOptions createContentStripperOptions() {
        var cqlExportFile = this.cqlExportDirectory != null ? new File(this.cqlExportDirectory) : null;
        return ContentStripperOptions.defaultOptions().cqlExportDirectory(cqlExportFile);
    }

    private ContentStripper createContentStripper() {
        switch (versionEnum) {
            case DSTU3:
                return new ContentStripperDstu3();
            case R4:
                return new ContentStripperR4();
            case R5:
                return new ContentStripperR5();
            default:
                throw new IllegalArgumentException("Unsupported FHIR version");
        }
    }

    private File validateDirectory(String pathToDir) {
        checkNotNull(pathToDir, "The path to the directory is required");
        File directory = new File(pathToDir);
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("The path supplied is not a directory");
        }
        return directory;
    }

    private Collection<File> listResourceFiles(File file) {
        return FileUtils.listFiles(file, new String[] { "json", "xml"}, true);
    }

    private FhirVersionEnum versionForString(String version) {
        if (version == null) {
            return FhirVersionEnum.R4;
        }
        return FhirVersionEnum.forVersionString(version.toUpperCase());
    }
}
