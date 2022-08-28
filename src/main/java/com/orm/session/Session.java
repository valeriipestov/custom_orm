package com.orm.session;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Predicate;

import com.orm.actions.Action;
import com.orm.actions.DeleteAction;
import com.orm.actions.InsertAction;
import com.orm.annotation.Id;
import com.orm.annotation.Table;
import com.orm.mapper.SimpleTypesResultMapper;
import com.orm.utils.EntityUtils;

import lombok.SneakyThrows;


public class Session {

    private DataSource dataSource;

    private SimpleTypesResultMapper mapper = new SimpleTypesResultMapper();

    private Map<EntityKey<?>, Object> cacheMap = new HashMap<>();
    private Map<EntityKey<?>, Object[]> snapshotMap = new HashMap();

    private Queue<Action> actionQueue = new LinkedList<>();

    public Session(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T find(Class<T> entityType, Object id) {
        var key = new EntityKey<>(entityType, id);
        var result = cacheMap.computeIfAbsent(key, this::loadFromDb);
        return entityType.cast(result);
    }

    public void persist(Object entity) {
        actionQueue.add(new InsertAction(entity));
    }

    public void remove(Object entity) {
        actionQueue.add(new DeleteAction(entity));
    }

    public void flush() {
//        updateIfChanged();
        snapshotMap.clear();
        executeActions();
        cacheMap.clear();
    }

    public void close() {
        flush();
    }

    private void executeActions() {
        actionQueue.stream()
          .sorted(Comparator.comparing(Action::getPriority))
          .forEach(this::executeAction);
    }

    @SneakyThrows
    private void executeAction(Action action) {
        try (var connection = dataSource.getConnection();
             var stmt = connection.createStatement()) {
            var sql = action.prepareQuery();
            System.out.println(sql);
            stmt.execute(sql);
        }
    }

    private void updateIfChanged() {
        cacheMap.entrySet()
          .forEach(el -> {
              var fieldValues = snapshotMap.get(el.getKey());
              var sortedFields = Arrays.stream(el.getValue().getClass().getDeclaredFields())
                .sorted(Comparator.comparing(Field::getName)).toList();
              var tableName = el.getKey().resultClass().getAnnotation(Table.class).value();
              var sql = buildUpdateQuery(tableName, sortedFields, fieldValues, el.getValue());
              executeUpdate(sql);
          });
    }

    private String buildUpdateQuery(String tableName, List<Field> sortedFields, Object[] fieldValues,
                                    Object currValue) {
        boolean executeUpdate = false;
        StringBuilder sb = new StringBuilder("UPDATE ")
          .append(tableName)
          .append(" SET ");
        var count = 0;
        Field idField = null;
        Object idFieldValue = null;
        for (int i = 0; i < sortedFields.size(); i++) {
            try {
                var field = sortedFields.get(i);
                field.setAccessible(true);
                var cachedFieldValue = field.get(currValue);
                if (field.isAnnotationPresent(Id.class)) {
                    idField = field;
                    idFieldValue = cachedFieldValue;
                }
                if (!cachedFieldValue.equals(fieldValues[i])) {
                    count++;
                    executeUpdate = true;
                    sb.append(sortedFields.get(i).getName()).append(" = ")
                      .append(EntityUtils.getValue(sortedFields.get(i), cachedFieldValue))
                      .append(",");
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        if (executeUpdate) {
            return addWhereClause(sb, idField, idFieldValue);
        }
        return null;
    }

    private String addWhereClause(StringBuilder sb, Field idField, Object idFieldValue) {

        return sb.deleteCharAt(sb.length() - 1)
          .append(" WHERE ")
          .append(idField.getName())
          .append(" = ")
          .append(EntityUtils.getValue(idField, idFieldValue))
          .toString();
    }

    @SneakyThrows
    private void executeUpdate(String sql) {
        if (!Objects.isNull(sql)) {
            try (var connection = dataSource.getConnection();
                 var statement = connection.createStatement()) {
                System.out.println(sql);
                statement.executeUpdate(sql);
            }
        }

    }

    @SneakyThrows
    private <T> T loadFromDb(EntityKey<T> entityKey) {
        var entityType = entityKey.resultClass();
        var id = entityKey.id();
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            var sql = buildQuery(entityType, id);
            System.out.println(sql);
            var resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                var resultObject = mapper.mapToObject(entityType, resultSet);
                addToSnapshot(entityKey, resultObject);
                return resultObject;
            }
            return null;
        }
    }

    private <T> void addToSnapshot(EntityKey<T> entityKey, T object) {
        var snapshot = Arrays.stream(object.getClass().getDeclaredFields())
          .sorted(Comparator.comparing(Field::getName))
          .map(field -> {
              try {
                  field.setAccessible(true);
                  return field.get(object);
              } catch (IllegalAccessException e) {
                  throw new RuntimeException(e);
              }
          })
          .toArray();
        snapshotMap.put(entityKey, snapshot);
    }

    private String buildQuery(Class<?> entityClass, Object id) {
        String table = Optional.ofNullable(entityClass.getAnnotation(Table.class))
          .map(Table::value)
          .filter(Predicate.not(String::isEmpty))
          .orElseThrow();
        String idField = Arrays.stream(entityClass.getDeclaredFields())
          .filter(field -> field.isAnnotationPresent(Id.class))
          .map(Field::getName)
          .findFirst()
          .orElseThrow();
        return String.format("select * from %s where %s = %s", table, idField, id);
    }
}
