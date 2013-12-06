package io.github.ibuildthecloud.gdapi.model;

import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

public interface Field {

    String getType();

    @XmlTransient
    @io.github.ibuildthecloud.gdapi.annotation.Field(include = false)
    FieldType getTypeEnum();

    String getDefault();

    boolean isUnique();

    boolean isNullable();

    boolean isCreate();

    boolean isRequired();

    boolean isUpdate();

    Long getMinLength();

    Long getMaxLength();

    Long getMin();

    Long getMax();

    List<String> getOptions();

    String getValidChars();

    String getInvalidChars();

    @XmlTransient
    @io.github.ibuildthecloud.gdapi.annotation.Field(include = false)
    boolean isIncludeInList();

    Object getValue(Object object);

    // @XmlTransient
    // @io.github.ibuildthecloud.gdapi.annotation.Field(include = false)
    // Class<?> getTypeClass();
    //
    // @XmlTransient
    // @io.github.ibuildthecloud.gdapi.annotation.Field(include = false)
    // Class<?> getSubTypeClass();

    @XmlTransient
    @io.github.ibuildthecloud.gdapi.annotation.Field(include = false)
    List<FieldType> getSubTypeEnums();

    @XmlTransient
    @io.github.ibuildthecloud.gdapi.annotation.Field(include = false)
    List<String> getSubTypes();

}