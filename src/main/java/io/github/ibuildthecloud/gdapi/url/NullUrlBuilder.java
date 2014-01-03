package io.github.ibuildthecloud.gdapi.url;

import java.net.URL;

import io.github.ibuildthecloud.gdapi.model.Resource;
import io.github.ibuildthecloud.gdapi.model.Sort.SortOrder;
import io.github.ibuildthecloud.url.UrlBuilder;

public class NullUrlBuilder implements UrlBuilder {

    @Override
    public URL resourceReferenceLink(Resource resource) {
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

    @Override
    public URL resourceLink(Resource resource, String name) {
        return null;
    }

    @Override
    public URL reverseSort(SortOrder currentOrder) {
        return null;
    }

    @Override
    public URL sort(String field) {
        return null;
    }

    @Override
    public URL next(Long id) {
        return null;
    }

    @Override
    public URL version(String version) {
        return null;
    }

    @Override
    public URL current() {
        return null;
    }

    @Override
    public URL resourceReferenceLink(Class<?> type, String id) {
        return null;
    }

    @Override
    public URL actionLink(Resource resource, String name) {
        return null;
    }

}
