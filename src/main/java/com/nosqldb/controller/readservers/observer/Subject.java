package com.nosqldb.controller.readservers.observer;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * the Subject interface is The subject in the Observer design pattern.
 */
public interface Subject {
    void addObserver(Observer observer);

    void removeObserver(Observer observer);

    void notifyObservers(ObjectNode message);
}
