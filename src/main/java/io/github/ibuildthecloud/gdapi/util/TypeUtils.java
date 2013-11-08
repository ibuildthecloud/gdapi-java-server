package io.github.ibuildthecloud.gdapi.util;

public class TypeUtils {

    public static String guessPluralName(String name) {
        if ( name.endsWith("s") || name.endsWith("ch") || name.endsWith("x") )
            return name + "es";
        return name + "s";
    }

}
