package io.github.ibuildthecloud.gdapi.request.handler;

import io.github.ibuildthecloud.gdapi.factory.SchemaFactory;
import io.github.ibuildthecloud.gdapi.request.ApiRequest;
import io.github.ibuildthecloud.gdapi.request.resource.ResourceManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class ResourceManagerRequestHandler extends AbstractResponseGenerator {

    SchemaFactory schemaFactory;
    List<ResourceManager> resourceManager;
    ResourceManager defaultResourceManager;
    Map<String,ResourceManager> resourceManagerByType = new HashMap<String, ResourceManager>();
    Map<Class<?>,ResourceManager> resourceManagerByClass = new HashMap<Class<?>, ResourceManager>();

    @Override
    protected void generate(ApiRequest request) throws IOException {
        ResourceManager manager = resourceManagerByType.get(request.getType());

        if ( manager == null ) {
            Class<?> schemaCls = schemaFactory.getSchemaClass(request.getType());
            manager = resourceManagerByClass.get(schemaCls);
        }

        if ( manager == null ) {
            manager = defaultResourceManager; 
        }

        if ( manager == null ) {
            return;
        }

        if ( request.getId() != null ) {
            request.setResponseObject(manager.getById(request.getId(), request));
        } else if ( request.getType() != null ) {
            request.setResponseObject(manager.list(request.getType(), request));
        }
    }

    @PostConstruct
    public void init() {
        for ( ResourceManager rm : resourceManager ) {
            for ( String type : rm.getTypes() ) {
                resourceManagerByType.put(type, rm);
            }
            for ( Class<?> clz : rm.getTypeClasses() ) {
                resourceManagerByClass.put(clz, rm);
            }
        }
    }

    public List<ResourceManager> getResourceManager() {
        return resourceManager;
    }

    @Inject
    public void setResourceManager(List<ResourceManager> resourceManager) {
        this.resourceManager = resourceManager;
    }

    public ResourceManager getDefaultResourceManager() {
        return defaultResourceManager;
    }

    public void setDefaultResourceManager(ResourceManager defaultResourceManager) {
        this.defaultResourceManager = defaultResourceManager;
    }

}