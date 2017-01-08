/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dplatz.padersprinter.entity;

/**
 *
 * @author daniel.platz
 */
public class Transport {

    public enum Type {
        WALK,
        BUS,
        TRAIN
    }

    private final String id;
    private final Type type;

    public Transport(String id, Type type) {
        this.id = id;
        this.type = type;
    }

    public static Transport walk(String id) {
        return new Transport(id, Type.WALK);
    }
    
    public static Transport bus(String id) {
        return new Transport(id, Type.BUS);
    }

    @Override
    public String toString() {
        return "Transport{" + "id=" + id + ", type=" + type + '}';
    }
}
