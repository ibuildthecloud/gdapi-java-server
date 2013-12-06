package io.github.ibuildthecloud.gdapi.model;

import io.github.ibuildthecloud.gdapi.annotation.Type;

@Type(name = "error", list = false)
public interface ApiError {

    int getStatus();

    String getCode();

    String getMessage();

    String getDetail();

}
