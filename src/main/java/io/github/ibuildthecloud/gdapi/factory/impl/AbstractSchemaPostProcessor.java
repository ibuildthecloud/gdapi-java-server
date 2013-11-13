package io.github.ibuildthecloud.gdapi.factory.impl;

import io.github.ibuildthecloud.model.impl.SchemaImpl;

public abstract class AbstractSchemaPostProcessor implements SchemaPostProcessor {

    @Override
    public SchemaImpl postProcessRegister(SchemaImpl schema, SchemaFactoryImpl factory) {
        return schema;
    }

    @Override
    public SchemaImpl postProcess(SchemaImpl schema, SchemaFactoryImpl factory) {
        return schema;
    }

}
