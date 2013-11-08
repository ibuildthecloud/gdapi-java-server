package io.github.ibuildthecloud.gdapi.url;

import java.net.URL;

import io.github.ibuildthecloud.gdapi.model.Resource;
import io.github.ibuildthecloud.url.UrlBuilder;

public class NullUrlBuilder implements UrlBuilder {

    @Override
    public URL resourceLink(Resource resource) {
        return null;
    }

    @Override
    public URL resourceCollection(Class<?> type) {
        return null;
    }

    @Override
    public URL resourceCollection(String type) {
        return null;
    }

}
