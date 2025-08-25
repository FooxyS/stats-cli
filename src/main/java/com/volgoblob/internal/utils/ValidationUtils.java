package com.volgoblob.internal.utils;

/**
 * supporting util class to validate function arguments
 */
public final class ValidationUtils {

    /**
     * check obj. If null - throws and do nothing if not.
     * @param obj obj to check.
     * @param name name of obj to print in error message.
     */
    public static void checkNotNull(Object obj, String name) {
        if (obj == null) throw new IllegalArgumentException(name + "Must be not null");
    }

    /**
     * check str. Throws if blank and do nothing if not.
     * @param str str to check.
     * @param name name of str to print error message.
     */
    public static void checkNotBlank(String str, String name) {
        if (str.isBlank()) throw new IllegalArgumentException(name + "cannot be blank.");
    }

}
