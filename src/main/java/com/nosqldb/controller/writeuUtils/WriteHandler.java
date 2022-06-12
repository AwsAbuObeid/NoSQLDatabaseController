package com.nosqldb.controller.writeuUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nosqldb.controller.DAO.ControllerDao;
import com.nosqldb.controller.readservers.ReadServersManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * WriteHandler service is responsible for handling the write requests that come from the
 * user controller, it first checks if the write command is valid,
 * then it edits the database schema that is saved here on the Main controller if needed,
 * after that it sets up JSON message with the required write operation,
 * and finally it uses the readServerManager to send the created message to the cluster.
 */
@Service
public class WriteHandler {

    @Autowired
    private ControllerDao dao;
    @Autowired
    private ReadServersManager readServersManager;
    @Autowired
    JsonSchemaVaildator validator;
    @Autowired
    JsonSchemaApplicator applicator;

    private final ObjectMapper mapper;

    public WriteHandler() {
        mapper=new ObjectMapper();
    }

    /**
     * @return "OK" if the write went correctly, else it returns a description of the problem.
     */
    public String addDocument(String DB, String collection, JsonNode document) throws IOException {
        if (!dao.getDatabaseSchema(DB).has(collection))
            return "Collection " + collection + " doesnt Exist!";
        JsonNode schema=dao.getDatabaseSchema(DB).get(collection);
        String schemaStatus= validator.validateDocument(document,schema);
        if (schemaStatus.equals("OK")) {
            applicator.applySchema(document, schema);

            ObjectNode message= mapper.createObjectNode();
            message.put("op",Operation.ADD_DOCUMENT.name());
            message.put("DB",DB);
            message.put("collection",collection);
            message.set("document",document);

            if(!readServersManager.updateReadServers(message))
                return "CANNOT SAVE DATA";
            return "OK";
        } return schemaStatus;
    }

    /**
     * @return "OK" if the write went correctly, else it returns a description of the problem.
     */
    public String addCollection(String DB, String collection, JsonNode schema) throws IOException {
        if (dao.getDatabaseSchema(DB).has(collection))
            return "Collection " + collection + " Already Exists!";
        if (!validator.isValidSchema(schema))
            return "Bad schema format";

        addId(schema);
        ObjectNode databaseSchema=dao.getDatabaseSchema(DB);
        databaseSchema.set(collection,schema);

        ObjectNode message= mapper.createObjectNode();
        message.put("op",Operation.ADD_COLLECTION.name());
        message.put("DB",DB);
        message.put("collection",collection);
        message.set("schema",schema);

        if(readServersManager.updateReadServers(message))
            dao.setDatabaseSchema(DB,databaseSchema);
        else return "CANNOT SAVE DATA";
        return "OK";
    }

    /**
     * @return "OK" if the write went correctly, else it returns a description of the problem.
     */
    public String deleteCollection(String DB, String collection) throws IOException {
        if (!dao.getDatabaseSchema(DB).has(collection))
            return "Collection " + collection + " doesnt Exist!";

        ObjectNode databaseSchema=dao.getDatabaseSchema(DB);
        databaseSchema.remove(collection);

        ObjectNode message= mapper.createObjectNode();
        message.put("op",Operation.DELETE_COLLECTION.name());
        message.put("DB",DB                     );
        message.put("collection",collection);

        if(readServersManager.updateReadServers(message))
            dao.setDatabaseSchema(DB,databaseSchema);
        else return "CANNOT SAVE DATA";
        return "OK";

    }

    /**
     * @return "OK" if the write went correctly, else it returns a description of the problem.
     */
    public String deleteDocument(String DB, String collection, String doc_ID) throws IOException {
        if (!dao.getDatabaseSchema(DB).has(collection))
            return "Collection " + collection + " doesnt Exist!";

        ObjectNode message= mapper.createObjectNode();
        message.put("op",Operation.DELETE_DOCUMENT.name());
        message.put("DB",DB);
        message.put("collection",collection);
        message.put("doc_ID",doc_ID);

        if(!readServersManager.updateReadServers(message))
            return "CANNOT SAVE DATA";
        return "OK";
    }

    private void addId(JsonNode coll){
        ObjectNode id=mapper.createObjectNode();
        id.put("type","string");
        ((ObjectNode)coll.get("properties")).set("_id",id);
    }

    /**
     * @return "OK" if the write went correctly, else it returns a description of the problem.
     */
    public String addAttribute(String DB, String collection, String attribName, ObjectNode attribute) throws IOException {
        ObjectNode databaseSchema=dao.getDatabaseSchema(DB);
        if(attribName.equals("_id"))
            return "Cant change object id";
        if (!databaseSchema.has(collection))
            return "Collection " + collection + " doesnt Exist!";
        if (databaseSchema.get(collection).get("properties").has(attribName))
            return "Attribute " + collection + " already Exists!";

        ((ObjectNode)databaseSchema.get(collection).get("properties")).set(attribName,attribute);

        ObjectNode message= mapper.createObjectNode();
        message.put("op",Operation.ADD_ATTRIBUTE.name());
        message.put("DB",DB);
        message.put("collection",collection);
        message.put("attribName",attribName);
        message.set("attribute",attribute);

        if(readServersManager.updateReadServers(message))
            dao.setDatabaseSchema(DB,databaseSchema);
        else return "CANNOT SAVE DATA";
        return "OK";
    }

    /**
     * @return "OK" if the write went correctly, else it returns a description of the problem.
     */
    public String deleteAttribute(String DB, String collection, String attribName) throws IOException {
        ObjectNode databaseSchema=dao.getDatabaseSchema(DB);
        if(attribName.equals("_id"))
            return "Cant change object id";
        if (!databaseSchema.has(collection))
            return "Collection " + collection + " doesnt Exist!";
        if (!databaseSchema.get(collection).get("properties").has(attribName))
            return "Attribute " + attribName + " doesnt Exist!";

        ((ObjectNode)databaseSchema.get(collection).get("properties")).remove(attribName);

        ObjectNode message= mapper.createObjectNode();
        message.put("op",Operation.SET_SCHEMA.name());
        message.put("DB",DB);
        message.set("schema",databaseSchema);

        if(readServersManager.updateReadServers(message))
            dao.setDatabaseSchema(DB,databaseSchema);
        else return "CANNOT SAVE DATA";
        return "OK";
    }
}
