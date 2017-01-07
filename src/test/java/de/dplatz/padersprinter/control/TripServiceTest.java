/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dplatz.padersprinter.control;

import de.dplatz.padersprinter.TestData;
import de.dplatz.padersprinter.control.TripService.HttpClient;
import de.dplatz.padersprinter.entity.TripQuery;
import de.dplatz.padersprinter.entity.Trip;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.mockito.Matchers;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import rx.Observable;

/**
 *
 * @author daniel.platz
 */
public class TripServiceTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    private TripService cut = new TripService();

    @Test
    public void queryTrips() {

        HttpClient httpClientMock = mock(HttpClient.class);
        when(httpClientMock.get(Matchers.anyObject())).thenReturn(defaultGetResponse());
        cut.httpClient = httpClientMock;

        TripQuery q = TripQuery.Builder
                .go()
                .today()
                .at(LocalTime.of(18, 00))
                .from(TestData.Locations.City)
                .to(TestData.Locations.Offices)
                .query();

        List<Trip> trips = cut.query(q).toList().toBlocking().single();
        trips.forEach(System.out::println);
        assertThat(trips.size(), is(4));
    }

    private Observable<String> defaultGetResponse() {
        try {
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(TripServiceTest.class.getResourceAsStream("/padersprinter-response.html")))) {
                return Observable.just(buffer.lines().collect(Collectors.joining("\n")));
            }
        } catch (IOException ex) {
            return Observable.error(ex);
        }
    }
}
