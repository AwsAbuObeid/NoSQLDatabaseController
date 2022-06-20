package com.nosqldb.controller.DAO;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nosqldb.controller.fileservice.FileService;
import com.nosqldb.controller.security.DBUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ControllerDaoImpl is an implementation of the ControllerDao interface, it represents
 * it uses a file service to access the controller files and do the needed read and
 * write operations.
 */
@Repository
public class ControllerDaoImpl implements ControllerDao {
    @Autowired
    FileService files;

    private final ObjectMapper mapper;

    private ControllerDaoImpl() {
        this.mapper = new ObjectMapper();
    }

    @Override
    public List<DBUser> getUsers() {
        try {
            ArrayNode users = (ArrayNode) mapper.readTree(files.getUsersFile());
            List<DBUser> ret = new ArrayList<>();
            for (JsonNode j : users)
                ret.add(mapper.convertValue(j, DBUser.class));
            return ret;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public synchronized void addUser(DBUser user) throws IOException {
        ObjectNode node = mapper.createObjectNode();
        node.put("database", user.getDatabase());
        node.put("username", user.getUsername());
        node.put("password", user.getPassword());
        node.put("role", user.getRole());
        File usersFile = files.getUsersFile();
        ArrayNode users = (ArrayNode) mapper.readTree(usersFile);
        users.add(node);
        mapper.writeValue(usersFile, users);
    }

    @Override
    public synchronized void deleteUser(String username) throws IOException {
        File usersFile = files.getUsersFile();
        ArrayNode users = (ArrayNode) mapper.readTree(usersFile);
        for (int i = 0; i < users.size(); i++)
            if (users.get(i).get("username").asText().equals(username))
                users.remove(i);
        mapper.writeValue(usersFile, users);

    }

    @Override
    public ObjectNode getDatabaseSchema(String DB) throws IOException {
        JsonNode schema = mapper.readTree(files.getDatabaseSchemaFile(DB));
        return (ObjectNode) schema;
    }

    @Override
    public boolean databaseExists(String DB) {
        return files.getDatabaseSchemaFile(DB).exists();
    }

    @Override
    public List<String> getDatabases() {
        List<String> names = new ArrayList<>();
        for (File db : files.getAllDatabaseFiles())
            if (!db.equals(files.getUsersFile()))
                names.add(db.getName().replace("_schema.json", ""));
        return names;
    }

    @Override
    public synchronized void deleteDatabase(String DB) {
        files.getDatabaseSchemaFile(DB).delete();
    }

    @Override
    public synchronized void setDatabaseSchema(String DB, JsonNode schema) throws IOException {
        mapper.writeValue(files.getDatabaseSchemaFile(DB), schema);
    }
}
