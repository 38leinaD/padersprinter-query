/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dplatz.padersprinter.entity;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import org.apache.log4j.Logger;

/**
 *
 * @author daniel.platz
 */
public class LocationQuery {
    
    private static final Logger logger = Logger.getLogger(LocationQuery.class.getName());
    
    private String location;
    private String type;

    public LocationQuery(String location, String type) {
        this.location = location;
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }
    
    public WebTarget toWebTarget(Client client) {
        final WebTarget webtarget = client.target("https://www.padersprinter.de/wp-admin/admin-ajax.php?name_sf={location}&page=1&itdLPxx_usage={type}&action=efa_stopfinder")
                        .resolveTemplate("location", getLocation().replace(' ', '+')) // whitespaces are replaced via + signs in ajax api
                        .resolveTemplate("type", getType());

        logger.info("Query URL is: " + webtarget.getUri().toString());
        return webtarget;
    }
}
