package org.opencds.cqf.tooling.operations.dateroller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.context.RuntimeChildChoiceDefinition;
import ca.uhn.fhir.context.RuntimeChildPrimitiveDatatypeDefinition;
import ca.uhn.fhir.util.BundleBuilder;
import ca.uhn.fhir.util.BundleUtil;
import ca.uhn.fhir.util.ExtensionUtil;
import ca.uhn.fhir.util.FhirTerser;
import ca.uhn.fhir.util.TerserUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.exception.InvalidOperationArgs;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.utilities.FhirContextCache;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Operation(name = "RollTestDates")
public class RollTestDates implements ExecutableOperation {
   private static final Logger logger = LoggerFactory.getLogger(RollTestDates.class);
   public static final String DATEROLLER_EXT_URL = "http://fhir.org/guides/cdc/opioid-cds/StructureDefinition/dataDateRoller";
   @OperationParam(alias = { "ptres", "pathtoresources" }, setter = "setPathToResources",
           description = "Path to the directory containing the resource files to be updated (required if -ptreq not present)")
   private String pathToResources;
   @OperationParam(alias = { "ptreq", "pathtorequests" }, setter = "setPathToRequests",
           description = "Path to the directory containing the CDS Hooks request files to be updated (required if -ptres not present)")
   private String pathToRequests;
   @OperationParam(alias = { "e", "encoding" }, setter = "setEncoding", defaultValue = "json",
           description = "The file format to be used for representing the resulting resource { json, xml } (default json)")
   private String encoding;
   @OperationParam(alias = { "v", "version" }, setter = "setVersion", defaultValue = "r4",
           description = "FHIR version { stu3, r4, r5 } (default r4)")
   private String version;
   @OperationParam(alias = { "op", "outputPath" }, setter = "setOutputPath",
           description = "The file system location where the resulting resources/requests are written (default same as -ptreq or -ptres)")
   private String outputPath;

   private FhirContext fhirContext;

   @Override
   public void execute() {
      fhirContext = FhirContextCache.getContext(version);
      if (pathToResources == null && pathToRequests == null) {
         throw new InvalidOperationArgs("Either pathtoresources (-ptres) or pathtorequests (-ptreq) parameter must be provided");
      }
      if (pathToResources != null) {
         if (outputPath == null) {
            outputPath = pathToResources;
         }
         List<IBaseResource> resources = IOUtils.readResources(Collections.singletonList(pathToResources), fhirContext)
                 .stream().filter(resource -> ExtensionUtil.hasExtension(resource, DATEROLLER_EXT_URL))
                 .filter(resource -> getAllDateElements(fhirContext, resource, getDateClasses(fhirContext)))
                 .collect(Collectors.toList());
         IOUtils.writeResources(resources, outputPath, IOUtils.Encoding.valueOf(encoding), fhirContext);
      }
      else {
         if (outputPath == null) {
            outputPath = pathToRequests;
         }
         Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
         processCDSHooksRequests(new File(pathToRequests), gson);
      }
   }

   // NOTE: the legacy CDSHooks prefetch format is NOT supported
   private void processCDSHooksRequests(File requestDirectory, Gson gson) {
      if (requestDirectory.isDirectory()) {
         File[] requests = requestDirectory.listFiles();
         if (requests != null) {
            for (File nextFile : requests) {
               if (nextFile.isDirectory()) {
                  processCDSHooksRequests(nextFile, gson);
               }
               processFile(nextFile, gson);
            }
         }
      }
      else if (requestDirectory.isFile()) {
         processFile(requestDirectory, gson);
      }
   }

   private void processFile(File file, Gson gson) {
      if (file.getName().toLowerCase(Locale.ROOT).endsWith("json")) {
         JsonObject request = gson.fromJson(IOUtils.getFileContent(file), JsonObject.class);
         getUpdatedRequest(request, gson);
         try (FileWriter writer = new FileWriter(file.getAbsolutePath())) {
            writer.write(gson.toJson(request));
         } catch (IOException e) {
            logger.error("Error writing file {}", file.getName(), e);
         }
      }
   }

