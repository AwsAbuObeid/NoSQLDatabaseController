package com.nosqldb.controller.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nosqldb.controller.security.DBUser;
import com.nosqldb.controller.DAO.ControllerDao;
import com.nosqldb.controller.writeuUtils.WriteHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


/**
 * UserController Class is responsible for receiving all user requests,
 * divided into two main categories,read and write.
 * a read request is handled by generating an API and sending it to
 * the user and to the chosen read server, with th database name.
 * the controller also has mappings for all the available write commands,
 * it uses an instance of Write handler which executes them.
 */
@RestController
@RequestMapping("/write")
@SessionAttributes("{username,database,role}")
public class UserWriteController {

    @Autowired
    private ControllerDao dao;
    @Autowired
    private WriteHandler writeHandler;

    private void getUserInfo(ModelMap model) {
        if (model.getAttribute("username") != null)
            return;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        String DBName = null;
        String role = null;
        for (DBUser i : dao.getUsers())
            if (i.getUsername().equals(username)) {
                DBName = i.getDatabase();
                role = i.getRole();
            }
        model.put("username", username);
        model.put("database", DBName);
        model.put("role", role);
    }

    @RequestMapping(value = "/{colName}", method = RequestMethod.PUT)
    public ResponseEntity addDocument(@PathVariable String colName, @RequestBody ObjectNode document, ModelMap model) {
        getUserInfo(model);

        String DBName = (String) model.getAttribute("database");
        String username = (String) model.getAttribute("username");
        String role = (String) model.getAttribute("role");
        if (!"ADMIN".equals(role))
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);

        String status;
        try {
            status = writeHandler.addDocument(DBName, colName, document);
        } catch (IOException e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (status.equals("OK"))
            return new ResponseEntity(HttpStatus.OK);
        return new ResponseEntity(status, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/{colName}/{docId}", method = RequestMethod.DELETE)
    public ResponseEntity deleteDocument(@PathVariable String colName, @PathVariable String docId, ModelMap model) {
        getUserInfo(model);

        String DBName = (String) model.getAttribute("database");
        String username = (String) model.getAttribute("username");
        String role = (String) model.getAttribute("role");
        if (!"ADMIN".equals(role))
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);

        String status;
        try {
            status = writeHandler.deleteDocument(DBName, colName, docId);
        } catch (IOException e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (status.equals("OK"))
            return new ResponseEntity(HttpStatus.OK);
        return new ResponseEntity(status, HttpStatus.BAD_REQUEST);

    }

    @RequestMapping(value = "/schema/{colName}", method = RequestMethod.PUT)
    public ResponseEntity addCollection(@PathVariable String colName, @RequestBody ObjectNode schema, ModelMap model) {
        getUserInfo(model);

        String DBName = (String) model.getAttribute("database");
        String username = (String) model.getAttribute("username");
        String role = (String) model.getAttribute("role");
        if (!"ADMIN".equals(role))
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);

        String status;
        try {
            status = writeHandler.addCollection(DBName, colName, schema);
        } catch (IOException e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (status.equals("OK"))
            return new ResponseEntity(HttpStatus.OK);
        return new ResponseEntity(status, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/schema/{colName}", method = RequestMethod.DELETE)
    public ResponseEntity deleteCollection(@PathVariable String colName, ModelMap model) {
        getUserInfo(model);

        String DBName = (String) model.getAttribute("database");
        String username = (String) model.getAttribute("username");
        String role = (String) model.getAttribute("role");
        if (!"ADMIN".equals(role))
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);

        String status;
        try {
            status = writeHandler.deleteCollection(DBName, colName);
        } catch (IOException e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (status.equals("OK"))
            return new ResponseEntity(HttpStatus.OK);
        return new ResponseEntity(status, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/schema/{colName}/{attribName}", method = RequestMethod.PUT)
    public ResponseEntity addAttribute(@PathVariable String colName,
                                       @PathVariable String attribName,
                                       @RequestBody ObjectNode attribute,
                                       ModelMap model
    ) {
        getUserInfo(model);
        String DBName = (String) model.getAttribute("database");
        String username = (String) model.getAttribute("username");
        String role = (String) model.getAttribute("role");
        if (!"ADMIN".equals(role))
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);

        String status;
        try {
            status = writeHandler.addAttribute(DBName, colName, attribName, attribute);
        } catch (IOException e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (status.equals("OK"))
            return new ResponseEntity(HttpStatus.OK);
        return new ResponseEntity(status, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/schema/{colName}/{attribName}", method = RequestMethod.DELETE)
    public ResponseEntity deleteAttribute(@PathVariable String colName,
                                          @PathVariable String attribName,
                                          ModelMap model
    ) {
        getUserInfo(model);
        String DBName = (String) model.getAttribute("database");
        String username = (String) model.getAttribute("username");
        String role = (String) model.getAttribute("role");
        if (!"ADMIN".equals(role))
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);

        String status;
        try {
            status = writeHandler.deleteAttribute(DBName, colName, attribName);
        } catch (IOException e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (status.equals("OK"))
            return new ResponseEntity(HttpStatus.OK);
        return new ResponseEntity(status, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(name = "/getSchema", method = RequestMethod.GET)
    public ResponseEntity getSchema(ModelMap model) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        getUserInfo(model);

        String DBName = (String) model.getAttribute("database");
        String username = (String) model.getAttribute("username");

        JsonNode ret;
        try {
            ret = dao.getDatabaseSchema(DBName);
        } catch (IOException e) {
            return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity(ret, headers, HttpStatus.OK);
    }

}