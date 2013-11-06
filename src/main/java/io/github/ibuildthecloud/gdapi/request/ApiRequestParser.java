package io.github.ibuildthecloud.gdapi.request;

import io.github.ibuildthecloud.gdapi.server.model.ApiRequest;

public interface ApiRequestParser {

    boolean parse(ApiRequest apiRequest);

}
