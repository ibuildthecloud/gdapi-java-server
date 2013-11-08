package io.github.ibuildthecloud.gdapi.context;

import io.github.ibuildthecloud.gdapi.factory.SchemaFactory;
import io.github.ibuildthecloud.gdapi.request.ApiRequest;
import io.github.ibuildthecloud.gdapi.url.DefaultUrlBuilder;
import io.github.ibuildthecloud.gdapi.url.NullUrlBuilder;
import io.github.ibuildthecloud.url.UrlBuilder;

public class ApiContext {

    private static final ThreadLocal<ApiContext> TL = new ThreadLocal<ApiContext>();

    ApiRequest apiRequest;
    SchemaFactory schemaFactory;

    protected ApiContext() {
        super();
    }

    public static ApiContext getContext() {
        return TL.get();
    }

    public static ApiContext newContext() {
        ApiContext context = new ApiContext();
        TL.set(context);
        return context;
    }

    public static void remove() {
        TL.remove();
    }

    public static UrlBuilder getUrlBuilder() {
        ApiContext context = ApiContext.getContext();
        if ( context != null ) {
            UrlBuilder writer = context.getApiRequest().getUrlBuilder();
            if ( writer == null ) {
                writer = new DefaultUrlBuilder(context.getApiRequest(), context.getSchemaFactory());
                context.getApiRequest().setUrlBuilder(writer);
            }
            return writer;
        }
        return new NullUrlBuilder();
    }

    public ApiRequest getApiRequest() {
        return apiRequest;
    }

    public void setApiRequest(ApiRequest apiRequest) {
        this.apiRequest = apiRequest;
    }

    public SchemaFactory getSchemaFactory() {
        return schemaFactory;
    }

    public void setSchemaFactory(SchemaFactory schemaFactory) {
        this.schemaFactory = schemaFactory;
    }

}
