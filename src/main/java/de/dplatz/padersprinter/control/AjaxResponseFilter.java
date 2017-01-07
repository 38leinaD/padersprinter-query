/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dplatz.padersprinter.control;

import java.io.IOException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

/**
 * @author daniel.platz
 */
class AjaxResponseFilter implements ClientResponseFilter {
    
    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        // For some reason the content-type for the JSON-data is set to text/html.
        // The JAX-RS-client is not able to deserialize to JSON properly.
        // That's why we overwrite it to the "correct" value which is application/json.
        responseContext.getHeaders().putSingle("Content-Type", "application/json; charset=UTF-8");
    }
    
}
