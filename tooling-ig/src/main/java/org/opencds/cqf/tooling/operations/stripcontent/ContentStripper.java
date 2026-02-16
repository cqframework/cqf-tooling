package org.opencds.cqf.tooling.operations.stripcontent;

import java.io.File;

// Intentionally package-private. This is a package-internal API for ContentStripper
interface ContentStripper {
    void stripFile(File inputPath, File outputPath, ContentStripperOptions options);
}