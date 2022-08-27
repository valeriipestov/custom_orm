package com.orm.session;

public record EntityKey<T>(Class<T> resultClass, Object id) {
}
