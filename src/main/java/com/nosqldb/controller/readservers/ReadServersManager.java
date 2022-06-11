package com.nosqldb.controller.readservers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nosqldb.controller.readservers.observer.ReadServerNode;
import com.nosqldb.controller.readservers.observer.ReadServerSubject;
import com.nosqldb.controller.readservers.observer.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.nosqldb.controller.Constants.*;

/**
 * The ReadServersManager class is responsible for managing the nodes in the cluster,
 * it does operations such as, start a new node, or kill one.
 * it uses the Subject of the Observers to send updates to the servers.
 */
@Component
public class ReadServersManager {
    private int count;
    private final Subject subject;
    private final ArrayList<ReadServerNode> readServerNodes;
    private final Logger logger= LoggerFactory.getLogger(ReadServersManager.class);


    public ReadServersManager(){
        count=0;
        readServerNodes =new ArrayList<>();
        subject=new ReadServerSubject();
        createNewReadNode(READ_SERVER_STARTING_PORT);
    }

    /**
     * Commits one container and creates a new one, if it failed to commit, it
     * will not create a new node.
     */
    public void createNewReadNode(int port) {
        ReadServerNode newNode;

        if(readServerNodes.size()!=0) {
            ReadServerNode clean=null;
            for(ReadServerNode node :readServerNodes) {
                if(!node.isDirty()) {
                    clean=node;
                    break;
                }
            }
            if (clean==null||!clean.commitContainer()) {
                logger.error("No container was committed.");
                logger.error("This is a critical error, newly created Nodes could have dirty data!");
                return;
            }
        }
        newNode=new ReadServerNode(count+1,port);
        if (!newNode.runContainer()) {
            logger.error("failed to run container at port "+port);
            return;
        }
        count++;
        readServerNodes.add(newNode);
        subject.addObserver(newNode);
        logger.info("Started new ReadServer at port ["+port+"] id: "+newNode.getId());
    }

    public List<ReadServerNode> getRunningNodes(){
        return readServerNodes;
    }

    public void updateReadServers(ObjectNode message){
        logger.info("Sending update to "+count+" nodes in the cluster");
        subject.notifyObservers(message);
    }

    /**
     * @return Link to the server
     */
    public String sendReaderToNode(String APIKey,String DBName){
        ReadServerNode readeNode=chooseNode();
        ObjectNode message=new ObjectMapper().createObjectNode();
        message.put("key",APIKey);
        message.put("DB",DBName);

        if(!readeNode.sendSession( message)){
            logger.error("Failed to send User to server at port: "+readeNode.getPort() +"");
            return null;
        }

        logger.info("User was Sent to : "+HOST_URL+":"+readeNode.getPort());
        return HOST_URL+":"+readeNode.getPort();
    }

    private ReadServerNode chooseNode(){
        int minLoad=readServerNodes.get(0).getLoad();
        int minID=0;
        for(ReadServerNode node :readServerNodes) {
            if (node.getLoad()<minLoad)
                minID=node.getId();
        }
        return readServerNodes.get(minID-1);
    }

    public void stopNode(int id) {
        if(readServerNodes.size()==1){
            logger.error("cannot stop last server");
            return;
        }

        if (!readServerNodes.get(id-1).stopContainer()){
            logger.error("Failed to stop server with ID: "+id);
            return;
        }
        subject.removeObserver(readServerNodes.get(id-1));
        readServerNodes.remove(id-1);
        count--;
        logger.info("Stopped server with ID :"+id);
    }

    public void stopAllNodes() {
        logger.info("Stopping all servers");
        for (ReadServerNode i : readServerNodes)
           i.stopContainer();
    }

    public boolean isPortFree(int port){
        for (ReadServerNode i : readServerNodes)
            if(i.getPort()==port){
                return false;
            }
        return port > 0 && port <= 65534 && port != CONTROLLER_PORT;
    }

}
