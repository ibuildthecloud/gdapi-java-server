package io.github.ibuildthecloud.url;

import io.github.ibuildthecloud.gdapi.model.Resource;

import java.net.URL;

public interface UrlBuilder {

    public static final String SELF = "self";
    public static final String COLLECTION = "collection";

    URL resourceLink(Resource resource);

    URL resourceCollection(Class<?> type);

    URL resourceCollection(String type);

}
