package io.github.ibuildthecloud.gdapi.validation;

import static io.github.ibuildthecloud.gdapi.validation.ValidationErrorCodes.*;
import io.github.ibuildthecloud.gdapi.context.ApiContext;
import io.github.ibuildthecloud.gdapi.exception.ClientVisibleException;
import io.github.ibuildthecloud.gdapi.factory.SchemaFactory;
import io.github.ibuildthecloud.gdapi.id.IdFormatter;
import io.github.ibuildthecloud.gdapi.model.Field;
import io.github.ibuildthecloud.gdapi.model.FieldType;
import io.github.ibuildthecloud.gdapi.model.Schema;
import io.github.ibuildthecloud.gdapi.model.Schema.Method;
import io.github.ibuildthecloud.gdapi.model.impl.ValidationErrorImpl;
import io.github.ibuildthecloud.gdapi.request.ApiRequest;
import io.github.ibuildthecloud.gdapi.request.handler.AbstractApiRequestHandler;
import io.github.ibuildthecloud.gdapi.util.DateUtils;
import io.github.ibuildthecloud.gdapi.util.RequestUtils;
import io.github.ibuildthecloud.gdapi.util.ResponseCodes;
import io.github.ibuildthecloud.gdapi.util.TypeUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;

public class ValidationHandler extends AbstractApiRequestHandler {

    ReferenceValidator referenceValidator;
    Set<String> supportedMethods;

    @Override
    public void handle(ApiRequest request) throws IOException {
        ValidationContext context = new ValidationContext();
        context.schemaFactory = ApiContext.getContext().getSchemaFactory();
        context.idFormatter = ApiContext.getContext().getIdFormatter();
        context.schema = context.schemaFactory.getSchema(request.getType());

        validateVersion(request, context);
        validateId(request, context);
        validateType(request, context);
        validateMethod(request, context);
        validateField(request, context);
    }

    protected void validateType(ApiRequest request, ValidationContext context) {
        if ( request.getType() != null && context.schema == null ) {
            error(ResponseCodes.NOT_FOUND);
        }
    }

    protected void validateField(ApiRequest request, ValidationContext context) {
        if ( RequestUtils.isReadMethod(request.getMethod()) ) {
            validateReadField(request, context);
        } else {
            validateWriteField(request, context);
        }
    }

    protected void validateReadField(ApiRequest request, ValidationContext context) {
        request.setRequestObject(new HashMap<String,Object>());
    }

    protected void validateWriteField(ApiRequest request, ValidationContext context) {
        if ( Method.PUT.isMethod(request.getMethod()) ) {
            validateOperationField(request, false, context);
        } else if ( Method.POST.isMethod(request.getMethod()) ) {
            validateOperationField(request, true, context);
        }
    }

    protected void validateOperationField(ApiRequest request, boolean create, ValidationContext context) {
        if ( context.schema == null ) {
            return;
        }

        String type = request.getType();
        Map<String,Object> input = RequestUtils.toMap(request.getRequestObject());
        Map<String,Object> sanitized = new LinkedHashMap<String, Object>();
        Map<String,Field> fields = context.schema.getResourceFields();

        for ( Map.Entry<String, Object> entry : input.entrySet() ) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();

            if ( ! create && TypeUtils.ID_FIELD.equals(fieldName) ) {
                /* For right now, just never let anyone update "id" */
                continue;
            }

            Field field = fields.get(fieldName);
            if ( field == null || ! isOperation(field, create) ) {
                continue;
            }

            boolean wasNull = value == null;
            value = convert(fieldName, field, value, context);

            if ( value != null || wasNull ) {
                if ( value instanceof List ) {
                    for ( Object individualValue : (List<?>)value ) {
                        checkFieldCriteria(type, fieldName, field, individualValue);
                    }
                } else {
                    checkFieldCriteria(type, fieldName, field, value);
                }
                sanitized.put(fieldName, value);
            }
        }

        for ( Map.Entry<String, Field> entry : fields.entrySet() ) {
            String fieldName = entry.getKey();
            Field field = entry.getValue();

            if ( isOperation(field, create) && field.isRequired() && ! sanitized.containsKey(fieldName) ) {
                error(MISSING_REQUIRED, fieldName);
            }

            if ( create && ! sanitized.containsKey(fieldName) && field.hasDefault() ) {
                sanitized.put(fieldName, field.getDefault());
            }
        }

