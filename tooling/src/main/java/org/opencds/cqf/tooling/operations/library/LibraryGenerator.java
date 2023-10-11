package org.opencds.cqf.tooling.operations.library;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.util.FhirTerser;
import ca.uhn.fhir.util.TerserUtil;
import org.apache.commons.codec.binary.Base64;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.cql2elm.DefaultLibrarySourceProvider;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.elm.requirements.fhir.DataRequirementsProcessor;
import org.hl7.elm.r1.IncludeDef;
import org.hl7.elm.r1.Library;
import org.hl7.fhir.convertors.advisors.impl.BaseAdvisor_30_50;
import org.hl7.fhir.convertors.advisors.impl.BaseAdvisor_40_50;
import org.hl7.fhir.convertors.conv30_50.VersionConvertor_30_50;
import org.hl7.fhir.convertors.conv40_50.VersionConvertor_40_50;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.Operation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.utilities.FhirContextCache;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.opencds.cqf.tooling.utilities.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

@Operation(name = "CqlToLibrary")
public class LibraryGenerator implements ExecutableOperation {
   private static final Logger logger = LoggerFactory.getLogger(LibraryGenerator.class);

   // TODO: either enable user to pass in translator options as a param or search directory for cql-options.json
   @OperationParam(alias = { "ptcql", "pathtocqlcontent" }, setter = "setPathToCqlContent", required = true,
           description = "Path to the directory or file containing the CQL content to be transformed into FHIR Library resources (required)")
   private String pathToCqlContent;
   @OperationParam(alias = { "e", "encoding" }, setter = "setEncoding", defaultValue = "json",
           description = "The file format to be used for representing the resulting FHIR Library { json, xml } (default json)")
   private String encoding;
   @OperationParam(alias = { "v", "version" }, setter = "setVersion", defaultValue = "r4",
           description = "FHIR version { stu3, r4, r5 } (default r4)")
   private String version;
   @OperationParam(alias = { "op", "outputpath" }, setter = "setOutputPath",
           defaultValue = "src/main/resources/org/opencds/cqf/tooling/library/output",
           description = "The directory path to which the generated FHIR Library resources should be written (default src/main/resources/org/opencds/cqf/tooling/library/output)")
   private String outputPath;

   private FhirContext fhirContext;
   private CqlTranslatorOptions translatorOptions = CqlTranslatorOptions.defaultOptions();
   private final DataRequirementsProcessor dataRequirementsProcessor = new DataRequirementsProcessor();

   @Override
   public void execute() {
      fhirContext = FhirContextCache.getContext(version);
      translatorOptions = ResourceUtils.getTranslatorOptions(pathToCqlContent);
      ModelManager modelManager = new ModelManager();
      LibraryManager libraryManager = new LibraryManager(modelManager, translatorOptions.getCqlCompilerOptions());
      File cqlContent = new File(pathToCqlContent);
      LibrarySourceProvider librarySourceProvider = new DefaultLibrarySourceProvider(cqlContent.isDirectory() ?
              cqlContent.toPath() : cqlContent.getParentFile().toPath());
      libraryManager.getLibrarySourceLoader().registerProvider(librarySourceProvider);

      if (cqlContent.isDirectory()) {
         File[] cqlFiles = cqlContent.listFiles();
         if (cqlFiles != null) {
            for (File cqlFile : cqlFiles) {
               if (cqlFile.getAbsolutePath().endsWith("cql")) {
                  processCqlFile(cqlFile, libraryManager);
               }
            }
         }
      }
      else {
         processCqlFile(cqlContent, libraryManager);
      }
   }

