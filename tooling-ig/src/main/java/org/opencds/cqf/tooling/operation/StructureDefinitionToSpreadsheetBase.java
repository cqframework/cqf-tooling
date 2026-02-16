package org.opencds.cqf.tooling.operation;

import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.acceleratorkit.CanonicalResourceAtlas;

public abstract class StructureDefinitionToSpreadsheetBase extends Operation {
    protected String inputPath;
    protected String resourcePaths;
    protected String modelName;
    protected String modelVersion;
    protected boolean snapshotOnly = true;
    protected CanonicalResourceAtlas canonicalResourceAtlas;
    protected CanonicalResourceAtlas canonicalResourceDependenciesAtlas;

    protected CreationHelper helper;
    protected XSSFCellStyle linkStyle;

    protected boolean isParameterListComplete() {
        if (null == inputPath || inputPath.length() < 1 ||
                null == modelName || modelName.length() < 1 ||
                null == modelVersion || modelName.length() < 1 ||
                null == resourcePaths || resourcePaths.length() < 1) {
            System.out.println("These parameters are required: ");
            System.out.println("-modelName/-mn");
            System.out.println("-modelVersion/-mv");
            System.out.println("-outputpath/-op");
            System.out.println("-resourcePaths/-rp");
            return false;
        }
        return true;
    }
}
