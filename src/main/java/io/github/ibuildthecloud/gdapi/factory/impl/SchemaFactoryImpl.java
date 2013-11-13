package io.github.ibuildthecloud.gdapi.factory.impl;

import io.github.ibuildthecloud.gdapi.factory.SchemaFactory;
import io.github.ibuildthecloud.gdapi.model.Field;
import io.github.ibuildthecloud.gdapi.model.Field.Type;
import io.github.ibuildthecloud.gdapi.model.Schema;
import io.github.ibuildthecloud.model.impl.FieldImpl;
import io.github.ibuildthecloud.model.impl.SchemaImpl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

@io.github.ibuildthecloud.gdapi.annotation.Type
public class SchemaFactoryImpl implements SchemaFactory {

    final io.github.ibuildthecloud.gdapi.annotation.Field defaultField;
    final io.github.ibuildthecloud.gdapi.annotation.Type defaultType;

    Map<String, SchemaImpl> schemasByName = new TreeMap<String, SchemaImpl>();
    Map<Class<?>, SchemaImpl> schemas = new HashMap<Class<?>, SchemaImpl>();
    Map<String, String> typeToPluralName = new HashMap<String, String>();
    Map<String, String> pluralNameToType = new HashMap<String, String>();
    Map<String, Class<?>> typeToClass = new HashMap<String, Class<?>>();
    List<Class<?>> types = new ArrayList<Class<?>>();
    List<String> typeNames = new ArrayList<String>();

    List<Schema> schemasList = new ArrayList<Schema>();
    List<SchemaPostProcessor> postProcessors = new ArrayList<SchemaPostProcessor>();

