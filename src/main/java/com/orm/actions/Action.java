package com.orm.actions;

public interface Action {

    int getPriority();

    String prepareQuery();

}
