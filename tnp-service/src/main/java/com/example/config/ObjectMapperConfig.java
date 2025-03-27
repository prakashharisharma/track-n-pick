package com.example.config;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Creates bean for object mapper
 *
 * <p>Reuse heavy-weight objects: ObjectMapper (data-binding)
 *
 * <p>Please join the stack overflow discussion for more details
 *
 * <p>https://stackoverflow.com/questions/50362883/what-is-the-advantage-of-declaring-objectmapper-as-a-bean
 *
 * @author prakash
 */
@Component
public class ObjectMapperConfig {

    @Primary
    @Bean
    public ObjectMapper objectMapper() {

        ObjectMapper mapper = new ObjectMapper();
        registerTimeModule(mapper);
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setVisibility(
                mapper.getVisibilityChecker().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

        return mapper;
    }

    /**
     * This methods registers module to serialize LocalDateTime
     *
     * @param mapper
     */

    private void registerTimeModule(ObjectMapper mapper) {
        JavaTimeModule module = new JavaTimeModule();
        mapper.registerModule(module);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

}


