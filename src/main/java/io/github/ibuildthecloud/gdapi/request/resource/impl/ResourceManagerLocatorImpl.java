package io.github.ibuildthecloud.gdapi.request.resource.impl;

import io.github.ibuildthecloud.gdapi.factory.SchemaFactory;
import io.github.ibuildthecloud.gdapi.request.ApiRequest;
import io.github.ibuildthecloud.gdapi.request.resource.ResourceManager;
import io.github.ibuildthecloud.gdapi.request.resource.ResourceManagerLocator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class ResourceManagerLocatorImpl implements ResourceManagerLocator {

    SchemaFactory schemaFactory;
    List<ResourceManager> resourceManager;
    ResourceManager defaultResourceManager;
    Map<String, ResourceManager> resourceManagerByType = new HashMap<String, ResourceManager>();
    Map<Class<?>, ResourceManager> resourceManagerByClass = new HashMap<Class<?>, ResourceManager>();

    @Override
    public ResourceManager getResourceManager(ApiRequest request) {
        if ( request.getType() == null )
            return null;

        ResourceManager manager = resourceManagerByType.get(request.getType());

        if (manager == null) {
            Class<?> schemaCls = schemaFactory.getSchemaClass(request.getType());
            manager = resourceManagerByClass.get(schemaCls);
        }

        if (manager == null) {
            manager = defaultResourceManager;
        }

        return manager;
    }

    @Override
    public ResourceManager getResourceManagerByType(String type) {
        if ( type == null )
            return null;

        ResourceManager manager = resourceManagerByType.get(type);

        if (manager == null) {
            manager = defaultResourceManager;
        }

        return manager;
    }


    @PostConstruct
    public void init() {
        for (ResourceManager rm : resourceManager) {
            for (String type : rm.getTypes()) {
                resourceManagerByType.put(type, rm);
            }
            for (Class<?> clz : rm.getTypeClasses()) {
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

    public SchemaFactory getSchemaFactory() {
        return schemaFactory;
    }

    @Inject
    public void setSchemaFactory(SchemaFactory schemaFactory) {
        this.schemaFactory = schemaFactory;
    }

}
