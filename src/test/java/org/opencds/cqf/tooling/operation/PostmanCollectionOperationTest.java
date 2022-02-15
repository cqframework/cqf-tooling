package org.opencds.cqf.tooling.operation;

import java.net.URISyntaxException;

import org.opencds.cqf.tooling.Operation;
import org.testng.annotations.Test;

public class PostmanCollectionOperationTest {

    @Test
    public void test_worked() throws URISyntaxException {
        String dataInputPath = "postman-collection.resources";
        String operation = "PostmanCollection";
        String inputFilePath = PostmanCollectionOperationTest.class.getResource(dataInputPath).toURI().getPath();
        String outputPath = "target/test-output/create-postman-collection";
        String version = "r4";
        String urlBase = "cqm-sandbox.alphora.com";
        String urlPath = "cqf-ruler-r4/fhir/";
        String protocol = "http";
        String name = "Postman Collection";
        String[] args = { "-" + operation, "-ptbd=" + inputFilePath, "-op=" + outputPath, "-v=" + version, "-host=" + urlBase, "-path=" + urlPath, "-protocol=" + protocol, "-name=" + name };
        Operation postmanCollectionOperation = new PostmanCollectionOperation();
        postmanCollectionOperation.execute(args);
    }
}