   private IBaseBundle updateBundleDates(IBaseBundle bundle) {
      BundleBuilder builder = new BundleBuilder(fhirContext);
      BundleUtil.toListOfResources(fhirContext, bundle).forEach(
              resource -> {
                 getAllDateElements(fhirContext, resource, getDateClasses(fhirContext));
                 builder.addCollectionEntry(resource);
              }
      );
      return builder.getBundle();
   }

   // Library method
   public void getUpdatedRequest(JsonObject request, Gson gson) {
      if (request.has("context") &&
              request.get("context").getAsJsonObject().has("draftOrders")) {
         IBaseResource draftOrdersBundle = fhirContext.newJsonParser().parseResource(
                 request.getAsJsonObject("context").getAsJsonObject("draftOrders").toString());
         if (draftOrdersBundle instanceof IBaseBundle) {
            request.getAsJsonObject("context").add("draftOrders",
                    gson.fromJson(fhirContext.newJsonParser().encodeResourceToString(
                            updateBundleDates((IBaseBundle) draftOrdersBundle)), JsonObject.class));
         }
      }
      if (request.has("prefetch")) {
         JsonObject prefetchResources = request.getAsJsonObject("prefetch");
         JsonObject updatedPrefetch = new JsonObject();
         for (Map.Entry<String, JsonElement> prefetchElement : prefetchResources.entrySet()) {
            if (prefetchElement.getValue().isJsonNull()) {
               updatedPrefetch.add(prefetchElement.getKey(), prefetchElement.getValue());
               continue;
            }
            IBaseResource resource = fhirContext.newJsonParser().parseResource(prefetchElement.getValue().toString());
            if (resource instanceof IBaseBundle) {
               updatedPrefetch.add(prefetchElement.getKey(), gson.fromJson(fhirContext.newJsonParser()
                               .encodeResourceToString(updateBundleDates((IBaseBundle) resource)),
                       JsonObject.class));
            }
            else {
               getAllDateElements(fhirContext, resource, getDateClasses(fhirContext));
               updatedPrefetch.add(prefetchElement.getKey(), gson.fromJson(
                       fhirContext.newJsonParser().encodeResourceToString(resource), JsonObject.class));
            }
         }
         request.add("prefetch", updatedPrefetch);
      }
   }

