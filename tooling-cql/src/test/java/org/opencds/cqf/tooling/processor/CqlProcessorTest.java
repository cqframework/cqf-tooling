package org.opencds.cqf.tooling.processor;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.hl7.fhir.r5.context.ILoggingService;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.testng.annotations.Test;

public class CqlProcessorTest {

    private static final ILoggingService NOOP_LOGGER = new ILoggingService() {
        @Override
        public void logMessage(String s) {}

        @Override
        public void logDebugMessage(LogCategory logCategory, String s) {}

        @Override
        public boolean isDebugLogging() {
            return false;
        }
    };

    private CqlProcessor createProcessor(List<String> folders) {
        return new CqlProcessor(
                new ArrayList<>(), // packages
                folders,
                null, // reader
                NOOP_LOGGER,
                null, // ucumService
                null, // packageId
                null, // canonicalBase
                false // verboseMessaging
        );
    }

    // ── CqlSourceFileInformation ──

    @Test
    public void cqlSourceFileInformation_storesPath() {
        CqlProcessor processor = createProcessor(new ArrayList<>());
        CqlProcessor.CqlSourceFileInformation info = processor.new CqlSourceFileInformation("/path/to/lib.cql");
        assertEquals(info.getPath(), "/path/to/lib.cql");
    }

    @Test
    public void cqlSourceFileInformation_defaultsAreNull() {
        CqlProcessor processor = createProcessor(new ArrayList<>());
        CqlProcessor.CqlSourceFileInformation info = processor.new CqlSourceFileInformation("/test.cql");
        assertNull(info.getCql());
        assertNull(info.getElm());
        assertNull(info.getJsonElm());
        assertNull(info.getIdentifier());
        assertNull(info.getOptions());
        assertNotNull(info.getErrors());
        assertTrue(info.getErrors().isEmpty());
    }

    @Test
    public void cqlSourceFileInformation_setAndGet() {
        CqlProcessor processor = createProcessor(new ArrayList<>());
        CqlProcessor.CqlSourceFileInformation info = processor.new CqlSourceFileInformation("/test.cql");
        info.setCql("test".getBytes());
        info.setElm("elm".getBytes());
        info.setJsonElm("json".getBytes());

        assertNotNull(info.getCql());
        assertNotNull(info.getElm());
        assertNotNull(info.getJsonElm());
    }

    // ── getFileInformation ──

    @Test
    public void getFileInformation_beforeExecute_throws() {
        CqlProcessor processor = createProcessor(new ArrayList<>());
        // fileMap is null before execute()
        assertThrows(IllegalStateException.class,
                () -> processor.getFileInformation("test.cql"));
    }

    @Test
    public void getFileInformation_nullFilename_returnsNull() throws Exception {
        CqlProcessor processor = createProcessor(new ArrayList<>());
        processor.execute();
        assertNull(processor.getFileInformation(null));
    }

    @Test
    public void getFileInformation_missingFile_returnsNull() throws Exception {
        CqlProcessor processor = createProcessor(new ArrayList<>());
        processor.execute();
        assertNull(processor.getFileInformation("nonexistent.cql"));
    }

    // ── getAllFileInformation ──

    @Test
    public void getAllFileInformation_beforeExecute_throws() {
        CqlProcessor processor = createProcessor(new ArrayList<>());
        assertThrows(IllegalStateException.class,
                () -> processor.getAllFileInformation());
    }

    @Test
    public void getAllFileInformation_afterExecuteWithNoFolders_returnsEmpty() throws Exception {
        CqlProcessor processor = createProcessor(new ArrayList<>());
        processor.execute();

        Collection<CqlProcessor.CqlSourceFileInformation> infos = processor.getAllFileInformation();
        assertNotNull(infos);
        assertTrue(infos.isEmpty());
    }

    // ── getFileMap ──

    @Test
    public void getFileMap_beforeExecute_returnsNull() {
        CqlProcessor processor = createProcessor(new ArrayList<>());
        assertNull(processor.getFileMap());
    }

    @Test
    public void getFileMap_afterExecute_returnsMap() throws Exception {
        CqlProcessor processor = createProcessor(new ArrayList<>());
        processor.execute();

        Map<String, CqlProcessor.CqlSourceFileInformation> map = processor.getFileMap();
        assertNotNull(map);
    }

    // ── getGeneralErrors ──

    @Test
    public void getGeneralErrors_beforeExecute_returnsEmpty() {
        CqlProcessor processor = createProcessor(new ArrayList<>());
        // fileMap is null, so getGeneralErrors returns empty list
        List<ValidationMessage> errors = processor.getGeneralErrors();
        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void getGeneralErrors_afterExecuteNoOrphans_returnsEmpty() throws Exception {
        CqlProcessor processor = createProcessor(new ArrayList<>());
        processor.execute();

        List<ValidationMessage> errors = processor.getGeneralErrors();
        assertTrue(errors.isEmpty());
    }

    // ── execute with empty folders ──

    @Test
    public void execute_emptyFolders_completesWithoutError() throws Exception {
        CqlProcessor processor = createProcessor(new ArrayList<>());
        processor.execute();
        assertNotNull(processor.getFileMap());
        assertTrue(processor.getFileMap().isEmpty());
    }

    // ── namespace ──

    @Test
    public void constructor_withPackageIdAndCanonicalBase_createsNamespace() throws Exception {
        CqlProcessor processor = new CqlProcessor(
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                NOOP_LOGGER,
                null,
                "com.example.ig",
                "http://example.org/ig",
                false
        );
        processor.execute();
        // Should not throw - namespace was created
        assertNotNull(processor.getFileMap());
    }

    @Test
    public void constructor_withNullPackageId_noNamespace() throws Exception {
        CqlProcessor processor = new CqlProcessor(
                new ArrayList<>(),
                new ArrayList<>(),
                null,
                NOOP_LOGGER,
                null,
                null,
                null,
                false
        );
        processor.execute();
        assertNotNull(processor.getFileMap());
    }
}
