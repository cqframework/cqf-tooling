package org.opencds.cqf.tooling.acceleratorkit.util;

import java.io.File;

public abstract class FilesComparator {
    public abstract void compareFilesAndAssertIfNotEqual(File inputFile, File compareFile);
}
