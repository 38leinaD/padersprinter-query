/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dplatz.padersprinter.entity;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import de.dplatz.padersprinter.TestData;

/**
 *
 * @author daniel.platz
 */
public class TripQueryTest {

	@Test
	public void validUrlFromQuery() throws UnsupportedEncodingException {
		LocalDateTime date = LocalDateTime.of(2017, Month.JANUARY, 1, 15, 0);
		Location from = TestData.Locations.City;
		Location to = TestData.Locations.Offices;

		TripQuery query = new TripQuery(date, from, to);
		WebTarget webtarget = query.toWebTarget(ClientBuilder.newClient());

		String queryString = URLDecoder.decode(webtarget.getUri().toString(), "UTF-8");
		assertThat(queryString, CoreMatchers.containsString("date=01.01.2017 15:00"));
		assertThat(queryString, CoreMatchers.containsString("name_origin=" + from.getName()));
		assertThat(queryString, CoreMatchers.containsString("nameInfo_origin=" + from.getToken()));
		assertThat(queryString, CoreMatchers.containsString("name_destination=" + to.getName()));
		assertThat(queryString, CoreMatchers.containsString("nameInfo_destination=" + to.getToken()));
	}

	@Test
	public void validQueryFromBuilder() {

		final LocalDate date = LocalDate.of(2017, Month.JANUARY, 1);
		final LocalTime time = LocalTime.of(15, 30);
		final Location from = TestData.Locations.City;
		final Location to = TestData.Locations.Offices;

		TripQuery query = TripQuery.Builder
				.go()
				.on(date)
				.at(time)
				.from(from)
				.to(to)
				.query();

		assertThat(query.start, CoreMatchers.is(LocalDateTime.of(date, time)));
		assertThat(query.fromLocation, is(from));
		assertThat(query.toLocation, is(to));
	}

}
