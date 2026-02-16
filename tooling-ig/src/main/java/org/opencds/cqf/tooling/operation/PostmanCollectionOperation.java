package org.opencds.cqf.tooling.operation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.opencds.cqf.tooling.Operation;
import org.opencds.cqf.tooling.utilities.CanonicalUtils;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.R4FHIRUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostmanCollectionOperation extends Operation {
    private final static Logger logger = LoggerFactory.getLogger(PostmanCollectionOperation.class);
    private static final String POSTMAN_COLLECTION_SCHEMA = "https://schema.getpostman.com/json/collection/v2.1.0/collection.json";
    private String pathToBundlesDir;
    private FhirContext context;
    String version;
    private static String protocol;
    private static String urlBase;
    private static String urlPath;
    private String collectionName;

    @Override
    public void execute(String[] args) {
        for (String arg : args) {
            if (arg.equals("-PostmanCollection")) continue;
            String[] flagAndValue = arg.split("=");
            if (flagAndValue.length < 2) {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
            String flag = flagAndValue[0];
            String value = flagAndValue[1].replace("\"", ""); // Strip quotes

            switch (flag.replace("-", "").toLowerCase()) {
                case "outputpath":
                case "op":
                    setOutputPath(value);
                    break;
                case "pathtobundledir":
                case "ptbd":
                    pathToBundlesDir = value;
                    break;
                case "version": case "v":
                    version = value;
                    break;
                case "protocol":
                    protocol = value;
                    break;
                case "host":
                    urlBase = value;
                    break;
                case "path":
                    urlPath = value;
                    break;
                case "name":
                    collectionName = value;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        }

        File bundleDirectory = validateDirectory(pathToBundlesDir);
        this.context = setContext(version);
        validateHostAndPath();
        validateCollectionName();
        validateProtocol();
        generateUrlHostTokens();
        generateUrlPathTokens();

        try {
            // Expect the path directory will contain directories each of that will contain bundle json
            File[] bundleDirectories = getListOfActionableDirectories(bundleDirectory);

            PostmanCollection postmanCollection = createPostmanCollection();
            BaseItem versionItem = populateVersionItem(postmanCollection, version);

            for (File bundleDir : bundleDirectories) {

                File[] bundleFiles = bundleDir.listFiles(pathname -> pathname.isFile() && pathname.getName().endsWith(".json"));
                if (bundleFiles == null) {
                    continue;
                }
                for (File file : bundleFiles) {
                    IBaseResource resource = parseBundle(file);
                    if (resource != null) {
                        String fileContent = IOUtils.getFileContent(file);
                        populatePostmanCollection(resource, versionItem, fileContent, version);
                    }
                }

            }
            writePostmanCollection(postmanCollection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void validateHostAndPath(){
        if(StringUtils.isEmpty(urlBase)) {
            urlBase = "{server-base}";
        }
        if(StringUtils.isEmpty(urlPath)) {
            urlPath = "{path}";
        }
    }

    private void validateCollectionName() {
        if (StringUtils.isEmpty(collectionName)) {
            collectionName = createDefaultName();
        }
    }

    private String createDefaultName() {
        return String.format("Postman-Collection-%s",
                new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(new Date()));
    }

    private void validateProtocol() {
        if(StringUtils.isEmpty(protocol)) {
            protocol = "http";
        }
    }

    private static List<String> hostTokens;
    private static StringBuffer hostNames;

    private static List<String> generateUrlHostTokens() {
        try {
            if (hostTokens == null) {
                hostTokens = new ArrayList<>();
                hostNames = new StringBuffer();
                if (!StringUtils.isEmpty(urlBase)) {
                    hostTokens.addAll(Arrays.asList(urlBase.split("\\.")));
                }
                if (StringUtils.isNotBlank(protocol)) {
                    hostNames.append(protocol);
                    hostNames.append("://");
                }

                for (String token : hostTokens) {
                    hostNames.append(token);
                    hostNames.append(".");
                }
                if (!StringUtils.isEmpty(urlBase)) {
                    hostNames.delete(hostNames.length() - 1, hostNames.length());
                    hostNames.append("/");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hostTokens;
    }

    private static List<String> pathTokens;
    private static StringBuffer pathNames;

    private static List<String> generateUrlPathTokens() {
        try {
            if (pathTokens == null) {
                pathTokens = new ArrayList<>();
                pathNames = new StringBuffer();
                pathNames.append(hostNames);
                if (!StringUtils.isEmpty(urlPath)) {
                    pathTokens.addAll(Arrays.asList(urlPath.split("/")));
                }

                for (String token : pathTokens) {
                    pathNames.append(token);
                    pathNames.append("/");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pathTokens;
    }

    private List<String> generatePathTokensForMeasure(String measureId) {
        List<String> list = new ArrayList<>(pathTokens);
        list.add("Measure");
        if (StringUtils.isNotBlank(measureId)) {
            list.add(measureId);
        }
        list.add("$evaluate-measure");
        return list;
    }

    private String  generateMeasureUrl(String measureId, String patient, String start, String end) {
        StringBuilder sb = new StringBuilder();
        sb.append(pathNames);
        if(StringUtils.isNotBlank(measureId)) {
            sb.append("Measure/");
            sb.append(measureId);
            sb.append("/$evaluate-measure?");
        }
        if (StringUtils.isNotBlank(patient)) {
            sb.append("patient=");
            sb.append(patient);
        }
        if (StringUtils.isNotBlank(start)) {
            sb.append("&periodStart=");
            sb.append(start);
        }
        if (StringUtils.isNotBlank(end)) {
            sb.append("&periodEnd=");
            sb.append(end);
        }

        return sb.toString();
    }

    private String  generatePostUrl() {
        return pathNames.toString();
    }

    private File validateDirectory(String pathToDir) {
        if (pathToDir == null) {
            throw new IllegalArgumentException("The path to the bundles directory is required");
        }

        File bundleDirectory = new File(pathToDir);
        if (!bundleDirectory.isDirectory()) {
            throw new RuntimeException("The specified path to bundles is not a directory");
        }
        return bundleDirectory;
    }

    private File[] getListOfActionableDirectories(File bundleDirectory) {
        File[] bundleDirectories = bundleDirectory.listFiles(file -> file.isDirectory());
        if (bundleDirectories == null) {
            throw new RuntimeException("The specified path to bundle files is empty");
        }
        return bundleDirectories;
    }

    private FhirContext setContext(String version) {
        FhirContext context;
        if (StringUtils.isEmpty(version)) {
            context = FhirContext.forR4Cached();
        } else {
            switch (version.toLowerCase()) {
                case "dstu3":
                    context = FhirContext.forDstu3Cached();
                    break;
                case "r4":
                    context = FhirContext.forR4Cached();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown fhir version: " + version);
            }
        }
        return context;
    }

    private String getVersionLebel(String version) {
        String versionLebel;
        switch (version.toUpperCase()) {
            case "DSTU3":
                versionLebel = "FHIR3";
                break;
            case "R4":
                versionLebel = "FHIR4";
                break;
            default:
                versionLebel = "";
        }
        return versionLebel;
    }

    private PostmanCollection createPostmanCollection() {
        PostmanCollection postmanCollection = new PostmanCollection();
        PostmanCollectionInfo info = new PostmanCollectionInfo();
        info.set_postman_id(UUID.randomUUID().toString());
        info.setName(collectionName);
        info.setSchema(POSTMAN_COLLECTION_SCHEMA);
        postmanCollection.setInfo(info);

        postmanCollection.setItem(generateEmptyBaseItemList());

        return postmanCollection;
    }

    private BaseItem populateVersionItem(PostmanCollection postmanCollection, String version) {

        if(postmanCollection.getItem() == null) {
            postmanCollection.setItem(generateEmptyBaseItemList());
        }
        BaseItem itemBase = new BaseItem();
        itemBase.setName(getVersionLebel(version));
        itemBase.setItem(generateEmptyBaseItemList());

        postmanCollection.getItem().add(itemBase);
        return itemBase;
    }

    private List<BaseItem> generateEmptyBaseItemList() {
        return new ArrayList<>();
    }

    private void populatePostmanCollection(IBaseResource resourceBundle, BaseItem versionItem, String content, String version) {

        if(versionItem != null && versionItem.getItem() == null) {
            versionItem.setItem(generateEmptyBaseItemList());
        }

        BaseItem itemSubFolder = new BaseItem();
        versionItem.getItem().add(itemSubFolder);


        if (version.equals("r4")) {

            Bundle bundle = (Bundle) resourceBundle;
            generateSubfolderItem(itemSubFolder, bundle.getId(), content);
            List<Map<String, String>> requestMapList;

            for (Bundle.BundleEntryComponent component : bundle.getEntry()) {
                Resource resource = component.getResource();

                if (resource.getResourceType().compareTo(ResourceType.MeasureReport) == 0) {
                    MeasureReport measureReport = (MeasureReport) resource;

                    String measureId = getMeasureId(measureReport);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String patient = R4FHIRUtils.parseId(measureReport.getSubject().getReference());
                    String start = sdf.format(measureReport.getPeriod().getStart());
                    String end = sdf.format(measureReport.getPeriod().getEnd());
                    String measureUrl = generateMeasureUrl(measureId, patient, start, end);
                    String requestName = "measure";

                    if (StringUtils.isNotBlank(patient)) {
                        requestName = patient;
                    }
                    populateRequestItems(itemSubFolder, measureId, requestName, measureUrl, patient, start, end);
                }
            }
        } else if (version.equals("dstu3")) {
            org.hl7.fhir.dstu3.model.Bundle bundle = (org.hl7.fhir.dstu3.model.Bundle) resourceBundle;
            generateSubfolderItem(itemSubFolder, bundle.getId(), content);


            for (org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent component : bundle.getEntry()) {
                org.hl7.fhir.dstu3.model.Resource resource = component.getResource();

                if (resource.getResourceType().compareTo(org.hl7.fhir.dstu3.model.ResourceType.MeasureReport) == 0) {
                    org.hl7.fhir.dstu3.model.MeasureReport measureReport = (org.hl7.fhir.dstu3.model.MeasureReport) resource;

                    String measureId = R4FHIRUtils.parseId(measureReport.getMeasure().getReference());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String patient = R4FHIRUtils.parseId(measureReport.getPatient().getReference());
                    String start = sdf.format(measureReport.getPeriod().getStart());
                    String end = sdf.format(measureReport.getPeriod().getEnd());
                    String measureUrl = generateMeasureUrl(measureId, patient, start, end);

                    String requestName = "measure";
                    if (StringUtils.isNotBlank(patient)) {
                        requestName = patient;
                    }
                    populateRequestItems(itemSubFolder, measureId, requestName, measureUrl, patient, start, end);
                }
            }
        }
    }

    private String getMeasureId(MeasureReport measureReport) {
        String id = R4FHIRUtils.parseId(measureReport.getMeasure());
        if (StringUtils.isEmpty(id)) {
            id = CanonicalUtils.getId(measureReport.getMeasure());
        }
        return id;
    }

    private void populateRequestItems( BaseItem itemSubFolder,String measureId,String requestName, String measureUrl, String patient, String start, String end) {

        List<Map<String,String>> requestMapList;

        requestMapList = new ArrayList<>();
        addItemToQueryList(requestMapList,"patient", patient);
        addItemToQueryList(requestMapList,"periodStart", start);
        addItemToQueryList(requestMapList,"periodEnd",end);

        populateItemWithRequestItem("GET",requestName, "", measureId, measureUrl, itemSubFolder, requestMapList);

    }

    private void generateSubfolderItem(BaseItem itemSubFolder, String id, String content) {
        itemSubFolder.setName(id.split("-")[0]);  //EXM104-FHIR4-8.1.000-bundle  -> EXM104
        itemSubFolder.setItem(generateEmptyBaseItemList());
        populateItemWithRequestItem("POST","Post Bundle", content, "", generatePostUrl(), itemSubFolder, null);
    }

    private void populateItemWithRequestItem(String method, String name, String body,  String measureId, String measureUrl, BaseItem itemSubFolder, List<Map<String, String>> requestMapList) {
        RequestItem requestItem = new RequestItem();
        if (itemSubFolder.getItem() != null) {
            itemSubFolder.getItem().add(requestItem);
        }
        populateRequestItem(requestItem, name, method, body, measureId, measureUrl, requestMapList);
    }

    private void populateRequestItem(RequestItem requestItem, String name, String method, String body, String measureId, String measureUrl, List<Map<String, String>> requestMapList) {

        if(StringUtils.isNotBlank(name)) {
            requestItem.setName(name);
        }
        ProtocolProfileBehavior behavior = new ProtocolProfileBehavior();
        behavior.setDisableBodyPruning(true);
        if(StringUtils.isEmpty(body) ) {
            requestItem.setProtocolProfileBehavior(behavior);
        }
        RequestInfo requestInfo = new RequestInfo();
        requestItem.setRequest(requestInfo);

        if(StringUtils.isEmpty(body) ) {
            requestItem.setResponse(new ArrayList<>());
        }

        populateRequestInfo(requestInfo, method, body, measureId, measureUrl, requestMapList);
    }

    private void populateRequestInfo(RequestInfo requestInfo, String method, String body, String measureId, String measureUrl, List<Map<String, String>> requestMapList) {

        if (StringUtils.isNotBlank(method)) {
            requestInfo.setMethod(method);
        }
        if (StringUtils.isNotBlank(body)) {
            requestInfo.setBody(populateRequestBody(body));
        }
        requestInfo.setHeader(generateRequestHeaderMap());

        RequestUrl requestUrl = new RequestUrl();
        requestInfo.setUrl(requestUrl);

        populateRequestUrl(requestUrl, measureUrl, measureId, requestMapList);
    }


    private void populateRequestUrl(RequestUrl requestUrl, String measureUrl, String measureId, List<Map<String, String>> requestMapList) {
        if (StringUtils.isNotBlank(measureUrl)) {
            requestUrl.setRaw(measureUrl);
        }
        requestUrl.setProtocol(protocol);
        if (requestMapList != null) {
            requestUrl.setQuery(requestMapList);
            if (StringUtils.isNotBlank(measureId)) {
                requestUrl.setPath(generatePathTokensForMeasure(measureId));
            }
        } else {
            requestUrl.setPath(generateUrlPathTokens());
        }
        requestUrl.setHost(generateUrlHostTokens());
    }


    private RequestBody populateRequestBody(String body) {
        RequestBody requestBody = new RequestBody();
        requestBody.setMode("raw");
        if(body != null) {
            requestBody.setRaw(body);
        }
        return requestBody;
    }

    private static List<Map<String, String>> requestHeader;
    private static List<Map<String, String>> generateRequestHeaderMap() {

        if(requestHeader == null || requestHeader.isEmpty() ){
            requestHeader = new ArrayList<>();
            Map<String, String> map = new HashMap<>();
            map.put("key", "Content-Type");
            map.put("name", "Content-Type");
            map.put("type", "text");
            map.put("value", "application/json");
            requestHeader.add(map);
        }
        return  requestHeader;
    }

    private IBaseResource parseBundle(File resource) {
        IBaseResource theResource = null;
        try {
            theResource = context.newJsonParser().parseResource(new FileReader(resource));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
            String message = String.format("'%s' will not be included in the bundle because the following error occurred: '%s'", resource.getName(), e.getMessage());
            logger.error(message, e);
        }
        return theResource;
    }

    private void writePostmanCollection(PostmanCollection postmanCollection) {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);


        try {
            String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(postmanCollection);
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(String.format("%s/%s.json", getOutputPath(), createDefaultName())));
            writer.write(jsonString);
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void addItemToQueryList(List<Map<String, String>> list, String key, String value) {
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
            map.put("key", key);
            map.put("value", value);
        }
        list.add(map);
    }


    private static List<Map<String, String>> listHeader;

    private static List<Map<String, String>> populateRequestHeader() {
        if (listHeader == null) {
            listHeader = new ArrayList<>();
            Map<String, String> mapHeader = new HashMap<>();
            mapHeader.put("key", "Content-Type");
            mapHeader.put("name", "Content-Type");
            mapHeader.put("type", "text");
            mapHeader.put("value", "application/json");
            listHeader.add(mapHeader);
        }

        return listHeader;
    }


    // try to model postman collection

    class PostmanCollection {
        private PostmanCollectionInfo info;
        private List<BaseItem> item ;

        public PostmanCollectionInfo getInfo() {
            return info;
        }

        public void setInfo(PostmanCollectionInfo info) {
            this.info = info;
        }

        public List<BaseItem> getItem() {
            return item;
        }

        public void setItem(List<BaseItem> item) {
            this.item = item;
        }
    }

    class BaseItem {
        private String name;
        private List<BaseItem> item ;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<BaseItem> getItem() {
            return item;
        }

        public void setItem(List<BaseItem> item) {
            this.item = item;
        }
    }

    class PostmanCollectionInfo {
        private String _postman_id;
        private String name;
        private String schema;

        public String get_postman_id() {
            return _postman_id;
        }

        public void set_postman_id(String _postman_id) {
            this._postman_id = _postman_id;
        }


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSchema() {
            return schema;
        }

        public void setSchema(String schema) {
            this.schema = schema;
        }
    }

    class RequestItem extends BaseItem{
        private RequestInfo request;
        private List<?> response;
        private ProtocolProfileBehavior protocolProfileBehavior;

        public RequestInfo getRequest() {
            return request;
        }

        public void setRequest(RequestInfo request) {
            this.request = request;
        }

        public List<?> getResponse() {
            return response;
        }

        public void setResponse(List<?> response) {
            this.response = response;
        }

        public ProtocolProfileBehavior getProtocolProfileBehavior() {
            return protocolProfileBehavior;
        }

        public void setProtocolProfileBehavior(ProtocolProfileBehavior protocolProfileBehavior) {
            this.protocolProfileBehavior = protocolProfileBehavior;
        }
    }

    class ProtocolProfileBehavior {
        private boolean disableBodyPruning;

        public boolean isDisableBodyPruning() {
            return disableBodyPruning;
        }

        public void setDisableBodyPruning(boolean disableBodyPruning) {
            this.disableBodyPruning = disableBodyPruning;
        }
    }

    class RequestInfo {
        private String method;
        private List<Map<String,String>> header;
        private RequestBody body;
        private RequestUrl url;

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public List<Map<String, String>> getHeader() {
            return header;
        }

        public void setHeader(List<Map<String, String>> header) {
            this.header = header;
        }

        public RequestBody getBody() {
            return body;
        }

        public void setBody(RequestBody body) {
            this.body = body;
        }

        public RequestUrl getUrl() {
            return url;
        }

        public void setUrl(RequestUrl url) {
            this.url = url;
        }
    }

    class RequestBody {
        private String mode;
        private String raw;

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getRaw() {
            return raw;
        }

        public void setRaw(String raw) {
            this.raw = raw;
        }
    }

    class RequestUrl {
        private String raw;
        private String protocol;
        private List<String> host;
        private List<String> path;
        private List<Map<String,String>> query;

        public String getRaw() {
            return raw;
        }

        public void setRaw(String raw) {
            this.raw = raw;
        }

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public List<String> getHost() {
            return host;
        }

        public void setHost(List<String> host) {
            this.host = host;
        }

        public List<String> getPath() {
            return path;
        }

        public void setPath(List<String> path) {
            this.path = path;
        }

        public List<Map<String, String>> getQuery() {
            return query;
        }

        public void setQuery(List<Map<String, String>> query) {
            this.query = query;
        }
    }


}
