package com.nosqldb.controller.readservers.observer;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * the Observer interface for the observer design pattern used for managing the
 * read servers.
 */
public interface Observer {
    void update(ObjectNode message);
}
