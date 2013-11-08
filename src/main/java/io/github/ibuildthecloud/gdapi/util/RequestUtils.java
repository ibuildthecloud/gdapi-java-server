package io.github.ibuildthecloud.gdapi.util;

import io.github.ibuildthecloud.gdapi.request.ApiRequest;

import java.util.List;


public class RequestUtils {

    public static final String POST = "POST";
    public static final String GET = "GET";
    public static final String DELETE = "DELETE";
    public static final String PUT = "PUT";

    public static boolean isReadMethod(String method) {
        return ! isWriteMethod(method);
    }

    public static boolean isWriteMethod(String method) {
        return POST.equals(method) ||
                PUT.equals(method) ||
                DELETE.equals(method);
    }

    public static boolean mayHaveBody(String method) {
        return POST.equals(method) ||
                PUT.equals(method);
    }

    public static Object makeSingularIfCan(Object input) {
        if ( input instanceof List ) {
            List<?> list = (List<?>)input;
            if ( list.size() == 1 )
                return list.get(0);
            if ( list.size() == 0 )
                return null;
        }

        if ( input instanceof String[] ) {
            String[] array = (String[])input;
            if ( array.length == 1 ) {
                return array[0];
            }
            if ( array.length == 0 ) {
                return null;
            }
        }

        return input;
    }

    public static String makeSingularStringIfCan(Object input) {
        Object result = makeSingularIfCan(input);
        return result == null ? null : result.toString();
    }
    
    public static boolean hasBeenHandled(ApiRequest request) {
        if ( request.isCommited() || request.getResponseObject() != null ) {
            return true;
        }

        return false;
    }

}