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

    private int idCount;
    private int count;
    private final Subject subject;
    private final ArrayList<ReadServerNode> readServerNodes;


    private final Logger logger= LoggerFactory.getLogger(ReadServersManager.class);


    public ReadServersManager(){
        idCount=0;
        count=0;
        readServerNodes =new ArrayList<>();
        subject=new ReadServerSubject();
        createNewReadNode(READ_SERVER_STARTING_PORT);
    }

    public void createNewReadNode(int port) {
        ReadServerNode newNode;
        newNode=new ReadServerNode(idCount+1,port);
        if (!newNode.runContainer()) {
            logger.error("failed to run container at port "+port);
            return;
        }
        idCount++;
        count++;
        readServerNodes.add(newNode);
        subject.addObserver(newNode);
        logger.info("Started new ReadServer at port ["+port+"] id: "+newNode.getId());
    }

    public List<ReadServerNode> getRunningNodes(){
        return readServerNodes;
    }

    /**
     * First it sends the message to the servers, then checks if any are didn't update,
     * if so it attempts to fix it, else it deletes it, if none updated, it returns false.
     * returns true if everything is ok.
     */
    public synchronized boolean updateReadServers(ObjectNode message){
        logger.info("Sending update to "+count+" nodes in the cluster");
        subject.notifyObservers(message);

        if(!commitContainer()) {
            logger.error("No container was committed."                                                  );
            logger.error("This is a critical error, newly created Nodes could have dirty data!"         );
            logger.error("Recommend restarting Database, and running more servers for data redundancy"  );
            for(ReadServerNode node :readServerNodes)
                node.cleanNode();
            return false;
        }
        for(ReadServerNode node :readServerNodes)
            if (node.isDirty()) {
                logger.info("attempting to fix read server id : "+node.getId()+" -restarting...");
                node.stopContainer();
                if(!node.runContainer()) {
                    logger.error("failed to restart stopping server id : "+node.getId());
                    stopNode(node.getId());
                }else node.cleanNode();
            }
        return true;
    }
    private boolean commitContainer(){
        boolean committed=false;
        for(ReadServerNode node :readServerNodes)
            if(!node.isDirty()&& node.commitContainer()) {
                logger.info("committed read_server_"+node.getId());
                committed=true;
                break;
            }
        return committed;
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
        logger.info("User was Sent to : "+DB_URL+":"+readeNode.getPort());
        return DB_URL+":"+readeNode.getPort();
    }

    private ReadServerNode chooseNode(){
        int minLoad=readServerNodes.get(0).getLoad();
        ReadServerNode minNode=readServerNodes.get(0);
        for(ReadServerNode node :readServerNodes) {
            if (node.getLoad()<minLoad)
                minNode=node;
        }
        return minNode;
    }
    private ReadServerNode getNodeById(int id){
        for(ReadServerNode node :readServerNodes) {
            if (node.getId()==id)
                return node;
        }
        return null;
    }

    public void stopNode(int id) {
        ReadServerNode stopped=getNodeById(id);
        if (!stopped.stopContainer()){
            logger.error("Failed to stop Container with ID: "+id);
            logger.error("Deleting from the List,  "+id);
            return;
        }
        subject.removeObserver(stopped);
        readServerNodes.remove(stopped);
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
