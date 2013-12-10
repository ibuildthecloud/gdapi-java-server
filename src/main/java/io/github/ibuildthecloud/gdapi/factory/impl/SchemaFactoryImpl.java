package io.github.ibuildthecloud.gdapi.factory.impl;

import io.github.ibuildthecloud.gdapi.factory.SchemaFactory;
import io.github.ibuildthecloud.gdapi.model.ApiError;
import io.github.ibuildthecloud.gdapi.model.ApiVersion;
import io.github.ibuildthecloud.gdapi.model.Collection;
import io.github.ibuildthecloud.gdapi.model.Field;
import io.github.ibuildthecloud.gdapi.model.FieldType;
import io.github.ibuildthecloud.gdapi.model.FieldType.TypeAndName;
import io.github.ibuildthecloud.gdapi.model.Resource;
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
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

@io.github.ibuildthecloud.gdapi.annotation.Type
public class SchemaFactoryImpl implements SchemaFactory {

    final io.github.ibuildthecloud.gdapi.annotation.Field defaultField;
    final io.github.ibuildthecloud.gdapi.annotation.Type defaultType;

    String id = UUID.randomUUID().toString();
    boolean includeDefaultTypes = true, writableByDefault = false;
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

    @Override
    public String getId() {
        return id;
    }

    @io.github.ibuildthecloud.gdapi.annotation.Field
    public Object getDefaultField() {
        return null;
    }

    @Override
    public Schema registerSchema(Object obj) {
        Class<?> clz = obj instanceof Class<?> ? (Class<?>)obj : null;
        SchemaImpl schema = schemaName(obj);

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

    @Override
    public Schema getSchema(Class<?> clz) {
        return schemas.get(clz);
    }

    @Override
    public Schema parseSchema(String name) {
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

    protected SchemaImpl schemaName(Object obj) {
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
            schema = schemaName(clz);

        io.github.ibuildthecloud.gdapi.annotation.Type type = clz.getAnnotation(io.github.ibuildthecloud.gdapi.annotation.Type.class);

        if ( type == null )
            type = defaultType;

        if ( type == defaultType ) {
            schema.setCreate(writableByDefault);
            schema.setUpdate(writableByDefault);
            schema.setDeletable(writableByDefault);
        } else {
            schema.setCreate(type.create());
            schema.setUpdate(type.update());
            schema.setDeletable(type.delete());
        }
        schema.setById(type.byId());
        schema.setList(type.list());

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

        if ( f == this.defaultField ) {
            field.setNullable(writableByDefault);
            field.setUpdate(writableByDefault);
            field.setCreate(writableByDefault);
        } else {
            field.setNullable(f.nullable());
            field.setUpdate(f.update());
            field.setCreate(f.create());
        }
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
        if ( f.type() != FieldType.NONE ) {
            field.setTypeEnum(f.type());
        }

        if ( ! StringUtils.isEmpty(f.typeString()) ) {
            field.setType(f.typeString());
        }

        assignSimpleType(prop.getPropertyType(), field);

        List<TypeAndName> types = new ArrayList<FieldType.TypeAndName>();
        Method readMethod = prop.getReadMethod();
        if ( readMethod != null ) {
            getTypes(readMethod.getGenericReturnType(), types);
        }

        if ( types.size() == 1 ) {
            field.setType(types.get(0).getName());
        } else if ( types.size() > 1 ) {
            types.remove(0);
            field.setSubTypesList(types);
        }

        if ( f.password() )
            field.setTypeEnum(FieldType.PASSWORD);
    }

    protected void getTypes(java.lang.reflect.Type type, List<TypeAndName> types) {
        Class<?> clz = null;
        if ( type instanceof Class<?> ) {
            clz = (Class<?>)type;
        }

        if ( type instanceof ParameterizedType ) {
            java.lang.reflect.Type rawType = ((ParameterizedType)type).getRawType();
            if ( rawType instanceof Class<?> )
                clz = (Class<?>)rawType;
        }

        if ( clz == null ) {
            throw new IllegalArgumentException("Failed to find class for type [" + type + "]");
        }

        FieldType fieldType = assignSimpleType(clz, null);
        String name = fieldType.getExternalType();
        if ( fieldType == FieldType.TYPE ) {
            Schema subSchema = getSchema(clz);
            if ( subSchema != null ) {
                name = subSchema.getId();
            }
        }

        types.add(new TypeAndName(fieldType, name));

        java.lang.reflect.Type subType = null;
        switch (fieldType) {
        case ARRAY:
            if ( clz.isArray() ) {
                subType = clz.getComponentType();
            } else {
                subType = getGenericType(type, 0);
            }
            break;
        case MAP:
            subType = getGenericType(type, 1);
            break;
        case REFERENCE:
            subType = getGenericType(type, 0);
            break;
        case TYPE:
            return;
        default:
            break;
        }

        if ( subType != null ) {
            getTypes(subType, types);
        }
    }


    protected java.lang.reflect.Type getGenericType(java.lang.reflect.Type t, int index) {
        if ( t instanceof ParameterizedType && ((ParameterizedType)t).getActualTypeArguments().length == index + 1) {
            return ((ParameterizedType)t).getActualTypeArguments()[index];
        }

        return Object.class;
    }

    protected FieldType assignSimpleType(Class<?> clzType, FieldImpl field) {
        FieldType result = null;

        if ( clzType.isEnum() ) {
            result = FieldType.ENUM;
        } else {
            outer:
            for ( FieldType type : FieldType.values() ) {
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

    @Override
    @PostConstruct
    public void init() {
        if ( includeDefaultTypes ) {
            registerSchema(Schema.class);
            registerSchema(ApiVersion.class);
            registerSchema(ApiError.class);
            registerSchema(Collection.class);
            registerSchema(Resource.class);
        }

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
        String result = pluralNameToType.get(lower(type));
        if ( result == null && typeToPluralName.containsKey(type) ) {
            return type;
        }
        return result;
    }

    protected String lower(String type) {
        return type == null ? "" : type.toLowerCase();
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

    @Override
    public Class<?> getSchemaClass(Class<?> type) {
        Schema schema = getSchema(type);
        return schema == null ? null : getSchemaClass(schema.getId());
    }

    @Override
    public void addPostProcessor(SchemaPostProcessor postProcessor) {
        postProcessors.add(postProcessor);
    }

    @Override
    public String getSchemaName(Class<?> clz) {
        Schema schema = getSchema(clz);
        return schema == null ? null : schema.getId();
    }

    @Override
    public String getSchemaName(String type) {
        Schema schema = getSchema(type);
        return schema == null ? null : schema.getId();
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

    public boolean isIncludeDefaultTypes() {
        return includeDefaultTypes;
    }

    public void setIncludeDefaultTypes(boolean includeDefaultTypes) {
        this.includeDefaultTypes = includeDefaultTypes;
    }

    public boolean isWritableByDefault() {
        return writableByDefault;
    }

    public void setWritableByDefault(boolean writableByDefault) {
        this.writableByDefault = writableByDefault;
    }

}