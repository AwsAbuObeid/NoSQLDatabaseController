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
                DBUser user = new DBUser();
                user.setUsername(allRequestParams.get("username"));
                user.setDatabase(allRequestParams.get("database"));
                user.setPassword(allRequestParams.get("password"));
                user.setRole(allRequestParams.get("role"));
                if(!inMemManager.userExists(user.getUsername())&&
                        dao.databaseExists(allRequestParams.get("database"))){
                    dao.addUser(user);
                    inMemManager.createUser(user);
                    model.put("good", "User Created!");
                    logger.info("Added new user");
                    break;
                }
                model.put("bad", "Creation Failed");
                break;

            case "Delete User":
                dao.deleteUser(allRequestParams.get("deletedUser"));
                inMemManager.deleteUser(allRequestParams.get("deletedUser"));
                logger.info("Deleted user "+allRequestParams.get("deletedUser"));
                break;

            case "Delete Database":
                String deleted=allRequestParams.get("deletedDB");
                List<DBUser> users=dao.getUsers();
                for (DBUser u :users)
                    if(u.getDatabase().equals(deleted)) {
                        dao.deleteUser(deleted);
                        inMemManager.deleteUser(u.getUsername());
                    }

                ObjectNode message=mapper.createObjectNode();
                message.put("DB",deleted);
                message.put("op", Operation.DELETE_DATABASE.name());
                if(!readServersManager.updateReadServers(message))
                    break;
                dao.deleteDatabase(deleted);
                logger.info("Deleted database "+allRequestParams.get("deletedDB"));
                break;

            case "Change MasterAdmin Credentials":
                return "changeAdmin";

            case "Set Admin Credentials":
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                inMemManager.deleteUser(auth.getName());
                HashSet<GrantedAuthority> x = new HashSet<>();
                x.add((GrantedAuthority) () -> "ROLE_ADMIN");
                UserDetails MasterAdmin = new User(allRequestParams.get("username"),
                        allRequestParams.get("password"), x);
                inMemManager.createUser(MasterAdmin);
                logger.info("Admin Credentials changed");
                break;
            case "Create New Database":
                String dbname=allRequestParams.get("DatabaseName");
                if(!dao.databaseExists(dbname)){
                    createNewDB(dbname);
                }
                else model.put("badDB","Database already exists!");
                break;
            case "Create New Node":
                int port=Integer.parseInt(allRequestParams.get("port"));
                if(readServersManager.isPortFree(port))
                    readServersManager.createNewReadNode(port);
                else model.put("badnode","Port not available");
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
    private void fillModel(ModelMap model){
        List<DBUser> users=dao.getUsers();
        model.put("users",users);
        List<ReadServerNode> nodes = readServersManager.getRunningNodes();
        model.put("nodes",nodes);
        List<String> databaseNames=dao.getDatabaseNames();
        model.put("dbs",databaseNames);
    }

    private void createNewDB(String db) throws IOException {
        ObjectNode message=mapper.createObjectNode();
        message.put("DB",db);
        message.put("op",Operation.SET_SCHEMA.name());
        message.set("schema",mapper.createObjectNode());
        if(!readServersManager.updateReadServers(message))
            return;
        dao.setDatabaseSchema(db,mapper.createObjectNode());
        logger.info(db+" Database Created");
    }
}
