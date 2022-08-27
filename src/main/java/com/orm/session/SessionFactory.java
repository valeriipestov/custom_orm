package com.orm.session;

import javax.sql.DataSource;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SessionFactory {

    private DataSource dataSource;

    public Session createSession() {
        return new Session(dataSource);
    }
}
