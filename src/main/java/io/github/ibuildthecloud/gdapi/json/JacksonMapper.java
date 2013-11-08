package io.github.ibuildthecloud.gdapi.json;

import io.github.ibuildthecloud.gdapi.model.Resource;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

public class JacksonMapper implements JsonMapper {

    ObjectMapper mapper;

    @PostConstruct
    public void init() {
        SimpleModule module = new SimpleModule();
        module.setMixInAnnotation(Resource.class, ResourceMix.class);

        mapper = new ObjectMapper();
        mapper.registerModule(new JaxbAnnotationModule());
        mapper.registerModule(module);
        mapper.getFactory().configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
    }

    @Override
    public Object readValue(byte[] content) throws IOException {
        return mapper.readValue(content, Object.class);
    }

    @Override
    public void writeValue(OutputStream os, Object object) throws IOException {
        mapper.writeValue(os, object);
    }

    public static interface ResourceMix {
        @JsonAnyGetter
        Map<String,Object> getFields();
    }
}
