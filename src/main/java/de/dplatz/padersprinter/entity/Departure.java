package de.dplatz.padersprinter.entity;

import java.time.LocalTime;

public class Departure {
	private final LocalTime time;
	private final String line;
	private final String to;

	public Departure(LocalTime time, String line, String to) {
		super();
		this.time = time;
		this.line = line;
		this.to = to;
	}

	public LocalTime getTime() {
		return time;
	}

	public String getLine() {
		return line;
	}

	public String getTo() {
		return to;
	}

	@Override
	public String toString() {
		return "Departure [time=" + time + ", line=" + line + ", to=" + to + "]";
	}
}
