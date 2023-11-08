package org.opencds.cqf.tooling.utilities;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.lang3.tuple.Pair;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * A utility class for collecting HTTP requests to a FHIR server and executing them collectively.
 */
public class HttpClientUtils {
    private static final String FHIR_SERVER_URL = "FHIR Server URL";
    private static final String BUNDLE_RESOURCE = "Bundle Resource";
    private static final String ENCODING_TYPE = "Encoding Type";
    private static final String FHIR_CONTEXT = "FHIR Context";
    private static final int MAX_SIMULTANEOUS_POST_COUNT = 10;

    //failedPostCalls needs to maintain the details built in the FAILED message, as well as a copy of the inputs for a retry by the user on failed posts.
    private static Queue<Pair<String, PostComponent>> failedPostCalls = new ConcurrentLinkedQueue<>();
    private static List<String> successfulPostCalls = new CopyOnWriteArrayList<>();
    private static Map<IBaseResource, Callable<Void>> tasks = new ConcurrentHashMap<>();
    private static List<IBaseResource> runningPostTaskList = new CopyOnWriteArrayList<>();
    private static final AtomicInteger counter = new AtomicInteger(0);

    private HttpClientUtils() {}

    public static boolean hasPostTasksInQueue() {
        return !tasks.isEmpty();
    }

    /**
     * Initiates an HTTP POST request to a FHIR server with the specified parameters.
     *
     * @param fhirServerUrl The URL of the FHIR server to which the POST request will be sent.
     * @param resource      The FHIR resource to be posted.
     * @param encoding      The encoding type of the resource.
     * @param fhirContext   The FHIR context for the resource.
     * @throws IOException If an I/O error occurs during the request.
     */
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


    /**
     * Validates a value and adds its representation to the provided lists using a custom value-to-string function.
     * <p>
     * This method checks if the given value is null. If the value is not null, it is converted to a string using the provided
     * value-to-string function, and the value along with its label is added to the 'values' list. If the value is null, the
     * label is added to the 'missingValues' list to indicate a missing or invalid value.
     *
     * @param value         The value to be validated and added.
     * @param label         A label describing the value (e.g., parameter name).
     * @param missingValues A list to collect missing or invalid values.
     * @param values        A list to collect valid values along with their labels.
     * @param valueToString A custom function to convert the value to a string.
     * @param <T>           The type of the value to be validated.
     */
    private static <T> void validateAndAddValue(T value, String label, List<String> missingValues, List<String> values, Function<T, String> valueToString) {
        if (value == null) {
            missingValues.add(label);
        } else {
            values.add(label + ": " + valueToString.apply(value));
        }
    }
    private static <T> void validateAndAddValue(T value, String label, List<String> missingValues, List<String> values) {
        validateAndAddValue(value, label, missingValues, values, Object::toString);
    }

    /**
     * Creates a task for handling an HTTP POST request to a FHIR server with the specified parameters.
     * <p>
     * This method is responsible for creating a task that prepares and executes an HTTP POST request to the provided FHIR server
     * with the given FHIR resource, encoding type, and FHIR context. It adds the task to the queue of tasks for later execution.
     * If any exceptions occur during task creation or configuration, an error message is logged.
     *
     * @param fhirServerUrl The URL of the FHIR server to which the POST request will be sent.
     * @param resource      The FHIR resource to be posted.
     * @param encoding      The encoding type of the resource.
     * @param fhirContext   The FHIR context for the resource.
     */
    private static void createPostTask(String fhirServerUrl, IBaseResource resource, IOUtils.Encoding encoding, FhirContext fhirContext) {
        try {
            final int currentTaskIndex = tasks.size() + 1;
            PostComponent postPojo = new PostComponent(fhirServerUrl, resource, encoding, fhirContext);
            HttpPost post = configureHttpPost(fhirServerUrl, resource, encoding, fhirContext);

            Callable<Void> task = createPostCallable(post, postPojo, currentTaskIndex);
            tasks.put(resource, task);
        } catch (Exception e) {
            LogUtils.putException("Error while submitting the POST request: " + e.getMessage(), e);
        }
    }

