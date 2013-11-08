package io.github.ibuildthecloud.gdapi.model;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlTransient;

public interface Schema extends Resource {

    public enum Method {
        GET, PUT, DELETE, POST
    }

    @XmlTransient
    String getPluralName();

    List<String> getResourceMethods();

    Map<String, Field> getResourceFields();

    Map<String, Action> getResourceActions();

    List<String> getCollectionMethods();

    Map<String, Action> getCollectionActions();

    Map<String, Field> getCollectionFields();

    Map<String, Filter> getCollectionFilters();

}
