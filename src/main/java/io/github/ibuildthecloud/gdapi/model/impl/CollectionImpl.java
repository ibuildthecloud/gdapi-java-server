package io.github.ibuildthecloud.gdapi.model.impl;

import io.github.ibuildthecloud.gdapi.model.Collection;
import io.github.ibuildthecloud.gdapi.model.Resource;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CollectionImpl implements Collection {

    String type = "collection";
    String resourceType;
    Map<String, URL> links = new HashMap<String, URL>();
    Map<String, URL> createTypes = new HashMap<String, URL>();
    Map<String, URL> actions = new HashMap<String, URL>();
    List<Resource> data = new LinkedList<Resource>();

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getResourceType() {
        return resourceType;
    }

    @Override
    public Map<String, URL> getLinks() {
        return links;
    }

    @Override
    public List<Resource> getData() {
        return data;
    }

    @Override
    public Map<String, URL> getCreateTypes() {
        return createTypes;
    }

    @Override
    public Map<String, URL> getActions() {
        return actions;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public void setLinks(Map<String, URL> links) {
        this.links = links;
    }

    public void setCreateTypes(Map<String, URL> createTypes) {
        this.createTypes = createTypes;
    }

    public void setActions(Map<String, URL> actions) {
        this.actions = actions;
    }

    public void setData(List<Resource> data) {
        this.data = data;
    }

}
