package io.github.ibuildthecloud.gdapi.request.resource.impl;

import io.github.ibuildthecloud.gdapi.factory.SchemaFactory;
import io.github.ibuildthecloud.gdapi.request.ApiRequest;
import io.github.ibuildthecloud.gdapi.request.resource.ResourceManager;
import io.github.ibuildthecloud.gdapi.request.resource.ResourceManagerFilter;
import io.github.ibuildthecloud.gdapi.request.resource.ResourceManagerLocator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class ResourceManagerLocatorImpl implements ResourceManagerLocator {

    SchemaFactory schemaFactory;
    List<ResourceManager> resourceManager;
    List<ResourceManagerFilter> resourceManagerFilters = Collections.emptyList();
    ResourceManager defaultResourceManager;
    Map<String, List<ResourceManagerFilter>> resourceManagerFiltersByType = new HashMap<String, List<ResourceManagerFilter>>();
    Map<String, ResourceManager> resourceManagerByType = new HashMap<String, ResourceManager>();
    Map<Object, ResourceManager> cached = new ConcurrentHashMap<Object, ResourceManager>();

    @Override
    public ResourceManager getResourceManager(ApiRequest request) {
        if ( request.getType() == null )
            return null;

        return getResourceManagerByType(request.getType());
    }

    @Override
    public ResourceManager getResourceManagerByType(String type) {
        ResourceManager rm = cached.get(type);
        if ( rm != null ) {
            return rm;
        }

        rm = resourceManagerByType.get(type);

        if ( rm == null ) {
            rm = defaultResourceManager;
        }

        if ( rm == null ) {
            return rm;
        }

        List<ResourceManagerFilter> filters = resourceManagerFiltersByType.get(type);
        if ( filters == null ) {
            return rm;
        }

        rm = wrap(filters, rm);
        cached.put(type, rm);

        return rm;
    }

    protected ResourceManager wrap(List<ResourceManagerFilter> filters, ResourceManager resourceManager) {
        if ( filters.size() == 0 ) {
            return resourceManager;
        }

        List<ResourceManagerFilter> subList = new ArrayList<ResourceManagerFilter>();
        return new FilteredResourceManager(filters.get(0), wrap(subList, resourceManager));
    }

    @PostConstruct
    public void init() {
        for (ResourceManager rm : resourceManager) {
            for (String type : rm.getTypes()) {
                resourceManagerByType.put(type, rm);
            }
            for (Class<?> clz : rm.getTypeClasses()) {
                String type = schemaFactory.getSchemaName(clz);
                if ( type != null )
                    resourceManagerByType.put(type, rm);
            }
        }

        for ( ResourceManagerFilter filter : resourceManagerFilters ) {
            for (String type : filter.getTypes()) {
                add(resourceManagerFiltersByType, type, filter);
            }
            for (Class<?> clz : filter.getTypeClasses()) {
                String type = schemaFactory.getSchemaName(clz);
                if ( type != null )
                    add(resourceManagerFiltersByType, type, filter);
            }
        }
    }

    protected void add(Map<String, List<ResourceManagerFilter>> filters, String key, ResourceManagerFilter filter) {
        List<ResourceManagerFilter> list = filters.get(key);
        if ( list == null ) {
            list = new ArrayList<ResourceManagerFilter>();
            filters.put(key, list);
        }

        list.add(filter);
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

    public Map<String, List<ResourceManagerFilter>> getResourceManagerFiltersByType() {
        return resourceManagerFiltersByType;
    }

    public void setResourceManagerFiltersByType(Map<String, List<ResourceManagerFilter>> resourceManagerFiltersByType) {
        this.resourceManagerFiltersByType = resourceManagerFiltersByType;
    }

    public Map<String, ResourceManager> getResourceManagerByType() {
        return resourceManagerByType;
    }

    public void setResourceManagerByType(Map<String, ResourceManager> resourceManagerByType) {
        this.resourceManagerByType = resourceManagerByType;
    }

    public List<ResourceManagerFilter> getResourceManagerFilters() {
        return resourceManagerFilters;
    }

    @Inject
    public void setResourceManagerFilters(List<ResourceManagerFilter> resourceManagerFilters) {
        this.resourceManagerFilters = resourceManagerFilters;
    }

}
