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
import java.util.*;
import java.util.concurrent.*;

public class HttpClientUtils {

    private static Map<String, POSTInfoPojo> failedPostCalls = new ConcurrentHashMap<>();
    private static List<String> successfulPostCalls = new CopyOnWriteArrayList<>();
    private static Map<IBaseResource, Callable<Void>> tasks = new ConcurrentHashMap<>();
    private static int counter = 0;


    private static void reportProgress() {
        counter++;
        double percentage = (double) counter / tasks.size() * 100;
        System.out.print("\rPOST calls: " + String.format("%.2f%%", percentage) + " processed. ");
    }

    public static boolean hasPostTasksInQueue() {
        return !tasks.isEmpty();
    }

    /**
     * Method builds up tasks to be executed later
     */
    public static void post(String fhirServerUrl, IBaseResource resource, Encoding encoding, FhirContext fhirContext) throws IOException {
        try {
            POSTInfoPojo postPojo = new POSTInfoPojo(fhirServerUrl, resource, encoding, fhirContext);

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
                try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
                    HttpResponse response = httpClient.execute(post);
                    StatusLine statusLine = response.getStatusLine();
                    int statusCode = statusLine.getStatusCode(); // Get the HTTP status code (e.g., 200, 404, 500, etc.)
                    String reasonPhrase = statusLine.getReasonPhrase(); // Get the reason phrase (e.g., OK, Not Found, Internal Server Error, etc.)
                    String httpVersion = statusLine.getProtocolVersion().toString(); // Get the HTTP version (e.g., HTTP/1.1)


                    if (statusCode >= 200 && statusCode < 300) {
                        //status codes in the 200 range indicate success
                        successfulPostCalls.add(currentTaskIndex + " out of " + tasks.size() + " - Resource successfully posted to FHIR server: " + resource.getIdElement().getIdPart());
                    } else {

                        String detailedMessage = currentTaskIndex + " out of " + tasks.size() + " - Error posting resource to FHIR server (" + fhirServerUrl
                                + ") " + resource.getIdElement().getIdPart() + ": HTTP Status: " + statusCode + " " + reasonPhrase + " (HTTP Version: " + httpVersion + ")";

                        failedPostCalls.put(detailedMessage,
                                postPojo);
                    }


                } catch (IOException e) {
                    failedPostCalls.put(currentTaskIndex + " out of " + tasks.size() + " - Error while making the POST request: " + e.getMessage(),
                            postPojo);
                } catch (Exception e) {
                    failedPostCalls.put(currentTaskIndex + " out of " + tasks.size() + " - Error during POST request execution: " + e.getMessage(),
                            postPojo);
                }

                reportProgress();

                return null; // task requires a return statement
            };

            tasks.put(resource, task);
        } catch (Exception e) {
            LogUtils.putException("Error while submitting the POST request: " + e.getMessage(), e);
        }
    }


    public static void postTaskCollection() {

            LogUtils.info(tasks.size() + " POST calls to be made. Starting now. Please wait...");
            double percentage = (double) 0;
            System.out.print("\rPOST: " + String.format("%.2f%%", percentage) + " done. ");

            for (Callable<Void> task : tasks.values()) {
                try {
                    task.call();
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
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
            printSuccessList(postResultMessageComparator);
            StringBuilder message;


            //retry the failed POSTs once
            if (!failedPostCalls.isEmpty()){
                LogUtils.info(failedPostCalls.size() + " tasks failed to POST. Retrying.");
                Map<String, POSTInfoPojo> failedPostCallList = new ConcurrentHashMap<>(failedPostCalls);

                //reset lists before processing:
                failedPostCalls = new ConcurrentHashMap<>();
                tasks = new ConcurrentHashMap<>();
                successfulPostCalls = new CopyOnWriteArrayList<>();
                counter = 0;
                //process each sequentially:
                for (POSTInfoPojo postPojo : failedPostCallList.values()){
                    try {
                        post(postPojo.fhirServerUrl, postPojo.resource, postPojo.encoding, postPojo.fhirContext);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                for (Callable<Void> task : tasks.values()){
                    try {
                        task.call();
                        Thread.sleep(100);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            if (!failedPostCalls.isEmpty()) {
                LogUtils.info("\n" + failedPostCalls.size() + " task(s) still failed to POST: ");
                List<String> failedMessages = new ArrayList<>(failedPostCalls.keySet());
                failedMessages.sort(postResultMessageComparator);
                message = new StringBuilder();
                message.append("\r\n").append(failedMessages.size()).append(" resources failed to post.");
                for (String failedPost : failedMessages) {
                    message.append("\n").append(failedPost);
                }
                LogUtils.info(message.toString());
            }else{
                LogUtils.info("Retry successful, all tasks successfully posted");
            }

            if (!successfulPostCalls.isEmpty()){
                printSuccessList(postResultMessageComparator);
            }



    }

    private static void printSuccessList(Comparator<String> postResultMessageComparator) {
        successfulPostCalls.sort(postResultMessageComparator);
        StringBuilder message = new StringBuilder();
        message.append("\r\n").append(successfulPostCalls.size()).append(" resources successfully posted.");
        for (String successPost : successfulPostCalls) {
            message.append("\n").append(successPost);
        }
        LogUtils.info(message.toString());
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


    private static class POSTInfoPojo {
        String fhirServerUrl;
        IBaseResource resource;
        Encoding encoding;
        FhirContext fhirContext;

        public POSTInfoPojo(String fhirServerUrl, IBaseResource resource, Encoding encoding, FhirContext fhirContext){
            this.fhirServerUrl = fhirServerUrl;
            this.resource = resource;
            this.encoding = encoding;
            this.fhirContext = fhirContext;
        }
    }
}
