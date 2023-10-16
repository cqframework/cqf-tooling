package org.opencds.cqf.tooling.utilities;

import ca.uhn.fhir.context.FhirContext;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.utilities.IOUtils.Encoding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpClientUtils {
    public static void post(String fhirServerUrl, IBaseResource resource, Encoding encoding, FhirContext fhirContext) throws IOException {
        LogUtils.info("Calling POST on " + encoding.toString() + " resource " + resource.getIdElement().toString());

        // Create a new ExecutorService for each POST request
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        try {
            executorService.submit(() -> {
                try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
                    HttpPost post = new HttpPost(fhirServerUrl);
                    post.addHeader("content-type", "application/" + encoding.toString());

                    String resourceString = IOUtils.encodeResourceAsString(resource, encoding, fhirContext);
                    StringEntity input = new StringEntity(resourceString);
                    post.setEntity(input);
                    HttpResponse response = httpClient.execute(post);
                    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    String responseMessage = "";
                    String line = "";
                    while ((line = rd.readLine()) != null) {
                        responseMessage += line;
                    }

                    if (responseMessage.contains("error")) {
                        // Handle the error and provide feedback to the user
                        LogUtils.info("Error posting resource to FHIR server (" + fhirServerUrl + "). Resource was not posted : " + resource.getIdElement().getIdPart());
                        // You can extract and handle the error code from the responseMessage here
                    } else {
                        LogUtils.info("Resource successfully posted to FHIR server: " + resource.getIdElement().getIdPart());
                    }
                } catch (IOException e) {
                    // Handle the exception appropriately, e.g., log, rethrow, or provide user feedback
                    LogUtils.putException("Error while making the POST request: " +  e.getMessage(), e);
                } catch (Exception e) {
                    // Handle other exceptions, e.g., log, rethrow, or provide user feedback
                    LogUtils.putException("Error during POST request execution: " + e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            // Handle the exception appropriately, e.g., log, rethrow, or provide user feedback
            LogUtils.putException("Error while submitting the POST request: " + e.getMessage(), e);
        } finally {
            executorService.shutdown();
        }
    }


    public static String get(String path) throws IOException {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpGet get = new HttpGet(path);

            HttpResponse response = httpClient.execute(get);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String responseMessage = "";
            String line = "";
            while ((line = rd.readLine()) != null) {
                responseMessage += line;
            }

            return responseMessage;
        }
    }
}
