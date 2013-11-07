package io.github.ibuildthecloud.model.impl;

import io.github.ibuildthecloud.gdapi.model.Action;
import io.github.ibuildthecloud.gdapi.model.Field;
import io.github.ibuildthecloud.gdapi.model.Filter;
import io.github.ibuildthecloud.gdapi.model.Schema;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SchemaImpl implements Schema {

    String name;
    String type = "schema";
    boolean create, update, list = true, deletable, byId = true;
    Map<String, Field> resourceFields;

    public String getId() {
        return name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
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

    public boolean isCreate() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public boolean isList() {
        return list;
    }

    public void setList(boolean list) {
        this.list = list;
    }

    public boolean isDeletable() {
        return deletable;
    }

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    public boolean isById() {
        return byId;
    }

    public void setById(boolean byId) {
        this.byId = byId;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, URL> getActions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Action> getResourceActions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Action> getCollectionActions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Field> getCollectionFields() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Filter> getCollectionFilters() {
        // TODO Auto-generated method stub
        return null;
    }


}
