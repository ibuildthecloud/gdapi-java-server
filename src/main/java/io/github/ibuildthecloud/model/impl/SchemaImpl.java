package io.github.ibuildthecloud.model.impl;

import io.github.ibuildthecloud.gdapi.context.ApiContext;
import io.github.ibuildthecloud.gdapi.model.Action;
import io.github.ibuildthecloud.gdapi.model.Field;
import io.github.ibuildthecloud.gdapi.model.Filter;
import io.github.ibuildthecloud.gdapi.model.Schema;
import io.github.ibuildthecloud.gdapi.util.TypeUtils;
import io.github.ibuildthecloud.url.UrlBuilder;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlTransient;

public class SchemaImpl extends ResourceImpl implements Schema {

    String name;
    String pluralName;
    boolean create, update, list = true, deletable, byId = true;
    Map<String, Field> resourceFields;

    public SchemaImpl() {
        setType("schema");
    }

    public String getId() {
        return name;
    }

    public void setId(String name) {
        this.name = name;
    }

    public Map<String, Field> getResourceFields() {
        return resourceFields;
    }

    public void setResourceFields(Map<String, Field> resourceFields) {
        this.resourceFields = resourceFields;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlTransient
    public boolean isCreate() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }

    @XmlTransient
    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    @XmlTransient
    public boolean isList() {
        return list;
    }

    public void setList(boolean list) {
        this.list = list;
    }

    public boolean isDeletable() {
        return deletable;
    }

    @XmlTransient
    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    @XmlTransient
    public boolean isById() {
        return byId;
    }

    public void setById(boolean byId) {
        this.byId = byId;
    }

    public void setResourceMethods(List<String> resourceMethods) {
        if ( resourceMethods == null ) {
            byId = false;
            update = false;
            deletable = false;
            return;
        }

        byId = resourceMethods.contains(Method.GET.toString());
        update = resourceMethods.contains(Method.PUT.toString());
        deletable = resourceMethods.contains(Method.DELETE.toString());
    }

    public List<String> getResourceMethods() {
        List<String> methods = new ArrayList<String>();

        if ( byId ) {
            methods.add(Method.GET.toString());
        }

        if ( update ) {
            methods.add(Method.PUT.toString());
        }

        if ( deletable ) {
            methods.add(Method.DELETE.toString());
        }

        return methods;
    }

    public void setCollectionMethods(List<String> collectionMethods) {
        if ( collectionMethods == null ) {
            list = false;
            create = false;
            return;
        }

        list = collectionMethods.contains(Method.GET.toString());
        create = collectionMethods.contains(Method.POST.toString()); 
    }

    public List<String> getCollectionMethods() {
        List<String> methods = new ArrayList<String>();

        if ( list ) {
            methods.add(Method.GET.toString());
        }

        if ( create ) {
            methods.add(Method.POST.toString());
        }

        return methods;
    }

    @Override
    public Map<String, URL> getLinks() {
        Map<String,URL> result = links;
        if ( ! links.containsKey(UrlBuilder.SELF) ) {
            result = new HashMap<String, URL>(links);
            result.put(UrlBuilder.SELF, ApiContext.getUrlBuilder().resourceLink(this));
        }

        if ( list && ! result.containsKey(UrlBuilder.COLLECTION) ) {
            result = result == null ? new HashMap<String, URL>(links) : result;
            result.put(UrlBuilder.COLLECTION, ApiContext.getUrlBuilder().resourceCollection(getId()));
        }

        return result == null ? links : result;
    }

    @Override
    public Map<String, Action> getResourceActions() {
        // TODO Auto-generated method stub
        return new HashMap<String, Action>();
    }

    @Override
    public Map<String, Action> getCollectionActions() {
        // TODO Auto-generated method stub
        return new HashMap<String, Action>();
    }

    @Override
    public Map<String, Field> getCollectionFields() {
        // TODO Auto-generated method stub
        return new HashMap<String, Field>();
    }

    @Override
    public Map<String, Filter> getCollectionFilters() {
        // TODO Auto-generated method stub
        return new HashMap<String, Filter>();
    }

    @Override
    @XmlTransient
    public String getPluralName() {
        if ( pluralName == null )
            return TypeUtils.guessPluralName(name);
        return pluralName;
    }

    public void setPluralName(String pluralName) {
        this.pluralName = pluralName;
    }

    @XmlTransient
    public String getRawPluralName() {
        return pluralName;
    }

}