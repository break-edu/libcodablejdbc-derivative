package me.hysong.libcodablejdbc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.ResultSet;

public class RSCodableUtil {
    public static boolean isMarkedMappingElement(Field f, Class<? extends Annotation> annotation) {
        return f.getDeclaringClass().isAnnotationPresent(annotation)
                || f.isAnnotationPresent(annotation);
    }

    public static String getFieldNameInDB(Field f) {

    }
}
