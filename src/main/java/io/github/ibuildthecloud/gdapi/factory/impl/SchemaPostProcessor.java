package io.github.ibuildthecloud.gdapi.factory.impl;

import io.github.ibuildthecloud.model.impl.SchemaImpl;

public interface SchemaPostProcessor {

    SchemaImpl postProcessRegister(SchemaImpl schema, SchemaFactoryImpl factory);

    SchemaImpl postProcess(SchemaImpl schema, SchemaFactoryImpl factory);

}
