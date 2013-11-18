package io.github.ibuildthecloud.gdapi.request.resource;

import io.github.ibuildthecloud.gdapi.request.ApiRequest;

public interface ResourceManager {

    String[] getTypes();

    Class<?>[] getTypeClasses();

    Object getById(String id, ApiRequest request);

    Object list(String type, ApiRequest request);

    Object create(String type, ApiRequest request);

}
