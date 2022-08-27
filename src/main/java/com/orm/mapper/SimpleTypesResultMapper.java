package com.orm.mapper;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;

import com.orm.annotation.Column;

import lombok.SneakyThrows;

public class SimpleTypesResultMapper {

    @SneakyThrows
    public <T> T mapToObject(Class<T> tClass, ResultSet resultSet) {
        var resultObject = tClass.getConstructor().newInstance();
        for (Field field : tClass.getDeclaredFields()) {
            var name = getFieldName(field);
            var fieldValue = resultSet.getObject(name, field.getType());
            field.setAccessible(true);
            if (fieldValue instanceof Timestamp tsp){
                fieldValue = tsp.toLocalDateTime();
            }

            field.set(resultObject, fieldValue);
        }
        return resultObject;
    }

    private String getFieldName(Field field) {
        if (field.isAnnotationPresent(Column.class) && !field.getAnnotation(Column.class).value().isEmpty()) {
            return field.getAnnotation(Column.class).value();
        }
        return field.getName();
    }
}
