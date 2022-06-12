package com.nosqldb.controller.writeuUtils;

/**
 * Operation enum represents all the write operations received
 * from the Main database controller.
 */
public enum Operation {
    ADD_DOCUMENT, DELETE_DOCUMENT, DELETE_COLLECTION, SET_SCHEMA,
    DELETE_DATABASE, ADD_ATTRIBUTE, ADD_COLLECTION
}
