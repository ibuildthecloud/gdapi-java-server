package io.github.ibuildthecloud.gdapi.validation;

import io.github.ibuildthecloud.gdapi.annotation.Field;

public class ParentType {

    SubType subType;

    @Field(create = true)
    public SubType getSubType() {
        return subType;
    }

    public void setSubType(SubType subType) {
        this.subType = subType;
    }

}