   // Library method
   public boolean getAllDateElements(FhirContext fhirContext, IBaseResource resource, List<Class<? extends IBase>> classes) {
      FhirTerser terser = new FhirTerser(fhirContext);
      if (ExtensionUtil.hasExtension(resource, DATEROLLER_EXT_URL) && doUpdate(resource)) {
         terser.visit(resource, (theResource, theElement, thePathToElement, theChildDefinition, theDefinition) -> {
            // TODO - handle timing elements
            if (theElement.fhirType().equalsIgnoreCase("timing")) {
               return;
            }
            for (Class<? extends IBase> clazz : classes) {
               if (clazz.isAssignableFrom(theElement.getClass())) {
                  // skip extensions and children of composite Date elements (Period)
                  if (thePathToElement.contains("extension") ||
                          (theChildDefinition instanceof RuntimeChildPrimitiveDatatypeDefinition &&
                                  ((RuntimeChildPrimitiveDatatypeDefinition) theChildDefinition).getField()
                                          .getDeclaringClass().getSimpleName().equalsIgnoreCase("period")))
                  {
                     continue;
                  }
                  // Resolve choice type path names by type
                  if (theChildDefinition instanceof RuntimeChildChoiceDefinition) {
                     String s = theChildDefinition.getChildNameByDatatype(clazz);
                     thePathToElement.remove(thePathToElement.size() - 1);
                     thePathToElement.add(s);
                  }
                  int daysToAdd = getDaysBetweenDates(getLastUpdatedDate(resource), LocalDate.now());
                  if (theElement instanceof org.hl7.fhir.dstu3.model.BaseDateTimeType) {
                     TimeZone timeZone = ((org.hl7.fhir.dstu3.model.BaseDateTimeType) theElement).getTimeZone();
                     ((org.hl7.fhir.dstu3.model.BaseDateTimeType) theElement).setValue(DateUtils.addDays(
                                     ((org.hl7.fhir.dstu3.model.BaseDateTimeType) theElement).getValue(), daysToAdd))
                             .setTimeZone(timeZone);
                  } else if (theElement instanceof org.hl7.fhir.r4.model.BaseDateTimeType) {
                     TimeZone timeZone = ((org.hl7.fhir.r4.model.BaseDateTimeType) theElement).getTimeZone();
                     ((org.hl7.fhir.r4.model.BaseDateTimeType) theElement).setValue(DateUtils.addDays(
                                     ((org.hl7.fhir.r4.model.BaseDateTimeType) theElement).getValue(), daysToAdd))
                             .setTimeZone(timeZone);
                  } else if (theElement instanceof org.hl7.fhir.r5.model.BaseDateTimeType) {
                     TimeZone timeZone = ((org.hl7.fhir.r5.model.BaseDateTimeType) theElement).getTimeZone();
                     ((org.hl7.fhir.r5.model.BaseDateTimeType) theElement).setValue(DateUtils.addDays(
                                     ((org.hl7.fhir.r5.model.BaseDateTimeType) theElement).getValue(), daysToAdd))
                             .setTimeZone(timeZone);
                  } else if (theElement instanceof org.hl7.fhir.dstu3.model.Period) {
                     org.hl7.fhir.dstu3.model.BaseDateTimeType start = terser.getSingleValueOrNull(theElement,
                             "start", org.hl7.fhir.dstu3.model.BaseDateTimeType.class);
                     org.hl7.fhir.dstu3.model.BaseDateTimeType end = terser.getSingleValueOrNull(theElement,
                             "end", org.hl7.fhir.dstu3.model.BaseDateTimeType.class);
                     ((org.hl7.fhir.dstu3.model.Period) theElement).setEnd(end.setValue(
                             DateUtils.addDays(end.getValue(), daysToAdd)).setTimeZone(end.getTimeZone()).getValue());
                     ((org.hl7.fhir.dstu3.model.Period) theElement).setStart(start.setValue(
                             DateUtils.addDays(start.getValue(), daysToAdd)).setTimeZone(start.getTimeZone()).getValue());
                  } else if (theElement instanceof org.hl7.fhir.r4.model.Period) {
                     org.hl7.fhir.r4.model.BaseDateTimeType start = terser.getSingleValueOrNull(theElement,
                             "start", org.hl7.fhir.r4.model.BaseDateTimeType.class);
                     org.hl7.fhir.r4.model.BaseDateTimeType end = terser.getSingleValueOrNull(theElement,
                             "end", org.hl7.fhir.r4.model.BaseDateTimeType.class);
                     ((org.hl7.fhir.r4.model.Period) theElement).setEnd(end.setValue(
                             DateUtils.addDays(end.getValue(), daysToAdd)).setTimeZone(end.getTimeZone()).getValue());
                     ((org.hl7.fhir.r4.model.Period) theElement).setStart(start.setValue(
                             DateUtils.addDays(start.getValue(), daysToAdd)).setTimeZone(start.getTimeZone()).getValue());
                  } else if (theElement instanceof org.hl7.fhir.r5.model.Period) {
                     org.hl7.fhir.r5.model.BaseDateTimeType start = terser.getSingleValueOrNull(theElement,
                             "start", org.hl7.fhir.r5.model.BaseDateTimeType.class);
                     org.hl7.fhir.r5.model.BaseDateTimeType end = terser.getSingleValueOrNull(theElement,
                             "end", org.hl7.fhir.r5.model.BaseDateTimeType.class);
                     ((org.hl7.fhir.r5.model.Period) theElement).setEnd(end.setValue(
                             DateUtils.addDays(end.getValue(), daysToAdd)).setTimeZone(end.getTimeZone()).getValue());
                     ((org.hl7.fhir.r5.model.Period) theElement).setStart(start.setValue(
                             DateUtils.addDays(start.getValue(), daysToAdd)).setTimeZone(start.getTimeZone()).getValue());
                  } else {
                     throw new IllegalArgumentException(
                             "Expected type: date | datetime | timing | instant | period, found: " +
                                     theElement.fhirType());
                  }
                  TerserUtil.setFieldByFhirPath(terser, resolveFhirPath(theResource.fhirType(), thePathToElement),
                          resource, theElement);
                  updateDateRollerExtension(fhirContext, resource);
               }
            }
         });
         return true;
      }
      return false;
   }

