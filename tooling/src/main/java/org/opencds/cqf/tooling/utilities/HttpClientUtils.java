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
        //let OS handle threading:
        ExecutorService executorService = Executors.newCachedThreadPool();
        try {

            LogUtils.info(tasks.size() + " POST calls to be made. Starting now. Please wait...");
            double percentage = (double) 0;
            System.out.print("\rPOST: " + String.format("%.2f%%", percentage) + " done. ");
            List<Future<Void>> futures = new ArrayList<>();
            for (Callable<Void> task : tasks) {
                futures.add(executorService.submit(task));
            }

            // Wait for all tasks to complete
            for (Future<Void> future : futures) {
                try {
                    future.get();
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

        } finally {
            // Shutdown the executor when you're done, even if an exception occurs
            executorService.shutdown();
        }
    }

    public static void post(String fhirServerUrl, IBaseResource resource, Encoding encoding, FhirContext fhirContext) throws IOException {
        try {
            final HttpPost post = new HttpPost(fhirServerUrl);
            post.addHeader("content-type", "application/" + encoding.toString());

            final String resourceString = IOUtils.encodeResourceAsString(resource, encoding, fhirContext);
            final StringEntity input = new StringEntity(resourceString);
            post.setEntity(input);

            final int currentTaskIndex = tasks.size() + 1;

            Callable<Void> task = () -> {
//                LogUtils.info(currentTaskIndex + " out of " + tasks.size() + " - Calling POST on " + encoding.toString() + " resource " + resource.getIdElement().toString());
                try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
                    HttpResponse response = httpClient.execute(post);
                    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    StringBuilder responseMessage = new StringBuilder();
                    String line = "";
                    while ((line = rd.readLine()) != null) {
                        responseMessage.append(line);
                    }
                    if (responseMessage.toString().contains("error")) {
                        failedPOSTcalls.add(currentTaskIndex + " out of " + tasks.size() + " - Error posting resource to FHIR server (" + fhirServerUrl + "). Resource was not posted : " + resource.getIdElement().getIdPart());
                    } else {
                        successfulPOSTcalls.add(currentTaskIndex + " out of " + tasks.size() + " - Resource successfully posted to FHIR server: " + resource.getIdElement().getIdPart());
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
