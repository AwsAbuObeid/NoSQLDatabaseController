package com.nosqldb.controller.writeuUtils;

import com.fasterxml.jackson.databind.JsonNode;

public interface JsonSchemaApplicator {
    String applySchema(JsonNode document, JsonNode schema);
}
