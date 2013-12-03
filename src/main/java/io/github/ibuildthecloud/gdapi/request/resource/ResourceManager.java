package io.github.ibuildthecloud.gdapi.request.resource;

import io.github.ibuildthecloud.gdapi.model.Collection;
import io.github.ibuildthecloud.gdapi.model.Resource;
import io.github.ibuildthecloud.gdapi.request.ApiRequest;

import java.util.List;
import java.util.Map;

public interface ResourceManager {

    String[] getTypes();

    Class<?>[] getTypeClasses();

    Object getById(String type, String id);

    Object getLink(String type, String id, String link, ApiRequest request);

    Object list(String type, ApiRequest request);

    Object list(String type, Map<Object,Object> criteria);

    Object create(String type, ApiRequest request);

    Collection convertResponse(List<?> object, ApiRequest request);

    Resource convertResponse(Object obj, ApiRequest request);

}
