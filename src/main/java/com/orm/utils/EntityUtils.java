package com.orm.utils;

import java.lang.reflect.Field;
import java.util.Objects;

import com.orm.annotation.Column;

public final class EntityUtils {

    private EntityUtils() {
    }

    public static String getValue(Field field, Object val) {
        if (!Objects.isNull(val)) {
            if (field.getType().equals(String.class)) {
                return String.format("'%s'", val);
            }
            return val.toString();
        }
        return null;
    }

    public static String getFieldName(Field field) {
        if (field.isAnnotationPresent(Column.class) && !field.getAnnotation(Column.class).value().isEmpty()) {
            return field.getAnnotation(Column.class).value();
        }
        return field.getName();
    }
}
