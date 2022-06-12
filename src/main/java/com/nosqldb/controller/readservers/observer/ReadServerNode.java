package com.nosqldb.controller.readservers.observer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nosqldb.controller.readservers.ReadServersManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.io.IOException;

import static com.nosqldb.controller.Constants.*;

/**
 * ReadServerNode class represents an object holding all the references to a Node in
 * the cluster, it can preform all the needed operations on the read server container through the
 * docker API.
 * it also implements the Observer interface, is it is an Observer in the observer
 * design pattern.
 */
public class ReadServerNode implements Observer {
    private final int id;
    private String containerId;
    private final int port;
    private boolean dirty;
    private final Logger logger= LoggerFactory.getLogger(ReadServersManager.class);

    public ReadServerNode(int id, int port) {
        dirty=false;
        this.id = id;
        this.port = port;
    }

    @Override
    public void update(ObjectNode message) {
        try {
            String resp=WebClient.create().post().uri(DB_URL + ":" + port + "/write").
                    header("x-api-key", CONTROLLER_API_KEY).
                    contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(new JsonMapper().writeValueAsString(message)))
                    .retrieve().bodyToMono(String.class).block();
            if(!"OK".equals(resp))
                throw new Exception("Bad response");
        } catch (Exception e) {
            logger.error("server with ID : " +
                    id + " at port: " + port + " Failed to update with Error: "+e.getMessage());
            dirty=true;
        }
    }

    public boolean runContainer() {
        String s = "{" +
                "  \"ExposedPorts\": { \"" + IMAGE_INTERNAL_PORT + "/tcp\": {} }," +
                "  \"Image\": \"" + IMAGE_TAG + "\"," +
                "  \"HostConfig\": {" +
                "    \"PortBindings\": {" +
                "      \"" + IMAGE_INTERNAL_PORT + "/tcp\": [" +
                "        {\n" +
                "          \"HostIp\": \"\"," +
                "          \"HostPort\": \"" + port + "\"" +
                "        }" +
                "      ]" +
                "    }" +
                "  }" +
                "}";
        String url = DB_URL + ":" + DOCKER_API_PORT +
                "/containers/create?name=" + READ_SERVER_NAME + "_" + id;

        try {
            String response = WebClient.create().post().uri(url).contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(s))
                    .retrieve().bodyToMono(String.class).block();
            JsonNode resp = new JsonMapper().readTree(response);

            if (!resp.has("Id"))
                return false;
            containerId = resp.get("Id").asText();

            url = DB_URL + ":" + DOCKER_API_PORT + "/containers/" + READ_SERVER_NAME + "_" + id + "/start";
            WebClient.create().post().uri(url).contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(""))
                    .retrieve().bodyToMono(String.class).block();
            return true;
        } catch (WebClientException | IOException e) {
            logger.error("Container failed to start with error :" +e.getMessage());
            return false;
        }


    }

    public boolean commitContainer() {
        try {
            String url = DB_URL + ":" + DOCKER_API_PORT +
                    "/commit?container=" + READ_SERVER_NAME + "_" + id + "&repo=" + IMAGE_TAG;
            String response = WebClient.create().post().uri(url).contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(""))
                    .retrieve().bodyToMono(String.class).block();

            return new ObjectMapper().readTree(response).has("Id");
        } catch (WebClientException | IOException e) {
            return false;
        }

    }

    public boolean stopContainer() {
        try {
            String url = DB_URL + ":" + DOCKER_API_PORT + "/containers/" + READ_SERVER_NAME + "_" + id + "/stop";
            WebClient.create().post().uri(url).contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(""))
                    .retrieve().bodyToMono(String.class).block();
            url = DB_URL + ":" + DOCKER_API_PORT + "/containers/" + READ_SERVER_NAME + "_" + id;
            WebClient.create().delete().uri(url)
                    .retrieve().bodyToMono(String.class).block();
            return true;
        } catch (WebClientException e) {
            return false;
        }
    }

    public boolean sendSession(ObjectNode message) {
        try {
            String response = WebClient.create().post().uri(DB_URL + ":" + port + "/addAPIKey").
                    header("x-api-key", CONTROLLER_API_KEY).
                    contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(new JsonMapper().writeValueAsString(message)))
                    .retrieve().bodyToMono(String.class).block();
            return true;
        } catch (WebClientException |IOException e) {
            return false;
        }
    }

    public int getLoad() {
        try {
            return WebClient.create().get().uri(DB_URL + ":" + port + "/load").
                   header("x-api-key", CONTROLLER_API_KEY)
                   .retrieve().bodyToMono(Integer.class).block();
        } catch (WebClientException e) {
            return Integer.MAX_VALUE;
        }
    }

    public int getPort() {
        return port;
    }
    public int getId() {
        return id;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void cleanNode() {
        dirty=false;
    }
    public String getStatus(){
        if(getLoad()==Integer.MAX_VALUE)
            return "Not Available";
        return "Available";
    }
}