    /**
     * Configures and prepares an HTTP POST request with the specified parameters.
     * <p>
     * This method creates and configures an HTTP POST request to be used for posting a FHIR resource to the given FHIR server.
     * It sets the request's headers, encodes the FHIR resource, and sets request timeouts. If an unsupported encoding type is
     * encountered, it throws a runtime exception.
     *
     * @param fhirServerUrl The URL of the FHIR server to which the POST request will be sent.
     * @param resource      The FHIR resource to be posted.
     * @param encoding      The encoding type of the resource.
     * @param fhirContext   The FHIR context for the resource.
     * @return An HTTP POST request configured for the FHIR server and resource.
     */
    private static HttpPost configureHttpPost(String fhirServerUrl, IBaseResource resource, IOUtils.Encoding encoding, FhirContext fhirContext) {
        HttpPost post = new HttpPost(fhirServerUrl);
        post.addHeader("content-type", "application/" + encoding.toString());

        String resourceString = IOUtils.encodeResourceAsString(resource, encoding, fhirContext);
        StringEntity input;
        try {
            input = new StringEntity(resourceString);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        post.setEntity(input);

        //60 second timeout
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(60000)
                .setConnectTimeout(60000)
                .build();
        post.setConfig(requestConfig);

        return post;
    }

    /**
     * Creates a callable task for executing an HTTP POST request and handling the response.
     * <p>
     * This method constructs a callable task that performs the following steps:
     * 1. Executes an HTTP POST request using the provided parameters.
     * 2. Processes the HTTP response, checking the status code and reason phrase.
     * 3. Logs success or failure messages based on the response status.
     * 4. Handles exceptions related to the request and response.
     * 5. Updates the progress and status of the post task.
     *
     * @param post          The HTTP POST request to be executed.
     * @param postPojo      A data object containing additional information about the POST request.
     * @param currentTaskIndex The current index of the POST task among all tasks.
     * @return A callable task for executing the HTTP POST request.
     */
    private static Callable<Void> createPostCallable(HttpPost post, PostComponent postPojo, int currentTaskIndex) {
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

    /**
     * Reports the progress of HTTP POST calls and the current thread pool size.
     * <p>
     * This method updates and prints the progress of HTTP POST calls by calculating the percentage of completed tasks
     * relative to the total number of tasks. It also displays the current size of the running thread pool. The progress
     * and pool size information is printed to the standard output.
     */
    private static void reportProgress() {
        int currentCounter = counter.incrementAndGet();
        double percentage = (double) currentCounter / tasks.size() * 100;
        System.out.print("\rPOST calls: " + String.format("%.2f%%", percentage) + " processed. Thread pool size: " + runningPostTaskList.size() + " ");
    }

    /**
     * Posts a collection of tasks to execute HTTP POST requests to a FHIR server.
     * <p>
     * This method orchestrates the execution of a collection of HTTP POST requests, each represented as a task.
     * The method performs the following steps:
     * 1. Creates a thread pool using a fixed number of threads (usually 1).
     * 2. Initiates the HTTP POST tasks for FHIR resources and monitors their progress.
     * 3. Collects and logs success or failure messages for each task.
     * 4. Sorts and reports the results of the post tasks, both successful and failed.
     * 5. Offers the option to retry failed tasks, if desired by the user.
     * 6. Cleans up resources and shuts down the thread pool when finished.
     * <p>
     * This method serves as the entry point for posting tasks and provides progress monitoring and result reporting.
     */
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
                    threadSleep(10);
                    i--;
                }
            }

            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    LogUtils.putException("HTTPClientUtils future.get()", e);
                }
            }

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
                    List<Pair<String, PostComponent>> failedPostCallList = new ArrayList<>(failedPostCalls);
                    cleanUp(); //clear the queue, reset the counter, start fresh

                    for (Pair<String, PostComponent> pair : failedPostCallList) {
                        PostComponent postPojo = pair.getRight();
                        try {
                            post(postPojo.fhirServerUrl, postPojo.resource, postPojo.encoding, postPojo.fhirContext);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    for (Callable<Void> task : tasks.values()) {
                        try {
                            task.call();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        threadSleep(50);
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
                for (Pair<String, PostComponent> pair : failedPostCalls) {
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

    /**
     * Pauses the current thread's execution for a specified duration.
     *
     * This method causes the current thread to sleep for the given duration, allowing a pause in execution. If an
     * interruption occurs during the sleep, it is logged as an exception.
     *
     * @param i The duration, in milliseconds, for which the thread should sleep.
     */
    private static void threadSleep(int i){
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            LogUtils.putException("postTaskCollection", new RuntimeException(e));
        }
    }
    /**
     * Cleans up and resets internal data structures after processing HTTP POST tasks.
     * <p>
     * This method is responsible for resetting various data structures used during the processing of HTTP POST tasks. It performs the following actions:
     * 1. Clears the queue of failed POST calls.
     * 2. Clears the list of successful POST call results.
     * 3. Resets the map of tasks to be executed.
     * 4. Resets the counter that tracks the number of processed POST calls.
     * 5. Clears the list of resources currently being posted.
     * <p>
     * This method ensures a clean state and prepares the system for potential subsequent POST calls or retries.
     */
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

    /**
     * A data class representing information needed for HTTP POST requests.
     * <p>
     * The PostComponent class encapsulates the essential information required for making an HTTP POST request to a FHIR server.
     * It includes the FHIR server URL, the FHIR resource to be posted, the encoding type, and the FHIR context.
     */
    private static class PostComponent {
        String fhirServerUrl;
        IBaseResource resource;
        IOUtils.Encoding encoding;
        FhirContext fhirContext;

        public PostComponent(String fhirServerUrl, IBaseResource resource, IOUtils.Encoding encoding, FhirContext fhirContext) {
            this.fhirServerUrl = fhirServerUrl;
            this.resource = resource;
            this.encoding = encoding;
            this.fhirContext = fhirContext;
        }
    }

    /**
     * Sorts a list by the initial numbers so that we see the [iteration] of [total] message
     * in ascending order
     **/
    private static final Comparator<String> postResultMessageComparator = new Comparator<>() {
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
}