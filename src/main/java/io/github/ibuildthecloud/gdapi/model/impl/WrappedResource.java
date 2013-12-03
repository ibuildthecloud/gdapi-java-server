package io.github.ibuildthecloud.gdapi.model.impl;

import io.github.ibuildthecloud.gdapi.model.Field;
import io.github.ibuildthecloud.gdapi.model.Resource;
import io.github.ibuildthecloud.gdapi.model.Schema;
import io.github.ibuildthecloud.gdapi.model.Field.Type;
import io.github.ibuildthecloud.model.impl.ResourceImpl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WrappedResource extends ResourceImpl implements Resource {

    Schema schema;
    Object obj;
    Map<String, Object> fields = new HashMap<String, Object>();

    public WrappedResource(Schema schema, Object obj) {
        super();
        this.schema = schema;
        this.obj = obj;
        init();
    }

    protected void init() {
        for ( Map.Entry<String,Field> entry : schema.getResourceFields().entrySet() ) {
            String name = entry.getKey();
            Field field = entry.getValue();
            Object value = field.getValue(obj);
            fields.put(name, value);
            if ( field.getTypeEnum() == Type.DATE && value instanceof Date ) {
                fields.put(name + "TS", ((Date)value).getTime());
            }
        }
    }

    @Override
    public String getId() {
        Field idField = schema.getResourceFields().get("id");
        Object result = idField == null ? null : idField.getValue(obj);
        return result == null ? null : result.toString();
    }

    @Override
    public String getType() {
        return schema.getId();
    }

    @Override
    public Map<String, Object> getFields() {
        return fields;
    }

}
