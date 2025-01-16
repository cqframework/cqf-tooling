package org.opencds.cqf.tooling.utilities;

import ca.uhn.fhir.context.FhirContext;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;


/**
 * A utility class for collecting HTTP requests to a FHIR server and executing them collectively.
 */
public class HttpClientUtils {
    //60 second timeout
    protected static final RequestConfig requestConfig = RequestConfig.custom()
            .setSocketTimeout(60000)
            .setConnectTimeout(60000)
            .build();

    protected static final Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);
    private static final String FHIR_SERVER_URL = "FHIR Server URL";
    private static final String BUNDLE_RESOURCE = "Bundle Resource";
    private static final String ENCODING_TYPE = "Encoding Type";
    private static final String FHIR_CONTEXT = "FHIR Context";

    //This is not to maintain a thread count, but rather to maintain the maximum number of HTTP calls that can simultaneously be waiting for a response from the server.
    //This gives us some control over how many HTTP requests we're making so we don't crash the server.
    //possible TODO:Allow users to specify this value on their own with arg passed into operation so that more robust servers can process put list faster
    private static final int MAX_SIMULTANEOUS_REQUEST_COUNT = 10;

    //failedHttptCalls needs to maintain the details built in the FAILED message, as well as a copy of the inputs for a retry by the user on failed puts.
    private static Queue<Pair<String, HttpRequestComponent>> failedHttpCalls = new ConcurrentLinkedQueue<>();
    private static List<String> successfulHttpCalls = new CopyOnWriteArrayList<>();

    //Parent map uses resourceType as key so that resourceTypes can have their tasks called in a specific order:
    private static ConcurrentHashMap<HttpRequestResourceType, ConcurrentHashMap<IBaseResource, Callable<Void>>> mappedTasksByPriorityRank = new ConcurrentHashMap<>();

    private static List<IBaseResource> runningRequestTaskList = new CopyOnWriteArrayList<>();
    private static int processedRequestCounter = 0;


    //used for PUT order by resource type:
    public enum HttpRequestResourceType {
        BUNDLE("Bundles"),
        LIBRARY_DEPS("Library Dependencies"),
        VALUESETS("Value Sets"),
        TESTS("Tests"),
        GROUP("Patient Groups"),
        OTHER("Everything Else");

        private final String displayName;

        HttpRequestResourceType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    //Send HTTP requests in order: Bundle, Library-Deps, ValueSets, Tests, Group
    private static final List<HttpRequestResourceType> resourceTypeOrder = new ArrayList<>(Arrays.asList(
            HttpRequestResourceType.BUNDLE,
            HttpRequestResourceType.LIBRARY_DEPS,
            HttpRequestResourceType.VALUESETS,
            HttpRequestResourceType.TESTS,
            HttpRequestResourceType.GROUP,
            HttpRequestResourceType.OTHER));

    private HttpClientUtils() {
    }

    public static boolean hasPutTasksInQueue() {

        return !mappedTasksByPriorityRank.isEmpty();

    }

    /**
     * Initiates an HTTP request to a FHIR server with the specified parameters.
     *
     * @param fhirServerUrl The URL of the FHIR server to which the request will be sent.
     * @param resource      The FHIR resource to be sent.
     * @param encoding      The encoding type of the resource.
     * @param fhirContext   The FHIR context for the resource.
     * @param fileLocation  Optional fileLocation indicator for identifying resources by raw filename
     * @param priorityRank  A key in our mappedTasksByPriorityRank using HttpRequestResourceType. Allows specification of
     *                      the resource type for priority ordering at execute.
     * @throws IOException If an I/O error occurs during the request.
     */
    public static void sendToServer(String fhirServerUrl, IBaseResource resource, IOUtils.Encoding encoding, FhirContext fhirContext, String fileLocation, HttpRequestResourceType priorityRank) throws IOException {
        List<String> missingValues = new ArrayList<>();
        List<String> values = new ArrayList<>();
        validateAndAddValue(fhirServerUrl, FHIR_SERVER_URL, missingValues, values);
        validateAndAddValue(resource, BUNDLE_RESOURCE, missingValues, values, r -> r.getIdElement().getIdPart());
        validateAndAddValue(encoding, ENCODING_TYPE, missingValues, values);
        validateAndAddValue(fhirContext, FHIR_CONTEXT, missingValues, values);

        if (!missingValues.isEmpty()) {
            String missingValueString = String.join(", ", missingValues);
            logger.error("An invalid HTTP request was attempted with a null value for: " + missingValueString +
                    (!values.isEmpty() ? "\\nRemaining values are: " + String.join(", ", values) : ""));
            return;
        }

        createHttpRequestTask(fhirServerUrl, resource, encoding, fhirContext, fileLocation, priorityRank);
    }

    public static void sendToServer(String fhirServerUrl, IBaseResource resource, IOUtils.Encoding encoding, FhirContext fhirContext, String fileLocation) throws IOException {
        sendToServer(fhirServerUrl, resource, encoding, fhirContext, fileLocation, null);
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
     * Creates a task for handling an HTTP request to a FHIR server with the specified parameters.
     * <p>
     * This method is responsible for creating a task that prepares and executes an HTTP request to the provided FHIR server
     * with the given FHIR resource, encoding type, and FHIR context. It adds the task to the queue of tasks for later execution.
     * If any exceptions occur during task creation or configuration, an error message is logged.
     *
     * @param fhirServerUrl The URL of the FHIR server to which the request will be sent.
     * @param encoding      The encoding type of the resource.
     * @param fhirContext   The FHIR context for the resource.
     */
    private static void createHttpRequestTask(String fhirServerUrl, IBaseResource resource, IOUtils.Encoding encoding, FhirContext fhirContext, String fileLocation, HttpRequestResourceType priorityRank) {

        if (priorityRank == null) {
            //added to last in the list:
            priorityRank = HttpRequestResourceType.OTHER;
        }
        ConcurrentHashMap<IBaseResource, Callable<Void>> theseTasks = new ConcurrentHashMap<>();

        try {
            HttpRequestComponent httpRequestPojo = new HttpRequestComponent(fhirServerUrl, resource, encoding, fhirContext, fileLocation, priorityRank);
            HttpEntityEnclosingRequestBase httpRequest = configureHttpRequest(fhirServerUrl, resource, encoding, fhirContext);
            theseTasks.put(resource, createHttpRequestCallable(httpRequest, httpRequestPojo));
        } catch (Exception e) {
            logger.error("Error while submitting the HTTP request: " + e.getMessage(), e);
        }

        //check if a callable tasks map has started based on this resourceType:
        if (mappedTasksByPriorityRank.containsKey(priorityRank)) {
            mappedTasksByPriorityRank.get(priorityRank).putAll(theseTasks);
        } else {
            mappedTasksByPriorityRank.put(priorityRank, theseTasks);
        }
    }

    /**
     * Configures and prepares an HTTP request with the specified parameters.
     * <p>
     * This method creates and configures an HTTP request to be used for sending a FHIR resource to the given FHIR server.
     * It sets the request's headers, encodes the FHIR resource, and sets request timeouts. If an unsupported encoding type is
     * encountered, it throws a runtime exception.
     *
     * @param fhirServerUrl The URL of the FHIR server to which the request will be sent.
     * @param resource      The FHIR resource to be sent.
     * @param encoding      The encoding type of the resource.
     * @param fhirContext   The FHIR context for the resource.
     * @return An HTTP request configured for the FHIR server and resource.
     */
    private static HttpEntityEnclosingRequestBase configureHttpRequest(String fhirServerUrl, IBaseResource resource, IOUtils.Encoding encoding, FhirContext fhirContext) {

        //Transaction bundles get POST to /fhir but other resources get PUT to /fhir/resourceType/id ie fhir/Group/Group-123456
        String fhirUrl = fhirServerUrl.endsWith("/") ? fhirServerUrl : fhirServerUrl + "/";

        if (BundleUtils.resourceIsTransactionBundle(resource)) {
            HttpPost post = new HttpPost(fhirUrl);
            post.addHeader("content-type", "application/" + encoding.toString());
            String resourceString = IOUtils.encodeResourceAsString(resource, encoding, fhirContext);
            StringEntity input;
            try {
                input = new StringEntity(resourceString);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            post.setEntity(input);
            post.setConfig(requestConfig);

            return post;

        } else {

            String fhirServer = fhirUrl + "/" + resource.fhirType() + "/" + resource.getIdElement().getIdPart();

            HttpPut put = new HttpPut(fhirServer);
            put.addHeader("content-type", "application/" + encoding.toString());
            String resourceString = IOUtils.encodeResourceAsString(resource, encoding, fhirContext);
            StringEntity input;
            try {
                input = new StringEntity(resourceString);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            put.setEntity(input);
            put.setConfig(requestConfig);

            return put;


        }
    }


    /**
     * Creates a callable task for executing an HTTP PUT request and handling the response.
     * <p>
     * This method constructs a callable task that performs the following steps:
     * 1. Executes an HTTP PUT request using the provided parameters.
     * 2. Processes the HTTP response, checking the status code and reason phrase.
     * 3. Logs success or failure messages based on the response status.
     * 4. Handles exceptions related to the request and response.
     * 5. Updates the progress and status of the put task.
     *
     * @param request          The HTTP PUT request to be executed.
     * @param httpRequestComponent A data object containing additional information about the PUT request.
     * @return A callable task for executing the HTTP PUT request.
     */
    private static Callable<Void> createHttpRequestCallable(HttpEntityEnclosingRequestBase request, HttpRequestComponent httpRequestComponent) {
        return () -> {
            String resourceIdentifier = (httpRequestComponent.fileLocation != null ?
                    Paths.get(httpRequestComponent.fileLocation).getFileName().toString()
                    :
                    httpRequestComponent.resource.getIdElement().getIdPart());
            try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

                HttpResponse response = httpClient.execute(request);

                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                String diagnosticString = getDiagnosticString(EntityUtils.toString(response.getEntity()));

                if (statusCode >= 200 && statusCode < 300) {
                    successfulHttpCalls.add(buildSuccessMessage(httpRequestComponent.fhirServerUrl, resourceIdentifier));
                } else if (statusCode == 301) {
                    //redirected, find new location:
                    Header locationHeader = response.getFirstHeader("Location");
                    if (locationHeader != null) {
                        httpRequestComponent.redirectFhirServerUrl = locationHeader.getValue();
                        HttpEntityEnclosingRequestBase redirectedHttpRequest = configureHttpRequest(httpRequestComponent.redirectFhirServerUrl, httpRequestComponent.resource, httpRequestComponent.encoding, httpRequestComponent.fhirContext);
                        String redirectLocationIdentifier = httpRequestComponent.redirectFhirServerUrl
                                + "(redirected from " + httpRequestComponent.fhirServerUrl + ")";
                        //attempt to put at location specified in redirect response:
                        try (CloseableHttpClient redirectHttpClient = HttpClientBuilder.create().build()) {
                            HttpResponse redirectResponse = redirectHttpClient.execute(redirectedHttpRequest);
                            StatusLine redirectStatusLine = redirectResponse.getStatusLine();
                            int redirectStatusCode = redirectStatusLine.getStatusCode();
                            String redirectDiagnosticString = getDiagnosticString(EntityUtils.toString(redirectResponse.getEntity()));

                            //treat new response same as we would before:
                            if (redirectStatusCode >= 200 && redirectStatusCode < 300) {
                                successfulHttpCalls.add(buildSuccessMessage(redirectLocationIdentifier, resourceIdentifier));
                            } else {
                                failedHttpCalls.add(buildFailedHTTPMessage(httpRequestComponent, redirectStatusCode, redirectLocationIdentifier, resourceIdentifier, redirectDiagnosticString));
                            }
                        } catch (Exception e) {
                            failedHttpCalls.add(buildExceptionMessage(httpRequestComponent, e, resourceIdentifier, redirectLocationIdentifier));
                        }

                    } else {
                        //failed to extract a location from redirect message:
                        failedHttpCalls.add(Pair.of("[FAIL] Exception during " + resourceIdentifier + " HTTP request execution to "
                                + httpRequestComponent.fhirServerUrl + ": Redirect, but no new location specified", httpRequestComponent));
                    }
                } else {
                    failedHttpCalls.add(buildFailedHTTPMessage(httpRequestComponent, statusCode, httpRequestComponent.fhirServerUrl, resourceIdentifier, diagnosticString));
                }

            } catch (Exception e) {
                failedHttpCalls.add(buildExceptionMessage(httpRequestComponent, e, resourceIdentifier, httpRequestComponent.fhirServerUrl));
            }

            runningRequestTaskList.remove(httpRequestComponent.resource);
            reportProgress(httpRequestComponent.type);
            return null;
        };
    }

    private static Pair<String, HttpRequestComponent> buildExceptionMessage(HttpRequestComponent requestComponent, Exception e, String resourceIdentifier, String locationIdentifier) {
        return Pair.of("[FAIL] Exception during " + resourceIdentifier + " PUT request execution to " + locationIdentifier + ": " + e.getMessage(), requestComponent);
    }

    private static Pair<String, HttpRequestComponent> buildFailedHTTPMessage(HttpRequestComponent requestComponent, int statusCode, String locationIdentifier, String resourceIdentifier, String diagnosticString) {
        return Pair.of("[FAIL] Error " + statusCode + " from " + locationIdentifier + ": " + resourceIdentifier + ": " + diagnosticString, requestComponent);
    }

    private static String buildSuccessMessage(String locationIdentifier, String resourceIdentifier) {
        return "[SUCCESS] Resource successfully sent to " + locationIdentifier + ": " + resourceIdentifier;
    }

    /**
     * This method takes in a json string from the endpoint that might look like this:
     * {
     * "resourceType": "OperationOutcome",
     * "issue": [ {
     * "severity": "error",
     * "code": "processing",
     * "diagnostics": "HAPI-1094: Resource Condition/delivery-of-singleton-f83c not found, specified in path: Encounter.diagnosis.condition"
     * } ]
     * }
     * It extracts the diagnostics and returns a string appendable to the response
     */
    private static String getDiagnosticString(String jsonString) {
        try {
            // Get the "diagnostics" property
            return JsonParser.parseString(jsonString)
                    .getAsJsonObject()
                    .getAsJsonArray("issue")
                    .get(0)
                    .getAsJsonObject()
                    .getAsJsonPrimitive("diagnostics")
                    .getAsString();
        } catch (Exception e) {
            return "";
        }
    }

    private static String getSectionProgress(HttpRequestResourceType httpRequestResourceType) {
        //processedPutCounter holds the count we're at.
        //logically all preceding items up until this point are processed
        //add all sections until we get to this type, subtract total processed from other type count
        //get percentage from that using (processedPutCounter - theseTypesCount) / currentTypeProcessedCount

        int otherTypesProcessedCount = 0;

        for (int i = 0; i < resourceTypeOrder.size(); i++) {
            HttpRequestResourceType iterType = resourceTypeOrder.get(i);
            if (iterType == httpRequestResourceType) {
                break;
            }
            if (mappedTasksByPriorityRank.containsKey(iterType)) {
                otherTypesProcessedCount = otherTypesProcessedCount + (mappedTasksByPriorityRank.get(iterType)).size();
            }
        }

        int thisTypeProcessedCount = mappedTasksByPriorityRank.get(httpRequestResourceType).size();
        int currentCounter = processedRequestCounter - otherTypesProcessedCount;

        double percentage = (double) currentCounter / thisTypeProcessedCount * 100;

        return currentCounter + "/" + thisTypeProcessedCount + " (" + String.format("%.2f%%", percentage) + ")";
    }

    /**
     * Reports the progress of HTTP calls and the current thread pool size.
     * <p>
     * This method updates and prints the progress of HTTP calls by calculating the percentage of completed tasks
     * relative to the total number of tasks. It also displays the current size of the running thread pool. The progress
     * and pool size information is printed to the standard output.
     */
    private static void reportProgress(HttpRequestResourceType httpRequestResourceType) {
        int currentCounter = processedRequestCounter++;

        String fileGroup = "";
        if (httpRequestResourceType != null) {
            fileGroup = " | Sending: " + httpRequestResourceType.getDisplayName() + " " + getSectionProgress(httpRequestResourceType);
        }

        int taskCount = getTotalTaskCount();
        double percentage = (double) currentCounter / taskCount * 100;
        String percentOutput = String.format("%.2f%%", percentage);

        String progressStr = "\rProgress: " + percentOutput + " (" + currentCounter + "/" + taskCount + ")" +
                fileGroup +
                " | Response pool: " + runningRequestTaskList.size() ;


        String repeatedString = " ".repeat(progressStr.length() * 2);
        System.out.print(repeatedString);

        System.out.print(progressStr);
    }


    private static void reportProgress() {
        reportProgress(null);
    }

    private static int getTotalTaskCount() {
        int totalCount = 0;
        for (ConcurrentHashMap<IBaseResource, Callable<Void>> map : mappedTasksByPriorityRank.values()) {
            totalCount += map.size();
        }
        return totalCount;
    }

    private static String getPresentTypesAndCounts() {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < resourceTypeOrder.size(); i++) {
            //execute the tasks in order specified
            if (mappedTasksByPriorityRank.containsKey(resourceTypeOrder.get(i))) {
                output.append("\n\r - ")
                        .append(resourceTypeOrder.get(i).displayName)
                        .append(": ")
                        .append(mappedTasksByPriorityRank.get(resourceTypeOrder.get(i)).size());
            }
        }

        return output.toString();
    }

    /**
     * Puts a collection of tasks to execute HTTP requests to a FHIR server.
     * <p>
     * This method orchestrates the execution of a collection of HTTP requests, each represented as a task.
     * The method performs the following steps:
     * 1. Creates a thread pool using a fixed number of threads (usually 1).
     * 2. Initiates the HTTP tasks for FHIR resources and monitors their progress.
     * 3. Collects and logs success or failure messages for each task.
     * 4. Sorts and reports the results of the tasks, both successful and failed.
     * 5. Offers the option to retry failed tasks, if desired by the user.
     * 6. Cleans up resources and shuts down the thread pool when finished.
     * <p>
     * This method serves as the entry point for sending tasks and provides progress monitoring and result reporting.
     */
    public static void putTaskCollection() {
        ExecutorService executorService = Executors.newFixedThreadPool(1);

        try {
            logger.info("\n\r" + getTotalTaskCount() +
                    " HTTP calls to be made: " +
                    getPresentTypesAndCounts() +
                    "\n\r Executing, please wait..." + "\n\r");

            runHttpRequestCalls(executorService);


            logger.info("\n\r" + "Processing results..." + "\n\r");
            Collections.sort(successfulHttpCalls);

            StringBuilder message = new StringBuilder();
            for (String successRequest : successfulHttpCalls) {
                message.append("\n").append(successRequest);
            }
            message.append("\r\n").append(successfulHttpCalls.size()).append(" resources successfully sent.");
            logger.info(message.toString());
            successfulHttpCalls = new ArrayList<>();

            if (!failedHttpCalls.isEmpty()) {
                logger.info("\n\r" + failedHttpCalls.size() + " tasks failed to send. Retry these failed requests? (Y/N)");
                Scanner scanner = new Scanner(System.in);
                String userInput = scanner.nextLine().trim().toLowerCase();

                if (userInput.equalsIgnoreCase("y")) {
                    List<Pair<String, HttpRequestComponent>> failedPutCallList = new ArrayList<>(failedHttpCalls);
                    cleanUp(); //clear the queue, reset the counter, start fresh

                    for (Pair<String, HttpRequestComponent> pair : failedPutCallList) {
                        HttpRequestComponent httpRequestComponent = pair.getRight();
                        try {
                            sendToServer(httpRequestComponent.fhirServerUrl,
                                    httpRequestComponent.resource,
                                    httpRequestComponent.encoding,
                                    httpRequestComponent.fhirContext,
                                    httpRequestComponent.fileLocation,
                                    httpRequestComponent.type);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    runHttpRequestCalls(executorService);

                    if (failedHttpCalls.isEmpty()) {
                        logger.info("\r\nRetry successful, all tasks successfully sent.");
                    }
                }
            }

            if (!successfulHttpCalls.isEmpty()) {
                message = new StringBuilder();
                for (String successPut : successfulHttpCalls) {
                    message.append("\n").append(successPut);
                }
                message.append("\r\n").append(successfulHttpCalls.size()).append(" resources successfully sent.");
                logger.info(message.toString());
                successfulHttpCalls = new ArrayList<>();
            }

            if (!failedHttpCalls.isEmpty()) {
                List<String> failedMessages = new ArrayList<>();
                for (Pair<String, HttpRequestComponent> pair : failedHttpCalls) {
                    failedMessages.add(pair.getLeft());
                }
                Collections.sort(failedMessages);
                message = new StringBuilder();


                for (String failedRequest : failedMessages) {
                    message.append("\n").append(failedRequest);
                }


                message.append("\r\n").append(failedMessages.size()).append(" resources failed to send.");
                logger.info(message.toString());

                writeFailedHttpRequestAttemptsToLog(failedMessages);
            }

        } finally {
            cleanUp();
            executorService.shutdown();
        }
    }

    /**
     * Executes the tasks for each priority ranking, sorted:
     *
     * @param executorService
     */
    private static void runHttpRequestCalls(ExecutorService executorService) {
        //execute the tasks for each priority ranking, sorted:
        for (int i = 0; i < resourceTypeOrder.size(); i++) {
            //execute the tasks in order specified
            if (mappedTasksByPriorityRank.containsKey(resourceTypeOrder.get(i))) {
                executeTasks(executorService, mappedTasksByPriorityRank.get(resourceTypeOrder.get(i)));
            }
        }

        reportProgress();
    }

    /**
     * Gives the user a log file containing failed attempts during requestTaskCollection()
     *
     * @param failedMessages
     */
    private static void writeFailedHttpRequestAttemptsToLog(List<String> failedMessages) {
        if (!failedMessages.isEmpty()) {
            //generate a unique filename based on simple timestamp:
            String httpFailLogFilename = "http_request_fail_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".log";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(httpFailLogFilename))) {
                for (String str : failedMessages) {
                    writer.write(str + "\n");
                }
                logger.info("\r\nRecorded failed HTTP tasks to log file: " + new File(httpFailLogFilename).getAbsolutePath() + "\r\n");
            } catch (IOException e) {
                logger.info("\r\nRecording of failed HTTP tasks to log failed with exception: " + e.getMessage() + "\r\n");
            }
        }
    }


    private static void executeTasks(ExecutorService executorService, Map<IBaseResource, Callable<Void>> executableTasksMap) {


        List<Future<Void>> futures = new ArrayList<>();
        List<IBaseResource> resources = new ArrayList<>(executableTasksMap.keySet());
        for (int i = 0; i < resources.size(); i++) {
            IBaseResource thisResource = resources.get(i);
            if (runningRequestTaskList.size() < MAX_SIMULTANEOUS_REQUEST_COUNT) {
                runningRequestTaskList.add(thisResource);
                futures.add(executorService.submit(executableTasksMap.get(thisResource)));
            } else {
                threadSleep(10);
                i--;
            }
        }

        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                logger.error("HTTPClientUtils future.get()", e);
            }
        }
    }

    /**
     * Pauses the current thread's execution for a specified duration.
     * This method causes the current thread to sleep for the given duration, allowing a pause in execution. If an
     * interruption occurs during the sleep, it is logged as an exception.
     *
     * @param i The duration, in milliseconds, for which the thread should sleep.
     */
    private static void threadSleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            logger.error("httpRequestTaskCollection", new RuntimeException(e));
        }
    }

    /**
     * Cleans up and resets internal data structures after processing HTTP PUT tasks.
     * <p>
     * This method is responsible for resetting various data structures used during the processing of HTTP PUT tasks. It performs the following actions:
     * 1. Clears the queue of failed PUT calls.
     * 2. Clears the list of successful PUT call results.
     * 3. Resets the map of tasks to be executed.
     * 4. Resets the counter that tracks the number of processed PUT calls.
     * 5. Clears the list of resources currently being puted.
     * <p>
     * This method ensures a clean state and prepares the system for potential subsequent PUT calls or retries.
     */
    private static void cleanUp() {
        failedHttpCalls = new ConcurrentLinkedQueue<>();
        successfulHttpCalls = new CopyOnWriteArrayList<>();
        mappedTasksByPriorityRank = new ConcurrentHashMap<>();
        processedRequestCounter = 0;
        runningRequestTaskList = new CopyOnWriteArrayList<>();
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
     * A data class representing information needed for HTTP requests.
     * <p>
     * The httpRequestComponent class encapsulates the essential information required for making an HTTP request to a FHIR server.
     * It includes the FHIR server URL, the FHIR resource to be sent, the encoding type, and the FHIR context.
     */
    private static class HttpRequestComponent {
        private final String fhirServerUrl;
        private String redirectFhirServerUrl;
        private final IBaseResource resource;
        private final IOUtils.Encoding encoding;
        private final FhirContext fhirContext;
        private final String fileLocation;

        private final HttpRequestResourceType type;

        public HttpRequestComponent(String fhirServerUrl, IBaseResource resource, IOUtils.Encoding encoding, FhirContext fhirContext, String fileLocation, HttpRequestResourceType type) {
            this.fhirServerUrl = fhirServerUrl;
            this.resource = resource;
            this.encoding = encoding;
            this.fhirContext = fhirContext;
            this.fileLocation = fileLocation;
            this.type = type;
        }
    }

    public static ResponseHandler<String> getDefaultResponseHandler() {
        return response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        };
    }
}