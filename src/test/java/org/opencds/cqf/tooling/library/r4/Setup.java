package org.opencds.cqf.tooling.library.r4;

import java.io.IOException;

import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager;
import org.hl7.fhir.utilities.npm.ToolsVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;


public class Setup {

    private static final Logger logger = LoggerFactory.getLogger(Setup.class);


    @BeforeSuite(alwaysRun = true, enabled = true)
    public static void initializeTheNpmProvider() {
        try {
            logger.info("Initializing the npm cache");
            FilesystemPackageCacheManager pcm = new FilesystemPackageCacheManager(true, ToolsVersion.TOOLS_VERSION);
            try {
                pcm.loadPackage("hl7.fhir.r3.core", "3.0.2");
            }
            catch(IOException e) {}
            try {
                pcm.loadPackage("hl7.fhir.r4.core", "4.0.1");
            }
            catch(IOException e) {}
        }
        catch (IOException e) {
            logger.error("error creating package manager", e);
        }
    }
    
    @Test 
    public void setup() {
        // This is just to trick test ng into running this
    }
}
