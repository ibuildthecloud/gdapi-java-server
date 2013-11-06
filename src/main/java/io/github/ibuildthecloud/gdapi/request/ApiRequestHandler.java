package io.github.ibuildthecloud.gdapi.request;

import io.github.ibuildthecloud.gdapi.server.model.ApiRequest;

public interface ApiRequestHandler {

    void handle(ApiRequest request);

    boolean handleException(ApiRequest request, Throwable e);

}