        request.setRequestObject(sanitized);
    }

    protected boolean isOperation(Field field, boolean create) {
        return ( create && field.isCreate() ) || ( ! create && field.isUpdate() );
    }

    protected Object convert(String fieldName, Field field, Object value, ValidationContext context) {
        return convert(fieldName, field.getTypeEnum(), field.getSubTypeEnums(), field.getSubTypes(), value, context);
    }

    protected Object convert(String fieldName, FieldType type, List<FieldType> subTypes, List<String> subTypeNames,
            Object value, ValidationContext context) {

        if ( value == null ) {
            return value;
        }

        switch(type) {
        case MAP:
            return checkType(fieldName, value, Map.class);
        case DATE:
            return convertDate(fieldName, value);
        case ARRAY:
            if ( subTypes.size() ==  0) {
                return error(INVALID_FORMAT, fieldName);
            }
            return convertArray(fieldName, subTypes, subTypeNames, value, context);
        case BLOB:
            return checkType(fieldName, value, InputStream.class);
        case BOOLEAN:
        case ENUM:
        case FLOAT:
        case INT:
        case PASSWORD:
        case STRING:
            return convertGenericType(fieldName, value, type);
        case REFERENCE:
            if ( subTypeNames.size() == 0 ) {
                return error(INVALID_FORMAT, fieldName);
            }
            return convertReference(subTypeNames.get(0), fieldName, value, context);
        case NONE:
        case TYPE:
        default:
            throw new IllegalStateException("Do not know how to convert type [" + type + "]");
        }
    }

    protected Object convertReference(String type, String fieldName, Object value, ValidationContext context) {
        String id = context.idFormatter.parseId(value.toString());
        if ( id == null ) {
            error(INVALID_REFERENCE, fieldName);
        }

        if ( referenceValidator != null ) {
            Object referenced = referenceValidator.getById(type, id);
            if ( referenced == null ) {
                error(INVALID_REFERENCE, fieldName);
            }
        }

        return id;
    }

    protected Object convertGenericType(String fieldName, Object value, FieldType type) {
        if ( type.getClasses().length == 0 )
            return error(INVALID_FORMAT, fieldName);

        Class<?> clz = type.getClasses()[0];
        value = ConvertUtils.convert(value, clz);
        if ( value == null || ! clz.isAssignableFrom(value.getClass()) ) {
            return error(INVALID_FORMAT, fieldName);
        }

        return value;
    }

    protected Object checkType(String fieldName, Object value, Class<?> type) {
        if ( type.isAssignableFrom(value.getClass()) ) {
            return value;
        }
        return error(INVALID_FORMAT, fieldName);
    }

    protected Object convertArray(String fieldName, List<FieldType> subTypes, List<String> subTypesNames, Object value,
            ValidationContext context) {
        List<Object> result = new ArrayList<Object>();
        List<?> items = null;

        if ( value instanceof Object[] ) {
            items = Arrays.asList(value);
        } else if ( value instanceof List ) {
            items = (List<?>)value;
        } else {
            items = Arrays.asList(value);
        }

        FieldType type = subTypes.get(0);
        for ( Object item : items ) {
            item = convert(fieldName, type, subTypes.subList(1, subTypes.size()),
                    subTypesNames.subList(1, subTypesNames.size()), item, context);
            result.add(item);
        }

        return result;
    }

    protected Object convertDate(String fieldName, Object value) {
        if ( value instanceof Date ) {
            return value;
        }
        try {
            if ( StringUtils.isBlank(value.toString()) ) {
                return null;
            }
            return DateUtils.parse(value.toString());
        } catch ( ParseException e ) {
            return error(INVALID_DATE_FORMAT, fieldName);
        }
    }

    protected void checkFieldCriteria(String type, String fieldName, Field field, Object inputValue) {
        Object value = inputValue;
        Number numVal = null;
        String stringValue = null;

        Long minLength = field.getMinLength();
        Long maxLength = field.getMaxLength();
        Long min = field.getMin();
        Long max = field.getMax();
        List<String> options = field.getOptions();
        String validChars = field.getValidChars();
        String invalidChars = field.getInvalidChars();

        if ( value == null && field.getDefault() != null ) {
            value = field.getDefault();
        }

        if ( value instanceof Number ) {
            numVal = (Number)value;
        }

        if ( value != null ) {
            stringValue = value.toString();
        }

        if ( value == null && ! field.isNullable() ) {
            error(NOT_NULLABLE, fieldName);
        }

        if ( value != null && field.isUnique() && referenceValidator != null) {
            if ( referenceValidator.getByField(type, fieldName, value) != null ) {
                error(NOT_UNIQUE, fieldName);
            }
        }

        if ( numVal != null ) {
            if ( min != null && numVal.longValue() < min.longValue() ) {
                error(MIN_LIMIT_EXCEEDED, fieldName);
            }
            if ( max != null && numVal.longValue() > max.longValue() ) {
                error(MAX_LIMIT_EXCEEDED, fieldName);
            }
        }

        if ( stringValue != null ) {
            if ( minLength != null && stringValue.length() < minLength.longValue() ) {
                error(MIN_LENGTH_EXCEEDED, fieldName);
            }
            if ( maxLength != null && stringValue.length() > maxLength.longValue() ) {
                error(MAX_LENGTH_EXCEEDED, fieldName);
            }
        }

        if ( options != null && options.size() > 0 ) {
            if ( ! options.contains(stringValue) ) {
                error(INVALID_OPTION, fieldName);
            }
        }

        if ( validChars != null && stringValue != null ) {
            if ( ! stringValue.matches("^[" + validChars + "]*$") ) {
                error(INVALID_CHARACTERS, fieldName);
            }
        }

        if ( invalidChars != null && stringValue != null ) {
            if ( stringValue.matches("^[" + invalidChars + "]*$") ) {
                error(INVALID_CHARACTERS, fieldName);
            }
        }
    }

    protected void validateVersion(ApiRequest request, ValidationContext context) {
        String version = request.getRequestVersion();
        if ( version != null && ! request.getApiVersion().equals(version) ) {
            error(UNSUPPORTED_VERSION, null);
        }
    }

    protected void validateId(ApiRequest request, ValidationContext context) {
        String id = request.getId();
        if ( id == null ) {
            return;
        }

        //TODO should add some property on whether the ID should be formatted
        if ( context.schemaFactory.typeStringMatches(Schema.class, request.getType()) ) {
            return;
        }

        String formattedId = context.idFormatter.parseId(id);
        if ( formattedId == null ) {
            error(ResponseCodes.NOT_FOUND);
        } else {
            request.setId(formattedId);
        }
    }

    protected void validateMethod(ApiRequest request, ValidationContext context) {
        String method = request.getMethod();
        if ( method == null || ! supportedMethods.contains(method) ) {
            error(ResponseCodes.METHOD_NOT_ALLOWED);
        }

        String type = request.getType();
        String id = request.getId();

        if ( type == null || context.schema == null ) {
            return;
        }

        List<String> allowed = id == null ? context.schema.getCollectionMethods() : context.schema.getResourceMethods();
        if ( ! allowed.contains(method) ) {
            error(ResponseCodes.METHOD_NOT_ALLOWED);
        }
    }

    protected Object error(String code, String fieldName) {
        ValidationErrorImpl error = new ValidationErrorImpl(code, fieldName);
        throw new ClientVisibleException(error);
    }

    protected Object error(int code) {
        throw new ClientVisibleException(code);
    }

    @PostConstruct
    public void init() {
        if ( supportedMethods == null ) {
            supportedMethods = new HashSet<String>();
            for ( Method m : Method.values() ) {
                supportedMethods.add(m.toString());
            }
        }
    }

    public Set<String> getSupportedMethods() {
        return supportedMethods;
    }

    public void setSupportedMethods(Set<String> supportedMethods) {
        this.supportedMethods = supportedMethods;
    }

    private static final class ValidationContext {
        SchemaFactory schemaFactory;
        Schema schema;
        IdFormatter idFormatter;
    }

    public ReferenceValidator getReferenceValidator() {
        return referenceValidator;
    }

    public void setReferenceValidator(ReferenceValidator referenceValidator) {
        this.referenceValidator = referenceValidator;
    }
}
