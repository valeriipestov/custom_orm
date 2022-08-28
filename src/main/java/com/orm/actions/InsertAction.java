package com.orm.actions;

import java.util.Arrays;

import com.orm.annotation.Table;
import com.orm.utils.EntityUtils;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InsertAction implements Action {

    private static final int PRIORITY = 0;

    private Object entity;

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public String prepareQuery() {
        var entityName = entity.getClass().getAnnotation(Table.class).value();
        StringBuilder fields = new StringBuilder();
        StringBuilder values = new StringBuilder();
        Arrays.stream(entity.getClass().getDeclaredFields())
          .forEach(field -> {
              try {
                  field.setAccessible(true);
                  fields.append(EntityUtils.getFieldName(field)).append(",");
                  values.append(EntityUtils.getValue(field, field.get(entity))).append(",");
              } catch (IllegalAccessException e) {
                  throw new RuntimeException(e);
              }
          });
        fields.deleteCharAt(fields.length() - 1);
        values.deleteCharAt(values.length() - 1);
        return String.format("INSERT INTO %s (%s) VALUES (%s)", entityName, fields, values);
    }
}
