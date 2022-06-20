package com.nosqldb.controller;

/**
 * The Constants class holds all the constants and starting values needed to interact with the docker API
 * and other information.
 */
public class Constants {
    public static final int CONTROLLER_PORT = 8080;
    public static final int DOCKER_API_PORT = 2375;
    public static final int IMAGE_INTERNAL_PORT = 5050;
    public static final int READ_SERVER_STARTING_PORT = 8081;
    public static final String IMAGE_TAG = "awssaleh/nosql-read-server";
    public static final String DB_URL = "http://localhost";
    public static final String CONTROLLER_API_KEY = "Controller_API_key";
    public static final String READ_SERVER_NAME = "read_server";
    //public static final String HOST_URL="http://host.docker.internal";
    public static final String HOST_URL = "http://localhost";

}
