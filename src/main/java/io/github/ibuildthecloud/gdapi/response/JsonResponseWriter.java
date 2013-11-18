package io.github.ibuildthecloud.gdapi.response;

import io.github.ibuildthecloud.gdapi.factory.SchemaFactory;
import io.github.ibuildthecloud.gdapi.json.JsonMapper;
import io.github.ibuildthecloud.gdapi.model.Collection;
import io.github.ibuildthecloud.gdapi.model.Resource;
import io.github.ibuildthecloud.gdapi.model.Schema;
import io.github.ibuildthecloud.gdapi.model.impl.CollectionImpl;
import io.github.ibuildthecloud.gdapi.model.impl.WrappedResource;
import io.github.ibuildthecloud.gdapi.request.ApiRequest;
import io.github.ibuildthecloud.gdapi.request.handler.AbstractApiRequestHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.inject.Inject;

public class JsonResponseWriter extends AbstractApiRequestHandler {

    JsonMapper jsonMapper;
    SchemaFactory schemaFactory;

    @Override
    public void handle(ApiRequest request) throws IOException {
        if ( request.isCommited() )
            return;

        if ( ! getResponseFormat().equals(request.getResponseFormat()) ) {
            return;
        }

        Object responseObject = getResponseObject(request);

        if ( responseObject == null )
            return;

        request.setResponseContentType(getContentType());

        OutputStream os = request.getOutputStream();
        writeJson(os, responseObject, request);
    }

    protected String getContentType() {
        return "application/json; charset=utf-8";
    }

    protected String getResponseFormat() {
        return "json";
    }

    protected Object getResponseObject(ApiRequest request) {
        Object object = request.getResponseObject();

        if ( object instanceof List ) {
            return createCollection((List<?>)object, request);
        }

        return createResource(object);
    }

    protected Collection createCollection(List<?> list, ApiRequest request) {
        CollectionImpl collection = new CollectionImpl();
        collection.setResourceType(request.getType());

        for ( Object obj : list ) {
            Resource resource = createResource(obj);
            if ( resource != null ) {
                collection.getData().add(resource);
                if ( collection.getResourceType() == null ) {
                    collection.setResourceType(resource.getType());
                }
            }
        }

        return collection;
    }

    protected Resource createResource(Object obj) {
        if ( obj == null )
            return null;

        if ( obj instanceof Resource )
            return (Resource)obj;

        Schema schema = schemaFactory.getSchema(obj.getClass());
        return schema == null ? null : new WrappedResource(schema, obj);
    }

    protected void writeJson(OutputStream os, Object responseObject, ApiRequest request) throws IOException {
        jsonMapper.writeValue(os, responseObject);
    }

    public JsonMapper getJsonMapper() {
        return jsonMapper;
    }

    @Inject
    public void setJsonMapper(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public SchemaFactory getSchemaFactory() {
        return schemaFactory;
    }

    @Inject
    public void setSchemaFactory(SchemaFactory schemaFactory) {
        this.schemaFactory = schemaFactory;
    }

}
