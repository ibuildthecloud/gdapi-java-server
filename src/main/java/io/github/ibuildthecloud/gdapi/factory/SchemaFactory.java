package io.github.ibuildthecloud.gdapi.factory;

import java.util.List;

import io.github.ibuildthecloud.gdapi.model.Schema;

public interface SchemaFactory {

    List<Schema> listSchemas();

    Schema getSchema(Class<?> clz);

    Schema getSchema(String type);

    Class<?> getSchemaClass(String type);

    String getPluralName(String type);

    String getSingularName(String type);

    Schema registerSchema(Object obj);

    void init();

    boolean typeStringMatches(Class<?> clz, String type);

}
