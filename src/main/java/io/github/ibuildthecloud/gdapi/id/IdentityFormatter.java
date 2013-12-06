package io.github.ibuildthecloud.gdapi.id;

public class IdentityFormatter implements IdFormatter {

    @Override
    public String formatId(String type, Object id) {
        return id == null ? null : id.toString();
    }

    @Override
    public String parseId(String id) {
        return id;
    }

}
