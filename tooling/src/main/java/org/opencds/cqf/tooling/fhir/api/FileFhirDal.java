package org.opencds.cqf.tooling.fhir.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.cqframework.fhir.api.FhirDal;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.opencds.cqf.tooling.parameter.FileFhirPlatformParameters;
import org.opencds.cqf.tooling.utilities.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IQueryParameterType;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.EncodingEnum;

public class FileFhirDal implements FhirDal {
  protected final String resourceDir;
  protected final EncodingEnum encoding;
  protected final FhirContext fhirContext;
  private static final Logger logger = LoggerFactory.getLogger(FileFhirDal.class);


  public FileFhirDal(String resourceDir){
    this.resourceDir = resourceDir;
    this.encoding = EncodingEnum.JSON;
    this.fhirContext = FhirContext.forR4();
  }

  public FileFhirDal(FileFhirPlatformParameters params){
    this.resourceDir = params.resourceDir;
    this.encoding = params.encoding;
    this.fhirContext = params.fhirContext;
  }

  @Override
  public void create(IBaseResource resource){
    if (resourceTypeDefined(resource)){

      //ensure resource type directory exists
      String typePath = IOUtils.concatFilePath(this.resourceDir, resource.getIdElement().getResourceType());
      String path = getPath(resource);

      File typeDir = new File(typePath);
      if (!typeDir.exists()){
        typeDir.mkdirs();
      }

      writeResource(path, resource);
    }
  }

  @Override
  public IBaseResource read(IIdType id){
    return resourceTypeDefined(id) ? readResource(getPath(id)) : null;
  }

  @Override
  public void update(IBaseResource resource) {
    if (resourceTypeDefined(resource)) {

      File file = new File(getPath(resource));

      if (file.exists()){
        writeResource(getPath(resource), resource);
      } else {
        create(resource);
      }
    }
  }

  @Override
  public void delete(IIdType id){
    if (resourceTypeDefined(id)){
      File file = new File(getPath(id));

      if (file.exists()){
        if (!file.delete()){
          logger.warn("Could not delete {} :", id.getIdPart());
        }
      }
    }
  }

  // TODO: search package cache if resource not found
  private IBaseResource readResource(String path){
    IParser parser = this.encoding.newParser(this.fhirContext);
    IBaseResource resource;
    File file = new File(path);

    if (!file.exists()){
      logger.warn("{} does not exist", file.getName());
      return null;
    }

    if (file.isDirectory()) {
      logger.warn("Cannot read a resource from a directory: {}", file.getName());
      return null;
    }

    try (FileReader reader = new FileReader(file)){
      resource = parser.parseResource(reader);
    } catch (IOException e){
      e.printStackTrace();
      throw new RuntimeException(String.format("Cannot read a resource from a directory: %s", file.getName()));
    }

    return resource;
  }

  private void writeResource(String path, IBaseResource resource){
    IParser parser = this.encoding.newParser(this.fhirContext);

    try (FileOutputStream writer = new FileOutputStream(path)) {
      writer.write(parser.setPrettyPrint(true).encodeResourceToString(resource).getBytes());
      writer.flush();
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("Error writing resource to file: " + e.getMessage());
    }
  }

  private String getPath(IBaseResource resource){
    return IOUtils.concatFilePath(this.resourceDir, resource.getIdElement().getResourceType(),
            resource.getIdElement().getIdPart() + "." + this.encoding.toString());
  }

  private String getPath(IIdType id){
    return IOUtils.concatFilePath(this.resourceDir, id.getResourceType(),
            id.getIdPart() + "." + this.encoding.toString());
  }

  private boolean resourceTypeDefined(IBaseResource resource){
    if (resource.getIdElement().hasResourceType()){
      return true;
    } else {
      logger.warn("ResourceType not defined for: {}", resource.getIdElement().getIdPart());
      return false;
    }
  }

  private boolean resourceTypeDefined(IIdType id){
    if (id.hasResourceType()){
      return true;
    } else {
      logger.warn("ResourceType not defined for: {}", id);
      return false;
    }
  }

  // TODO: search resource directory / package cache
  @Override
  public IBaseBundle search(String resourceType, Map<String, List<List<IQueryParameterType>>> searchParameters){
    return null;
  };
}
