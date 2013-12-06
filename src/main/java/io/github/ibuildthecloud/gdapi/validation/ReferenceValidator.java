package io.github.ibuildthecloud.gdapi.validation;

public interface ReferenceValidator {

    Object getById(String type, String id);

    Object getByField(String type, String fieldName, Object value);

}
