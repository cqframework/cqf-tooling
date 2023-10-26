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
import org.apache.commons.lang3.tuple.Pair;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class HttpClientUtils {
    private static final String FHIR_SERVER_URL = "FHIR Server URL";
    private static final String BUNDLE_RESOURCE = "Bundle Resource";
    private static final String ENCODING_TYPE = "Encoding Type";
    private static final String FHIR_CONTEXT = "FHIR Context";
    private static final int MAX_SIMULTANEOUS_POST_COUNT = 10;

    private static Queue<Pair<String,POSTInfoPojo>> failedPostCalls = new ConcurrentLinkedQueue<>();
    private static List<String> successfulPostCalls = new CopyOnWriteArrayList<>();
    private static Map<IBaseResource, Callable<Void>> tasks = new ConcurrentHashMap<>();
    private static List<IBaseResource> runningPostTaskList = new CopyOnWriteArrayList<>();
    private static final AtomicInteger counter = new AtomicInteger(0);

    public static boolean hasPostTasksInQueue() {
        return !tasks.isEmpty();
    }

    public static void post(String fhirServerUrl, IBaseResource resource, IOUtils.Encoding encoding, FhirContext fhirContext) throws IOException {
        List<String> missingValues = new ArrayList<>();
        List<String> values = new ArrayList<>();
        validateAndAddValue(fhirServerUrl, FHIR_SERVER_URL, missingValues, values);
        validateAndAddValue(resource, BUNDLE_RESOURCE, missingValues, values, r -> r.getIdElement().getIdPart());
        validateAndAddValue(encoding, ENCODING_TYPE, missingValues, values);
        validateAndAddValue(fhirContext, FHIR_CONTEXT, missingValues, values);

        if (!missingValues.isEmpty()) {
            String missingValueString = String.join(", ", missingValues);
            LogUtils.info("An invalid HTTP POST call was attempted with a null value for: " + missingValueString +
                    (!values.isEmpty() ? "\\nRemaining values are: " + String.join(", ", values) : ""));
        }

        createPostTask(fhirServerUrl, resource, encoding, fhirContext);
    }

    private static <T> void validateAndAddValue(T value, String label, List<String> missingValues, List<String> values) {
        validateAndAddValue(value, label, missingValues, values, Object::toString);
    }

    private static <T> void validateAndAddValue(T value, String label, List<String> missingValues, List<String> values, Function<T, String> valueToString) {
        if (value == null) {
            missingValues.add(label);
        } else {
            values.add(label + ": " + valueToString.apply(value));
        }
    }

    private static void createPostTask(String fhirServerUrl, IBaseResource resource, IOUtils.Encoding encoding, FhirContext fhirContext) {
        try {
            final int currentTaskIndex = tasks.size() + 1;
            POSTInfoPojo postPojo = new POSTInfoPojo(fhirServerUrl, resource, encoding, fhirContext);
            HttpPost post = configureHttpPost(fhirServerUrl, resource, encoding, fhirContext);

            Callable<Void> task = createPostCallable(post, postPojo, currentTaskIndex);
            tasks.put(resource, task);
        } catch (Exception e) {
            LogUtils.putException("Error while submitting the POST request: " + e.getMessage(), e);
        }
    }

    private static HttpPost configureHttpPost(String fhirServerUrl, IBaseResource resource, IOUtils.Encoding encoding, FhirContext fhirContext) {
        HttpPost post = new HttpPost(fhirServerUrl);
        post.addHeader("content-type", "application/" + encoding.toString());

        String resourceString = IOUtils.encodeResourceAsString(resource, encoding, fhirContext);
        StringEntity input = null;
        try {
            input = new StringEntity(resourceString);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        post.setEntity(input);

        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(60000)
                .setConnectTimeout(60000)
                .build();
        post.setConfig(requestConfig);

        return post;
    }

    private static Callable<Void> createPostCallable(HttpPost post, POSTInfoPojo postPojo, int currentTaskIndex) {
        return () -> {
            try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
                HttpResponse response = httpClient.execute(post);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                String reasonPhrase = statusLine.getReasonPhrase();
                String httpVersion = statusLine.getProtocolVersion().toString();

                if (statusCode >= 200 && statusCode < 300) {
                    successfulPostCalls.add(currentTaskIndex + " out of " + tasks.size() + " - Resource successfully posted to FHIR server: " + postPojo.resource.getIdElement().getIdPart());
                } else {
                    String detailedMessage = currentTaskIndex + " out of " + tasks.size() + " - Error posting resource to FHIR server (" + postPojo.fhirServerUrl
                            + ") " + postPojo.resource.getIdElement().getIdPart() + ": HTTP Status: " + statusCode + " " + reasonPhrase + " (HTTP Version: " + httpVersion + ")";

                    failedPostCalls.add(Pair.of(detailedMessage, postPojo));
                }

            } catch (IOException e) {
                failedPostCalls.add(Pair.of(currentTaskIndex + " out of " + tasks.size() + " - Error while making the POST request: " + e.getMessage(), postPojo));
            } catch (Exception e) {
                failedPostCalls.add(Pair.of(currentTaskIndex + " out of " + tasks.size() + " - Error during POST request execution: " + e.getMessage(), postPojo));
            }

            runningPostTaskList.remove(postPojo.resource);
            reportProgress();
            return null;
        };
    }

    private static void reportProgress() {
        int currentCounter = counter.incrementAndGet();
        double percentage = (double) currentCounter / tasks.size() * 100;
        System.out.print("\rPOST calls: " + String.format("%.2f%%", percentage) + " processed. Thread pool size: " + runningPostTaskList.size() + " ");
    }

    public static void postTaskCollection() {
        ExecutorService executorService = Executors.newFixedThreadPool(1);

        try {
            LogUtils.info(tasks.size() + " POST calls to be made. Starting now. Please wait...");
            double percentage = 0;
            System.out.print("\rPOST: " + String.format("%.2f%%", percentage) + " done. ");

            List<Future<Void>> futures = new ArrayList<>();
            List<IBaseResource> resources = new ArrayList<>(tasks.keySet());
            for (int i = 0; i < resources.size(); i++) {
                IBaseResource thisResource = resources.get(i);
                if (runningPostTaskList.size() < MAX_SIMULTANEOUS_POST_COUNT) {
                    runningPostTaskList.add(thisResource);
                    futures.add(executorService.submit(tasks.get(thisResource)));
                } else {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        LogUtils.putException("postTaskCollection", new RuntimeException(e));
                    }
                    i--;
                }
            }

            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

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
            successfulPostCalls.sort(postResultMessageComparator);

            StringBuilder message = new StringBuilder();
            message.append("\r\n").append(successfulPostCalls.size()).append(" resources successfully posted.");
            for (String successPost : successfulPostCalls) {
                message.append("\n").append(successPost);
            }
            LogUtils.info(message.toString());
            successfulPostCalls = new ArrayList<>();

            if (!failedPostCalls.isEmpty()) {
                System.out.println(failedPostCalls.size() + " tasks failed to POST. Retry these failed posts? (Y/N)");
                Scanner scanner = new Scanner(System.in);
                String userInput = scanner.nextLine().trim().toLowerCase();

                if (userInput.equalsIgnoreCase("y")) {
                    List<Pair<String, POSTInfoPojo>> failedPostCallList = new ArrayList<>(failedPostCalls);
                    cleanUp(); //clear the queue, reset the counter, start fresh

                    for (Pair<String, POSTInfoPojo> pair : failedPostCallList) {
                        String errorMessage = pair.getLeft();
                        POSTInfoPojo postPojo = pair.getRight();
                        try {
                            post(postPojo.fhirServerUrl, postPojo.resource, postPojo.encoding, postPojo.fhirContext);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    for (Callable<Void> task : tasks.values()) {
                        try {
                            task.call();
                            Thread.sleep(50);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    if (failedPostCalls.isEmpty()) {
                        LogUtils.info("Retry successful, all tasks successfully posted");
                    }
                }
            }

            if (!successfulPostCalls.isEmpty()) {
                message = new StringBuilder();
                message.append("\r\n").append(successfulPostCalls.size()).append(" resources successfully posted.");
                for (String successPost : successfulPostCalls) {
                    message.append("\n").append(successPost);
                }
                LogUtils.info(message.toString());
                successfulPostCalls = new ArrayList<>();
            }

            if (!failedPostCalls.isEmpty()) {
                LogUtils.info("\n" + failedPostCalls.size() + " task(s) still failed to POST: ");
                List<String> failedMessages = new ArrayList<>();
                for (Pair<String, POSTInfoPojo> pair : failedPostCalls) {
                    failedMessages.add(pair.getLeft());
                }
                failedMessages.sort(postResultMessageComparator);
                message = new StringBuilder();
                message.append("\r\n").append(failedMessages.size()).append(" resources failed to post.");
                for (String failedPost : failedMessages) {
                    message.append("\n").append(failedPost);
                }
                LogUtils.info(message.toString());
            }

        } finally {
            cleanUp();
            executorService.shutdown();
        }
    }


    private static void cleanUp() {
        failedPostCalls = new ConcurrentLinkedQueue<>();
        successfulPostCalls = new CopyOnWriteArrayList<>();
        tasks = new ConcurrentHashMap<>();
        counter.set(0);
        runningPostTaskList = new CopyOnWriteArrayList<>();
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
        IOUtils.Encoding encoding;
        FhirContext fhirContext;

        public POSTInfoPojo(String fhirServerUrl, IBaseResource resource, IOUtils.Encoding encoding, FhirContext fhirContext) {
            this.fhirServerUrl = fhirServerUrl;
            this.resource = resource;
            this.encoding = encoding;
            this.fhirContext = fhirContext;
        }
    }
}