   private int getDaysBetweenDates(LocalDate start, LocalDate end) {
      return (int) ChronoUnit.DAYS.between(start, end);
   }

   private LocalDate getLastUpdatedDate(IBaseResource resource) {
      IBaseDatatype dateLastUpdated = ExtensionUtil.getExtensionByUrl(
              ExtensionUtil.getExtensionByUrl(resource, DATEROLLER_EXT_URL), "dateLastUpdated").getValue();
      if (dateLastUpdated instanceof org.hl7.fhir.dstu3.model.BaseDateTimeType) {
         return LocalDate.parse(((org.hl7.fhir.dstu3.model.BaseDateTimeType) dateLastUpdated).getValueAsString().split("T")[0]);
      } else if (dateLastUpdated instanceof org.hl7.fhir.r4.model.BaseDateTimeType) {
         return LocalDate.parse(((org.hl7.fhir.r4.model.BaseDateTimeType) dateLastUpdated).getValueAsString().split("T")[0]);
      } else if (dateLastUpdated instanceof org.hl7.fhir.r5.model.BaseDateTimeType) {
         return LocalDate.parse(((org.hl7.fhir.r5.model.BaseDateTimeType) dateLastUpdated).getValueAsString().split("T")[0]);
      } else {
         throw new IllegalArgumentException("Unsupported type/version found for dateLastUpdated extension: "
                 + dateLastUpdated.fhirType());
      }
   }

   private int getFrequencyInDays(IBaseResource resource) {
      IBaseDatatype frequency = ExtensionUtil.getExtensionByUrl(
              ExtensionUtil.getExtensionByUrl(resource, DATEROLLER_EXT_URL), "frequency").getValue();
      int numDays;
      String precision;
      if (frequency instanceof org.hl7.fhir.dstu3.model.Duration) {
         numDays = ((org.hl7.fhir.dstu3.model.Duration) frequency).getValue().intValue();
         precision = StringUtils.firstNonEmpty(((org.hl7.fhir.dstu3.model.Duration) frequency).getCode(), ((org.hl7.fhir.dstu3.model.Duration) frequency).getUnit());
      } else if (frequency instanceof org.hl7.fhir.r4.model.Duration) {
         numDays = ((org.hl7.fhir.r4.model.Duration) frequency).getValue().intValue();
         precision = StringUtils.firstNonEmpty(((org.hl7.fhir.r4.model.Duration) frequency).getCode(), ((org.hl7.fhir.r4.model.Duration) frequency).getUnit());
      } else if (frequency instanceof org.hl7.fhir.r5.model.Duration) {
         numDays = ((org.hl7.fhir.r5.model.Duration) frequency).getValue().intValue();
         precision = StringUtils.firstNonEmpty(((org.hl7.fhir.r5.model.Duration) frequency).getCode(), ((org.hl7.fhir.r5.model.Duration) frequency).getUnit());
      } else {
         throw new IllegalArgumentException("Unsupported type/version found for frequency duration extension: " + frequency.fhirType());
      }
      if (precision == null) {
         throw new IllegalArgumentException("The frequency duration precision not found");
      } else if (precision.toLowerCase().startsWith("d")) {
         return numDays;
      } else if (precision.toLowerCase().startsWith("w")) {
         return numDays * 7;
      } else if (precision.toLowerCase().startsWith("m")) {
         return numDays * 30;
      } else if (precision.toLowerCase().startsWith("y")) {
         return numDays * 365;
      } else {
         throw new IllegalArgumentException("The frequency duration precision is invalid. Must be one of (d | w | m | y)");
      }
   }

