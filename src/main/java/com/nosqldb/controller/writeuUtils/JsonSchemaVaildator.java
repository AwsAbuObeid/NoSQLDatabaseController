package com.nosqldb.controller.writeuUtils;

import com.fasterxml.jackson.databind.JsonNode;

public interface JsonSchemaVaildator {

    String validateDocument(JsonNode schema, JsonNode document);

    boolean isValidSchema(JsonNode schema);
}