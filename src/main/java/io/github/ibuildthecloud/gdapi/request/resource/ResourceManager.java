package io.github.ibuildthecloud.gdapi.request.resource;

import io.github.ibuildthecloud.gdapi.request.ApiRequest;

public interface ResourceManager {

    String[] getTypes();

    Object getById(String id, ApiRequest request);

    Object list(String type, ApiRequest request);

}