   private boolean doUpdate(IBaseResource resource) {
      return getLastUpdatedDate(resource).isBefore(LocalDate.now());
   }

   private void updateDateRollerExtension(FhirContext fhirContext, IBaseResource resource) {
      ExtensionUtil.setExtension(fhirContext, ExtensionUtil.getExtensionByUrl(ExtensionUtil.getExtensionByUrl(
              resource, DATEROLLER_EXT_URL), "dateLastUpdated"), "dateTime", new Date());
   }

   public List<Class<? extends IBase>> getDateClasses(FhirContext fhirContext) {
      List<Class<? extends IBase>> classes = new ArrayList<>();
      if (fhirContext.getVersion().getVersion() == FhirVersionEnum.DSTU3) {
         Collections.addAll(classes, org.hl7.fhir.dstu3.model.DateTimeType.class,
                 org.hl7.fhir.dstu3.model.DateType.class,
                 org.hl7.fhir.dstu3.model.InstantType.class,
                 org.hl7.fhir.dstu3.model.Timing.class,
                 org.hl7.fhir.dstu3.model.Period.class);
      } else if (fhirContext.getVersion().getVersion() == FhirVersionEnum.R4) {
         Collections.addAll(classes, org.hl7.fhir.r4.model.DateTimeType.class,
                 org.hl7.fhir.r4.model.DateType.class,
                 org.hl7.fhir.r4.model.InstantType.class,
                 org.hl7.fhir.r4.model.Timing.class,
                 org.hl7.fhir.r4.model.Period.class);
      } else if (fhirContext.getVersion().getVersion() == FhirVersionEnum.R5) {
         Collections.addAll(classes, org.hl7.fhir.r5.model.DateTimeType.class,
                 org.hl7.fhir.r5.model.DateType.class,
                 org.hl7.fhir.r5.model.InstantType.class,
                 org.hl7.fhir.r5.model.Timing.class,
                 org.hl7.fhir.r5.model.Period.class);
      } else {
         throw new UnsupportedOperationException("FHIR version "
                 + fhirContext.getVersion().getVersion().getFhirVersionString()
                 + " is not supported for this operation.");
      }
      return classes;
   }

   private String resolveFhirPath(String resourceType, List<String> paths) {
      StringBuilder builder = new StringBuilder();
      builder.append(resourceType).append(".");
      paths.forEach(path -> builder.append(path).append("."));
      builder.deleteCharAt(builder.length() - 1);
      return builder.toString();
   }

   public String getPathToResources() {
      return pathToResources;
   }

   public void setPathToResources(String pathToResources) {
      this.pathToResources = pathToResources;
   }

   public String getPathToRequests() {
      return pathToRequests;
   }

   public void setPathToRequests(String pathToRequests) {
      this.pathToRequests = pathToRequests;
   }

   public String getEncoding() {
      return encoding;
   }

   public void setEncoding(String encoding) {
      this.encoding = encoding;
   }

   public String getVersion() {
      return version;
   }

   public void setVersion(String version) {
      this.version = version;
   }

   public String getOutputPath() {
      return outputPath;
   }

   public void setOutputPath(String outputPath) {
      this.outputPath = outputPath;
   }

   public FhirContext getFhirContext() {
      return fhirContext;
   }

   public void setFhirContext(FhirContext fhirContext) {
      this.fhirContext = fhirContext;
   }
}
