package io.github.ibuildthecloud.model.impl;

import io.github.ibuildthecloud.gdapi.context.ApiContext;
import io.github.ibuildthecloud.gdapi.model.Resource;
import io.github.ibuildthecloud.url.UrlBuilder;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ResourceImpl implements Resource {

    String id, type;
    Map<String, URL> links = new HashMap<String, URL>();
    Map<String, URL> actions = new HashMap<String, URL>();
    Map<String, Object> fields = new HashMap<String, Object>();

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Map<String, URL> getLinks() {
        if ( ! links.containsKey(UrlBuilder.SELF) ) {
            links.put(UrlBuilder.SELF, ApiContext.getUrlBuilder().resourceReferenceLink(this));
        }
        return links;
    }

    @Override
    public Map<String, URL> getActions() {
        return actions;
    }

    @Override
    public Map<String, Object> getFields() {
        return fields;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLinks(Map<String, URL> links) {
        this.links = links;
    }

    public void setActions(Map<String, URL> actions) {
        this.actions = actions;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

}
