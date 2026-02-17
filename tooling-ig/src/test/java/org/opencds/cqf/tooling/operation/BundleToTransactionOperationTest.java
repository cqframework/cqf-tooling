package org.opencds.cqf.tooling.operation;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.operations.ExecutableOperationAdapter;
import org.opencds.cqf.tooling.operations.bundle.BundleTransaction;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class BundleToTransactionOperationTest {
    private WireMockServer wireMockServer;
    private int port;

    @BeforeMethod
    public void setUp() throws IOException {
        port = findAvailablePort();
        wireMockServer = new WireMockServer(port);
        wireMockServer.start();
        WireMock.configureFor("localhost", port);
        wireMockServer.stubFor(WireMock.post(WireMock.urlPathMatching("/fhir.*"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/fhir+json")
                        .withBody("{\"resourceType\":\"Bundle\",\"type\":\"transaction-response\"}")));
        wireMockServer.stubFor(WireMock.get(WireMock.urlPathMatching("/fhir.*"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/fhir+json")
                        .withBody("{\"resourceType\":\"CapabilityStatement\"}")));
    }

    @AfterMethod
    public void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    public void testExecute_BundleDecomposition() throws IOException {
        String projectPath = System.getProperty("user.dir");
        String relativeJsonPath = "src/main/resources/libraryevaluationtest-bundle.json";
        File bundleFile = new File(projectPath + File.separator + relativeJsonPath);
        Assert.assertTrue(bundleFile.exists(), "Input bundle file should exist: " + bundleFile.getAbsolutePath());

        // BundleTransaction expects a directory path, so copy the bundle to a temp directory
        File tempDir = Files.createTempDirectory("bundleTransactionTest").toFile();
        tempDir.deleteOnExit();
        File destFile = new File(tempDir, bundleFile.getName());
        Files.copy(bundleFile.toPath(), destFile.toPath());
        destFile.deleteOnExit();

        String fhirServerUrl = "http://localhost:" + port + "/fhir/";

        Operation bundleToTransactionOperation = new ExecutableOperationAdapter(new BundleTransaction());
        String[] args =
                new String[] {"-MakeTransaction", "-ptb=" + tempDir.getAbsolutePath(), "-v=r4", "-fs=" + fhirServerUrl};
        bundleToTransactionOperation.execute(args);

        // Verify the FHIR server received at least one transaction request
        int requestCount = WireMock.getAllServeEvents().size();
        Assert.assertTrue(requestCount > 0, "Expected at least one transaction request to the FHIR server");
    }

    private static int findAvailablePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
