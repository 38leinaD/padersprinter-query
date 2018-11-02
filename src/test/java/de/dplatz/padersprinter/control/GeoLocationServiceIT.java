package de.dplatz.padersprinter.control;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.Phaser;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.dplatz.padersprinter.entity.GeoLocationQuery;
import de.dplatz.padersprinter.entity.Location;

@Ignore
public class GeoLocationServiceIT {
	private GeoLocationService cut = new GeoLocationService();
	private Phaser phaser;
	private List<Location> locations;

	@Before
	public void init() {
		phaser = new Phaser(2);
	}

	@Test
	public void should_find_stops() {

		cut.search(new GeoLocationQuery(8.77, 51.72, "STOP"))
				.thenAccept(this::handleQueryResponse);

		phaser.arriveAndAwaitAdvance();

		locations.forEach(System.out::println);
		assertTrue(locations.size() > 0);
	}

	void handleQueryResponse(List<Location> locations) {
		this.locations = locations;
		phaser.arrive();
	}
}
