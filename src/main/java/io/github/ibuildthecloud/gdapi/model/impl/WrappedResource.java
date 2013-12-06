package io.github.ibuildthecloud.gdapi.model.impl;

import io.github.ibuildthecloud.gdapi.id.IdFormatter;
import io.github.ibuildthecloud.gdapi.id.IdFormatterUtils;
import io.github.ibuildthecloud.gdapi.model.Field;
import io.github.ibuildthecloud.gdapi.model.FieldType;
import io.github.ibuildthecloud.gdapi.model.Resource;
import io.github.ibuildthecloud.gdapi.model.Schema;
import io.github.ibuildthecloud.gdapi.util.TypeUtils;
import io.github.ibuildthecloud.model.impl.ResourceImpl;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlTransient;

public class WrappedResource extends ResourceImpl implements Resource {

    Schema schema;
    Object obj;
    Map<String, Object> fields = new LinkedHashMap<String, Object>();
    Map<String, Object> additionalFields;
    boolean createTsFields = true;
    IdFormatter idFormatter;

    public WrappedResource(IdFormatter idFormatter, Schema schema, Object obj, Map<String, Object> additionalFields) {
        super();
        this.schema = schema;
        this.obj = obj;
        this.idFormatter = idFormatter;
        this.additionalFields = additionalFields;
        init();
    }

    public WrappedResource(IdFormatter idFormatter, Schema schema, Object obj) {
        this(idFormatter, schema, obj, new HashMap<String,Object>());
    }

    protected void init() {
        for ( Map.Entry<String,Field> entry : schema.getResourceFields().entrySet() ) {
            String name = entry.getKey();
            if ( name.equals(TypeUtils.ID_FIELD) ) {
                continue;
            }
            Field field = entry.getValue();
            Object value = additionalFields.remove(name);
            if ( value == null ) {
                value = field.getValue(obj);
            }
            fields.put(name, IdFormatterUtils.formatReference(field, idFormatter, value));
            if ( createTsFields && field.getTypeEnum() == FieldType.DATE && value instanceof Date ) {
                fields.put(name + "TS", ((Date)value).getTime());
            }
        }

        for ( Map.Entry<String, Object> entry : additionalFields.entrySet() ) {
            fields.put(entry.getKey(), entry.getValue());
        }

        setId(idFormatter.formatId(getType(), getIdValue()));
    }

    protected Object getIdValue() {
        Field idField = schema.getResourceFields().get(TypeUtils.ID_FIELD);
        return idField == null ? null : idField.getValue(obj);
    }

    @Override
    public String getType() {
        return schema.getId();
    }

    @Override
    public Map<String, Object> getFields() {
        return fields;
    }

    @XmlTransient
    public boolean isCreateTsFields() {
        return createTsFields;
    }

    public void setCreateTsFields(boolean createTsFields) {
        this.createTsFields = createTsFields;
    }

}
