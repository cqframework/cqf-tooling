package org.opencds.cqf.tooling.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;

import ca.uhn.fhir.context.FhirContext;

public class HttpClientUtils {

    private HttpClientUtils() {}

    public static void post(String fhirServerUrl, IBaseResource resource, Encoding encoding, FhirContext fhirContext)
            throws IOException {  
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost post = new HttpPost(fhirServerUrl);
            post.addHeader("content-type", "application/" + encoding.toString());

            String resourceString = IOUtils.encodeResourceAsString(resource, encoding, fhirContext);
            StringEntity input = new StringEntity(resourceString);
            post.setEntity(input);
            String response = getResponse(httpClient.execute(post));
            if (response.contains("error")) {
                throw new IOException("Error posting resource to FHIR server (" + fhirServerUrl + "). Resource was not posted : " +  resource.getIdElement().getIdPart());
            }
        }
    }

    public static String get(String path) throws IOException {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpGet get = new HttpGet(path);
            return getResponse(httpClient.execute(get));
        }
    }

    public static String getResponse(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuilder responseMessage = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            responseMessage.append(line);
        }

        return responseMessage.toString();
    }
}