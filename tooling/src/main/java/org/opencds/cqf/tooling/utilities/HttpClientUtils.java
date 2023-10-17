package org.opencds.cqf.tooling.utilities;

import ca.uhn.fhir.context.FhirContext;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

public class HttpClientUtils {
    private static final List<String> failedPOSTcalls = new CopyOnWriteArrayList<>();
    private static final List<String> successfulPOSTcalls = new CopyOnWriteArrayList<>();
    private static final List<Callable<Void>> tasks = new ArrayList<>();
    private static int counter = 0;

    private static void reportProgress() {
        counter++;
        double percentage = (double) counter / tasks.size() * 100;
        System.out.print("\rPOST calls: " + String.format("%.2f%%", percentage) + " processed. ");
    }

    public static boolean hasPostTasksInQueue() {
        return !tasks.isEmpty();
    }

    public static void postTaskCollection() {

        LogUtils.info(tasks.size() + " POST calls to be made. Starting now. Please wait...");
        double percentage = (double) 0;
        System.out.print("\rPOST: " + String.format("%.2f%%", percentage) + " done. ");

        //sequential processing instead of multithreaded, for stability:
        for (Callable<Void> task : tasks) {
            try {
                task.call();  // Execute the task directly
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.putException("postTaskCollection", e);
                LogUtils.info("POST task " + tasks.indexOf(task) + " of " + tasks.size() + " encountered an error while starting. Stopping...");
                return;
            }
        }

        // Create a custom comparator to sort based on the numeric value
        Comparator<String> postResultMessageComparator = new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                int value1 = extractValue(s1);
                int value2 = extractValue(s2);
                return Integer.compare(value1, value2);
            }

            private int extractValue(String s) {
                String[] parts = s.split(" ");
                if (parts.length > 0) {
                    try {
                        return Integer.parseInt(parts[0]);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                }
                return 0;
            }
        };
        LogUtils.info("Processing results...");
        successfulPOSTcalls.sort(postResultMessageComparator);
        failedPOSTcalls.sort(postResultMessageComparator);


        StringBuilder message = new StringBuilder();
        message.append("\r\n").append(successfulPOSTcalls.size()).append(" resources successfully posted.");
        for (String successPost : successfulPOSTcalls) {
            message.append("\n").append(successPost);
        }
        LogUtils.info(message.toString());

        message = new StringBuilder();
        message.append("\r\n").append(failedPOSTcalls.size()).append(" resources failed to post.");
        for (String failedPost : failedPOSTcalls) {
            message.append("\n").append(failedPost);
        }
        LogUtils.info(message.toString());

    }

    public static void post(String fhirServerUrl, IBaseResource resource, Encoding encoding, FhirContext fhirContext) throws IOException {
        try {
            final HttpPost post = new HttpPost(fhirServerUrl);
            post.addHeader("content-type", "application/" + encoding.toString());

            final String resourceString = IOUtils.encodeResourceAsString(resource, encoding, fhirContext);
            final StringEntity input = new StringEntity(resourceString);
            post.setEntity(input);

            // Configure the request timeout
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(60000)
                    .setConnectTimeout(60000)
                    .build();
            post.setConfig(requestConfig);

            final int currentTaskIndex = tasks.size() + 1;

            Callable<Void> task = () -> {
//                LogUtils.info(currentTaskIndex + " out of " + tasks.size() + " - Calling POST on " + encoding.toString() + " resource " + resource.getIdElement().toString());
                try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
                    HttpResponse response = httpClient.execute(post);

                    StatusLine statusLine = response.getStatusLine();
                    int statusCode = statusLine.getStatusCode(); // Get the HTTP status code (e.g., 200, 404, 500, etc.)
                    String reasonPhrase = statusLine.getReasonPhrase(); // Get the reason phrase (e.g., OK, Not Found, Internal Server Error, etc.)
                    String httpVersion = statusLine.getProtocolVersion().toString(); // Get the HTTP version (e.g., HTTP/1.1)

                    if (statusCode >= 200 && statusCode < 300) {
                        //status codes in the 200 range indicate success
                        successfulPOSTcalls.add(currentTaskIndex + " out of " + tasks.size() + " - Resource successfully posted to FHIR server: " + resource.getIdElement().getIdPart());
                    } else {
                        String detailedMessage = "HTTP Status: " + statusCode + " " + reasonPhrase + " (HTTP Version: " + httpVersion + ")";
                        failedPOSTcalls.add(currentTaskIndex + " out of " + tasks.size() + " - Error posting resource to FHIR server (" + fhirServerUrl
                                + ") " + resource.getIdElement().getIdPart() + ": " + detailedMessage);
                    }
                } catch (IOException e) {
                    failedPOSTcalls.add(currentTaskIndex + " out of " + tasks.size() + " - Error while making the POST request: " + e.getMessage());
                } catch (Exception e) {
                    failedPOSTcalls.add(currentTaskIndex + " out of " + tasks.size() + " - Error during POST request execution: " + e.getMessage());
                }
                //task requires return statement
                reportProgress();
                return null;
            };

            tasks.add(task);
        } catch (Exception e) {
            LogUtils.putException("Error while submitting the POST request: " + e.getMessage(), e);
        }
    }


    public static String get(String path) throws IOException {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpGet get = new HttpGet(path);

            HttpResponse response = httpClient.execute(get);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder responseMessage = new StringBuilder();
            String line = "";
            while ((line = rd.readLine()) != null) {
                responseMessage.append(line);
            }

            return responseMessage.toString();
        }
    }
}
