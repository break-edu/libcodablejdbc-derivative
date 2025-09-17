package me.hysong.libcodablejdbc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class RSCodableUtil {
    public static boolean isMarkedMappingElement(Field f) {
        return f.getDeclaringClass().isAnnotationPresent(Record.class)
                || f.isAnnotationPresent(Column.class);
    }

    public static String getFieldNameInDB(Field f) {
        if (f.isAnnotationPresent(Column.class)) {
            Column column = f.getAnnotation(Column.class);
            return column.mapTo().isEmpty() ? f.getName() : column.mapTo();
        }else if (f.getDeclaringClass().isAnnotationPresent(Record.class)) {
            return f.getName();
        }
        return null;
    }
}
