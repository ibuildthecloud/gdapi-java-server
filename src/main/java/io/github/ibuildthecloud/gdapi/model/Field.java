package io.github.ibuildthecloud.gdapi.model;

import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlTransient;

public interface Field {

    public enum Type {
        STRING(String.class),
        PASSWORD,
        FLOAT(Float.class, Float.TYPE, Double.class, Double.TYPE), 
        INT(Integer.class, Integer.TYPE, Long.class, Long.TYPE), 
        DATE(Date.class),
        BLOB(OutputStream.class),
        BOOLEAN(Boolean.class, Boolean.TYPE),
        ENUM,
        REFERENCE(IdRef.class), 
        ARRAY(List.class, Object[].class),
        MAP(Map.class),
        TYPE(Object.class),
        RESOURCE(Object.class),
        NONE;
        
        Class<?>[] clzs;

        private Type(Class<?>... clzs) {
            this.clzs = clzs;
        }

        public Class<?>[] getClasses() {
            return clzs;
        }
        
        public String getExternalType() {
            return toString().toLowerCase();
        }
        
    }

    String getType();

    @XmlTransient
    @io.github.ibuildthecloud.gdapi.annotation.Field(include = false)
    Type getTypeEnum();
    
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
    
    @XmlTransient
    @io.github.ibuildthecloud.gdapi.annotation.Field(include = false)
    Class<?> getTypeClass();
    
    @XmlTransient
    @io.github.ibuildthecloud.gdapi.annotation.Field(include = false)
    Class<?> getSubTypeClass();
    
    @XmlTransient
    @io.github.ibuildthecloud.gdapi.annotation.Field(include = false)
    Type getSubTypeEnum();

}