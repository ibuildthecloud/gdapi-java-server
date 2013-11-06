package io.github.ibuildthecloud.gdapi.context;

import io.github.ibuildthecloud.gdapi.factory.SchemaFactory;
import io.github.ibuildthecloud.gdapi.server.model.ApiRequest;

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