    public SchemaFactoryImpl() {
        try {
            defaultField = PropertyUtils.getPropertyDescriptor(this, "defaultField")
                    .getReadMethod().getAnnotation(io.github.ibuildthecloud.gdapi.annotation.Field.class);

            defaultType = this.getClass().getAnnotation(io.github.ibuildthecloud.gdapi.annotation.Type.class);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @io.github.ibuildthecloud.gdapi.annotation.Field
    public Object getDefaultField() {
        return null;
    }

    @Override
    public Schema registerSchema(Object obj) {
        Class<?> clz = obj instanceof Class<?> ? (Class<?>)obj : null;
        SchemaImpl schema = getSchemaName(obj);

        for ( SchemaPostProcessor processor : postProcessors ) {
            processor.postProcessRegister(schema, this);
        }

        /* Register in the multitude of maps */
        if ( clz != null ) {
            addToMap(typeToClass, schema, clz, true, true);
        }
        addToMap(schemasByName, schema, schema, true, true);
        addToMap(typeToPluralName, schema, schema.getPluralName(), true, false);
        addToMap(pluralNameToType, schema, schema.getId(), false, true);

        if ( clz != null ) {
            schemas.put(clz, schema);
            for ( Class<?> iface : clz.getInterfaces() ) {
                schemas.put(iface, schema);
            }
        }

        schemasList.add(schema);

        return schema;
    }

    protected <T> void addToMap(Map<String,T> map, SchemaImpl key, T value, boolean id, boolean plural) {
        if ( key == null || value == null )
            return;
        if ( id ) {
            map.put(key.getId(), value);
            map.put(key.getId().toLowerCase(), value);
        }
        if ( plural ) {
            map.put(key.getPluralName(), value);
            map.put(key.getPluralName().toLowerCase(), value);
        }
    }

    public Schema getSchema(Class<?> clz) {
        return schemas.get(clz);
    }

    protected Schema parseSchema(String name) {
        SchemaImpl schema = readSchema(name);
        Class<?> clz = typeToClass.get(name);
        
        List<FieldImpl> fields = getFields(clz);
        Map<String,Field> resourceFields = sortFields(fields);

        schema.setResourceFields(resourceFields);

        for ( SchemaPostProcessor processor : postProcessors ) {
            processor.postProcess(schema, this);
        }

        return schema;
    }

    protected SchemaImpl getSchemaName(Object obj) {
        Class<?> clz = obj instanceof Class<?> ? (Class<?>)obj : obj.getClass();
        SchemaImpl schema = new SchemaImpl();
        io.github.ibuildthecloud.gdapi.annotation.Type type = clz.getAnnotation(io.github.ibuildthecloud.gdapi.annotation.Type.class);

        if ( type == null )
            type = defaultType;

        if ( ! StringUtils.isEmpty(type.name()) ) {
            schema.setName(type.name());
        } else if ( obj instanceof String ) {
            schema.setName((String)obj);
        } else {
            schema.setName(StringUtils.uncapitalize(clz.getSimpleName()));
        }

        if ( type.pluralName().length() > 0 ) {
            schema.setPluralName(type.pluralName());
        }

        return schema;
    }

    protected SchemaImpl readSchema(String name) {
        Class<?> clz = typeToClass.get(name);
        if ( clz == null )
            clz = Object.class;

        SchemaImpl schema = schemasByName.get(name);
        if ( schema == null )
            schema = getSchemaName(clz);

        io.github.ibuildthecloud.gdapi.annotation.Type type = clz.getAnnotation(io.github.ibuildthecloud.gdapi.annotation.Type.class);

        if ( type == null )
            type = defaultType;

        schema.setCreate(type.create());
        schema.setUpdate(type.update());
        schema.setById(type.byId());
        schema.setList(type.list());
        schema.setDeletable(type.delete());

        return schema;
    }
    
    protected Map<String, Field> sortFields(List<FieldImpl> fields) {
        Map<Integer, FieldImpl> indexed = new TreeMap<Integer, FieldImpl>();
        Map<String, FieldImpl> named = new TreeMap<String, FieldImpl>();
        Map<String, Field> result = new LinkedHashMap<String, Field>();
        
        for ( FieldImpl field : fields ) {
            Integer displayIndex = field.getDisplayIndex();
            
            if ( displayIndex == null ) {
                named.put(field.getName(), field);
            } else {
                indexed.put(displayIndex, field);
            }                
        }
        
        for ( FieldImpl field : indexed.values() ) {
            result.put(field.getName(), field);
        }
        
        for ( FieldImpl field : named.values() ) {
            result.put(field.getName(), field);
        }
        
        return result;
    }

    protected List<FieldImpl> getFields(Class<?> clz) {
        List<FieldImpl> result = new ArrayList<FieldImpl>();

        if ( clz == null )
            return result;

        for ( PropertyDescriptor prop : PropertyUtils.getPropertyDescriptors(clz) ) {
            FieldImpl field = getField(clz, prop);
            if ( field != null ) {
                result.add(field);
            }
        }

        return result;
    }

    protected FieldImpl getField(Class<?> clz, PropertyDescriptor prop) {
        FieldImpl field = new FieldImpl();

        Method readMethod = prop.getReadMethod();
        Method writeMethod = prop.getWriteMethod();
        if ( readMethod == null && writeMethod == null )
            return null;

        io.github.ibuildthecloud.gdapi.annotation.Field f = getFieldAnnotation(prop);

        if ( ! f.include() )
            return null;

        field.setReadMethod(readMethod);
        
        if ( readMethod != null && readMethod.getDeclaringClass() != clz )
            return null;
        
        if ( StringUtils.isEmpty(f.name()) ) {
            field.setName(prop.getName());
        } else {
            field.setName(f.name());
        }
        
        if ( f.displayIndex() > 0 ) {
            field.setDisplayIndex(f.displayIndex());
        }
        
        if ( readMethod == null ) {
            field.setIncludeInList(false);
        }
        
        assignSimpleProps(field, f);
        assignType(prop, field, f);
        assignLengths(field, f);
        assignOptions(prop, field, f);
        
        if ( writeMethod == null ) {
            field.setCreate(false);
            field.setUpdate(false);
        }
        
        return field;
    }
    
    protected void assignOptions(PropertyDescriptor prop, FieldImpl field, io.github.ibuildthecloud.gdapi.annotation.Field f) {
        Class<?> clz = prop.getPropertyType();
        
        if ( ! clz.isEnum() ) {
            return;
        }
        
        List<String> options = new ArrayList<String>(clz.getEnumConstants().length);
        for ( Object o : clz.getEnumConstants() ) {
            options.add(o.toString());
        }
        
        field.setOptions(options);
    }
    
    protected void assignSimpleProps(FieldImpl field, io.github.ibuildthecloud.gdapi.annotation.Field f) {
        if ( ! StringUtils.isEmpty(f.defaultValue()) ) {
            field.setDefault(f.defaultValue());
        }
        
        if ( ! StringUtils.isEmpty(f.validChars()) ) {
            field.setValidChars(f.validChars());
        }
        
        if ( ! StringUtils.isEmpty(f.invalidChars()) ) {
            field.setInvalidChars(f.invalidChars());
        }
        
        field.setNullable(f.nullable());
        field.setUpdate(f.update());
        field.setCreate(f.create());
        field.setUnique(f.unique());
        field.setRequired(f.required());
    }

    protected void assignLengths(FieldImpl field, io.github.ibuildthecloud.gdapi.annotation.Field f) {
        if ( f.min() != Long.MIN_VALUE ) {
            field.setMin(f.min());
        }
        
        if ( f.max() != Long.MAX_VALUE ) {
            field.setMax(f.max());
        }
        
        if ( f.minLength() != Long.MIN_VALUE ) {
            field.setMinLength(f.minLength());
        }
        
        if ( f.maxLength() != Long.MAX_VALUE ) {
            field.setMaxLength(f.maxLength());
        }
    }
    
    protected void assignType(PropertyDescriptor prop, FieldImpl field, io.github.ibuildthecloud.gdapi.annotation.Field f) {
        if ( f.type() != Type.NONE ) {
            field.setTypeEnum(f.type());
            if ( ! StringUtils.isEmpty(f.typeString()) ) {
                field.setType(f.typeString());
            }
        }
        
        assignSimpleType(prop.getPropertyType(), field);
        assignComplexType(prop.getPropertyType(), field);
        
        if ( f.password() )
            field.setTypeEnum(Type.PASSWORD);
    }

    protected void assignComplexType(Class<?> clzType, FieldImpl field) {
        Method readMethod = field.getReadMethod();
        Class<?> subTypeCls = null;
        
        if ( readMethod == null )
            return;
        
        switch (field.getTypeEnum()) {
        case ARRAY:
            if ( clzType.isArray() ) {
                subTypeCls = clzType.getComponentType();
            } else {
                subTypeCls = getGenericType(List.class, readMethod, 0);
            }
            break;
        case MAP:
            break;
        case REFERENCE:
            break;
        case TYPE:
            break;
        default:
            break;
        }
        
        if ( subTypeCls != null ) {
            field.setSubTypeClass(subTypeCls);
            Type subType = assignSimpleType(field.getSubTypeClass(), null);
            if ( subType == Type.TYPE ) {
                field.setSubTypeEnum(Type.RESOURCE);
            } else {
                field.setSubTypeEnum(subType);
            }
        }
    }
    
    protected Class<?> getGenericType(Class<?> iface, Method m, int index) {
        java.lang.reflect.Type t = m.getGenericReturnType();
        
        if ( t instanceof ParameterizedType && ((ParameterizedType)t).getActualTypeArguments().length == index + 1) {
            java.lang.reflect.Type argType = ((ParameterizedType)t).getActualTypeArguments()[index];
            if ( argType instanceof Class<?> ) {
                return (Class<?>)argType;
            }
            
            if ( argType instanceof ParameterizedType ) {
                java.lang.reflect.Type rawType = ((ParameterizedType)argType).getRawType();
                if ( rawType instanceof Class<?> )
                    return (Class<?>)rawType;
            }
        }

        return Object.class;
    }
    
    protected Type assignSimpleType(Class<?> clzType, FieldImpl field) {
        Type result = null;

        if ( clzType.isEnum() ) {
            result = Type.ENUM;
        } else {
            outer:
            for ( Field.Type type : Field.Type.values() ) {
                Class<?>[] clzs = type.getClasses();

                if ( clzs == null )
                    continue;
                
                for ( Class<?> clz : clzs ) {
                    if ( clz.isAssignableFrom(clzType) ) {
                        result = type;
                        
                        if ( ( Number.class.isAssignableFrom(clzType) ||
                                Boolean.class.isAssignableFrom(clzType) ) && ! clz.isPrimitive() &&
                                field != null ) {
                            field.setNullable(true);
                        }

                        break outer;
                    }
                }
            }
        }
        
        if ( field != null ) {
            field.setTypeEnum(result);
        }

        return result;
    }
    
    protected io.github.ibuildthecloud.gdapi.annotation.Field getFieldAnnotation(PropertyDescriptor prop) {
        Method readMethod = prop.getReadMethod();
        Method writeMethod = prop.getWriteMethod();
        
        io.github.ibuildthecloud.gdapi.annotation.Field f = null;

        if ( readMethod != null ) {
            f = readMethod.getAnnotation(io.github.ibuildthecloud.gdapi.annotation.Field.class);
        }
        
        if ( f == null && writeMethod != null ) {
            f = writeMethod.getAnnotation(io.github.ibuildthecloud.gdapi.annotation.Field.class);            
        }

        if ( f == null ) {
            f = defaultField; 
        }

        return f;
    }

    @PostConstruct
    public void init() {
        registerSchema(Schema.class);

        for ( Class<?> clz : types ) {
            registerSchema(clz);
        }

        for ( String name : typeNames) {
            registerSchema(name);
        }

        for ( Schema schema : schemasList ) {
            parseSchema(schema.getId());
        }
    }

    @Override
    public List<Schema> listSchemas() {
        return schemasList;
    }

    @Override
    public Schema getSchema(String type) {
        return schemasByName.get(lower(type));
    }

    @Override
    public String getPluralName(String type) {
        return typeToPluralName.get(lower(type));
    }

    @Override
    public Class<?> getSchemaClass(String type) {
        return typeToClass.get(lower(type));
    }

    @Override
    public String getSingularName(String type) {
        return pluralNameToType.get(lower(type));
    }

    protected String lower(String type) {
        return type == null ? null : type.toLowerCase();
    }

    @Override
    public boolean typeStringMatches(Class<?> clz, String type) {
        if ( type == null || clz == null )
            return false;

        Schema schema = getSchema(clz);
        if ( schema == null )
            return false;

        return schema == getSchema(type);
    }

    public List<Class<?>> getTypes() {
        return types;
    }

    public void setTypes(List<Class<?>> types) {
        this.types = types;
    }

    public List<SchemaPostProcessor> getPostProcessors() {
        return postProcessors;
    }

    public void setPostProcessors(List<SchemaPostProcessor> postProcessors) {
        this.postProcessors = postProcessors;
    }

    public List<String> getTypeNames() {
        return typeNames;
    }

    public void setTypeNames(List<String> typeNames) {
        this.typeNames = typeNames;
    }

}