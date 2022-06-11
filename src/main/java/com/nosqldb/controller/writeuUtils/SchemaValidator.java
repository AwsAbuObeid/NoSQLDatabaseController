package com.nosqldb.controller.writeuUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.stereotype.Service;

import java.util.UUID;

import java.util.Iterator;

/**
 * The SchemaValidator class implements JsonSchemaVaildator,
 * it checks if the collection schema is valid.
 * it also checks if a document fits into the schema.
 */
@Service
public class SchemaValidator implements JsonSchemaVaildator {

    @Override
    public boolean isValidSchema(JsonNode schema) {
        if (!schema.has("properties") || !schema.get("properties").isObject())
            return false;
        JsonNode properties = schema.get("properties");
        for (Iterator<String> it = properties.fieldNames(); it.hasNext(); ) {
            String i = it.next();
            if (!properties.get(i).isObject())
                return false;
            if (properties.get(i).get("type").asText().equals(DataType.array.name())) {
                if (!testSchemaProperty(properties.get(i).get("items")))
                    return false;
            } else if (!testSchemaProperty(properties.get(i)))
                return false;
        }
        return true;
    }

    /**
     * @return "OK" if the document fits the schema, else it returns a description of the problem.
     */
    @Override
    public String validateDocument(JsonNode document, JsonNode schema) {
        for (Iterator<String> it = schema.get("properties").fieldNames(); it.hasNext(); ) {
            String i = it.next();
            JsonNode node = document.get(i);
            JsonNode Snode = schema.get("properties").get(i);
            if (Snode.has("required") && Snode.get("required").asBoolean() && !document.has(i))
                return "Missing required value :" + i;
            if (!document.has(i))
                continue;
            if (!checkDataType(node, DataType.valueOf(Snode.get("type").asText())))
                return "Wrong type at: " + i;
            else if (node.isObject()) {
                String response = validateDocument(node, Snode);
                if (!response.equals("OK"))
                    return response;
            } else if (node.isArray()) {
                for (JsonNode element : node)
                    if (!checkDataType(element, DataType.valueOf(Snode.get("items").get("type").asText())))
                        return "Wrong array element type at: " + i + ", element :" + element;
                if (Snode.get("items").get("type").asText().equals(DataType.object.name())) {
                    for (JsonNode element : node) {
                        String response = validateDocument(element, Snode.get("items"));
                        if (!response.equals("OK"))
                            return response;
                    }
                }
            }
        }
        return "OK";
    }

    private boolean testSchemaProperty(JsonNode schemaObj) {
        if (!schemaObj.has("type") || !schemaObj.get("type").isTextual())
            return false;
        if (!EnumUtils.isValidEnum(DataType.class, schemaObj.get("type").asText()))
            return false;
        if (schemaObj.get("type").asText().equals(DataType.object.name())) {
            if (schemaObj.has("default"))
                return false;
            if (!isValidSchema(schemaObj))
                return false;
        }
        if (schemaObj.get("type").asText().equals(DataType.array.name())) {
            if (schemaObj.has("default"))
                return false;
            if (!isValidSchema(schemaObj.get("items")))
                return false;
        }
        if (schemaObj.has("default")) {
            if (!checkDataType(schemaObj.get("default"), DataType.valueOf(schemaObj.get("type").asText())))
                return false;
        }
        return true;
    }

    public void applySchema(JsonNode document, JsonNode schema) {
        ((ObjectNode) document).put("_id", UUID.randomUUID().toString());
        for (Iterator<String> it = schema.fieldNames(); it.hasNext(); ) {
            String i = it.next();
            if (schema.get(i).has("default") && !document.has(i))
                ((ObjectNode) document).set(i, schema.get(i).get("default"));
        }
    }

    private boolean checkDataType(JsonNode obj, DataType dataType) {
        switch (dataType) {
            case string:
                return obj.isTextual();
            case number:
                return obj.isDouble();
            case integer:
                return obj.isInt();
            case array:
                return obj.isArray();
            case object:
                return obj.isObject();
            default:
                return false;
        }
    }
}
