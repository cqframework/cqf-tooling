package org.opencds.cqf.npm;

import static org.junit.Assert.assertTrue;

import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class NpmPackageManagerTests implements IWorkerContext.ILoggingService {
    /*
    NOTE: This test depends on the dev package cache for the [sample-ig](https://github.com/FHIR/sample-ig)
    Running the IG publisher on a clone of this IG locally will create and cache the package
     */
    @Test
    public void TestSampleIG() throws IOException {
        NpmPackageManager pm = NpmPackageManager.fromStream(NpmPackageManagerTests.class.getResourceAsStream("myig.xml"), "4.0.1");
        assertTrue(pm.getNpmList().size() >= 1);
    }

    /*
    NOTE: This test depends on the dev package cache for the [sample-content-ig](https://github.com/cqframework/sample-content-ig)
    Running the IG publisher on a clone of this IG locally will create and cache the package
     */
    @Test
    public void TestSampleContentIG() throws IOException {
        NpmPackageManager pm = NpmPackageManager.fromStream(NpmPackageManagerTests.class.getResourceAsStream("mycontentig.xml"), "4.0.1");
        assertTrue(pm.getNpmList().size() >= 1);
    }

    /*
    NOTE: This test depends on the dev package cache for the [opioid-mme-r4](https://github.com/cqframework/opioid-mme-r4)
    Running the IG publisher on a clone of this IG locally will create and cache the package
     */
    @Test
    public void TestOpioidMMEIG() throws IOException {
        NpmPackageManager pm = NpmPackageManager.fromStream(NpmPackageManagerTests.class.getResourceAsStream("opioid-mme-r4.xml"), "4.0.1");
        assertTrue(pm.getNpmList().size() >= 1);
    }

    /*
    NOTE: This test depends on the dev package cache for the [sample-content-ig](https://github.com/cqframework/sample-content-ig)
    Running the IG publisher on a clone of this IG locally will create and cache the package
     */
    @Test
    public void TestLibrarySourceProvider() throws IOException {
        NpmPackageManager pm = NpmPackageManager.fromStream(NpmPackageManagerTests.class.getResourceAsStream("mycontentig.xml"), "4.0.1");
        assertTrue(pm.getNpmList().size() >= 1);

        LibraryLoader reader = new LibraryLoader("4.0.1");
        NpmLibrarySourceProvider sp = new NpmLibrarySourceProvider(pm.getNpmList(), reader, this);
        InputStream is = sp.getLibrarySource(new VersionedIdentifier().withSystem("http://somewhere.org/fhir/uv/myig").withId("example"));
        assertTrue(is != null);
        is = sp.getLibrarySource(new VersionedIdentifier().withSystem("http://somewhere.org/fhir/uv/myig").withId("example").withVersion("0.2.0"));
        assertTrue(is != null);
    }

    @Override
    public void logMessage(String msg) {
        System.out.println(msg);
    }

    @Override
    public void logDebugMessage(IWorkerContext.ILoggingService.LogCategory category, String msg) {
        logMessage(msg);
    }
}
