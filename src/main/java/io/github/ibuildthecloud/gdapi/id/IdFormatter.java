package io.github.ibuildthecloud.gdapi.id;

public interface IdFormatter {
    String formatId(String type, Object id);

    String parseId(String id);
}
