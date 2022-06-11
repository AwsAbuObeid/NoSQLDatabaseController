package com.nosqldb.controller.DAO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nosqldb.controller.security.DBUser;

import java.io.IOException;
import java.util.List;

public interface ControllerDao {
     List<DBUser> getUsers();
     void addUser(DBUser user) throws IOException;
     void deleteUser(String username) throws IOException;
     void setDatabaseSchema(String DB, JsonNode schema) throws IOException;
     ObjectNode getDatabaseSchema(String DB) throws IOException;
     boolean databaseExists(String DB);
     List<String> getDatabaseNames();
     boolean deleteDatabase(String DB);

}
