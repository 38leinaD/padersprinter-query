/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dplatz.padersprinter.entity;

import java.util.Optional;
import javax.json.JsonObject;
import org.apache.log4j.Logger;

/**
 *
 * @author daniel.platz
 */
public class Location {

    private static final Logger logger = Logger.getLogger(Location.class.getName());
    
    public static enum Type {
        Street,
        Stop
    }
    private final String name;
    private final Type type;
    private final String id;
    private final String token;
    private final int resultQuality;
    private final String description;

    public Location(String name, Type type, String id, String token, int resultQuality, String description) {
        this.name = name;
        this.type = type;
        this.id = id;
        this.token = token;
        this.resultQuality = resultQuality;
        this.description = description;
    }

    @Override
    public String toString() {
        return "Location{" + "name=" + name + ", type=" + type + ", id=" + id + ", token=" + token + ", resultQuality=" + resultQuality + ", description=" + description + '}';
    }

    public String getDescription() {
        return description;
    }

    public static Optional<Location> fromJsonObject(JsonObject json) {
        final String name = json.getString("name");
        final String token = json.getString("stateless");
        final String id = json.getJsonObject("ref").getString("id");
        final int resultQuality = Integer.parseInt(json.getString("quality"));

        final String typeString = json.getString("anyType");
        Type type;
        if ("street".equals(typeString)) {
            type = Type.Street;
        } else if ("stop".equals(typeString)) {
            type = Type.Stop;
        } else {
            logger.info("Unable to handle anyType-parameter with value '" + typeString + "'");
            return Optional.empty();
        }
        return Optional.of(new Location(name, type, id, token, resultQuality, null));
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public int getResultQuality() {
        return resultQuality;
    }
    
    
}
