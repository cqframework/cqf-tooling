package org.opencds.cqf.tooling.terminology.generate;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Config {

   @JsonProperty(required = true)
   String pathToIgResource;

   @JsonProperty
   Author author;

   @JsonProperty("codesystems")
   List<CodeSystems> codeSystems;

   @JsonAlias("valuesets")
   @JsonProperty(required = true)
   List<ValueSets> valueSets;

   public String getPathToIgResource() {
      return pathToIgResource;
   }

   public void setPathToIgResource(String pathToIgResource) {
      this.pathToIgResource = pathToIgResource;
   }

   public Author getAuthor() {
      return author;
   }

   public void setAuthor(Author author) {
      this.author = author;
   }

   public List<CodeSystems> getCodeSystems() {
      return codeSystems;
   }

   public void setCodeSystems(List<CodeSystems> codeSystems) {
      this.codeSystems = codeSystems;
   }

   public List<ValueSets> getValueSets() {
      return valueSets;
   }

   public void setValueSets(List<ValueSets> valueSets) {
      this.valueSets = valueSets;
   }

   static class Author {
      @JsonProperty(required = true)
      String name;
      @JsonProperty(required = true)
      String contactType;
      @JsonProperty(required = true)
      String contactValue;

      public String getName() {
         return name;
      }

      public void setName(String name) {
         this.name = name;
      }

      public String getContactType() {
         return contactType;
      }

      public void setContactType(String contactType) {
         this.contactType = contactType;
      }

      public String getContactValue() {
         return contactValue;
      }

      public void setContactValue(String contactValue) {
         this.contactValue = contactValue;
      }
   }

   static class CodeSystems {
      @JsonProperty(required = true)
      String name;
      @JsonProperty(required = true)
      String url;
      @JsonProperty(required = true)
      String version;

      public String getName() {
         return name;
      }

      public void setName(String name) {
         this.name = name;
      }

      public String getUrl() {
         return url;
      }

      public void setUrl(String url) {
         this.url = url;
      }

      public String getVersion() {
         return version;
      }

      public void setVersion(String version) {
         this.version = version;
      }
   }

   static class ValueSets {
      @JsonProperty(required = true)
      String id;
      @JsonProperty(required = true)
      String canonical;
      @JsonProperty
      String name;
      @JsonProperty
      String title;
      @JsonProperty
      String status;
      @JsonProperty
      Boolean experimental;
      @JsonProperty
      String description;
      @JsonProperty
      String purpose;
      @JsonProperty
      List<String> profiles;
      @JsonProperty
      String clinicalFocus;
      @JsonProperty
      String dataElementScope;
      @JsonProperty
      String inclusionCriteria;
      @JsonProperty
      String exclusionCriteria;
      @JsonProperty
      String usageWarning;
      @JsonProperty
      List<String> knowledgeCapability;
      @JsonProperty
      List<String> knowledgeRepresentationLevel;

      @JsonProperty
      RulesText rulesText;

      @JsonProperty
      Hierarchy hierarchy;

      @JsonProperty
      Expand expand;

      public String getId() {
         return id;
      }

      public void setId(String id) {
         this.id = id;
      }

      public String getCanonical() {
         return canonical;
      }

      public void setCanonical(String canonical) {
         this.canonical = canonical;
      }

      public String getName() {
         return name;
      }

      public void setName(String name) {
         this.name = name;
      }

      public String getTitle() {
         return title;
      }

      public void setTitle(String title) {
         this.title = title;
      }

      public String getStatus() {
         return status;
      }

      public void setStatus(String status) {
         this.status = status;
      }

      public Boolean getExperimental() {
         return experimental;
      }

      public void setExperimental(Boolean experimental) {
         this.experimental = experimental;
      }

      public String getDescription() {
         return description;
      }

      public void setDescription(String description) {
         this.description = description;
      }

      public String getPurpose() {
         return purpose;
      }

      public void setPurpose(String purpose) {
         this.purpose = purpose;
      }

      public List<String> getProfiles() {
         return profiles;
      }

      public void setProfiles(List<String> profiles) {
         this.profiles = profiles;
      }

      public String getClinicalFocus() {
         return clinicalFocus;
      }

      public void setClinicalFocus(String clinicalFocus) {
         this.clinicalFocus = clinicalFocus;
      }

      public String getDataElementScope() {
         return dataElementScope;
      }

      public void setDataElementScope(String dataElementScope) {
         this.dataElementScope = dataElementScope;
      }

      public String getInclusionCriteria() {
         return inclusionCriteria;
      }

      public void setInclusionCriteria(String inclusionCriteria) {
         this.inclusionCriteria = inclusionCriteria;
      }

      public String getExclusionCriteria() {
         return exclusionCriteria;
      }

      public void setExclusionCriteria(String exclusionCriteria) {
         this.exclusionCriteria = exclusionCriteria;
      }

      public String getUsageWarning() {
         return usageWarning;
      }

      public void setUsageWarning(String usageWarning) {
         this.usageWarning = usageWarning;
      }

      public List<String> getKnowledgeCapability() {
         return knowledgeCapability;
      }

      public void setKnowledgeCapability(List<String> knowledgeCapability) {
         this.knowledgeCapability = knowledgeCapability;
      }

      public List<String> getKnowledgeRepresentationLevel() {
         return knowledgeRepresentationLevel;
      }

      public void setKnowledgeRepresentationLevel(List<String> knowledgeRepresentationLevel) {
         this.knowledgeRepresentationLevel = knowledgeRepresentationLevel;
      }

      public RulesText getRulesText() {
         return rulesText;
      }

      public void setRulesText(RulesText rulesText) {
         this.rulesText = rulesText;
      }

      public Hierarchy getHierarchy() {
         return hierarchy;
      }

      public void setHierarchy(Hierarchy hierarchy) {
         this.hierarchy = hierarchy;
      }

      public Expand getExpand() {
         return expand;
      }

      public void setExpand(Expand expand) {
         this.expand = expand;
      }

      static class RulesText {
         @JsonProperty(required = true)
         String narrative;
         @JsonProperty(required = true)
         String workflowXml;
         @JsonProperty(required = true)
         String input;
         @JsonProperty
         List<String> includeFilter;
         @JsonProperty
         List<String> excludeFilter;

         public String getNarrative() {
            return narrative;
         }

         public void setNarrative(String narrative) {
            this.narrative = narrative;
         }

         public String getWorkflowXml() {
            return workflowXml;
         }

         public void setWorkflowXml(String workflowXml) {
            this.workflowXml = workflowXml;
         }

         public String getInput() {
            return input;
         }

         public void setInput(String input) {
            this.input = input;
         }

         public List<String> getIncludeFilter() {
            return includeFilter;
         }

         public void setIncludeFilter(List<String> includeFilter) {
            this.includeFilter = includeFilter;
         }

         public List<String> getExcludeFilter() {
            return excludeFilter;
         }

         public void setExcludeFilter(List<String> excludeFilter) {
            this.excludeFilter = excludeFilter;
         }
      }

      static class Hierarchy {
         @JsonProperty(required = true)
         String narrative;
         @JsonProperty(required = true)
         List<String> parents;
         @JsonProperty
         List<Property> property;
         @JsonProperty
         String query;
         @JsonProperty(required = true)
         Auth auth;

         public String getNarrative() {
            return narrative;
         }

         public void setNarrative(String narrative) {
            this.narrative = narrative;
         }

         public List<String> getParents() {
            return parents;
         }

         public void setParents(List<String> parents) {
            this.parents = parents;
         }

         public List<Property> getProperty() {
            return property;
         }

         public void setProperty(List<Property> property) {
            this.property = property;
         }

         public String getQuery() {
            return query;
         }

         public void setQuery(String query) {
            this.query = query;
         }

         public Auth getAuth() {
            return auth;
         }

         public void setAuth(Auth auth) {
            this.auth = auth;
         }

         static class Property {
            @JsonProperty(required = true)
            String name;
            @JsonProperty(required = true)
            String value;

            public String getName() {
               return name;
            }

            public void setName(String name) {
               this.name = name;
            }

            public String getValue() {
               return value;
            }

            public void setValue(String value) {
               this.value = value;
            }
         }
      }

      static class Expand {
         @JsonProperty(required = true)
         String pathToValueSet;
         @JsonProperty
         TxServer txServer;
         @JsonProperty
         boolean validateFSN = true;
         @JsonProperty
         String vsacId;
         @JsonProperty
         String apikey;

         public String getPathToValueSet() {
            return pathToValueSet;
         }

         public void setPathToValueSet(String pathToValueSet) {
            this.pathToValueSet = pathToValueSet;
         }

         public TxServer getTxServer() {
            return txServer;
         }

         public void setTxServer(TxServer txServer) {
            this.txServer = txServer;
         }

         public boolean isValidateFSN() {
            return validateFSN;
         }

         public void setValidateFSN(boolean validateFSN) {
            this.validateFSN = validateFSN;
         }

         public String getVsacId() {
            return vsacId;
         }

         public void setVsacId(String vsacId) {
            this.vsacId = vsacId;
         }

         public String getApikey() {
            return apikey;
         }

         public void setApikey(String apikey) {
            this.apikey = apikey;
         }

         static class TxServer {
            @JsonProperty(required = true)
            String baseUrl;
            @JsonProperty
            Auth auth;

            public String getBaseUrl() {
               return baseUrl;
            }

            public void setBaseUrl(String baseUrl) {
               this.baseUrl = baseUrl;
            }

            public Auth getAuth() {
               return auth;
            }

            public void setAuth(Auth auth) {
               this.auth = auth;
            }
         }
      }

      static class Auth {
         @JsonProperty(required = true)
         String user;
         @JsonProperty(required = true)
         String password;

         public String getUser() {
            return user;
         }

         public void setUser(String user) {
            this.user = user;
         }

         public String getPassword() {
            return password;
         }

         public void setPassword(String password) {
            this.password = password;
         }
      }
   }

}
