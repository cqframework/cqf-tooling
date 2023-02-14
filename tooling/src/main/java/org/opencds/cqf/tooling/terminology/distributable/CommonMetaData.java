package org.opencds.cqf.tooling.terminology.distributable;

import java.util.HashMap;
import java.util.Map;

public class CommonMetaData {

    private OrganizationalMetaData organizationalMetaData = new OrganizationalMetaData();
    private Map<String, CodeSystemMeta> codeSystemMetaMap = new HashMap<>();
    private Map<String, ValueSetMeta> valueSetMetaMap = new HashMap<>();

    public OrganizationalMetaData getOrganizationalMetaData() {
        return organizationalMetaData;
    }

    public void setOrganizationalMetaData(OrganizationalMetaData organizationalMetaData) {
        this.organizationalMetaData = organizationalMetaData;
    }

    public Map<String, CodeSystemMeta> getCodeSystemMeta() {
        return codeSystemMetaMap;
    }

    public void setCodeSystemMeta(Map<String, CodeSystemMeta> codeSystemMetaMap) {
        this.codeSystemMetaMap = codeSystemMetaMap;
    }

    public CommonMetaData addCodeSystemMeta(String identifier, String url, String version) {
        if (!codeSystemMetaMap.containsKey(url)) {
            codeSystemMetaMap.put(url, new CodeSystemMeta().setIdentifier(identifier).setUrl(url).setVersion(version));
        }

        return this;
    }

    public Map<String, ValueSetMeta> getValueSetMeta() {
        return valueSetMetaMap;
    }

    public void setValueSetMeta(Map<String, ValueSetMeta> valueSetMetaMap) {
        this.valueSetMetaMap = valueSetMetaMap;
    }

    public CommonMetaData addValueSetMeta(String identifier, String metaPageName, String codeListPageName) {
        if (!codeSystemMetaMap.containsKey(identifier)) {
            valueSetMetaMap.put(identifier, new ValueSetMeta().setIdentifier(identifier).setMetaPageName(metaPageName).setCodeListPageName(codeListPageName));
        }

        return this;
    }

    static class CodeSystemMeta {
        private String identifier;
        private String url;
        private String version;

        public String getIdentifier() {
            return identifier;
        }

        public CodeSystemMeta setIdentifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public String getUrl() {
            return url;
        }

        public CodeSystemMeta setUrl(String url) {
            this.url = url;
            return this;
        }

        public String getVersion() {
            return version;
        }

        public CodeSystemMeta setVersion(String version) {
            this.version = version;
            return this;
        }
    }

    static class ValueSetMeta {
        private String identifier;
        private String metaPageName;
        private String codeListPageName;

        public String getIdentifier() {
            return identifier;
        }

        public ValueSetMeta setIdentifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public String getMetaPageName() {
            return metaPageName;
        }

        public ValueSetMeta setMetaPageName(String metaPageName) {
            this.metaPageName = metaPageName;
            return this;
        }

        public String getCodeListPageName() {
            return codeListPageName;
        }

        public ValueSetMeta setCodeListPageName(String codeListPageName) {
            this.codeListPageName = codeListPageName;
            return this;
        }
    }

}
