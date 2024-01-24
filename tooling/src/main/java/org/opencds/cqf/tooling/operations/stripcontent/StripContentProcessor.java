package org.opencds.cqf.tooling.operations.stripcontent;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Preconditions;
import ca.uhn.fhir.context.FhirVersionEnum;
public class StripContentProcessor {

    private FhirVersionEnum versionEnum;
    private File inputRoot;
    private File outputRoot;
    public StripContentProcessor(StripContentParams params) {
        Preconditions.checkNotNull(params);
        Preconditions.checkArgument(params.inputPath() != null, "Input path must be provided");
        Preconditions.checkArgument(params.outputPath() != null, "Output path must be provided");
        this.versionEnum = versionForString(params.version());
        this.inputRoot = validateDirectory(params.inputPath());
        this.outputRoot = new File(params.outputPath());
    }

    public void execute() {
        var files = getListOfActionableFiles(inputRoot);

        IStripContent stripContent = null;
        switch (versionEnum) {
            case DSTU3:
                stripContent = new StripContentDstu3();
                break;
            case R4:
                stripContent = new StripContentR4();
                break;
            case R5:
                stripContent = new StripContentR5();
                break;
            default:
                throw new IllegalArgumentException("Unsupported FHIR version");
        }

        for (File file : files) {
            var outputFile = outputRoot.toPath().resolve(file.getName()).toFile();
            stripContent.stripFile(file, outputFile);
        }
    }

    private File validateDirectory(String pathToDir) {
        if (pathToDir == null) {
            throw new IllegalArgumentException("The path to the directory is required");
        }

        File bundleDirectory = new File(pathToDir);
        if (!bundleDirectory.isDirectory()) {
            throw new IllegalArgumentException("The path supplied is not a directory");
        }
        return bundleDirectory;
    }

    private Collection<File> getListOfActionableFiles(File file) {
        return FileUtils.listFiles(file, new String[] { "json", "xml"}, true);
    }

    private FhirVersionEnum versionForString(String version) {
        if (version == null) {
            return FhirVersionEnum.R4;
        }
        return FhirVersionEnum.forVersionString(version.toUpperCase());
    }
}
