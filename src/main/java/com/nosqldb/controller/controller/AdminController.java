package com.nosqldb.controller.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nosqldb.controller.readservers.ReadServersManager;
import com.nosqldb.controller.readservers.observer.ReadServerNode;
import com.nosqldb.controller.security.DBUser;
import com.nosqldb.controller.DAO.ControllerDao;
import com.nosqldb.controller.writeuUtils.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * AdminController Class is responsible for receiving and executing all the Admin
 * operations that come from the Web interface, it uses instances of ControllerDao,
 * InMemoryUserDetailsManager and ReadServersManager, to execute all the requests
 * such as User config, cluster config, and database config, and system shutdown.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    ControllerDao dao;
    @Autowired
    InMemoryUserDetailsManager inMemManager;
    @Autowired
    ReadServersManager readServersManager;
    Logger logger= LoggerFactory.getLogger(AdminController.class);
    ObjectMapper mapper = new ObjectMapper();

    @GetMapping
    public String adminPage(ModelMap model)  {
        logger.info("Admin page loaded");
        fillModel(model);
        return "adminPage";
    }

    @PostMapping
    public String editUsers(@RequestParam Map<String,String> allRequestParams,ModelMap model) throws IOException {
        String action=allRequestParams.get("execute");
        switch (action) {
            case "Add User":
                addUser(model,allRequestParams.get("username")
                        ,allRequestParams.get("database")
                        ,allRequestParams.get("password")
                        ,allRequestParams.get("role"));
                break;
            case "Delete User":
                deleteUser(allRequestParams.get("deletedUser"));
                break;
            case "Delete Database":
                deleteDatabase(allRequestParams.get("deletedDB"));
                break;
            case "Change MasterAdmin Credentials":
                return "changeAdmin";
            case "Set Admin Credentials":
                changeAdmin(allRequestParams.get("username"),allRequestParams.get("password"));
                break;
            case "Create New Database":
                createDatabase(model,allRequestParams.get("DatabaseName"));
                break;
            case "Create New Node":
                createNode(model,Integer.parseInt(allRequestParams.get("port")));
                break;
            case "Stop Node":
                readServersManager.stopNode(Integer.parseInt(allRequestParams.get("stoppedNode")));
                break;
            case "ShutDown Database":
                readServersManager.stopAllNodes();
                logger.info("Server Shutting Down...");
                System.exit(0);
        }
        fillModel(model);
        return "adminPage";
    }

    private void createNode(ModelMap model,int port) {
        if(readServersManager.isPortFree(port))
            readServersManager.createNewReadNode(port);
        else model.put("badnode","Port not available");
    }

    private void createDatabase(ModelMap model,String DB) throws IOException {
        if(!dao.databaseExists(DB)){
            ObjectNode message=mapper.createObjectNode();
            message.put("DB",DB);
            message.put("op",Operation.SET_SCHEMA.name());
            message.set("schema",mapper.createObjectNode());
            if(!readServersManager.updateReadServers(message))
                return;
            dao.setDatabaseSchema(DB,mapper.createObjectNode());
            logger.info(DB+" Database Created");
        }
        else model.put("badDB","Database already exists!");
    }

    private void changeAdmin(String username,String password) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        inMemManager.deleteUser(auth.getName());
        HashSet<GrantedAuthority> x = new HashSet<>();
        x.add((GrantedAuthority) () -> "ROLE_ADMIN");
        UserDetails MasterAdmin = new User(username,password, x);
        inMemManager.createUser(MasterAdmin);
        logger.info("Admin Credentials changed");
    }

    private void deleteDatabase(String DB) throws IOException {
        ObjectNode message=mapper.createObjectNode();
        message.put("DB", DB);
        message.put("op", Operation.DELETE_DATABASE.name());
        if(!readServersManager.updateReadServers(message))
            return;
        List<DBUser> users=dao.getUsers();
        for (DBUser u :users)
            if(u.getDatabase().equals(DB)) {
                dao.deleteUser(u.getUsername());
                inMemManager.deleteUser(u.getUsername());
            }
        dao.deleteDatabase(DB);
        logger.info("Deleted database "+ DB);
    }

    private void addUser(ModelMap model,String username,String DB,String password,String role) throws IOException {
        DBUser user = new DBUser();
        user.setUsername(username);
        user.setDatabase(DB);
        user.setPassword(password);
        user.setRole(role);
        if(!inMemManager.userExists(user.getUsername())&&
                dao.databaseExists(DB)){
            dao.addUser(user);
            inMemManager.createUser(user);
            model.put("good", "User Created!");
            logger.info("Added new user");
            return;
        }
        model.put("bad", "Creation Failed");
    }

    private void deleteUser(String username) throws IOException {
        dao.deleteUser(username);
        inMemManager.deleteUser(username);
        logger.info("Deleted user "+username);
    }

    private void fillModel(ModelMap model){
        List<DBUser> users=dao.getUsers();
        model.put("users",users);
        List<ReadServerNode> nodes = readServersManager.getRunningNodes();
        model.put("nodes",nodes);
        List<String> databaseNames=dao.getDatabases();
        model.put("dbs",databaseNames);
    }
}
