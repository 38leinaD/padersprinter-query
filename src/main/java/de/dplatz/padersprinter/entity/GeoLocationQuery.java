package de.dplatz.padersprinter.entity;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

import org.apache.log4j.Logger;

public class GeoLocationQuery {
	private static final Logger logger = Logger.getLogger(LocationQuery.class.getName());

	private String type;
	private double longitude;
	private double latitude;

	public GeoLocationQuery(double longitude, double latitude, String type) {
		this.longitude = longitude;
		this.latitude = latitude;
		this.type = type;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public String getType() {
		return type;
	}

	public WebTarget toWebTarget(Client client) {
		final WebTarget webtarget = client.target(
				"https://www.padersprinter.de/fahrplan/XSLT_COORD_REQUEST?&boundingBox=&boundingBoxLU={lulong}%3A{lulat}%3AWGS84%5BDD.DDDDD%5D&boundingBoxRL={rllong}%3A{rllat}%3AWGS84%5BDD.DDDDD%5D&coordOutputFormat=WGS84%5BDD.DDDDD%5D&type_1={type}&outputFormat=json&inclFilter=1")
				.resolveTemplate("lulong", getLongitude() + 0.005)
				.resolveTemplate("lulat", getLatitude() - 0.005)
				.resolveTemplate("rllong", getLongitude() - 0.005)
				.resolveTemplate("rllat", getLatitude() + 0.005)
				.resolveTemplate("type", getType());

		logger.info("Query URL is: " + webtarget.getUri().toString());
		return webtarget;
	}
}
