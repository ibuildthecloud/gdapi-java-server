package io.github.ibuildthecloud.gdapi.id;

import io.github.ibuildthecloud.gdapi.factory.SchemaFactory;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import javax.inject.Inject;

public class TypeIdFormatter implements IdFormatter {

    private Map<String,String> typeCache = Collections.synchronizedMap(new WeakHashMap<String, String>());

    String globalPrefix = "1";
    SchemaFactory schemaFactory;

    @Override
    public String formatId(String type, Object id) {
        if ( id == null ) {
            return null;
        }

        String idString = id.toString();
        if ( idString.length() == 0 ) {
            return null;
        }

        String shortType = typeCache.get(type);
        if ( shortType == null ) {
            shortType = getShortType(type);
        }

        if ( ! Character.isDigit(idString.charAt(0)) ) {
            return shortType + "!" + id;
        } else {
            return shortType + id;
        }
    }

    @Override
    public String parseId(String id) {
        if ( id == null || id.length() == 0 )
            return null;

        if ( Character.isLetter(id.charAt(0)) && ! id.startsWith(globalPrefix) ) {
            return id;
        }

        if ( ! id.startsWith(globalPrefix) ) {
            return null;
        }

        id = id.substring(globalPrefix.length());

        if ( id.length() == 0 || ! Character.isLetter(id.charAt(0)) ) {
            return null;
        }

        String parsedId = id.replaceAll("^[a-z]*", "");
        try {
            if ( parsedId.startsWith("!") ) {
                return parsedId.substring(1);
            } else {
                return parsedId;
            }
        } catch ( NumberFormatException e ) {
            return null;
        }
    }

    protected String getShortType(String type) {
        String base = schemaFactory.getBaseType(type);
        if ( base != null ) {
            type = base;
        }

        StringBuilder buffer = new StringBuilder(globalPrefix);
        buffer.append(type.charAt(0));
        buffer.append(type.replaceAll("[a-z]+", ""));

        String result = buffer.toString().toLowerCase();
        typeCache.put(type, result);

        return result;
    }

    public SchemaFactory getSchemaFactory() {
        return schemaFactory;
    }

    @Inject
    public void setSchemaFactory(SchemaFactory schemaFactory) {
        this.schemaFactory = schemaFactory;
    }
}
