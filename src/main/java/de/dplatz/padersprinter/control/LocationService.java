/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dplatz.padersprinter.control;

import de.dplatz.padersprinter.entity.Location;
import de.dplatz.padersprinter.entity.LocationQuery;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import rx.Observable;

/**
 *
 * @author daniel.platz
 */
public class LocationService {

    private static final Logger logger = Logger.getLogger(LocationService.class.getName());

    public Observable<Location> searchOriginLocations(String location) {
        return searchLocations(new LocationQuery(location, "origin"));
    }

    public Observable<Location> searchDestinationLocations(String location) {
        return searchLocations(new LocationQuery(location, "destination"));
    }

    public Observable<Location> searchBestOriginLocation(String location) {
        return searchOriginLocations(location)
                .reduce((l1, l2) -> {
                    return l1.getResultQuality() >= l2.getResultQuality() ? l1 : l2;
                });
    }

    public Observable<Location> searchBestDestinationLocation(String location) {
        return searchDestinationLocations(location)
                .reduce((l1, l2) -> {
                    return l1.getResultQuality() >= l2.getResultQuality() ? l1 : l2;
                });
    }

    HttpClient httpClient = new HttpClient();

    static class HttpClient {

        public Response get(LocationQuery query) {
            final Client client = ClientBuilder.newClient()
                    .register(AjaxResponseFilter.class);
            final long startTime = System.currentTimeMillis();
            final Response response = query
                    .toWebTarget(client)
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            final long duration = System.currentTimeMillis() - startTime;
            logger.info("get: " + duration + "ms");
            return response;
        }
    }

    Observable<Location> searchLocations(LocationQuery query) {
        return Observable.fromCallable(() -> httpClient.get(query))
                .flatMap(this::filterNegativeHttpResponse)
                .map(this::mapHttpResponseToJson)
                .flatMap(this::mapJsonToLocation);
    }

    private Observable<Response> filterNegativeHttpResponse(Response response) {
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            logger.info("Response-code: " + response.getStatus());
            return Observable.empty();
        } else {
            return Observable.just(response);
        }
    }

    private JsonStructure mapHttpResponseToJson(Response response) {
        response.bufferEntity();
        try {
            return response.readEntity(JsonArray.class);
        } catch (ClassCastException e) {
            logger.trace("Response is no array. Trying to parse as object.", e);
            return response.readEntity(JsonObject.class).getJsonObject("point");
        }
    }

    private Observable<Location> mapJsonToLocation(JsonStructure jsonObject) {
        if (jsonObject instanceof JsonArray) {
            return Observable.from(JsonArray.class.cast(jsonObject))
                    .flatMap(json -> Location.fromJsonObject((JsonObject) json)
                            .map(Observable::just)
                            .orElse(Observable.empty()));
        } else if (jsonObject instanceof JsonObject) {
            List l = new LinkedList();
            l.add(Location.fromJsonObject((JsonObject) jsonObject));
            return Observable.from(l);
        } else {
            logger.warn("Unexpected instance type " + jsonObject.getClass());
            return Observable.empty();
        }
    }
}
