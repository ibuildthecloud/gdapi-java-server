package io.github.ibuildthecloud.gdapi.request.handler;

import io.github.ibuildthecloud.gdapi.exception.ResourceNotFoundException;
import io.github.ibuildthecloud.gdapi.factory.SchemaFactory;
import io.github.ibuildthecloud.gdapi.model.Schema;
import io.github.ibuildthecloud.gdapi.request.ApiRequest;

import java.io.IOException;

import javax.inject.Inject;

public class SchemasHandler extends AbstractResponseGenerator {

    SchemaFactory schemaFactory;

    @Override
    protected void generate(ApiRequest request) throws IOException {
        if ( ! schemaFactory.typeStringMatches(Schema.class, request.getType()) )
            return;

        if ( request.getId() == null ) {
            request.setResponseObject(schemaFactory.listSchemas());
        } else {
            Schema lookup = schemaFactory.getSchema(request.getId());
            if ( lookup == null ) {
                throw new ResourceNotFoundException();
            }
            request.setResponseObject(lookup);
        }
    }

    public SchemaFactory getSchemaFactory() {
        return schemaFactory;
    }

    @Inject
    public void setSchemaFactory(SchemaFactory schemaFactory) {
        this.schemaFactory = schemaFactory;
    }

}
