package com.nosqldb.controller.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nosqldb.controller.DAO.ControllerDao;
import com.nosqldb.controller.readservers.ReadServersManager;
import com.nosqldb.controller.security.DBUser;
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

import java.util.UUID;

import static com.nosqldb.controller.readservers.Constants.HOST_URL;

@RestController
@RequestMapping("/read")
public class UserReadController {

    @Autowired
    private ControllerDao dao;
    @Autowired
    private ReadServersManager readServersManager;
    @Autowired
    private WriteHandler writeHandler;

    private final ObjectMapper mapper = new ObjectMapper();

    @GetMapping
    public ResponseEntity read(ModelMap model){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        getUserInfo(model);

        String DBName = (String) model.getAttribute("database");
        String APIKey = UUID.randomUUID().toString();
        String link = readServersManager.sendReaderToNode(APIKey, DBName);
        if (link == null)
            return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);

        ObjectNode ret = mapper.createObjectNode();
        ret.put("key", APIKey);
        ret.put("read_server_link", link);
        return new ResponseEntity(ret, headers, HttpStatus.OK);
    }

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
}
