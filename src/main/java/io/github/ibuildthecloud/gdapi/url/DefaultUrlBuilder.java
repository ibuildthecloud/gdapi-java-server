package io.github.ibuildthecloud.gdapi.url;

import io.github.ibuildthecloud.gdapi.factory.SchemaFactory;
import io.github.ibuildthecloud.gdapi.model.Resource;
import io.github.ibuildthecloud.gdapi.model.Schema;
import io.github.ibuildthecloud.gdapi.request.ApiRequest;
import io.github.ibuildthecloud.url.UrlBuilder;

import java.net.MalformedURLException;
import java.net.URL;

public final class DefaultUrlBuilder implements UrlBuilder {

    ApiRequest apiRequest;
    SchemaFactory schemaFactory;

    public DefaultUrlBuilder(ApiRequest apiRequest, SchemaFactory schemaFactory) {
        this.apiRequest = apiRequest;
        this.schemaFactory = schemaFactory;
    }

    @Override
    public URL resourceLink(Resource resource) {
        return constructBasicUrl(getPluralName(resource), resource.getId());
    }

    protected String getPluralName(Resource resource) {
        return getPluralName(resource.getType());
    }

    protected String getPluralName(String type) {
        return schemaFactory.getPluralName(type);
    }

    protected URL constructBasicUrl(String... parts) {
        StringBuilder builder = new StringBuilder()
            .append(apiRequest.getResponseUrlBase())
            .append("/")
            .append(apiRequest.getApiVersion());

        for ( String part : parts ) {
            if ( part == null )
                return null;
            builder.append("/").append(part);
        }

        try {
            return new URL(builder.toString());
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        } 
    }

    @Override
    public URL resourceCollection(Class<?> type) {
        Schema schema = schemaFactory.getSchema(type);
        return schema == null ? null : constructBasicUrl(getPluralName(schema));
    }

    @Override
    public URL resourceCollection(String type) {
        String plural = getPluralName(type);
        return plural == null ? null : constructBasicUrl(getPluralName(type));
    }

}
