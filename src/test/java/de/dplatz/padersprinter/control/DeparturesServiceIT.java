package de.dplatz.padersprinter.control;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.Phaser;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.dplatz.padersprinter.entity.Departure;
import de.dplatz.padersprinter.entity.Location;
import de.dplatz.padersprinter.entity.Location.Type;

@Ignore
public class DeparturesServiceIT {
	private DeparturesServices cut = new DeparturesServices();
	private Phaser phaser;
	private List<Departure> departures;

	@Before
	public void init() {
		phaser = new Phaser(2);
	}

	@Test
	public void should_find_departures() {

		cut.query(new Location("name", Type.STOP, "23207070", "TOKEN", 1, "DESC"))
				.thenAccept(this::handleQueryResponse);

		phaser.arriveAndAwaitAdvance();

		departures.forEach(System.out::println);
		assertTrue(departures.size() > 0);
	}

	void handleQueryResponse(List<Departure> departures) {
		this.departures = departures;
		phaser.arrive();
	}
}
