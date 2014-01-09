package io.github.ibuildthecloud.gdapi.factory;

import java.util.List;

import io.github.ibuildthecloud.gdapi.factory.impl.SchemaPostProcessor;
import io.github.ibuildthecloud.gdapi.model.Schema;

public interface SchemaFactory {

    String getId();

    List<Schema> listSchemas();

    String getSchemaName(Class<?> clz);

    String getSchemaName(String type);

    Schema getSchema(Class<?> clz);

    Schema getSchema(String type);

    Class<?> getSchemaClass(String type, boolean resolveParent);

    Class<?> getSchemaClass(String type);

    Class<?> getSchemaClass(Class<?> type);

    String getPluralName(String type);

    String getSingularName(String type);

    String getBaseType(String type);

    Schema registerSchema(Object obj);

    Schema parseSchema(String name);

    List<String> getSchemaNames(Class<?> clz);

    boolean typeStringMatches(Class<?> clz, String type);

    void addPostProcessor(SchemaPostProcessor postProcessor);

}
