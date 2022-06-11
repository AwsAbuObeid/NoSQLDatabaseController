package com.nosqldb.controller.readservers.observer;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;

/**
 * the ReadServerSubject implements the Subject interface, and implements the needed
 * operations on the observers, for the Observer design pattern.
 */
public class ReadServerSubject implements Subject {

    private final ArrayList<Observer> observers;

    public ReadServerSubject() {
        observers =new ArrayList<>();
    }

    @Override
    public void addObserver(Observer observer) {
    observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
    observers.remove(observer);
    }

    @Override
    public void notifyObservers(ObjectNode message) {
    for (Observer o :observers)
        o.update(message);
    }
}
