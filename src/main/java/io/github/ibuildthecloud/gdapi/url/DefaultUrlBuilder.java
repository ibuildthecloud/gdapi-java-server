package io.github.ibuildthecloud.gdapi.url;

import io.github.ibuildthecloud.gdapi.context.ApiContext;
import io.github.ibuildthecloud.gdapi.factory.SchemaFactory;
import io.github.ibuildthecloud.gdapi.id.IdFormatter;
import io.github.ibuildthecloud.gdapi.model.Collection;
import io.github.ibuildthecloud.gdapi.model.Resource;
import io.github.ibuildthecloud.gdapi.model.Schema;
import io.github.ibuildthecloud.gdapi.model.Sort.SortOrder;
import io.github.ibuildthecloud.gdapi.request.ApiRequest;
import io.github.ibuildthecloud.url.UrlBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

public final class DefaultUrlBuilder implements UrlBuilder {

    private static final String REMOVE_PARAM_REGEXP = "&?%s=[^&]*";

    ApiRequest apiRequest;
    SchemaFactory schemaFactory;

    public DefaultUrlBuilder(ApiRequest apiRequest, SchemaFactory schemaFactory) {
        this.apiRequest = apiRequest;
        this.schemaFactory = schemaFactory;
    }

    @Override
    public URL resourceReferenceLink(Resource resource) {
        return constructBasicUrl(getPluralName(resource), resource.getId());
    }

    @Override
    public URL resourceReferenceLink(Class<?> type, String id) {
        IdFormatter formatter = ApiContext.getContext().getIdFormatter();

        Schema schema = schemaFactory.getSchema(type);
        return schema == null ? null : constructBasicUrl(schema.getPluralName(), formatter.formatId(schema.getId(), id));
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

        return toURL(builder.toString().toLowerCase());
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

    @Override
    public URL resourceLink(Resource resource, String name) {
        if ( name == null )
            return null;

        return constructBasicUrl(getPluralName(resource), resource.getId(), name.toLowerCase());
    }

    @Override
    public URL reverseSort(SortOrder currentOrder) {
        StringBuilder buffer = fullUrlToAppendQueryString(Collection.ORDER, Collection.MARKER);

        buffer.append(Collection.ORDER).append("=").append(currentOrder.getReverseExternalForm());

        return toURL(buffer.toString());
    }

    @Override
    public URL sort(String field) {
        StringBuilder buffer = fullUrlToAppendQueryString(Collection.SORT, Collection.ORDER);

        buffer.append(Collection.SORT).append("=").append(field);

        return toURL(buffer.toString());
    }

    @Override
    public URL next(Long id) {
        IdFormatter formatter = ApiContext.getContext().getIdFormatter();

        StringBuilder buffer = fullUrlToAppendQueryString(Collection.MARKER);

        buffer.append(Collection.MARKER).append("=").append(formatter.formatId(Collection.MARKER, id));

        return toURL(buffer.toString());
    }

    protected StringBuilder fullUrlToAppendQueryString(String... removes) {
        StringBuilder buffer = new StringBuilder(apiRequest.getRequestUrl());
        buffer.append("?");

        String queryString = removeParameter(apiRequest.getQueryString(), removes);
        buffer.append(queryString);

        if ( queryString.length() > 0 ) {
            buffer.append("&");
        }

        return buffer;
    }

    protected URL toURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Failed to create URL for [" + url + "]", e);
        }
    }

    protected String removeParameter(String queryString, String... names) {
        if ( queryString == null )
            return "";

        for ( String name : names ) {
            String pattern = String.format(REMOVE_PARAM_REGEXP, Pattern.quote(name));
            queryString = queryString.replaceAll(pattern, "");
        }

        return queryString;
    }

    @Override
    public URL version(String version) {
        StringBuilder builder = new StringBuilder()
        .append(apiRequest.getResponseUrlBase())
        .append("/")
        .append(version);

        return toURL(builder.toString());
    }

    @Override
    public URL current() {
        return toURL(apiRequest.getRequestUrl());
    }

}
