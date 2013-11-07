package io.github.ibuildthecloud.gdapi.request.impl;

import io.github.ibuildthecloud.gdapi.request.ApiRequestHandler;
import io.github.ibuildthecloud.gdapi.server.model.ApiRequest;

public class NoOpApiRequestHandler implements ApiRequestHandler {

    @Override
    public void handle(ApiRequest request) {
    }

    @Override
    public boolean handleException(ApiRequest request, Throwable e) {
        return false;
    }

}
