package com.nosqldb.controller.writeuUtils;

import com.fasterxml.jackson.databind.JsonNode;

public interface JsonSchemaApplicator {
    void applySchema(JsonNode document, JsonNode schema);
}
