package io.github.ibuildthecloud.gdapi.request.handler;

import io.github.ibuildthecloud.gdapi.factory.SchemaFactory;
import io.github.ibuildthecloud.gdapi.model.Schema.Method;
import io.github.ibuildthecloud.gdapi.request.ApiRequest;
import io.github.ibuildthecloud.gdapi.request.resource.ResourceManager;
import io.github.ibuildthecloud.gdapi.request.resource.ResourceManagerLocator;

import java.io.IOException;

import javax.inject.Inject;

public class ResourceManagerRequestHandler extends AbstractResponseGenerator {

    SchemaFactory schemaFactory;
    ResourceManagerLocator resourceManagerLocator;

    @Override
    protected void generate(ApiRequest request) throws IOException {
        ResourceManager manager = resourceManagerLocator.getResourceManager(request);

        if ( manager == null ) {
            return;
        }

        Object response = null;
        if ( Method.POST.isMethod(request.getMethod()) ) {
            response = manager.create(request.getType(), request);
        } else if ( Method.GET.isMethod(request.getMethod()) ){
            if ( request.getId() != null && request.getLink() == null ) {
                response = manager.getById(request.getType(), request.getId());
            } else if ( request.getType() != null && request.getLink() != null ) {
                response = manager.getLink(request.getType(), request.getId(), request.getLink(), request);
            } else if ( request.getType() != null ) {
                response = manager.list(request.getType(), request);
            }
        }
        request.setResponseObject(response);
    }

    public SchemaFactory getSchemaFactory() {
        return schemaFactory;
    }

    @Inject
    public void setSchemaFactory(SchemaFactory schemaFactory) {
        this.schemaFactory = schemaFactory;
    }

    public ResourceManagerLocator getResourceManagerLocator() {
        return resourceManagerLocator;
    }

    @Inject
    public void setResourceManagerLocator(ResourceManagerLocator resourceManagerLocator) {
        this.resourceManagerLocator = resourceManagerLocator;
    }

}