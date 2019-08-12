package com.scorch.core.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ReflectionUtils class
 * This class has utility methods that can be useful when writing code that uses reflection
 *
 * @author Gijs "kitsune" de Jong
 */
public class ReflectionUtils {

    /**
     * Gets all the declared fields of the type including the fields of the superclass, this is used for deserilization
     * of the type
     * @param type the type
     * @return     an array of the declared {@link Field}s in the type and superclass of the type
     *
     * @see Field
     */
    public static Field[] getFields (Class type){
        Field[] superclassFields = getSuperclassFields(type);
        List<Field> fieldList = new ArrayList<>(Arrays.asList(superclassFields));
        fieldList.addAll(Arrays.asList(type.getDeclaredFields()));
        return fieldList.stream().toArray(Field[]::new);
    }

    /**
     * Gets all the declared fields of the superclass(es) for the declared type
     * used in {@link ReflectionUtils#getFields(Class)}
     * @param type the type you want to get the superclasses of
     * @return     an array of the declared {@link Field}s in the superclasses of the type.
     */
    private static Field[] getSuperclassFields (Class type){
        if(type.getSuperclass() != Object.class){
            Field[] superclassFields = getSuperclassFields(type.getSuperclass());
            List<Field> fieldList = new ArrayList<>(Arrays.asList(superclassFields));
            fieldList.addAll(Arrays.asList(type.getDeclaredFields()));
            return fieldList.stream().toArray(Field[]::new);
        }
        else {
            return type.getDeclaredFields();
        }
    }

}
