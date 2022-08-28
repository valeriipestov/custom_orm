package com.orm.actions;

import java.util.Arrays;

import com.orm.annotation.Id;
import com.orm.annotation.Table;
import com.orm.utils.EntityUtils;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@AllArgsConstructor
public class DeleteAction implements Action {

    private static final int PRIORITY = 1;

    private Object entity;

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @SneakyThrows
    @Override
    public String prepareQuery() {
        var entityName = entity.getClass().getAnnotation(Table.class).value();
        var idField = Arrays.stream(entity.getClass().getDeclaredFields())
          .filter(field -> field.isAnnotationPresent(Id.class))
          .findFirst().orElseThrow();
        idField.setAccessible(true);
        var idValue = EntityUtils.getValue(idField, idField.get(entity));
        return String.format("DELETE FROM %s WHERE %s = %s", entityName, idField.getName(), idValue);
    }
}
