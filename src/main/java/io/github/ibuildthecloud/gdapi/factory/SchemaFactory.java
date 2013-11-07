package io.github.ibuildthecloud.gdapi.factory;

import io.github.ibuildthecloud.gdapi.model.Schema;

public interface SchemaFactory {

    Schema getSchema(Class<?> clz);

    Schema registerSchema(Class<?> clz);

    void init();

}
