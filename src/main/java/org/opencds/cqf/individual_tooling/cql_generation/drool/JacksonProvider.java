
package org.opencds.cqf.individual_tooling.cql_generation.drool;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.cdsframework.base.BaseDTO;
import org.cdsframework.rs.provider.BaseDTODeserializer;
import org.cdsframework.rs.provider.CoreJaxbAnnotationIntrospector;
import org.cdsframework.util.ClassUtils;
import org.cdsframework.util.LogUtils;

public class JacksonProvider  {
    
    protected static LogUtils logger = LogUtils.getLogger(JacksonProvider.class);
    private ObjectMapper defaultObjectMapper = null;

    public JacksonProvider() {
        this(JsonInclude.Include.NON_NULL);
    }    

    public JacksonProvider(JsonInclude.Include include) {
        final String METHODNAME = "constructor ";
        logger.info(METHODNAME);
        defaultObjectMapper = createObjectMapper(include, null);
    }
    
    public ObjectMapper getContext(Class<?> type) {
        return defaultObjectMapper;
    }

    public ObjectMapper createObjectMapper(JsonInclude.Include jsonInclude, String[] ignorableFields) {
        final String METHODNAME = "createObjectMapper ";
        logger.info(METHODNAME, "creating objectMapper for ", JacksonProvider.class.getCanonicalName());

        ObjectMapper objectMapper = new ObjectMapper();
        JacksonAnnotationIntrospector primaryIntrospector = new JacksonAnnotationIntrospector();
        CoreJaxbAnnotationIntrospector secondaryIntrospector = new CoreJaxbAnnotationIntrospector();
        if (ignorableFields != null) {
            secondaryIntrospector.setIgnoreableFields(ignorableFields);
        }
        AnnotationIntrospector annotationIntrospector = AnnotationIntrospector.pair(primaryIntrospector, secondaryIntrospector);            
        objectMapper.setAnnotationIntrospector(annotationIntrospector);

        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        if (jsonInclude != null) {
            objectMapper.setSerializationInclusion(jsonInclude);
        }
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return objectMapper;
    }

    public void registerDTOs(String packageName) {
        final String METHODNAME = "registerDTOs ";
        List<Class<? extends BaseDTO>> dtoClasses = new ArrayList<Class<? extends BaseDTO>>();
        try {
            Class[] classes = ClassUtils.getClassesFromClasspath(packageName);
            for (Class cls : classes) {
                if (BaseDTO.class.isAssignableFrom(cls)) {
                    dtoClasses.add(cls);
                }
            }
            if (!dtoClasses.isEmpty()) {
                registerDTOs(dtoClasses);
                logger.info(METHODNAME, "dtoClasses=", dtoClasses );
                logger.info(METHODNAME, "dtoClasses.size()=", dtoClasses.size() );                
            }
        }
        catch (IOException | ClassNotFoundException | URISyntaxException e) {
            String errorMessage = "An " + e.getClass().getSimpleName() + " has occurred; Message: " + e.getMessage();
            logger.error(METHODNAME, errorMessage);
            throw new RuntimeException(errorMessage, e);
        }
    }
    
    public void registerDTOs(List<Class<? extends BaseDTO>> dtoClasses) {
        if (defaultObjectMapper != null && dtoClasses != null) {
            BaseDTODeserializer deserializer = new BaseDTODeserializer();
            for (Class<? extends BaseDTO> dtoClass : dtoClasses) {
                deserializer.registerBaseDTO(dtoClass);
            }
            SimpleModule simpleModule = new SimpleModule("PolymorphicDTODeserializerModule", new Version(1, 0, 0, null));
            simpleModule.addDeserializer(BaseDTO.class, deserializer);
            defaultObjectMapper.registerModule(simpleModule);
        }
    }
}