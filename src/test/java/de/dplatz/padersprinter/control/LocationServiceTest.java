/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dplatz.padersprinter.control;

import de.dplatz.padersprinter.control.LocationService.HttpClient;
import de.dplatz.padersprinter.entity.Location;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import static java.util.stream.Stream.empty;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.core.Response;
import org.hamcrest.CoreMatchers;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Rule;
import org.mockito.Matchers;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 *
 * @author daniel.platz
 */
@Ignore
public class LocationServiceTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    
    final private LocationService cut = new LocationService();

    @Test
    public void searchLocations() throws IOException {
        Response response = multiResponse();
        HttpClient httpClientMock = mock(HttpClient.class);
        when(httpClientMock.get(Matchers.anyObject())).thenReturn(response);
        cut.httpClient = httpClientMock;

        List<Location> locations = cut.searchOriginLocations("Paderborn")
                .toList()
                .toBlocking()
                .single();

        assertThat(locations, is(not(empty())));
        assertThat(locations,
                CoreMatchers.hasItem(hasProperty("name", is("Paderborn, Almeweg"))));
    }

    @Test
    public void searchLocationsWithUnqiueResult() throws IOException {
        Response response = singleResponse();
        HttpClient httpClientMock = mock(HttpClient.class);
        when(httpClientMock.get(Matchers.anyObject())).thenReturn(response);
        cut.httpClient = httpClientMock;

        Location single = cut.searchOriginLocations("Paderborn Hbf")
                .toBlocking()
                .single();

        assertThat(single.getName(), is("Paderborn, Paderborn Hbf"));
    }

    private Response multiResponse() throws IOException {
        try (InputStream is = LocationServiceTest.class.getResourceAsStream("/ajax-multi-response.json")) {
            JsonArray jsonArray = Json.createReader(is).readArray();
            Response response = mock(Response.class);
            when(response.getStatus()).thenReturn(200);
            when(response.readEntity(JsonArray.class)).thenReturn(jsonArray);
            return response;
        }
    }
    
    private Response singleResponse() throws IOException {
        try (InputStream is = LocationServiceTest.class.getResourceAsStream("/ajax-single-response.json")) {
            JsonObject jsonObject = Json.createReader(is).readObject();
            Response response = mock(Response.class);
            when(response.getStatus()).thenReturn(200);
            when(response.readEntity(JsonArray.class)).thenThrow(new ClassCastException());
            when(response.readEntity(JsonObject.class)).thenReturn(jsonObject);
            return response;
        }
    }
}
