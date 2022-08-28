package com.orm.mapper;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;

import com.orm.annotation.Column;
import com.orm.utils.EntityUtils;

import lombok.SneakyThrows;

public class SimpleTypesResultMapper {

    @SneakyThrows
    public <T> T mapToObject(Class<T> tClass, ResultSet resultSet) {
        var resultObject = tClass.getConstructor().newInstance();
        for (Field field : tClass.getDeclaredFields()) {
            var name = EntityUtils.getFieldName(field);
            var fieldValue = resultSet.getObject(name, field.getType());
            field.setAccessible(true);
            if (fieldValue instanceof Timestamp tsp) {
                fieldValue = tsp.toLocalDateTime();
            }

            field.set(resultObject, fieldValue);
        }
        return resultObject;
    }

}