   private void processCqlFile(File cqlFile, LibraryManager libraryManager) {
      if (cqlFile.getAbsolutePath().endsWith("cql")) {
         try {
            CqlTranslator translator = CqlTranslator.fromFile(cqlFile, libraryManager);
            org.hl7.fhir.r5.model.Library dataReqLibrary = dataRequirementsProcessor.gatherDataRequirements(
                    libraryManager, translator.getTranslatedLibrary(), translatorOptions.getCqlCompilerOptions(),
                    null, false);
            IOUtils.writeResource(resolveFhirLibrary(translator, dataReqLibrary, IOUtils.getFileContent(cqlFile)),
                    outputPath, IOUtils.Encoding.valueOf(encoding), fhirContext);
         } catch (IOException e) {
            logger.error("Error encountered translating {}", cqlFile.getAbsolutePath(), e);
         }
      }
   }

   // Library access method
   public IBaseResource resolveFhirLibrary(CqlTranslator cqlTranslator, org.hl7.fhir.r5.model.Library dataReqLibrary, String cql) {
      IBaseResource library;
      switch (fhirContext.getVersion().getVersion()) {
         case DSTU3:
            library = new VersionConvertor_30_50(new BaseAdvisor_30_50()).convertResource(dataReqLibrary);
            break;
         case R4:
            library = new VersionConvertor_40_50(new BaseAdvisor_40_50()).convertResource(dataReqLibrary);
            break;
         case R5:
            library = dataReqLibrary;
            break;
         default:
            throw new UnsupportedOperationException(String.format(
                    "Library generation for version %s is not supported",
                    fhirContext.getVersion().getVersion().getFhirVersionString()));
      }

      Library elmLibrary = cqlTranslator.toELM();

      library.setId(nameToId(elmLibrary.getIdentifier().getId(), elmLibrary.getIdentifier().getVersion()));
      FhirTerser terser = new FhirTerser(fhirContext);

      // basic metadata information
      terser.setElement(library, "name", elmLibrary.getIdentifier().getId());
      terser.setElement(library, "version", elmLibrary.getIdentifier().getVersion());
      terser.setElement(library, "experimental", "true");
      terser.setElement(library, "status", "active");
      IBase type = TerserUtil.newElement(fhirContext, "CodeableConcept");
      IBase typeCoding = TerserUtil.newElement(fhirContext, "Coding");
      terser.setElement(typeCoding, "code", "logic-library");
      terser.setElement(typeCoding, "system", "http://hl7.org/fhir/library-type");
      terser.setElement(typeCoding, "display", "Logic Library");
      TerserUtil.setField(fhirContext, "type", library, type);
      TerserUtil.setFieldByFhirPath(fhirContext, "Library.type.coding", library, typeCoding);

      // content
      IBase cqlAttachment = TerserUtil.newElement(fhirContext, "Attachment");
      terser.setElement(cqlAttachment, "contentType", "text/cql");
      terser.setElement(cqlAttachment, "data", Base64.encodeBase64String(cql.getBytes()));
      IBase elmXmlAttachment = TerserUtil.newElement(fhirContext, "Attachment");
      terser.setElement(elmXmlAttachment, "contentType", "application/elm+xml");
      terser.setElement(elmXmlAttachment, "data", Base64.encodeBase64String(cqlTranslator.toXml().getBytes()));
      IBase elmJsonAttachment = TerserUtil.newElement(fhirContext, "Attachment");
      terser.setElement(elmJsonAttachment, "contentType", "application/elm+json");
      terser.setElement(elmJsonAttachment, "data", Base64.encodeBase64String(cqlTranslator.toJson().getBytes()));
      TerserUtil.setField(fhirContext, "content", library, cqlAttachment, elmXmlAttachment, elmJsonAttachment);

      return library;
   }

   private String nameToId(String name, String version) {
      String nameAndVersion = "library-" + name + "-" + version;
      return nameAndVersion.replace("_", "-");
   }

   private String getIncludedLibraryId(IncludeDef def) {
      return nameToId(getIncludedLibraryName(def), def.getVersion());
   }

   private String getIncludedLibraryName(IncludeDef def) {
      return def.getPath();
   }

   public String getPathToCqlContent() {
      return pathToCqlContent;
   }

   public void setPathToCqlContent(String pathToCqlContent) {
      this.pathToCqlContent = pathToCqlContent;
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
